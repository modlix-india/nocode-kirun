import {
    Function,
    FunctionDefinition,
    HybridRepository,
    KIRunFunctionRepository,
    Repository,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunSchemaRepository,
} from '../../../src';

const oldLog = console.log;

test('KIRuntime parallel execution waits for all paths to complete', async () => {
    // 3 separate functions, one per parallel path:
    //   Path1: Wait 2000ms -> Print "2000 Finished"
    //   Path2: Wait 3000ms -> Print "3000 Finished"
    //   Path3: Print "Without Time Finished" (instant)
    //
    // Main function calls all 3 in parallel and should wait for all to complete.

    const path1 = new KIRuntime(
        FunctionDefinition.from(
            JSON.parse(`{
                "name": "Path1",
                "namespace": "Internal",
                "events": {
                    "output": { "name": "output", "parameters": {} }
                },
                "steps": {
                    "wait": {
                        "statementName": "wait",
                        "namespace": "System",
                        "name": "Wait",
                        "parameterMap": {
                            "millis": { "one": { "key": "one", "type": "VALUE", "value": 2000 } }
                        }
                    },
                    "print": {
                        "statementName": "print",
                        "namespace": "System",
                        "name": "Print",
                        "parameterMap": {
                            "values": { "one": { "key": "one", "type": "VALUE", "value": "2000 Finished" } }
                        },
                        "dependentStatements": { "Steps.wait.output": true }
                    },
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "dependentStatements": { "Steps.print.output": true },
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } }
                        }
                    }
                }
            }`),
        ),
    );

    const path2 = new KIRuntime(
        FunctionDefinition.from(
            JSON.parse(`{
                "name": "Path2",
                "namespace": "Internal",
                "events": {
                    "output": { "name": "output", "parameters": {} }
                },
                "steps": {
                    "wait": {
                        "statementName": "wait",
                        "namespace": "System",
                        "name": "Wait",
                        "parameterMap": {
                            "millis": { "one": { "key": "one", "type": "VALUE", "value": 3000 } }
                        }
                    },
                    "print": {
                        "statementName": "print",
                        "namespace": "System",
                        "name": "Print",
                        "parameterMap": {
                            "values": { "one": { "key": "one", "type": "VALUE", "value": "3000 Finished" } }
                        },
                        "dependentStatements": { "Steps.wait.output": true }
                    },
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "dependentStatements": { "Steps.print.output": true },
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } }
                        }
                    }
                }
            }`),
        ),
    );

    const path3 = new KIRuntime(
        FunctionDefinition.from(
            JSON.parse(`{
                "name": "Path3",
                "namespace": "Internal",
                "events": {
                    "output": { "name": "output", "parameters": {} }
                },
                "steps": {
                    "print": {
                        "statementName": "print",
                        "namespace": "System",
                        "name": "Print",
                        "parameterMap": {
                            "values": { "one": { "key": "one", "type": "VALUE", "value": "Without Time Finished" } }
                        }
                    },
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "dependentStatements": { "Steps.print.output": true },
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } }
                        }
                    }
                }
            }`),
        ),
    );

    // Main function calls all 3 paths in parallel
    const mainDef = FunctionDefinition.from({
        name: 'TestParallel',
        namespace: 'UIApp',

        steps: {
            exPath1: {
                statementName: 'exPath1',
                name: 'Path1',
                namespace: 'Internal',
                parameterMap: {},
            },
            exPath2: {
                statementName: 'exPath2',
                name: 'Path2',
                namespace: 'Internal',
                parameterMap: {},
            },
            exPath3: {
                statementName: 'exPath3',
                name: 'Path3',
                namespace: 'Internal',
                parameterMap: {},
            },
        },
    });

    class InternalRepository implements Repository<Function> {
        async find(namespace: string, name: string): Promise<Function | undefined> {
            if (namespace !== 'Internal') return undefined;
            if (name === 'Path1') return path1;
            if (name === 'Path2') return path2;
            if (name === 'Path3') return path3;
            return undefined;
        }

        async filter(name: string): Promise<string[]> {
            return [
                path1.getSignature().getFullName(),
                path2.getSignature().getFullName(),
                path3.getSignature().getFullName(),
            ].filter((e) => e.toLowerCase().includes(name.toLowerCase()));
        }
    }

    const repo = new HybridRepository(new KIRunFunctionRepository(), new InternalRepository());
    const mock = jest.spyOn(global.console, 'log').mockImplementation(oldLog);

    const startTime = Date.now();

    await new KIRuntime(mainDef, false).execute(
        new FunctionExecutionParameters(repo, new KIRunSchemaRepository()),
    );

    const elapsed = Date.now() - startTime;

    // All 3 print statements should have been called
    expect(mock).toHaveBeenCalledTimes(3);
    expect(mock).toHaveBeenCalledWith('Without Time Finished');
    expect(mock).toHaveBeenCalledWith('2000 Finished');
    expect(mock).toHaveBeenCalledWith('3000 Finished');

    // Execution should have waited for the longest path (3000ms).
    // If parallel execution finishes early (before all paths), elapsed would be < 2000ms.
    expect(elapsed).toBeGreaterThanOrEqual(2000);

    mock.mockRestore();
}, 15000);

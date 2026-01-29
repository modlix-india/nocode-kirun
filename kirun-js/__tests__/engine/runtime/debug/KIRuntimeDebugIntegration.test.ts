import {
    Function,
    FunctionDefinition,
    HybridRepository,
    KIRunFunctionRepository,
    Repository,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunSchemaRepository,
} from '../../../../src';
import { DebugCollector } from '../../../../src/engine/runtime/debug';

describe('KIRuntime Debug Integration', () => {
    let collector: DebugCollector;

    beforeEach(() => {
        collector = DebugCollector.getInstance();
        collector.clear();
        collector.enable();
    });

    afterEach(() => {
        collector.disable();
        collector.clear();
    });

    test('should track nested KIRuntime execution hierarchy', async () => {
        // First function calls Second and Third
        const first = new KIRuntime(
            FunctionDefinition.from(
                JSON.parse(`{
                    "name": "First",
                    "namespace": "Internal",
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": { "aresult": { "name": "aresult", "type": "INTEGER" } }
                        }
                    },
                    "steps": {
                        "exSecond": {
                            "statementName": "exSecond",
                            "name": "Second",
                            "namespace": "Internal",
                            "parameterMap": {
                                "value" : { "one" : { "key": "one", "type": "VALUE", "value": 2 } }
                            }
                        },
                        "exThird": {
                            "statementName": "exThird",
                            "name": "Third",
                            "namespace": "Internal",
                            "dependentStatements": { "Steps.exSecond.output": true },
                            "parameterMap": {
                                "value" : { "one" : { "key": "one", "type": "VALUE", "value": 3 } }
                            }
                        },
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "dependentStatements": { "Steps.exThird.output": true },
                            "parameterMap": {
                                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                                "results": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": {
                                            "name": "aresult",
                                            "value": { "isExpression": true, "value": "Steps.exThird.output.result" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }`),
            ),
            true, // debugMode enabled
        );

        const second = new KIRuntime(
            FunctionDefinition.from(
                JSON.parse(`{
                    "name": "Second",
                    "namespace": "Internal",
                    "parameters": {
                        "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } }
                    },
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": { "result": { "name": "result", "type": "INTEGER" } }
                        }
                    },
                    "steps": {
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "parameterMap": {
                                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                                "results": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": {
                                            "name": "result",
                                            "value": { "isExpression": true, "value": "Arguments.value * 2" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }`),
            ),
            true, // debugMode enabled
        );

        const third = new KIRuntime(
            FunctionDefinition.from(
                JSON.parse(`{
                    "name": "Third",
                    "namespace": "Internal",
                    "parameters": {
                        "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } }
                    },
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": { "result": { "name": "result", "type": "INTEGER" } }
                        }
                    },
                    "steps": {
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "parameterMap": {
                                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                                "results": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": {
                                            "name": "result",
                                            "value": { "isExpression": true, "value": "Arguments.value * 3" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }`),
            ),
            true, // debugMode enabled
        );

        class InternalRepository implements Repository<Function> {
            async find(namespace: string, name: string): Promise<Function | undefined> {
                if (namespace !== 'Internal') return Promise.resolve(undefined);
                if (name === 'Third') return Promise.resolve(third);
                if (name === 'Second') return Promise.resolve(second);
                return Promise.resolve(undefined);
            }

            async filter(name: string): Promise<string[]> {
                return Promise.resolve(
                    [third.getSignature().getFullName(), second.getSignature().getFullName()].filter(
                        (e) => e.toLowerCase().includes(name.toLowerCase()),
                    ),
                );
            }
        }

        const repo = new HybridRepository(new KIRunFunctionRepository(), new InternalRepository());

        const results = (
            await first.execute(
                new FunctionExecutionParameters(repo, new KIRunSchemaRepository(), 'test-nested'),
            )
        ).next();

        // Verify result
        expect(results?.getResult().get('aresult')).toBe(9);

        // Verify debug info
        const execution = collector.getExecution('test-nested');
        expect(execution).toBeDefined();

        // All 3 function definitions should be stored
        expect(execution!.definitions.size).toBe(3);
        expect(execution!.definitions.has('Internal.First')).toBe(true);
        expect(execution!.definitions.has('Internal.Second')).toBe(true);
        expect(execution!.definitions.has('Internal.Third')).toBe(true);

        // Check hierarchy - First's steps should be at root level
        // exSecond should have Second's genOutput as child
        // exThird should have Third's genOutput as child
        const flatLogs = collector.getFlatLogs('test-nested');
        expect(flatLogs.length).toBeGreaterThan(0);

        // Root logs should be First's steps
        expect(execution!.logs.length).toBe(3); // exSecond, exThird, genOutput

        // Find exSecond step - it should have nested children from Second
        const exSecondLog = execution!.logs.find((l) => l.statementName === 'exSecond');
        expect(exSecondLog).toBeDefined();
        expect(exSecondLog!.children.length).toBe(1); // Second's genOutput
        expect(exSecondLog!.children[0].statementName).toBe('genOutput');

        // Find exThird step - it should have nested children from Third
        const exThirdLog = execution!.logs.find((l) => l.statementName === 'exThird');
        expect(exThirdLog).toBeDefined();
        expect(exThirdLog!.children.length).toBe(1); // Third's genOutput
        expect(exThirdLog!.children[0].statementName).toBe('genOutput');

        console.log('Definitions:', Array.from(execution!.definitions.keys()));
        console.log('Root logs:', execution!.logs.map((l) => l.statementName));
        console.log('exSecond children:', exSecondLog!.children.map((l) => l.statementName));
        console.log('exThird children:', exThirdLog!.children.map((l) => l.statementName));
    });
});

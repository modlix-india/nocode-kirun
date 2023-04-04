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

test('KIRuntime Function in Function', async () => {
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
                "parameterMap": {
                    "value" : { "one" : { "key": "one", "type": "VALUE", "value": 3 } }
                }
            },
            "genOutput": {
                "statementName": "genOutput",
                "namespace": "System",
                "name": "GenerateEvent",
                "dependentStatements": {
                    "Steps.exSecond.output": true,
                    "Steps.exThird.output": true
                },
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
        true,
    );

    const second = new KIRuntime(
        FunctionDefinition.from(
            JSON.parse(`{
            "name": "Second",
            "namespace": "Internal",
            "parameters": {
                "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
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
        true,
    );

    const third = new KIRuntime(
        FunctionDefinition.from(
            JSON.parse(`{
            "name": "Third",
            "namespace": "Internal",
            "parameters": {
                "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
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
        true,
    );

    class InternalRepository implements Repository<Function> {
        find(namespace: string, name: string): Function | undefined {
            if (namespace !== 'Internal') return;

            if (name === 'Third') return third;
            if (name === 'Second') return second;
        }

        filter(name: string): string[] {
            return [third.getSignature().getFullName(), second.getSignature().getFullName()].filter(
                (e) => e.toLowerCase().includes(name.toLowerCase()),
            );
        }
    }

    const repo = new HybridRepository(new KIRunFunctionRepository(), new InternalRepository());

    const results = (
        await first.execute(
            new FunctionExecutionParameters(repo, new KIRunSchemaRepository(), 'Testing'),
        )
    ).next();

    expect(results?.getResult().get('aresult')).toBe(9);
});

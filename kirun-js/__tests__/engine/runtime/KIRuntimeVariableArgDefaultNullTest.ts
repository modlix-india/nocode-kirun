import {
    AbstractFunction,
    EventResult,
    Function,
    FunctionOutput,
    FunctionSignature,
    HybridRepository,
    Parameter,
    Schema,
    SchemaType,
} from '../../../src';
import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { Namespaces } from '../../../src/engine/namespaces/Namespaces';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

test('KIRuntime Print function with no arguments', async () => {
    var def = {
        name: 'varArgWithNothing',
        namespace: 'Test',
        steps: {
            testFunction: {
                statementName: 'testFunction',
                namespace: 'LocalFunction',
                name: 'Other',
                parameterMap: {
                    storageName: {
                        one: {
                            type: 'VALUE',
                            value: 'Test',
                            key: 'one',
                            order: 1,
                        },
                    },
                },
            },
        },
    };

    var OtherFunction = new KIRuntime(
        FunctionDefinition.from({
            namespace: 'LocalFunction',
            name: 'Other',
            steps: {
                print: {
                    statementName: 'print',
                    namespace: Namespaces.SYSTEM,
                    name: 'Print',
                    parameterMap: {
                        values: {
                            one: {
                                type: 'EXPRESSION',
                                expression: '"Storage : " + Arguments.storageName + "\n"',
                                key: 'one',
                                order: 1,
                            },
                            two: {
                                type: 'EXPRESSION',
                                expression: '"Page : " + Arguments.page + "\n"',
                                key: 'two',
                                order: 2,
                            },
                            three: {
                                type: 'EXPRESSION',
                                expression: '"Size : " + Arguments.size + "\n"',
                                key: 'three',
                                order: 3,
                            },
                            five: {
                                type: 'EXPRESSION',
                                expression: '"Count : " + Arguments.count + "\n"',
                                key: 'five',
                                order: 5,
                            },
                            six: {
                                type: 'EXPRESSION',
                                expression: '"Client Code : " + Arguments.clientCode + "\n"',
                                key: 'six',
                                order: 6,
                            },

                            eight: {
                                type: 'EXPRESSION',
                                expression: '"Eager : " + Arguments.eager + "\n"',
                                key: 'eight',
                                order: 8,
                            },
                        },
                    },
                },
            },
            parameters: {
                appCode: {
                    schema: {
                        namespace: '_',
                        name: 'appCode',
                        version: 1,
                        type: {
                            type: 'STRING',
                        },
                        defaultValue: '',
                    },
                    parameterName: 'appCode',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                page: {
                    schema: {
                        namespace: '_',
                        name: 'page',
                        version: 1,
                        type: {
                            type: 'INTEGER',
                        },
                        defaultValue: 0,
                    },
                    parameterName: 'page',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                storageName: {
                    schema: {
                        namespace: '_',
                        name: 'storageName',
                        version: 1,
                        type: {
                            type: 'STRING',
                        },
                    },
                    parameterName: 'storageName',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                size: {
                    schema: {
                        namespace: '_',
                        name: 'size',
                        version: 1,
                        type: {
                            type: 'INTEGER',
                        },
                        defaultValue: 20,
                    },
                    parameterName: 'size',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                count: {
                    schema: {
                        namespace: '_',
                        name: 'count',
                        version: 1,
                        type: {
                            type: 'BOOLEAN',
                        },
                        defaultValue: true,
                    },
                    parameterName: 'count',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                clientCode: {
                    schema: {
                        namespace: '_',
                        name: 'clientCode',
                        version: 1,
                        type: {
                            type: 'STRING',
                        },
                        defaultValue: '',
                    },
                    parameterName: 'clientCode',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                eagerFields: {
                    schema: {
                        namespace: '_',
                        name: 'eagerFields',
                        version: 1,
                        type: {
                            type: 'STRING',
                        },
                    },
                    parameterName: 'eagerFields',
                    variableArgument: true,
                    type: 'EXPRESSION',
                },
                filter: {
                    schema: {
                        namespace: '_',
                        name: 'filter',
                        version: 1,
                        type: {
                            type: 'OBJECT',
                        },
                        defaultValue: {},
                    },
                    parameterName: 'filter',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
                eager: {
                    schema: {
                        namespace: '_',
                        name: 'eager',
                        version: 1,
                        type: {
                            type: 'BOOLEAN',
                        },
                        defaultValue: false,
                    },
                    parameterName: 'eager',
                    variableArgument: false,
                    type: 'EXPRESSION',
                },
            },
        }),
        false,
    );
    const fd = FunctionDefinition.from(def);
    const oldConsole = console.log;
    const test = (console.log = jest.fn().mockImplementation(() => {}));

    let result = await new KIRuntime(fd, false).execute(
        new FunctionExecutionParameters(
            new HybridRepository<Function>(new KIRunFunctionRepository(), {
                find: async (namespace: string, name: string): Promise<Function | undefined> => {
                    if (namespace === 'LocalFunction' && name === 'Other') return OtherFunction;
                    return undefined;
                },

                filter: async (name: string): Promise<string[]> => {
                    return ['LocalFunction.Other'];
                },
            }),
            new KIRunSchemaRepository(),
        ),
    );

    console.log = oldConsole;

    expect(test.mock.calls[0]).toMatchObject([
        'Storage : Test\n',
        'Page : 0\n',
        'Size : 20\n',
        'Count : true\n',
        'Client Code : \n',
        'Eager : false\n',
    ]);
});

test("KIRuntime Print function with 'null' argument", async () => {
    class TestPrint extends AbstractFunction {
        private static readonly VALUES: string = 'values';

        private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Print')
            .setNamespace(Namespaces.SYSTEM)
            .setParameters(
                new Map([
                    Parameter.ofEntry(
                        TestPrint.VALUES,
                        Schema.of(
                            TestPrint.VALUES,
                            SchemaType.STRING,
                            SchemaType.NULL,
                        ).setDefaultValue(null),
                        false,
                    ),
                ]),
            );

        public getSignature(): FunctionSignature {
            return TestPrint.SIGNATURE;
        }

        protected async internalExecute(
            context: FunctionExecutionParameters,
        ): Promise<FunctionOutput> {
            var values = context.getArguments()?.get(TestPrint.VALUES);

            console?.log(values);

            return new FunctionOutput([EventResult.outputOf(new Map())]);
        }
    }

    var fun = new TestPrint();
    fun.execute(
        new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()),
    );
});

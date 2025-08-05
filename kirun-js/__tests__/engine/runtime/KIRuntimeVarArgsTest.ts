import { Function, HybridRepository, ObjectValueSetterExtractor } from '../../../src';
import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { Namespaces } from '../../../src/engine/namespaces/Namespaces';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

test('KIRuntime Print function with variable arguments', async () => {
    var def = {
        name: 'varArgWithNothing',
        namespace: 'Test',
        steps: {
            testFunction: {
                statementName: 'testFunction',
                namespace: 'LocalFunction',
                name: 'Other',
                parameterMap: {
                    eagerFields: {
                        one: {
                            type: 'EXPRESSION',
                            expression: 'Store.names',
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
                                expression: 'Arguments.eagerFields',
                                value: 'Test',
                                key: 'one',
                                order: 1,
                            },
                        },
                    },
                },
            },
            parameters: {
                eagerFields: {
                    schema: {
                        namespace: '_',
                        name: 'eagerFields',
                        version: 1,
                        type: 'STRING',
                    },
                    parameterName: 'eagerFields',
                    variableArgument: true,
                    type: 'EXPRESSION',
                },
            },
        }),
        false,
    );
    const fd = FunctionDefinition.from(def);

    const store = new ObjectValueSetterExtractor(
        { names: ['kiran', 'kumar', 'grandhi'] },
        'Store.',
    );

    // console.log(store.getValue('Store.names'));

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
        ).setValuesMap(new Map([[store.getPrefix(), store]])),
    );

    console.log = oldConsole;

    expect(test.mock.calls[0]).toMatchObject(['kiran', 'kumar', 'grandhi']);
});

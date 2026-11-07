import {
    FunctionDefinition,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../src';

test('KIRuntime keeps a literal null inside a Make resultShape', async () => {
    const def = {
        name: 'makeNull',
        namespace: 'Test',
        steps: {
            make: {
                statementName: 'make',
                namespace: 'System',
                name: 'Make',
                parameterMap: {
                    resultShape: {
                        one: { key: 'one', type: 'VALUE', value: { a: 'x', b: null, c: { d: null } } },
                    },
                },
            },
            out: {
                statementName: 'out',
                namespace: 'System',
                name: 'GenerateEvent',
                parameterMap: {
                    results: {
                        one: {
                            key: 'one',
                            type: 'VALUE',
                            value: { name: 'v', value: { isExpression: true, value: 'Steps.make.output.value' } },
                        },
                    },
                },
            },
        },
    };

    const fo = await new KIRuntime(FunctionDefinition.from(def)).execute(
        new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(
            new Map(),
        ),
    );
    expect(fo.allResults()[0].getResult().get('v')).toStrictEqual({ a: 'x', b: null, c: { d: null } });
});

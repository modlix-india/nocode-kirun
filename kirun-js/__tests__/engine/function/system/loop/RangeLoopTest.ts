import {
    FunctionDefinition,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
} from '../../../../../src';

test('Range Loop Test', async () => {
    const breakDefinition = {
        name: 'Break Me 1',
        events: {
            output: {
                name: 'output',
                parameters: {
                    returnValue: {
                        schema: { type: 'ARRAY', items: { type: 'INTEGER' } },
                    },
                },
            },
        },
        steps: {
            create: {
                name: 'Create',
                namespace: 'System.Context',
                statementName: 'create',
                parameterMap: {
                    name: { one: { key: 'one', type: 'VALUE', value: 'array' } },
                    schema: {
                        one: {
                            key: 'one',
                            type: 'VALUE',
                            value: { type: 'ARRAY', items: { type: 'INTEGER' } },
                        },
                    },
                },
            },
            createSet: {
                name: 'Set',
                namespace: 'System.Context',
                statementName: 'createSet',
                parameterMap: {
                    name: { one: { key: 'one', type: 'VALUE', value: 'Context.array' } },
                    value: { one: { key: 'one', type: 'VALUE', value: [] } },
                },
                dependentStatements: {
                    'Steps.create.output': true,
                },
            },
            loop: {
                name: 'RangeLoop',
                namespace: 'System.Loop',
                statementName: 'loop',
                parameterMap: {
                    from: {
                        one: { key: 'one', type: 'VALUE', value: 5 },
                    },
                    to: { one: { key: 'one', type: 'VALUE', value: 10 } },
                    step: { one: { key: 'one', type: 'VALUE', value: 1 } },
                },
                dependentStatements: {
                    'Steps.createSet.output': true,
                },
            },
            insert: {
                name: 'InsertLast',
                namespace: 'System.Array',
                statementName: 'insert',
                parameterMap: {
                    source: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Context.array' },
                    },
                    element: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.loop.iteration.index',
                        },
                    },
                },
            },
            set: {
                name: 'Set',
                namespace: 'System.Context',
                statementName: 'set',
                parameterMap: {
                    name: { one: { key: 'one', type: 'VALUE', value: 'Context.array' } },
                    value: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.insert.output.result',
                        },
                    },
                },
            },
            generateEvent: {
                statementName: 'generateEvent',
                name: 'GenerateEvent',
                namespace: 'System',
                parameterMap: {
                    eventName: {
                        '5OdGxruBiEyysESbAubdX2': {
                            key: '5OdGxruBiEyysESbAubdX2',
                            type: 'VALUE',
                            expression: '',
                            value: 'output',
                        },
                    },
                    results: {
                        '4o0c0kvVtWiGjgb37hMTBX': {
                            key: '4o0c0kvVtWiGjgb37hMTBX',
                            type: 'VALUE',
                            order: 1,
                            value: {
                                name: 'returnValue',
                                value: {
                                    isExpression: true,
                                    value: 'Context.array',
                                },
                            },
                        },
                    },
                },
                dependentStatements: {
                    'Steps.loop.output': true,
                },
            },
        },
    };

    const fd = FunctionDefinition.from(breakDefinition);
    const result = (
        await new KIRuntime(fd, true).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ),
        )
    )
        .next()
        ?.getResult()
        .get('returnValue');
    expect(result).toMatchObject([5, 6, 7, 8, 9]);
});

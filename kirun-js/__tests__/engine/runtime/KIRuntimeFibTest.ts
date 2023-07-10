import {
    FunctionDefinition,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
} from '../../../src';

test('Testing Fib', async () => {
    let def = {
        name: 'fibonaccii',
        namespace: 'TestUI',
        parameters: {
            n: {
                parameterName: 'n',
                schema: {
                    type: ['INTEGER'],
                    version: 1,
                },
            },
        },
        events: {
            output: {
                name: 'output',
                parameters: {
                    result: {
                        type: ['ARRAY'],
                        version: 1,
                        items: {
                            type: ['INTEGER'],
                        },
                    },
                },
            },
        },
        steps: {
            create: {
                statementName: 'create',
                name: 'Create',
                namespace: 'System.Context',
                position: {
                    left: -3,
                    top: 65.5,
                },
                parameterMap: {
                    name: {
                        '2JUURU6NHj9voaArLv0pS8': {
                            key: '2JUURU6NHj9voaArLv0pS8',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: 'a',
                        },
                    },
                    schema: {
                        '2vxDXyOakSmhiUmH4CuDED': {
                            key: '2vxDXyOakSmhiUmH4CuDED',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: {
                                type: 'ARRAY',
                                items: {
                                    type: 'INTEGER',
                                },
                            },
                        },
                    },
                },
            },
            rangeLoop: {
                statementName: 'rangeLoop',
                name: 'RangeLoop',
                namespace: 'System.Loop',
                position: {
                    left: 503,
                    top: 377.5,
                },
                parameterMap: {
                    to: {
                        '6RAIEdM1OAek2AKf64ucqM': {
                            key: '6RAIEdM1OAek2AKf64ucqM',
                            type: 'EXPRESSION',
                            expression: 'Arguments.n',
                            value: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.create.output': false,
                    'Steps.set2.output': true,
                },
            },
            if: {
                statementName: 'if',
                name: 'If',
                namespace: 'System',
                position: {
                    left: 802,
                    top: 248.5,
                },
                parameterMap: {
                    condition: {
                        C2FB54BobtFXTmLKv4v6s: {
                            key: 'C2FB54BobtFXTmLKv4v6s',
                            type: 'EXPRESSION',
                            expression: 'Steps.rangeLoop.iteration.index < 2',
                            order: 1,
                        },
                    },
                },
            },
            generateEvent: {
                statementName: 'generateEvent',
                name: 'GenerateEvent',
                namespace: 'System',
                position: {
                    left: 706,
                    top: 739.5,
                },
                parameterMap: {
                    results: {
                        '8brcu20HciA9rYDPOFeef': {
                            key: '8brcu20HciA9rYDPOFeef',
                            type: 'VALUE',
                            value: {
                                name: 'result',
                                value: { isExpression: true, value: 'Context.a' },
                            },
                            order: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.rangeLoop.output': true,
                },
            },
            trueInsert: {
                statementName: 'trueInsert',
                name: 'InsertLast',
                namespace: 'System.Array',
                position: {
                    left: 1304,
                    top: 277.77777777777777,
                },
                parameterMap: {
                    source: {
                        '4DzS3icjwGl7nUPvk7QUST': {
                            key: '4DzS3icjwGl7nUPvk7QUST',
                            type: 'EXPRESSION',
                            expression: 'Context.a',
                            order: 1,
                        },
                    },
                    element: {
                        '1sWH8NKqkdKevU7vQJk9jn': {
                            key: '1sWH8NKqkdKevU7vQJk9jn',
                            type: 'EXPRESSION',
                            expression: 'Steps.rangeLoop.iteration.index',
                            order: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.if.true': true,
                },
            },
            set: {
                statementName: 'set',
                name: 'Set',
                namespace: 'System.Context',
                position: {
                    left: 1617,
                    top: 434.5,
                },
                parameterMap: {
                    name: {
                        UPMhlPSuFHCygh0DYa7Zc: {
                            key: 'UPMhlPSuFHCygh0DYa7Zc',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: 'Context.a',
                        },
                    },
                    value: {
                        '1B8Bfavmym6BmbdsDesmNX': {
                            key: '1B8Bfavmym6BmbdsDesmNX',
                            type: 'EXPRESSION',
                            expression: 'Steps.trueInsert.output.result',
                            order: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.if.true': false,
                },
            },
            set1: {
                statementName: 'set1',
                name: 'Set',
                namespace: 'System.Context',
                position: {
                    left: 1503,
                    top: 715.5,
                },
                parameterMap: {
                    name: {
                        '6dYVAyspJbF82O8qJm3f2O': {
                            key: '6dYVAyspJbF82O8qJm3f2O',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: 'Context.a',
                        },
                    },
                    value: {
                        fVB75EKlR6It0i7iho3oQ: {
                            key: 'fVB75EKlR6It0i7iho3oQ',
                            type: 'EXPRESSION',
                            expression: 'Steps.falseInsert.output.result',
                            order: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.if.false': false,
                },
            },
            falseInsert: {
                statementName: 'falseInsert',
                name: 'InsertLast',
                namespace: 'System.Array',
                position: {
                    left: 1140.3333333333333,
                    top: 548.8888888888889,
                },
                parameterMap: {
                    source: {
                        '1GsJu3FbuponBIcaqI48rF': {
                            key: '1GsJu3FbuponBIcaqI48rF',
                            type: 'EXPRESSION',
                            expression: 'Context.a',
                            order: 1,
                        },
                    },
                    element: {
                        '53vsp50RjvhS94xdCtWWx0': {
                            key: '53vsp50RjvhS94xdCtWWx0',
                            type: 'EXPRESSION',
                            expression:
                                'Context.a[Steps.rangeLoop.iteration.index - 1] + Context.a[Steps.rangeLoop.iteration.index - 2]',
                            order: 1,
                        },
                    },
                },
                dependentStatements: {
                    'Steps.if.false': true,
                },
            },
            set2: {
                statementName: 'set2',
                name: 'Set',
                namespace: 'System.Context',
                position: {
                    left: 257,
                    top: 273.5,
                },
                parameterMap: {
                    name: {
                        '2XVsWDK0VUQhZJ0VnJllrQ': {
                            key: '2XVsWDK0VUQhZJ0VnJllrQ',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: 'Context.a',
                        },
                    },
                    value: {
                        '1ksRfalqqgGfh4uZcLXDy9': {
                            key: '1ksRfalqqgGfh4uZcLXDy9',
                            type: 'VALUE',
                            expression: '',
                            order: 1,
                            value: [],
                        },
                    },
                },
                dependentStatements: {
                    'Steps.create.output': true,
                },
            },
        },
    };

    let fd = FunctionDefinition.from(def);

    let out = await new KIRuntime(fd, true).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['n', 5]])),
    );

    console.log(out.next()?.getResult());
});

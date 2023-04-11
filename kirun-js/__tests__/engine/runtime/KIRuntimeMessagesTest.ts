import {
    FunctionDefinition,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
    Namespaces,
} from '../../../src';

test('Messages Test 1', async () => {
    const func = {
        name: 'loginFunction',
        steps: {
            messageStep: {
                statementName: 'messageStep',
                namespace: 'UIEngine',
                name: 'Message',
                parameterMap: {
                    msg: {
                        value1: {
                            key: 'value1',
                            type: 'EXPRESSION',
                            expression: 'Steps.loginStep.error.data',
                        },
                    },
                },
                position: {
                    left: 198,
                    top: 245,
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: 'System',
                name: 'GenerateEvent',
                dependentStatements: {
                    'Steps.loginStep.output': true,
                },
                position: {
                    left: 482,
                    top: 172,
                },
            },
            loginStep1: {
                name: 'Login',
                namespace: 'UIEngine',
                statementName: 'loginStep1',
                parameterMap: {
                    userName: {
                        value1: {
                            key: 'value1',
                            type: 'EXPRESSION',
                            expression: 'Page.user.userName',
                        },
                    },
                    password: {
                        value1: {
                            key: 'value1',
                            type: 'EXPRESSION',
                            expression: 'Page.user.password',
                        },
                    },
                    rememberMe: {
                        value1: {
                            key: 'value1',
                            type: 'EXPRESSION',
                            expression: 'Page.user.rememberMe',
                        },
                    },
                },
                position: {
                    left: 472,
                    top: 333,
                },
            },
        },
    };

    const fd = FunctionDefinition.from(func);

    const graph = await new KIRuntime(fd, false).getExecutionPlan(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    const messages = Array.from(graph.getNodeMap().values()).flatMap((node) => {
        return node
            .getData()
            .getMessages()
            .map((e) => e.getMessage());
    });

    expect(messages).toStrictEqual([
        'UIEngine.Message is not available',
        'Unable to find the step with name loginStep',
        'UIEngine.Login is not available',
    ]);
});

test('Messages Test 2', async () => {
    var def = {
        name: 'getAppData',
        namespace: 'UIApp',
        parameters: {
            a: { parameterName: 'a', schema: { name: 'INTEGER', type: 'INTEGER' } },
            b: { parameterName: 'b', schema: { name: 'INTEGER', type: 'INTEGER' } },
            c: { parameterName: 'c', schema: { name: 'INTEGER', type: 'INTEGER' } },
        },
        events: {
            output: {
                name: 'output',
                parameters: { additionResult: { name: 'additionResult', type: 'INTEGER' } },
            },
        },
        steps: {
            add1: {
                statementName: 'add1',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Arguments.a' },
                        two: { key: 'two', type: 'EXPRESSION', expression: '10 + 1' },
                        three: { key: 'three', type: 'EXPRESSION', expression: 'Arguments.c' },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                parameterMap: {
                    eventName: { one: { key: 'one', type: 'VALUE', value: 'output' } },
                    results: {
                        one: {
                            key: 'one',
                            type: 'VALUE',
                            value: {
                                name: 'additionResult',
                                value: { isExpression: true, value: 'Steps.add.output.value' },
                            },
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const graph = await new KIRuntime(fd, false).getExecutionPlan(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    const messages = Array.from(graph.getNodeMap().values()).flatMap((node) => {
        return node
            .getData()
            .getMessages()
            .map((e) => e.getMessage());
    });

    expect(messages).toStrictEqual(['Unable to find the step with name add']);

    await expect(
        new KIRuntime(fd, false).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    ['a', 7],
                    ['b', 11],
                    ['c', 13],
                ]),
            ),
        ),
    ).rejects.toThrow('Unable to find the step with name add');
});

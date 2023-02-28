import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';
import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { Namespaces } from '../../../src/engine/namespaces/Namespaces';

test('KIRuntime Without Gen Event Definition 1', async () => {
    var def = {
        name: 'getAppData',
        namespace: 'UIApp',
        parameters: {
            a: { parameterName: 'a', schema: { name: 'INTEGER', type: 'INTEGER' } },
            b: { parameterName: 'b', schema: { name: 'INTEGER', type: 'INTEGER' } },
            c: { parameterName: 'c', schema: { name: 'INTEGER', type: 'INTEGER' } },
        },
        steps: {
            add: {
                statementName: 'add',
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
            print: {
                statementName: 'print',
                namespace: Namespaces.SYSTEM,
                name: 'Print',
                parameterMap: {
                    values: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add.output.value',
                            order: 2,
                        },
                        abc: {
                            key: 'abc',
                            type: 'VALUE',
                            value: 'Nothing muchh....',
                            order: 1,
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const test = (console.log = jest.fn().mockImplementation(() => {}));

    await new KIRuntime(fd, false).execute(
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
    );
    expect(test.mock.calls[0][0]).toBe('Nothing muchh....');
    expect(test.mock.calls[0][1]).toBe(31);
});

test('KIRuntime Without Gen Event Definition 2', async () => {
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
                parameters: {},
            },
        },
        steps: {
            add: {
                statementName: 'add',
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
            print: {
                statementName: 'print',
                namespace: Namespaces.SYSTEM,
                name: 'Print',
                parameterMap: {
                    values: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add.output.value',
                            order: 2,
                        },
                        abc: {
                            key: 'abc',
                            type: 'VALUE',
                            value: 'Something muchh....',
                            order: 1,
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const test = (console.log = jest.fn().mockImplementation(() => {}));

    await new KIRuntime(fd, false).execute(
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
    );
    expect(test.mock.calls[0][0]).toBe('Something muchh....');
    expect(test.mock.calls[0][1]).toBe(31);
});

test('KIRuntime With Definition 1', async () => {
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
            add: {
                statementName: 'add',
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
            print: {
                statementName: 'print',
                namespace: Namespaces.SYSTEM,
                name: 'Print',
                parameterMap: {
                    values: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add.output.value',
                            order: 2,
                        },
                        abc: {
                            key: 'abc',
                            type: 'VALUE',
                            value: 'Something muchh....',
                            order: 1,
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const fep = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map([
            ['a', 7],
            ['b', 11],
            ['c', 13],
        ]),
    );

    const runtime = new KIRuntime(fd, false);

    await expect(() => runtime.execute(fep)).rejects.toThrowError();
});

import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

const oldLog = console.log;

test('KIRuntime With ExecuteIfTrue 1', async () => {
    let def = {
        name: 'getAppData',
        namespace: 'UIApp',
        parameters: {
            a: {
                parameterName: 'a',
                schema: { name: 'INTEGER', type: ['ARRAY'], items: { type: ['OBJECT', 'NULL'] } },
            },
        },

        steps: {
            forEach: {
                statementName: 'forEach',
                namespace: 'System.Loop',
                name: 'ForEachLoop',
                parameterMap: {
                    source: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Arguments.a' },
                    },
                },
            },
            print: {
                statementName: 'print',
                namespace: 'System',
                name: 'Print',
                parameterMap: {
                    values: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.forEach.iteration.each.name',
                        },
                    },
                },
                executeIftrue: {
                    'Steps.forEach.iteration.each.name': true,
                },
            },
        },
    };

    let fd = FunctionDefinition.from(def);
    const mock = jest.spyOn(global.console, 'log').mockImplementation(oldLog);

    await new KIRuntime(fd, false).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([['a', [{ name: 'Kiran', age: 40 }, null, { name: 'Kumar', age: 39 }]]]),
        ),
    );

    expect(mock).toHaveBeenCalledTimes(2);

    let newDef = {
        name: 'getAppData',
        namespace: 'UIApp',
        parameters: {
            a: {
                parameterName: 'a',
                schema: { name: 'INTEGER', type: ['ARRAY'], items: { type: ['OBJECT', 'NULL'] } },
            },
        },

        steps: {
            forEach: {
                statementName: 'forEach',
                namespace: 'System.Loop',
                name: 'ForEachLoop',
                parameterMap: {
                    source: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Arguments.a' },
                    },
                },
            },
            print: {
                statementName: 'print',
                namespace: 'System',
                name: 'Print',
                parameterMap: {
                    values: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.forEach.iteration.each.name',
                        },
                    },
                },
                executeIftrue: {
                    '(Steps.forEach.iteration.each.age ?? 1) % 2 = 0': true,
                },
            },
        },
    };

    fd = FunctionDefinition.from(newDef);

    let result = await new KIRuntime(fd, false).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([['a', [{ name: 'Kiran', age: 40 }, null, { name: 'Kumar', age: 39 }]]]),
        ),
    );

    expect(mock).toHaveBeenCalledTimes(3);
});

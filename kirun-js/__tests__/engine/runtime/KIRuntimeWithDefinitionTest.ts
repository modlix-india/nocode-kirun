import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { Namespaces } from '../../../src/engine/namespaces/Namespaces';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

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

    let result = await new KIRuntime(fd, true).execute(
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
    expect(result.allResults()[0].getResult().get('additionResult')).toBe(31);
});

test('KIRuntime With Definition 2', async () => {
    var def = {
        name: 'checkWithNoParamsOrEvents',
        namespace: 'UIApp',
        steps: {
            add: {
                statementName: 'add',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'VALUE', value: 2 },
                        two: { key: 'two', type: 'VALUE', value: 5 },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                dependentStatements: { 'Steps.add.output': true },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    let result = await new KIRuntime(fd).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map()),
    );

    expect(result.allResults()[0].getResult()).toMatchObject({});
});

test('KIRuntime With Definition 3', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            add: {
                statementName: 'add',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'VALUE', value: 'X' },
                        two: { key: 'two', type: 'VALUE', value: 5 },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                dependentStatements: { 'Steps.add.output': true },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    try {
        await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
    } catch (e: any) {
        expect(e.message).toMatch(/error/i);
    }
});

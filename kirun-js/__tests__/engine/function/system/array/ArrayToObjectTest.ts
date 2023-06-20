import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { ArrayToObject } from '../../../../../src/engine/function/system/array/ArrayToObject';

test('Array To Object Test', async () => {
    const atoo = new ArrayToObject();

    let source = [
        { name: 'A', num: 1 },
        { name: 'B', num: 2 },
        null,
        { name: 'C', num: 3 },
        { name: 'D', num: 4 },
        { name: 'E', num: 4 },
        undefined,
    ];

    const funRepo = new KIRunFunctionRepository();
    const schemaRepo = new KIRunSchemaRepository();

    let result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'name'],
                    ['valuePath', 'num'],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({ A: 1, B: 2, C: 3, D: 4, E: 4 });

    result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'num'],
                    ['valuePath', 'name'],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({ 1: 'A', 2: 'B', 3: 'C', 4: 'E' });

    result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'num'],
                    ['valuePath', 'name'],
                    ['ignoreDuplicateKeys', true],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({ 1: 'A', 2: 'B', 3: 'C', 4: 'D' });
});

test('Array To Object Test - Invalid Key Path', async () => {
    const atoo = new ArrayToObject();

    let source = [
        { name: 'A', num: 1 },
        { name: 'B', num: 2 },
        { name: 'C', num: 3 },
        { name: 'D', num: 4 },
        { name: 'E', num: 4 },
    ];

    const funRepo = new KIRunFunctionRepository();
    const schemaRepo = new KIRunSchemaRepository();

    let result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'name1'],
                    ['valuePath', 'num'],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({});

    result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'name'],
                    ['valuePath', 'num1'],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({
        A: undefined,
        B: undefined,
        C: undefined,
        D: undefined,
        E: undefined,
    });

    result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'name'],
                    ['valuePath', 'num1'],
                    ['ignoreNullValues', true],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({});
});

test('Array To Object Test - Deep Path', async () => {
    const atoo = new ArrayToObject();

    let source = [
        { name: 'A', num: 1, info: { age: 10 } },
        { name: 'B', num: 2, info: { age: 20 } },
        { name: 'C', num: 3, info: { age: 30 } },
        { name: 'D', num: 4, info: { age: 40 } },
        { name: 'E', num: 4, info: { age: 50 } },
    ];

    const funRepo = new KIRunFunctionRepository();
    const schemaRepo = new KIRunSchemaRepository();

    let result = (
        await atoo.execute(
            new FunctionExecutionParameters(funRepo, schemaRepo).setArguments(
                new Map<string, any>([
                    ['source', source],
                    ['keyPath', 'info.age'],
                    ['valuePath', 'name'],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject({ 10: 'A', 20: 'B', 30: 'C', 40: 'D', 50: 'E' });
});

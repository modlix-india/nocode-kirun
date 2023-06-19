import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { ArrayToArrayOfObjects } from '../../../../../src/engine/function/system/array/ArrayToArrayOfObjects';

test('Array to Array of objects', async () => {
    let fun = new ArrayToArrayOfObjects();

    let result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['source', [1, 2, 3]]])),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([{ value: 1 }, { value: 2 }, { value: 3 }]);

    result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    ['source', [1, 2, 3]],
                    ['keyName', ['number']],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([{ number: 1 }, { number: 2 }, { number: 3 }]);

    result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['source', [{ number: 1 }, { number: 2 }, { number: 3 }]]])),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { value: { number: 1 } },
        { value: { number: 2 } },
        { value: { number: 3 } },
    ]);

    result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    [
                        'source',
                        [
                            ['a', 1],
                            ['b', 2],
                            ['c', '3'],
                        ],
                    ],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { value1: 'a', value2: 1 },
        { value1: 'b', value2: 2 },
        { value1: 'c', value2: '3' },
    ]);

    result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    [
                        'source',
                        [
                            ['a', 1],
                            ['b', 2],
                            ['c', '3'],
                        ],
                    ],
                    ['keyName', ['key', 'value', 'other']],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { key: 'a', value: 1, other: undefined },
        { key: 'b', value: 2, other: undefined },
        { key: 'c', value: '3', other: undefined },
    ]);

    result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    [
                        'source',
                        [
                            ['a', 1],
                            ['b', 2],
                            ['c', '3'],
                        ],
                    ],
                    ['keyName', ['maKey']],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([{ maKey: 'a' }, { maKey: 'b' }, { maKey: 'c' }]);
});

test('Array with nested object and mixed', async () => {
    let fun = new ArrayToArrayOfObjects();

    let result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    ['source', [true, 1, 2, ['a', 'b', 'c']]],
                    ['keyName', ['akey', 'bkey', 'ckey']],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toMatchObject([
        { akey: true },
        { akey: 1 },
        { akey: 2 },
        { akey: 'a', bkey: 'b', ckey: 'c' },
    ]);
});

test('Array with nested array object and mixed without key', async () => {
    let fun = new ArrayToArrayOfObjects();

    let result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['source', [true, 1, 2, ['a', 'b', 'c']]]])),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { value: true },
        { value: 1 },
        { value: 2 },
        { value1: 'a', value2: 'b', value3: 'c' },
    ]);
});

test('Array with nested array object and mixed without key', async () => {
    let fun = new ArrayToArrayOfObjects();

    let result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([['source', [true, 1, 2, ['a', 'b', 'c', ['d', 'e'], { obj1: 'val1' }]]]]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { value: true },
        { value: 1 },
        { value: 2 },
        {
            value1: 'a',
            value2: 'b',
            value3: 'c',
            value4: ['d', 'e'],
            value5: {
                obj1: 'val1',
            },
        },
    ]);
});

test('Array with nested array object and mixed with key arrays', async () => {
    let fun = new ArrayToArrayOfObjects();

    let result = (
        await fun.execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(
                new Map([
                    ['source', [[true, false], [1, 'surendhar'], ['satya']]],
                    ['keyName', ['valueA', 'valueB', 'valueC', 'valueD']],
                ]),
            ),
        )
    )
        .allResults()[0]
        .getResult()
        .get('result');

    expect(result).toStrictEqual([
        { valueA: true, valueB: false, valueC: undefined, valueD: undefined },
        { valueA: 1, valueB: 'surendhar', valueC: undefined, valueD: undefined },
        { valueA: 'satya', valueB: undefined, valueC: undefined, valueD: undefined },
    ]);
});

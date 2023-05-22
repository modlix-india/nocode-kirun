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
        .get('output');

    expect(result).toMatchObject([{ value: 1 }, { value: 2 }, { value: 3 }]);

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
        .get('output');

    expect(result).toMatchObject([{ number: 1 }, { number: 2 }, { number: 3 }]);

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
        .get('output');

    expect(result).toMatchObject([
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
        .get('output');

    expect(result).toMatchObject([
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
        .get('output');

    expect(result).toMatchObject([
        { key: 'a', value: 1 },
        { key: 'b', value: 2 },
        { key: 'c', value: '3' },
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
        .get('output');

    expect(result).toMatchObject([{ maKey: 'a' }, { maKey: 'b' }, { maKey: 'c' }]);
});

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { Sort } from '../../../../../src/engine/function/system/array/Sort';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let sort: Sort = new Sort();

test('sort test 1', async () => {
    let arr: any[] = [];

    arr.push(12);
    arr.push(15);
    arr.push(98);
    arr.push(1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
            [Sort.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
        ]),
    );

    let res: any[] = [];
    res.push(1);
    res.push(12);
    res.push(15);
    res.push(98);

    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('sort test 2', async () => {
    let arr: any[] = [];

    arr.push(12);
    arr.push(15);
    arr.push(98);
    arr.push(1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
            [Sort.PARAMETER_BOOLEAN_ASCENDING.getParameterName(), false],
        ]),
    );

    let res: any[] = [];
    res.push(12);
    res.push(98);
    res.push(15);
    res.push(1);

    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('sort test 3', async () => {
    let arr: any[] = [];
    arr.push(12);
    arr.push(15);
    arr.push(98);
    arr.push(1);
    arr.push('sure');
    arr.push('c');

    let res: any[] = [];
    res.push(12);
    res.push(15);
    res.push(1);
    res.push(98);
    res.push('c');
    res.push('sure');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
            [Sort.PARAMETER_BOOLEAN_ASCENDING.getParameterName(), true],
        ]),
    );

    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('sort test 4', async () => {
    let a;
    let arr: any[] = [];
    arr.push(12);
    arr.push(null);
    arr.push(null);
    arr.push(15);
    arr.push(98);
    arr.push(1);
    arr.push('sure');
    arr.push('c');

    let res: any[] = [];
    res.push(12);
    res.push(null);

    res.push('sure');
    res.push('c');
    res.push(98);
    res.push(15);
    res.push(1);
    res.push(null);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
            [Sort.PARAMETER_BOOLEAN_ASCENDING.getParameterName(), false],
        ]),
    );

    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('sort test 5', async () => {
    let arr: any[] = ['Banana', 'Orange', 'Apple', 'Mango'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
            [Sort.PARAMETER_INT_LENGTH.getParameterName(), 3],
        ]),
    );
    let res: any[] = ['Banana', 'Apple', 'Mango', 'Orange'];
    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject(res);
});

test('sort test 6', async () => {
    let arr: any[] = [
        { order: 13 },
        { order: 3 },
        { order: 130 },
        { order: 10 },
        { order: 21 },
        { order: 1 },
    ];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
            [Sort.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
            [Sort.PARAMETER_KEY_PATH.getParameterName(), 'order'],
        ]),
    );
    let res: any[] = [
        { order: 1 },
        { order: 3 },
        { order: 10 },
        { order: 13 },
        { order: 21 },
        { order: 130 },
    ];
    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject(res);
});

test('sort test 7', async () => {
    let arr: any[] = [
        { order: { order: 13 } },
        { order: { order: 3 } },
        { order: { order: 130 } },
        { order: { order: 10 } },
        { order: { order: 21 } },
        { order: { order: 1 } },
    ];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
            [Sort.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
            [Sort.PARAMETER_KEY_PATH.getParameterName(), 'order.order'],
        ]),
    );
    let res: any[] = [
        { order: { order: 1 } },
        { order: { order: 3 } },
        { order: { order: 10 } },
        { order: { order: 13 } },
        { order: { order: 21 } },
        { order: { order: 130 } },
    ];
    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject(res);
});

test('sort test 8', async () => {
    let arr: any[] = [
        { order: { order: 13 } },
        { order: { order: 3 } },
        { order: { order: 130 } },
        { order: { order: 10 } },
        { order: { order: 21 } },
        { order: { order: 1 } },
    ];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Sort.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],
            [Sort.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
            [Sort.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
            [Sort.PARAMETER_KEY_PATH.getParameterName(), 'order.order'],
            [Sort.PARAMETER_BOOLEAN_ASCENDING.getParameterName(), false],
        ]),
    );
    let res: any[] = [
        { order: { order: 130 } },
        { order: { order: 21 } },
        { order: { order: 13 } },
        { order: { order: 10 } },
        { order: { order: 3 } },
        { order: { order: 1 } },
    ];
    expect((await sort.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject(res);
});

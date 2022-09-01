import { Min } from '../../../../../src/engine/function/system/array/Min';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

let min: Min = new Min();

test('min test 1 ', async () => {
    let arr: any[] = [];
    arr.push(null);
    arr.push(12);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));

    expect(
        (await min.execute(fep)).allResults()[0].getResult().get(Min.EVENT_RESULT_ANY.getName()),
    ).toBe(12);
});

test('min test 2 ', async () => {
    let arr: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));

    await expect(min.execute(fep)).rejects.toThrow();
});

test('min test 3', async () => {
    let arr: any[] = [];
    arr.push(12);
    arr.push(15);
    arr.push(null);
    arr.push(98);
    arr.push(1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));
    expect((await min.execute(fep)).allResults()[0].getResult().get('output')).toBe(1);
});

test('min test 4', async () => {
    let arr: any[] = [];
    arr.push('nocode');
    arr.push('NoCode');
    arr.push('platform');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));
    expect((await min.execute(fep)).allResults()[0].getResult().get('output')).toBe('NoCode');
});

test('min test 5', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), null]]));
    await expect(min.execute(fep)).rejects.toThrow();
});

test('min test 6', async () => {
    let arr: any[] = [];

    arr.push(456);
    arr.push('nocode');

    arr.push('NoCode');
    arr.push('platform');
    arr.push(123);
    arr.push(1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));
    expect((await min.execute(fep)).allResults()[0].getResult().get('output')).toBe(1);
});

test('min test 7', async () => {
    let arr1: any[] = [];
    arr1.push('c');
    arr1.push('r');
    arr1.push('d');
    arr1.push('s');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1]]));

    expect((await min.execute(fep)).allResults()[0].getResult().get('output')).toBe('c');
});

test('min test 8', async () => {
    let arr: any[] = ['surendhar'];
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([[Min.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]));
    expect((await min.execute(fep)).allResults()[0].getResult().get('output')).toBe('surendhar');
});

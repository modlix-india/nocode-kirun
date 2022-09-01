import { Delete } from '../../../../../src/engine/function/system/array/Delete';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

test('Delete Test 1', async () => {
    let delet: Delete = new Delete();
    let source: any[] = [12, 14, 15, 9];

    let secondSource: any[] = [14, 15];

    let temp: any[] = [12, 9];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('Delete Test 2', async () => {
    let delet: Delete = new Delete();
    let source: any[] = ['nocode', 'platform', 14];

    let secondSource: any[] = ['platform'];

    let temp: any[] = ['nocode', 14];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('Delete Test 3', async () => {
    let delet: Delete = new Delete();
    let source = undefined;

    let secondSource: any[] = ['platform'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('Delete Test 3', async () => {
    let delet: Delete = new Delete();
    let source: any[] = ['platform'];

    let secondSource = undefined;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('Delete Test 4', async () => {
    let delet: Delete = new Delete();
    let source: any[] = ['platform'];

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('Delete Test 5', async () => {
    let delet: Delete = new Delete();

    var arr1: any[] = ['nocode', 'platform', 14];
    var arr2: any[] = ['nocode', 'platiform', 14];
    var obj: object = {
        arr: arr1,
        sri: 'krishna',
        name: 'surendhar',
    };

    var arr: any[] = [];
    arr.push(arr1);
    arr.push(arr2);
    arr.push(obj);
    arr.push(arr2);
    arr.push(obj);

    var delArr: any[] = [];
    delArr.push(obj);
    delArr.push('2');
    delArr.push([]);

    var res: any[] = [arr1, arr2, arr2];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), delArr],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(arr).toStrictEqual(res);
});

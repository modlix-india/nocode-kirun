import { DeleteLast } from '../../../../../src/engine/function/system/array/DeleteLast';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

test('DeleteLast Test 1', async () => {
    let delet: DeleteLast = new DeleteLast();

    let source: any[] = [12, 14, 15, 9];

    let temp: any[] = [12, 14, 15];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map([[DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('DeleteLast Test 2', async () => {
    let delet: DeleteLast = new DeleteLast();

    let source: any[] = ['c', 'p', 'i', 'e'];

    let temp: any[] = ['c', 'p', 'i'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map([[DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('DeleteLast Test 3', async () => {
    let delet: DeleteLast = new DeleteLast();

    let source: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map([[DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('DeleteFirst Test 4', async () => {
    let delet: DeleteLast = new DeleteLast();

    var array1 = ['test', 'Driven', 'developement', 'I', 'am'];

    var js1 = {
        boolean: false,
        array: array1,
        char: 'o',
    };

    var js2 = {
        boolean: false,
        array: array1,
        char: 'asd',
    };

    var js3 = {
        array: array1,
    };

    var js4 = {
        boolean: false,
        array: array1,
        char: 'ocbfr',
    };

    var arr: any[] = [js1, js2, js3, js4, js1];

    var res: any[] = [js1, js2, js3, js4];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map([[DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]))
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('DeleteFirst Test 5', async () => {
    let delet: DeleteLast = new DeleteLast();

    let source = null;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map([[DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

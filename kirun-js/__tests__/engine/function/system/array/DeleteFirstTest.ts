import { DeleteFirst } from '../../../../../src/engine/function/system/array/DeleteFirst';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('DeleteFirst Test 1', () => {
    let delet: DeleteFirst = new DeleteFirst();

    let source: any[] = [12, 14, 15, 9];

    let temp: any[] = [14, 15, 9];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map([[DeleteFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setOutput(new Map([]))
        .setContext(new Map([]));

    delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('DeleteFirst Test 2', () => {
    let delet: DeleteFirst = new DeleteFirst();

    let source: any[] = ['c', 'p', 'i', 'e'];

    let temp: any[] = ['p', 'i', 'e'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map([[DeleteFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setOutput(new Map([]))
        .setContext(new Map([]));

    delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('DeleteFirst Test 3', () => {
    let delet: DeleteFirst = new DeleteFirst();

    let source: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map([[DeleteFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setOutput(new Map([]))
        .setContext(new Map([]));

    expect(() => delet.execute(fep)).toThrow();
});

test('DeleteFirst Test 4', () => {
    let delet: DeleteFirst = new DeleteFirst();

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

    var res: any[] = [js2, js3, js4, js1];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map([[DeleteFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]))
        .setOutput(new Map([]))
        .setContext(new Map([]));

    delet.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('DeleteFirst Test 5', () => {
    let delet: DeleteFirst = new DeleteFirst();

    let source = null;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map([[DeleteFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source]]))
        .setOutput(new Map([]))
        .setContext(new Map([]));

    expect(() => delet.execute(fep)).toThrow();
});

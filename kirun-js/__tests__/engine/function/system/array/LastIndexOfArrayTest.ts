import { LastIndexOfArray } from '../../../../../src/engine/function/system/array/LastIndexOfArray';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let larr: LastIndexOfArray = new LastIndexOfArray();

test('Last Index of array Test 1', async () => {
    let array: string[] = [];

    array.push('a');
    array.push('b');
    array.push('c');
    array.push('d');
    array.push('a');
    array.push('b');
    array.push('c');
    array.push('e');
    array.push('d');
    array.push('b');
    array.push('c');
    array.push('d');

    let res: string[] = [];
    res.push('b');
    res.push('c');
    res.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );
    expect(
        (await larr.execute(fep))
            .allResults()[0]
            .getResult()
            .get(LastIndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(9);
});

test('last index of array test 2', async () => {
    let arr: any[] = [];
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('e');
    arr.push('d');
    arr.push('b');
    arr.push('c');
    arr.push('d');

    let res: any[] = [];
    res.push('b');
    res.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );

    expect(
        (await larr.execute(fep))
            .allResults()[0]
            .getResult()
            .get(LastIndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

test('last index of array test 3', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('e');
    arr.push('d');
    arr.push('b');
    arr.push('c');
    arr.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );

    await expect(larr.execute(fep)).rejects.toThrow();

    let fep1: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );

    await expect(larr.execute(fep1)).rejects.toThrow();
});

test('last index of array test 4', async () => {
    let array1: any[] = [];

    array1.push('test');
    array1.push('Driven');
    array1.push('developement');
    array1.push('I');
    array1.push('am');
    array1.push('using');
    array1.push('eclipse');
    array1.push('I');
    array1.push('to');
    array1.push('test');
    array1.push('the');
    array1.push('changes');
    array1.push('with');
    array1.push('test');
    array1.push('Driven');
    array1.push('developement');

    var array2: any[] = [];

    array2.push('test');
    array2.push('Driven');
    array2.push('developement');
    array2.push('I');
    array2.push('am');
    array2.push('using');
    array2.push('eclipse');
    array2.push('I');
    array2.push('to');
    array2.push('test');
    array2.push('the');
    array2.push('changes');
    array2.push('with');

    var array3: any[] = [];

    array3.push('test');
    array3.push('Driven');
    array3.push('developement');
    array3.push('I');
    array3.push('am');
    array3.push('using');
    array3.push('eclipse');
    array3.push('I');
    array3.push('to');
    array3.push('test');
    array3.push('the');
    array3.push('changes');
    array3.push('with');
    array3.push('test');
    array3.push('Driven');
    array3.push('developement');

    var array4: any[] = [];

    array4.push('test');
    array4.push('Driven');
    array4.push('developement');
    array4.push('I');
    array4.push('am');
    array4.push('using');
    array4.push('eclipse');
    array4.push('I');
    array4.push('to');

    var arr: any[] = [];
    arr.push(array2);
    arr.push(array1);
    arr.push(array1);
    arr.push(array4);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(array1);
    arr.push(array4);

    var res: any[] = [];
    res.push(array1);
    res.push(array1);
    res.push(array4);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );

    expect(
        (await larr.execute(fep))
            .allResults()[0]
            .getResult()
            .get(LastIndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(7);
});

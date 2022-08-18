import { IndexOfArray } from '../../../../../src/engine/function/system/array/IndexOfArray';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let ioa: IndexOfArray = new IndexOfArray();

test('index of array 1', () => {
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

    let res: any[] = [];
    res.push('b');
    res.push('c');
    res.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );

    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(1);
});

test('Index of array 2', () => {
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

    let res: any[] = [];
    res.push('b');
    res.push('c');
    res.push('e');
    res.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 4],
        ]),
    );

    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(5);
});

test('index of array 3', () => {
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

    let array2: any[] = [];
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

    let array3: any[] = [];
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

    let array4: any[] = [];
    array4.push('test');
    array4.push('Driven');
    array4.push('developement');
    array4.push('I');
    array4.push('am');
    array4.push('using');
    array4.push('eclipse');
    array4.push('I');
    array4.push('to');

    let arr: any[] = [];
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(array1);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(array1);
    arr.push(array4);

    let res: any[] = [];
    res.push(array1);
    res.push(array1);
    res.push(array4);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );

    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(7);
});

test('index of array test 4', () => {
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

    let res: any[] = [];
    res.push('b');
    res.push('e');
    res.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 4],
        ]),
    );

    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

test('index of array 5', () => {
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

    let as: any[] = [];
    as.push('c');
    as.push('e');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), as],
        ]),
    );
    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(6);
});

test('index of array 6', () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
        ]),
    );
    expect(() => ioa.execute(fep)).toThrow;
});

test('index of array 3', () => {
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

    let array2: any[] = [];
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

    let array3: any[] = [];
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

    let array4: any[] = [];
    array4.push('test');
    array4.push('Driven');
    array4.push('developement');
    array4.push('I');
    array4.push('am');
    array4.push('using');
    array4.push('eclipse');
    array4.push('I');
    array4.push('to');

    let arr: any[] = [];
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(array1);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(array1);
    arr.push(array4);

    let res: any[] = [];
    res.push(array1);
    res.push(array1);
    res.push(array4);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), arr.length],
        ]),
    );

    expect(
        ioa
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(IndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

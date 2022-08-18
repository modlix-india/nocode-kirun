import { MisMatch } from '../../../../../src/engine/function/system/array/MisMatch';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let mismatch: MisMatch = new MisMatch();

test('mismatch test 1', () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('l');
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
            [MisMatch.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [MisMatch.PARAMETER_INT_FIND_FROM.getParameterName(), 7],
            [MisMatch.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 0],
            [MisMatch.PARAMETER_INT_LENGTH.getParameterName(), 3],
        ]),
    );

    expect(
        mismatch
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(MisMatch.EVENT_RESULT_INTEGER.getName()),
    ).toBe(2);
});

test('mismatch test 2', () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('l');
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
            [MisMatch.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [MisMatch.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
            [MisMatch.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 2],
            [MisMatch.PARAMETER_INT_LENGTH.getParameterName(), 5],
        ]),
    );

    expect(() => mismatch.execute(fep)).toThrow();
});

test('Mismatch test 3', () => {
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
            [MisMatch.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [MisMatch.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
            [MisMatch.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 3],
            [MisMatch.PARAMETER_INT_LENGTH.getParameterName(), 3],
        ]),
    );

    expect(
        mismatch
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(MisMatch.EVENT_RESULT_INTEGER.getName()),
    ).toBe(2);
});

test('mismatch test 4', () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('l');
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
            [MisMatch.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [MisMatch.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
            [MisMatch.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res],
            [MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 0],
            [MisMatch.PARAMETER_INT_LENGTH.getParameterName(), 3],
        ]),
    );

    expect(
        mismatch
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(MisMatch.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

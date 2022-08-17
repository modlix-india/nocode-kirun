import { LastIndexOfArray } from '../../../../../src/engine/function/system/array/LastIndexOfArray';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let larr: LastIndexOfArray = new LastIndexOfArray();

test('Last Index of array Test 1', () => {
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
        larr
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(LastIndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(9);
});

test('last index of array test 2', () => {
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
        larr
            .execute(fep)
            .allResults()[0]
            .getResult()
            .get(LastIndexOfArray.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

test('last index of array test 3', () => {
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

    expect(() => larr.execute(fep)).toThrow;

    let fep1: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
            [LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
            [LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );

    expect(() => larr.execute(fep1)).toThrow;
});

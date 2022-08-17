import { SubArray } from '../../../../../src/engine/function/system/array/SubArray';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('SubArray of Test 1', () => {
    let sub: SubArray = new SubArray();

    let array: any[] = [];

    array.push('test');
    array.push('Driven');
    array.push('developement');
    array.push('I');
    array.push('am');
    array.push('using');
    array.push('eclipse');
    array.push('I');
    array.push('to');
    array.push('test');
    array.push('the');
    array.push('changes');
    array.push('with');
    array.push('test');
    array.push('Driven');
    array.push('developement');

    let res: any[] = [];

    res.push('am');
    res.push('using');
    res.push('eclipse');
    res.push('I');
    res.push('to');
    res.push('test');
    res.push('the');
    res.push('changes');
    res.push('with');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [SubArray.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                [SubArray.PARAMETER_INT_FIND_FROM.getParameterName(), 4],
                [SubArray.PARAMETER_INT_LENGTH.getParameterName(), 9],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    sub.execute(fep);

    expect(array).toStrictEqual(res);
});

test('SubArray of Test 2', () => {
    let sub: SubArray = new SubArray();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [SubArray.PARAMETER_ARRAY_SOURCE.getParameterName(), undefined],
                [SubArray.PARAMETER_INT_FIND_FROM.getParameterName(), 4],
                [SubArray.PARAMETER_INT_LENGTH.getParameterName(), 9],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    expect(() => sub.execute(fep)).toThrow;
});

test('SubArray of Test 2', () => {
    let sub: SubArray = new SubArray();

    let array: any[] = [];
    array.push('a');
    array.push('b');
    array.push('c');
    array.push('d');
    array.push('l');
    array.push('d');
    array.push('a');
    array.push('b');
    array.push('c');
    array.push('e');
    array.push('d');

    let res: any[] = [];
    res.push('b');
    res.push('c');
    res.push('d');
    res.push('l');
    res.push('d');
    res.push('a');
    res.push('b');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [SubArray.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                [SubArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
                [SubArray.PARAMETER_INT_LENGTH.getParameterName(), 7],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    sub.execute(fep);

    expect(array).toStrictEqual(res);
});

test('SubArray of Test 3', () => {
    let sub: SubArray = new SubArray();

    let array: any[] = [];
    array.push('a');
    array.push('b');
    array.push('c');
    array.push('d');
    array.push('l');
    array.push('d');
    array.push('a');
    array.push('b');
    array.push('c');
    array.push('e');
    array.push('d');

    let res: any[] = [];
    res.push('b');
    res.push('c');
    res.push('d');
    res.push('l');
    res.push('d');
    res.push('a');
    res.push('b');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [SubArray.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                [SubArray.PARAMETER_INT_FIND_FROM.getParameterName(), 1123],
                [SubArray.PARAMETER_INT_LENGTH.getParameterName(), 7],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    expect(() => sub.execute(fep)).toThrow;
});

test('SubArray of Test 4', () => {
    let sub: SubArray = new SubArray();

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

    let array: any[] = [];
    array.push(array2);
    array.push(array4);
    array.push(array1);
    array.push(array1);
    array.push(array1);
    array.push(array3);
    array.push(array2);
    array.push(array4);
    array.push(array1);
    array.push(array1);
    array.push(array4);

    let res: any[] = [];
    res.push(array2);
    res.push(array4);
    res.push(array1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [SubArray.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                // [SubArray.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
                [SubArray.PARAMETER_INT_LENGTH.getParameterName(), 3],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    sub.execute(fep);

    expect(array).toStrictEqual(res);
});

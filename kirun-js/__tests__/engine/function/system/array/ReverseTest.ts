import { Reverse } from '../../../../../src/engine/function/system/array/Reverse';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let rev: Reverse = new Reverse();

test('Reverse test 1 ', async () => {
    let src: any[] = [4, 5, 6, 7];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), src],
                [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 0],
                [Reverse.PARAMETER_INT_LENGTH.getParameterName(), 2],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res = [5, 4, 6, 7];
    await rev.execute(fep);
    expect(src).toStrictEqual(res);
});

test('Reverse test 2 ', async () => {
    let src: any[] = [];

    src.push('I');
    src.push('am');
    src.push('using');
    src.push('eclipse');
    src.push('to');
    src.push('test');
    src.push('the');
    src.push('changes');
    src.push('with');
    src.push('test');
    src.push('Driven');
    src.push('developement');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), src],
                [Reverse.PARAMETER_INT_LENGTH.getParameterName(), -2],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    await expect(rev.execute(fep)).rejects.toThrow();
});

test('Reverse test 3', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setContext(new Map([]))
        .setSteps(new Map([]));

    fep.setArguments(
        new Map<string, any>([
            [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Reverse.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
        ]),
    );
    await expect(rev.execute(fep)).rejects.toThrow();
});

test('Rev test 4', async () => {
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
    arr.push(array1);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
                [Reverse.PARAMETER_INT_LENGTH.getParameterName(), arr.length - 2],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
    res.push(array1);
    res.push(array4);
    res.push(array2);
    res.push(array3);
    res.push(array1);

    await rev.execute(fep);

    expect(arr).toStrictEqual(res);

    fep.setArguments(
        new Map<string, any>([
            [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
            [Reverse.PARAMETER_INT_LENGTH.getParameterName(), arr.length - 1],
        ]),
    );

    let res1: any[] = [];
    res1.push(array1);
    res1.push(array1);
    res1.push(array3);
    res1.push(array2);
    res1.push(array4);

    await rev.execute(fep);
    expect(arr).toStrictEqual(res1);
});

test('rev test 5', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('a');
    arr.push('c');
    arr.push('d');
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
    res.push('a');
    res.push('b');
    res.push('d');
    res.push('c');
    res.push('b');
    res.push('a');
    res.push('d');
    res.push('c');
    res.push('a');

    await rev.execute(fep);
    expect(arr).toStrictEqual(res);

    fep.setArguments(
        new Map<string, any>([
            [Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Reverse.PARAMETER_INT_LENGTH.getParameterName(), arr.length],
        ]),
    );

    await expect(rev.execute(fep)).rejects.toThrow();
});

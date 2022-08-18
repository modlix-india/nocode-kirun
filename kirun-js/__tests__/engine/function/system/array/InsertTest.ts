import { Insert } from '../../../../../src/engine/function/system/array/Insert';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Insert of Test 1', () => {
    let ins: Insert = new Insert();

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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                [Insert.PARAMETER_INT_OFFSET.getParameterName(), 4],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push('test');
    res.push('Driven');
    res.push('developement');
    res.push('I');
    res.push(['this is an array']);
    res.push('am');
    res.push('using');
    res.push('eclipse');
    res.push('I');
    res.push('to');
    res.push('test');
    res.push('the');
    res.push('changes');
    res.push('with');
    res.push('test');
    res.push('Driven');
    res.push('developement');

    ins.execute(fep);

    expect(array).toStrictEqual(res);
});

test('Insert of Test 2', () => {
    let ins: Insert = new Insert();

    let arr: any[] = [];

    arr.push(1);
    arr.push(2);
    arr.push(3);
    arr.push(4);
    arr.push(5);
    arr.push(6);
    arr.push(7);
    arr.push(8);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Insert.PARAMETER_INT_OFFSET.getParameterName(), 2],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push(1);
    res.push(2);
    res.push(['this is an array']);
    res.push(3);
    res.push(4);
    res.push(5);
    res.push(6);
    res.push(7);
    res.push(8);
    ins.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('Insert of Test 3', () => {
    let ins: Insert = new Insert();

    let arr: any[] = [];

    arr.push(1);
    arr.push(2);
    arr.push(3);
    arr.push(4);
    arr.push(5);
    arr.push(6);
    arr.push(7);
    arr.push(8);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Insert.PARAMETER_INT_OFFSET.getParameterName(), 0],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push(['this is an array']);
    res.push(1);
    res.push(2);
    res.push(3);
    res.push(4);
    res.push(5);
    res.push(6);
    res.push(7);
    res.push(8);
    ins.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('Insert of Test 4', () => {
    let ins: Insert = new Insert();

    let arr: any[] = [];

    arr.push(1);
    arr.push(2);
    arr.push(3);
    arr.push(4);
    arr.push(5);
    arr.push(6);
    arr.push(7);
    arr.push(8);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Insert.PARAMETER_INT_OFFSET.getParameterName(), arr.length],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push(1);
    res.push(2);
    res.push(3);
    res.push(4);
    res.push(5);
    res.push(6);
    res.push(7);
    res.push(8);
    res.push(['this is an array']);

    ins.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('Insert of Test 5', () => {
    let ins: Insert = new Insert();

    let arr: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push(['this is an array']);

    ins.execute(fep);

    expect(arr).toStrictEqual(res);
});

test('Insert of Test 6', () => {
    let ins: Insert = new Insert();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [Insert.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
                [Insert.PARAMETER_INT_OFFSET.getParameterName(), 0],
                [Insert.PARAMETER_ANY.getParameterName(), ['this is an array']],
            ]),
        )
        .setOutput(new Map([]))
        .setContext(new Map([]));

    let res: any[] = [];

    res.push(['this is an array']);

    expect(() => ins.execute(fep)).toThrow();
});

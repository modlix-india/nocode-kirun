import { IndexOf } from '../../../../../src/engine/function/system/array/IndexOf';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Index of Test 1', () => {
    let ind: IndexOf = new IndexOf();

    let array: string[] = [];

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

    let find: string = 'with';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
            [IndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );
    expect(
        ind.execute(fep).allResults()[0].getResult().get(IndexOf.EVENT_RESULT_INTEGER.getName()),
    ).toBe(12);
});

test('Index of Test 2', () => {
    let ind: IndexOf = new IndexOf();

    let array: string[] = [];

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

    let find: string = 'with';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
            [IndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), -2],
        ]),
    );

    expect(() => ind.execute(fep)).toThrow();

    fep.setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), []],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
            [IndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), -2],
        ]),
    );
    expect(ind.execute(fep).allResults()[0].getResult().get(IndexOf.EVENT_INDEX.getName())).toBe(
        -1,
    );
});

test('Index of Test 3', () => {
    let ind: IndexOf = new IndexOf();

    let array: string[] = [];

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

    let find: string = 'witah';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
            [IndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 0],
        ]),
    );

    expect(
        ind.execute(fep).allResults()[0].getResult().get(IndexOf.EVENT_RESULT_INTEGER.getName()),
    ).toBe(-1);
});

test('Index of Test 4', () => {
    let ind: IndexOf = new IndexOf();

    let array: string[] = [];

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

    let array2: string[] = [];

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

    let array3: string[] = [];
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

    let array4: string[] = [];
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
    arr.push(array3);
    arr.push(array);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array);

    let find: any = array;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
            [IndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );

    expect(
        ind.execute(fep).allResults()[0].getResult().get(IndexOf.EVENT_RESULT_INTEGER.getName()),
    ).toBe(2);
});

test('indexof test 5', () => {
    let ind: IndexOf = new IndexOf();

    let array1: string[] = [];

    array1.push('test');
    array1.push('Driven');
    array1.push('developement');
    array1.push('I');
    array1.push('am');

    let js1: object = {
        boolean: false,
        array: array1,
        char: 'o',
    };

    let js2: object = {
        boolean: false,
        array: array1,
        char: 'asd',
    };

    let js3: object = {
        array: array1,
    };

    let js4: object = {
        boolean: false,
        array: array1,
        char: 'o',
    };

    let arr: any[] = [js1, js2, js1, js3, js3, js4, js1];
    let find: any = js4;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), find],
        ]),
    );

    expect(
        ind.execute(fep).allResults()[0].getResult().get(IndexOf.EVENT_RESULT_INTEGER.getName()),
    ).toBe(5);

    fep.setArguments(
        new Map<string, any>([
            [IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [IndexOf.PARAMETER_ANY_NOT_NULL.getParameterName(), null],
        ]),
    );
    expect(() => ind.execute(fep)).toThrow();
});

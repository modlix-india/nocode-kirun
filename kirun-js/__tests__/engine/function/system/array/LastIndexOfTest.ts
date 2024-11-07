import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { LastIndexOf } from '../../../../../src/engine/function/system/array/LastIndexOf';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Last Index of Test 1', async () => {
    let lind: LastIndexOf = new LastIndexOf();

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

    let find: string = 'test';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), find],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );
    expect(
        (await lind.execute(fep)).allResults()[0].getResult().get(LastIndexOf.EVENT_RESULT_NAME),
    ).toBe(13);
});

test('Last Index of Test 2', async () => {
    let lind: LastIndexOf = new LastIndexOf();

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

    let find;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), find],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), -2],
        ]),
    );
    await expect(lind.execute(fep)).rejects.toThrow();
});

test('Last Index of Test 3', async () => {
    let lind: LastIndexOf = new LastIndexOf();

    let find;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), find],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 2],
        ]),
    );
    await expect(lind.execute(fep)).rejects.toThrow();
});

test('Last Index of Test 4', async () => {
    let lind: LastIndexOf = new LastIndexOf();

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

    let find: string = 'developement';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), find],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 12],
        ]),
    );
    expect(
        (await lind.execute(fep)).allResults()[0].getResult().get(LastIndexOf.EVENT_RESULT_NAME),
    ).toBe(15);

    fep.setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), 'newas'],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 12],
        ]),
    );
    expect(
        (await lind.execute(fep)).allResults()[0].getResult().get(LastIndexOf.EVENT_RESULT_NAME),
    ).toBe(-1);
});

test('Last Index of Test 5', async () => {
    let lind: LastIndexOf = new LastIndexOf();

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

    let find = 'changes';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), find],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), -2],
        ]),
    );
    await expect(lind.execute(fep)).rejects.toThrow();
});

test('Last Index of Test 6', async () => {
    let lind: LastIndexOf = new LastIndexOf();

    let arr: any[] = [];

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
        char: 'asdsd',
    };

    arr.push(js1);
    arr.push(js2);
    arr.push(js4);
    arr.push(js3);
    arr.push(js4);
    arr.push(js1);
    arr.push(js1);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName(), js4],
            [LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName(), 1],
        ]),
    );
    expect((await lind.execute(fep)).allResults()[0].getResult().get('result')).toBe(4);
});

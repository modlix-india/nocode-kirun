import { DeleteFrom } from '../../../../../src/engine/function/system/array/DeleteFrom';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

let del: DeleteFrom = new DeleteFrom();

test('delete from 1', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('e');
    arr.push('f');
    arr.push('g');
    arr.push('h');
    arr.push('i');
    arr.push('a');
    arr.push('a');
    arr.push('a');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [DeleteFrom.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [DeleteFrom.PARAMETER_INT_SOURCE_FROM.getParameterName(), 6],
                [DeleteFrom.PARAMETER_INT_LENGTH.getParameterName(), 6],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
    res.push('a');
    res.push('b');
    res.push('c');
    res.push('d');
    res.push('e');
    res.push('f');

    expect((await del.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('del from 2 ', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('e');
    arr.push('f');
    arr.push('g');
    arr.push('h');
    arr.push('i');
    arr.push('a');
    arr.push('a');
    arr.push('a');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [DeleteFrom.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [DeleteFrom.PARAMETER_INT_SOURCE_FROM.getParameterName(), 6],
                [DeleteFrom.PARAMETER_INT_LENGTH.getParameterName(), 3],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
    res.push('a');
    res.push('b');
    res.push('c');
    res.push('d');
    res.push('e');
    res.push('f');
    res.push('a');
    res.push('a');
    res.push('a');

    expect((await del.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('del from 3 ', async () => {
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

    let obj: object = {
        fname: 'surendhar',
        lname: 's',
        age: 23,
        company: 'fincity',
    };

    let arr: any[] = [];
    arr.push(obj);
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

    res.push(obj);
    res.push(array2);
    res.push(array4);
    res.push(array1);
    res.push(array1);
    res.push(array1);
    res.push(array1);
    res.push(array4);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [DeleteFrom.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [DeleteFrom.PARAMETER_INT_SOURCE_FROM.getParameterName(), 5],
                [DeleteFrom.PARAMETER_INT_LENGTH.getParameterName(), 3],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));
    expect((await del.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(res);
});

test('del from 4', async () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('e');
    arr.push('f');
    arr.push('g');
    arr.push('h');
    arr.push('i');
    arr.push('a');
    arr.push('a');
    arr.push('a');

    let res: any[] = [];
    res.push('a');
    res.push('b');
    res.push('c');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [DeleteFrom.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
                [DeleteFrom.PARAMETER_INT_SOURCE_FROM.getParameterName(), 3],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));
    expect((await del.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject(res);
});

import { Shuffle } from '../../../../../src/engine/function/system/array/Shuffle';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let shuffle: Shuffle = new Shuffle();

test('shuffle test 1', async () => {
    let array: any[] = [];
    array.push('I');
    array.push('am');
    array.push('using');
    array.push('eclipse');
    array.push('to');
    array.push('test');
    array.push('the');
    array.push('changes');
    array.push('with');
    array.push('test');
    array.push('Driven');
    array.push('developement');

    let res: any[] = [];
    res.push('I');
    res.push('am');
    res.push('using');
    res.push('eclipse');
    res.push('to');
    res.push('test');
    res.push('the');
    res.push('changes');
    res.push('with');
    res.push('test');
    res.push('Driven');
    res.push('developement');

    let set1: Set<any> = new Set<any>();

    res.forEach((element) => set1.add(element));

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([[Shuffle.PARAMETER_ARRAY_SOURCE.getParameterName(), array]]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let set2: Set<any> = new Set<any>();

    (await shuffle.execute(fep))
        .allResults()[0]
        .getResult()
        .get(Shuffle.EVENT_RESULT_EMPTY.getName());

    array.forEach((element) => set2.add(element));

    expect(set2).toStrictEqual(set1);
});

test('Shuffle test 2', async () => {
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

    let js1: object = {
        boolean: false,
        array: array4,
        char: 'asda',
    };

    let arr: any[] = [];
    arr.push(array1);
    arr.push(array3);
    arr.push(array2);
    arr.push(array4);
    arr.push(array1);
    arr.push(js1);

    let res: any[] = [];
    let Set1: Set<object> = new Set<Object>();
    arr.forEach((el) => {
        res.push(el);
        Set1.add(el);
    });

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([[Shuffle.PARAMETER_ARRAY_SOURCE.getParameterName(), arr]]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await shuffle.execute(fep);

    let Set2: Set<object> = new Set<Object>();

    arr.forEach((el) => Set2.add(el));

    expect(Set2).toStrictEqual(Set1);
});

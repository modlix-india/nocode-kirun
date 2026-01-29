import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { Disjoint } from '../../../../../src/engine/function/system/array/Disjoint';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Disjoint Test 1', async () => {
    let dis: Disjoint = new Disjoint();

    let arr: any[] = [];

    arr.push('a');
    arr.push('b');
    arr.push('c'); //
    arr.push('d');
    arr.push('e');
    arr.push('f'); //

    let arr2: any[] = [];
    arr2.push('a'); //
    arr2.push('b');
    arr2.push('p');
    arr2.push('a'); //
    arr2.push('f');
    arr2.push('f');
    arr2.push('e');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2],
            [Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 0],
            [Disjoint.PARAMETER_INT_LENGTH.getParameterName(), 4],
        ]),
    );

    let set1: Set<any> = new Set<any>();
    set1.add('c');
    set1.add('d');
    set1.add('e');
    set1.add('f');
    set1.add('a');
    set1.add('b');
    set1.add('p');

    let set2: Set<any> = new Set<any>();
    let res: any[] = (await dis.execute(fep))
        .allResults()[0]
        .getResult()
        .get(Disjoint.EVENT_RESULT_NAME);

    res.forEach((element) => set2.add(element));

    expect(set1).toStrictEqual(set2);
});

test('Disjoint Test 2', async () => {
    let dis: Disjoint = new Disjoint();

    let arr: any[] = [];

    arr.push('a');
    arr.push('b');
    arr.push('c'); //
    arr.push('d');
    arr.push('e');
    arr.push('f'); //

    let arr2: any[] = [];
    arr2.push('a'); //
    arr2.push('b');
    arr2.push('a');
    arr2.push('b');
    arr2.push('c');
    arr2.push('d'); //
    arr2.push('e');
    arr2.push('f');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), -12],
            [Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2],
            [Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 2],
            [Disjoint.PARAMETER_INT_LENGTH.getParameterName(), 3],
        ]),
    );

    expect(dis.execute(fep)).rejects.toThrow();
});

test('Disjoint test 3', async () => {
    let dis: Disjoint = new Disjoint();

    let array1: any[] = [];
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
        char: 's',
    };

    let js5: object = {
        boolean: false,
        array: array1,
        char: 'b',
    };

    let js6: object = {
        booleasan: false,
        arraay: array1,
        char: 'o',
    };

    let js7: object = {
        booleasan: false,
        arrrraay: array1,
        char: 'o',
    };

    var arr: any[] = [];

    arr.push(js1);
    arr.push(js2);
    arr.push(js3);
    arr.push(js4);
    arr.push(js1);

    var arr2: any[] = [];
    arr2.push(js5);
    arr2.push(js6);
    arr2.push(js7);
    arr2.push(js1);

    var d: any[] = [];
    d.push(js2);
    d.push(js3);
    d.push(js4);
    d.push(js5);
    d.push(js6);
    d.push(js7);

    let set1: Set<Object> = new Set<Object>();

    for (let i: number = 0; i < d.length; i++) set1.add(d[i]);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr],
            [Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
            [Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2],
            [Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), 0],
            [Disjoint.PARAMETER_INT_LENGTH.getParameterName(), 4],
        ]),
    );

    var res: any[] = (await dis.execute(fep)).allResults()[0].getResult().get('result');

    let set2: Set<Object> = new Set<Object>();

    for (let i: number = 0; i < res.length; i++) set2.add(res[i]);

    expect(set1).toMatchObject(set2);
});

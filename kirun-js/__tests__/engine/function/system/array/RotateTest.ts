import { Rotate } from '../../../../../src/engine/function/system/array/Rotate';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

let rotate: Rotate = new Rotate();

test('Rotate test1 ', async () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [Rotate.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
                [Rotate.PARAMETER_ROTATE_LENGTH.getParameterName(), 16],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
    res.push('to');
    res.push('test');
    res.push('the');
    res.push('changes');
    res.push('with');
    res.push('test');
    res.push('Driven');
    res.push('developement');

    res.push('I');
    res.push('am');
    res.push('using');
    res.push('eclipse');

    expect((await rotate.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        res,
    );
});

test('rotate  test 2', async () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([
                [Rotate.PARAMETER_ARRAY_SOURCE.getParameterName(), src],
                [Rotate.PARAMETER_ROTATE_LENGTH.getParameterName(), src.length - 1],
            ]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];

    res.push('developement');
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

    expect((await rotate.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        res,
    );
});

test('rotate test 3', async () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map<string, any>([[Rotate.PARAMETER_ARRAY_SOURCE.getParameterName(), array]]),
        )
        .setContext(new Map([]))
        .setSteps(new Map([]));

    let res: any[] = [];
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

    res.push('I');

    expect((await rotate.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        res,
    );
});

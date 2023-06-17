import { Frequency } from '../../../../../src/engine/function/system/array/Frequency';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

let freq: Frequency = new Frequency();

test('freq test 1', async () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'I'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 10],
        ]),
    );

    expect(
        (await freq.execute(fep)).allResults()[0].getResult().get(Frequency.EVENT_RESULT_NAME),
    ).toBe(2);
});

test('freq test 2', async () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'developement'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), -2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 20],
        ]),
    );

    await expect(freq.execute(fep)).rejects.toThrow();

    fep.setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'developement'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 20],
        ]),
    );

    await expect(freq.execute(fep)).rejects.toThrow();
});

test('freq test 3', async () => {
    let array: any[] = [];
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'I'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 10],
        ]),
    );

    expect(
        (await freq.execute(fep)).allResults()[0].getResult().get(Frequency.EVENT_RESULT_NAME),
    ).toBe(0);
});

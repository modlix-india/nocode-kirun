import { Frequency } from '../../../../../src/engine/function/system/array/Frequency';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let freq: Frequency = new Frequency();

test('freq test 1', () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'I'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 10],
        ]),
    );

    expect(freq.execute(fep).allResults()[0].getResult().get(Frequency.EVENT_INDEX.getName())).toBe(
        2,
    );
});

test('freq test 2', () => {
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

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'developement'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), -2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 20],
        ]),
    );

    expect(() => freq.execute(fep)).toThrow();

    fep.setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'developement'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 20],
        ]),
    );

    expect(() => freq.execute(fep)).toThrow();
});

test('freq test 3', () => {
    let array: any[] = [];
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [Frequency.PARAMETER_ARRAY_SOURCE.getParameterName(), array],
            [Frequency.PARAMETER_ANY.getParameterName(), 'I'],
            [Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName(), 2],
            [Frequency.PARAMETER_INT_LENGTH.getParameterName(), 10],
        ]),
    );

    expect(freq.execute(fep).allResults()[0].getResult().get(Frequency.EVENT_INDEX.getName())).toBe(
        0,
    );
});

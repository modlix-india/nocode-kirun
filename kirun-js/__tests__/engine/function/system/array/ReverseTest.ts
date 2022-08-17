import { Reverse } from '../../../../../src/engine/function/system/array/Reverse';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let rev: Reverse = new Reverse();

test('Reverse test 1 ', () => {
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
        .setOutput(new Map([]));

    let res = [5, 4, 6, 7];
    rev.execute(fep);
    expect(src).toStrictEqual(res);
});

test('Reverse test 2 ', () => {
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
        .setOutput(new Map([]));

    expect(() => rev.execute(fep)).toThrow;
});

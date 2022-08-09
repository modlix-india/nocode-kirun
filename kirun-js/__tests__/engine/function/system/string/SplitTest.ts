import { Split } from '../../../../../src/engine/function/system/string/Split';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const Spli: Split = new Split();

test('split test1', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            'string',
            'I am using eclipse to test the changes with test Driven developement',
            'searchString',
            ' ',
        ),
    );

    let array: string[] = [];
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

    expect(Spli.execute(fep).allResults()[0].getResult().get('output')).toStrictEqual(array);
});

test('split test2', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            'string',
            'I am using eclipse to test the changes with test Driven developement',
            'searchString',
            'e',
        ),
    );

    let array: string[] = [];
    array.push('I am using ');
    array.push('clips');
    array.push(' to t');
    array.push('st th');
    array.push(' chang');
    array.push('s with t');
    array.push('st Driv');
    array.push('n d');
    array.push('v');
    array.push('lop');
    array.push('m');
    array.push('nt');

    expect(Spli.execute(fep).allResults()[0].getResult().get('output')).toStrictEqual(array);
});

import { Concatenate } from '../../../../../src/engine/function/system/string/Concatenate';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const cat: Concatenate = new Concatenate();

test('conatenate test1', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    let array: string[] = [];
    array.push('I ');
    array.push('am ');
    array.push('using ');
    array.push('eclipse ');
    array.push('to ');
    array.push('test ');
    array.push('the ');
    array.push('changes ');
    array.push('with ');
    array.push('test ');
    array.push('Driven ');
    array.push('developement');

    fep.setArguments(MapUtil.of('value', array));

    expect(cat.execute(fep).allResults()[0].getResult().get('value')).toBe(
        'I am using eclipse to test the changes with test Driven developement',
    );
});

test('conatenate test2', () => {
    let list: string[] = [];
    list.push('no code ');
    list.push(' Kirun ');
    list.push(' true ');
    list.push('"\'this is between the strings qith special characters\'"');
    list.push(' PLATform ');
    list.push('2');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(MapUtil.of('value', list));
    expect(cat.execute(fep).allResults()[0].getResult().get('value')).toBe(
        'no code  Kirun  true "\'this is between the strings qith special characters\'" PLATform 2',
    );
});

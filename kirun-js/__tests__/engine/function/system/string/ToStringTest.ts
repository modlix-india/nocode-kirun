import { ToString } from '../../../../../src/engine/function/system/string/ToString';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const toString: ToString = new ToString();

test('toString test1', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(MapUtil.of('anytype', 123));

    expect(toString.execute(fep).allResults()[0].getResult().get('result')).toBe('123');
});

// test('toString test2', () => {
//     let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

//     let array: string[] = [];
//     array.push('I');
//     array.push('am');
//     array.push('using');
//     array.push('eclipse');
//     array.push('to');
//     array.push('test');
//     array.push('the');
//     array.push('changes');
//     array.push('with');
//     array.push('test');
//     array.push('Driven');
//     array.push('developement');

//     fep.setArguments(MapUtil.of('anytype', array));

//     expect(toString.execute(fep).allResults()[0].getResult().get('result')).toBe(
//         'I am using eclipse to test the changes with test Driven developement',
//     );
// });

test('toString test2 ', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(MapUtil.of('anytype', true));

    expect(toString.execute(fep).allResults()[0].getResult().get('result')).toBe('true');
});

import { Add } from '../../../../../src/engine/function/system/math/Add';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const add: Add = new Add();

test('add test 1', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map([['value', [1, 2, 3, 4, 5, 6]]]),
    );

    expect(add.execute(fep).allResults()[0].getResult().get('value')).toBe(21);
});

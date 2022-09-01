import { Add } from '../../../../../src/engine/function/system/math/Add';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const add: Add = new Add();

test('add test 1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', [1, 2, 3, 4, 5, 6, 5.5]]]));

    expect((await add.execute(fep)).allResults()[0].getResult().get('value')).toBe(26.5);
});

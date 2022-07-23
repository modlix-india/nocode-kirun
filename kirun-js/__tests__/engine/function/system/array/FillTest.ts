import { Fill } from '../../../../../src/engine/function/system/array/Fill';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

test('Fill Test', () => {
    let fill: Fill = new Fill();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    let array = [0, 1];

    fep.setArguments(MapUtil.of('source', array as any, 'element', 3));

    fill.execute(fep);
    expect(array).toStrictEqual([3, 3]);

    fep.setArguments(MapUtil.of('source', array as any, 'element', 5, 'srcFrom', 2, 'length', 5))
        .setContext(MapUtil.of())
        .setOutput(MapUtil.of());

    let finArray = [3, 3, 5, 5, 5, 5, 5];
    fill.execute(fep);

    expect(array).toStrictEqual(finArray);

    fep.setArguments(MapUtil.of('source', array as any, 'element', 25, 'srcFrom', 5));

    finArray = [3, 3, 5, 5, 5, 25, 25];

    fill.execute(fep);

    expect(array).toStrictEqual(finArray);

    fep.setArguments(MapUtil.of('source', array as any, 'element', 20, 'srcFrom', -1));

    expect(() => fill.execute(fep)).toThrow();
});

import { AbstractArrayFunction } from '../../../../../src/engine/function/system/array/AbstractArrayFunction';
import { Compare } from '../../../../../src/engine/function/system/array/Compare';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

test('Compare Test 1', async () => {
    let compare: Compare = new Compare();

    let source: any[] = [2, 2, 3, 4, 5];

    let find: any[] = [2, 2, 2, 3, 4, 5];

    expect(compare.compare(source, 0, 2, find, 1, 3)).toBe(0);

    find = [2, 2, 3, 4, 5];

    expect(compare.compare(source, 0, source.length, find, 0, find.length)).toBe(0);

    source = [true, true];

    find = [true, null];

    expect(compare.compare(source, 0, source.length, find, 0, find.length)).toBe(1);
});

test('Compare Test 2', async () => {
    let compare: Compare = new Compare();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    let source: any[] = [4, 5];

    let find: any[] = [4, 6];

    fep.setArguments(
        MapUtil.of(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName(),
            source,
            Compare.PARAMETER_ARRAY_FIND.getParameterName(),
            find,
        ),
    );

    let fo: FunctionOutput = await compare.execute(fep);

    expect(fo.allResults()[0].getResult().get(AbstractArrayFunction.EVENT_RESULT_NAME)).toBe(5 - 6);
});

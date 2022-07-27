import { Equals } from '../../../../../src/engine/function/system/array/Equals';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

test('Equals Test', () => {
    let equals: Equals = new Equals();

    let srcArray: any[] = [30, 31, 32, 33, 34];
    let findArray: any[] = [30, 31, 32, 33, 34];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            Equals.PARAMETER_ARRAY_SOURCE.getParameterName(),
            srcArray,
            Equals.PARAMETER_ARRAY_FIND.getParameterName(),
            findArray,
        ),
    );

    let fo: FunctionOutput = equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();

    findArray[1] = 41;

    fo = equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeFalsy;

    fep = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            Equals.PARAMETER_ARRAY_SOURCE.getParameterName(),
            srcArray as any,
            Equals.PARAMETER_ARRAY_FIND.getParameterName(),
            findArray,
            Equals.PARAMETER_INT_SOURCE_FROM.getParameterName(),
            2,
            Equals.PARAMETER_INT_FIND_FROM.getParameterName(),
            2,
        ),
    );

    fo = equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();

    srcArray = [true, true, false];

    findArray = [true, true, false];

    fep = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            Equals.PARAMETER_ARRAY_SOURCE.getParameterName(),
            srcArray,
            Equals.PARAMETER_ARRAY_FIND.getParameterName(),
            findArray,
        ),
    );

    fo = equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();
});

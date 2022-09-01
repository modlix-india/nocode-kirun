import { Equals } from '../../../../../src/engine/function/system/array/Equals';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

test('Equals Test', async () => {
    let equals: Equals = new Equals();

    let srcArray: any[] = [30, 31, 32, 33, 34];
    let findArray: any[] = [30, 31, 32, 33, 34];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(
        MapUtil.of(
            Equals.PARAMETER_ARRAY_SOURCE.getParameterName(),
            srcArray,
            Equals.PARAMETER_ARRAY_FIND.getParameterName(),
            findArray,
        ),
    );

    let fo: FunctionOutput = await equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();

    findArray[1] = 41;

    fo = await equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeFalsy;

    fep = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
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

    fo = await equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();

    srcArray = [true, true, false];

    findArray = [true, true, false];

    fep = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(
        MapUtil.of(
            Equals.PARAMETER_ARRAY_SOURCE.getParameterName(),
            srcArray,
            Equals.PARAMETER_ARRAY_FIND.getParameterName(),
            findArray,
        ),
    );

    fo = await equals.execute(fep);

    expect(fo.allResults()[0].getResult().get(Equals.EVENT_RESULT_NAME)).toBeTruthy();
});

import { AbstractArrayFunction } from '../../../../../src/engine/function/system/array/AbstractArrayFunction';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

import { MapUtil } from '../../../../../src/engine/util/MapUtil';

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { RemoveDuplicates } from '../../../../../src/engine/function/system/array/RemoveDuplicates';

test('RemoveDuplicates Test', async () => {
    let removeDuplicates: RemoveDuplicates = new RemoveDuplicates();

    let source: any[] = [2, 2, 2, 2, 2];

    let fep1: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        MapUtil.of(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName(),
            source as any,
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.getParameterName(),
            2,
            AbstractArrayFunction.PARAMETER_INT_LENGTH.getParameterName(),
            4,
        ),
    );

    await expect(removeDuplicates.execute(fep1)).rejects.toThrow();

    source.push(6);

    let result: any[] = [2, 2, 2, 6];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        MapUtil.of(
            RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(),
            source as any,
            RemoveDuplicates.PARAMETER_INT_SOURCE_FROM.getParameterName(),
            2,
            RemoveDuplicates.PARAMETER_INT_LENGTH.getParameterName(),
            4,
        ),
    );

    let fo: FunctionOutput = await removeDuplicates.execute(fep);
    expect(fo.allResults()[0].getResult().get(RemoveDuplicates.EVENT_RESULT_NAME)).toStrictEqual(
        result,
    );

    source = new Array();
    source.push({ name: 'Kiran' });
    source.push({ name: 'Kiran' });
    source.push({ name: 'Kiran' });
    source.push({ name: 'Kumar' });

    result = new Array();
    result.push({ name: 'Kiran' });
    result.push({ name: 'Kumar' });

    fep = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(MapUtil.of(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(), source));

    fo = await removeDuplicates.execute(fep);

    expect(fo.allResults()[0].getResult().get(RemoveDuplicates.EVENT_RESULT_NAME)).toMatchObject(
        result,
    );
});

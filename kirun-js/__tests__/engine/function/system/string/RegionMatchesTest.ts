import { RegionMatches } from '../../../../../src/engine/function/system/string/RegionMatches';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
const region: RegionMatches = new RegionMatches();

test('toString test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number | boolean>(
            RegionMatches.PARAMETER_BOOLEAN_NAME,
            true,
            RegionMatches.PARAMETER_STRING_NAME,
            ' THIScompatY IS A NOcoDE plATFNORM',
            RegionMatches.PARAMETER_FIRST_OFFSET_NAME,
            5,
            RegionMatches.PARAMETER_OTHER_STRING_NAME,
            ' fincitY compatY ',
            RegionMatches.PARAMETER_SECOND_OFFSET_NAME,
            9,
            RegionMatches.PARAMETER_INTEGER_NAME,
            7,
        ),
    );

    expect(
        (await region.execute(fep))
            .allResults()[0]
            .getResult()
            .get(RegionMatches.EVENT_RESULT_NAME),
    ).toBe(true);
});

test('toString test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(
        MapUtil.of<string, string | number | boolean>(
            RegionMatches.PARAMETER_BOOLEAN_NAME,
            false,
            RegionMatches.PARAMETER_STRING_NAME,
            ' THIScompatY IS A NOcoDE plATFNORM',
            RegionMatches.PARAMETER_FIRST_OFFSET_NAME,
            5,
            RegionMatches.PARAMETER_OTHER_STRING_NAME,
            ' fincitY compatY ',
            RegionMatches.PARAMETER_SECOND_OFFSET_NAME,
            1,
            RegionMatches.PARAMETER_INTEGER_NAME,
            7,
        ),
    );

    expect(
        (await region.execute(fep))
            .allResults()[0]
            .getResult()
            .get(RegionMatches.EVENT_RESULT_NAME),
    ).toBe(false);
});

test('toString test3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(
        MapUtil.of<string, string | number | boolean>(
            RegionMatches.PARAMETER_BOOLEAN_NAME,
            true,
            RegionMatches.PARAMETER_STRING_NAME,
            ' THIScompatY IS A NOcoDE plATFNORM',
            RegionMatches.PARAMETER_FIRST_OFFSET_NAME,
            10,
            RegionMatches.PARAMETER_OTHER_STRING_NAME,
            ' fincitY compatY ',
            RegionMatches.PARAMETER_SECOND_OFFSET_NAME,
            6,
            RegionMatches.PARAMETER_INTEGER_NAME,
            3,
        ),
    );

    expect(
        (await region.execute(fep))
            .allResults()[0]
            .getResult()
            .get(RegionMatches.EVENT_RESULT_NAME),
    ).toBe(true);
});

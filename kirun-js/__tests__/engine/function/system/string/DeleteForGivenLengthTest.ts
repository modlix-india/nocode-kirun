import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { DeleteForGivenLength } from '../../../../../src/engine/function/system/string/DeleteForGivenLength';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const deleteT: DeleteForGivenLength = new DeleteForGivenLength();

test('DeleteForGivenLength test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [DeleteForGivenLength.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [DeleteForGivenLength.PARAMETER_AT_START_NAME, 10],
            [DeleteForGivenLength.PARAMETER_AT_END_NAME, 18],
        ]),
    );

    let outputString: string = ' THIScompaNOcoDE plATFNORM';

    expect((await deleteT.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        outputString,
    );
});

test('DeleteForGivenLength test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [DeleteForGivenLength.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [DeleteForGivenLength.PARAMETER_AT_START_NAME, 4],
            [DeleteForGivenLength.PARAMETER_AT_END_NAME, 10],
        ]),
    );

    let outputString: string = ' THItY IS A NOcoDE plATFNORM';

    expect((await deleteT.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        outputString,
    );
});

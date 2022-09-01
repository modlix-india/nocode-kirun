import { PrePad } from '../../../../../src/engine/function/system/string/PrePad';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapEntry, MapUtil } from '../../../../../src/engine/util/MapUtil';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const prepad: PrePad = new PrePad();

test('prepad test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [PrePad.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [PrePad.PARAMETER_PREPAD_STRING_NAME, 'hiran'],
            [PrePad.PARAMETER_LENGTH_NAME, 12],
        ]),
    );

    let padded: string = 'hiranhiranhi THIScompatY IS A NOcoDE plATFNORM';

    expect(
        (await prepad.execute(fep)).allResults()[0].getResult().get(PrePad.EVENT_RESULT_NAME),
    ).toBe(padded);
});

test('prepad test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [PrePad.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [PrePad.PARAMETER_PREPAD_STRING_NAME, ' h '],
            [PrePad.PARAMETER_LENGTH_NAME, 11],
        ]),
    );

    let padded: string = ' h  h  h  h THIScompatY IS A NOcoDE plATFNORM';

    expect((await prepad.execute(fep)).allResults()[0].getResult().get('result')).toBe(padded);
});

test('prepad test3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [PrePad.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [PrePad.PARAMETER_PREPAD_STRING_NAME, 'hiran'],
            [PrePad.PARAMETER_LENGTH_NAME, 4],
        ]),
    );

    let reveresed: string = 'hira THIScompatY IS A NOcoDE plATFNORM';
    expect((await prepad.execute(fep)).allResults()[0].getResult().get('result')).toBe(reveresed);
});

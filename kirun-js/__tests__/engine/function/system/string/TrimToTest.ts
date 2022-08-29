import { TrimTo } from '../../../../../src/engine/function/system/string/TrimTo';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const trim: TrimTo = new TrimTo();

test('Trim to test1 ', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, string | number>([
            [TrimTo.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [TrimTo.PARAMETER_LENGTH_NAME, 14],
        ]),
    );

    expect(
        (await trim.execute(fep)).allResults()[0].getResult().get(TrimTo.EVENT_RESULT_NAME),
    ).toBe(' THIScompatY I');
});

test('Trim to test2 ', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, string | number>([
            [TrimTo.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [TrimTo.PARAMETER_LENGTH_NAME, 0],
        ]),
    );

    expect(
        (await trim.execute(fep)).allResults()[0].getResult().get(TrimTo.EVENT_RESULT_NAME),
    ).toBe('');
});

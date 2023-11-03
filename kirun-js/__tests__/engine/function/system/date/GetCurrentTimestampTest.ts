import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetCurrentTimestamp } from '../../../../../src/engine/function/system/date/GetCurrentTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getCurrentTimeStamp: GetCurrentTimestamp = new GetCurrentTimestamp();

describe('testing GetCurrentTimestampTest', () => {
    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        expect(
            (await getCurrentTimeStamp.execute(fep)).allResults()[0].getResult().get('timeStamp'),
        ).toBeTruthy();
    });
});

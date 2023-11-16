import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetLocalTimestamp } from '../../../../../src/engine/function/system/date/GetLocalTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getLocalTimestamp: GetLocalTimestamp = new GetLocalTimestamp();

describe('testing GetCurrentTimestampTest', () => {
    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        fep.setArguments(new Map([['isodate', '1994-10-24T02:10:30.000Z']]));

        expect(
            (await getLocalTimestamp.execute(fep)).allResults()[0].getResult().get('timestamp'),
        ).toBe('1994-10-24T07:40:30+05:30');
    });
});

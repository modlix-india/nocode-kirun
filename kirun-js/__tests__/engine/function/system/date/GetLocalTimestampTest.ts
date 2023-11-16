import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetLocalTimestamp } from '../../../../../src/engine/function/system/date/GetLocalTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getLocalTimestamp: GetLocalTimestamp = new GetLocalTimestamp();

describe('testing GetCurrentTimestampTest', () => {
    test('Test 1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        fep.setArguments(new Map([['isodate', '1994-10-24T02:10:30.000Z']]));

        expect(
            (await getLocalTimestamp.execute(fep)).allResults()[0].getResult().get('timestamp'),
        ).toBe('1994-10-24T07:40:30.000+05:30');
    });
    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        fep.setArguments(new Map([['isodate', '2023-10-24T09:10:30.000+09:00']]));

        expect(
            (await getLocalTimestamp.execute(fep)).allResults()[0].getResult().get('timestamp'),
        ).toBe('2023-10-24T05:40:30.000+05:30');
    });
    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        fep.setArguments(new Map([['isodate', '2000-10-24T09:10:30.010-09:00']]));

        expect(
            (await getLocalTimestamp.execute(fep)).allResults()[0].getResult().get('timestamp'),
        ).toBe('2000-10-24T23:40:30.010+05:30');
    });
    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );

        fep.setArguments(new Map([['isodate', '2016-05-03T22:15:01.678+02:00']]));

        expect(
            (await getLocalTimestamp.execute(fep)).allResults()[0].getResult().get('timestamp'),
        ).toBe('2016-05-04T01:45:01.678+05:30');
    });
});

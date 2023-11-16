import {
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    Namespaces,
} from '../../../../../src';
import { GetTimeZone } from '../../../../../src/engine/function/system/date/GetTimeZone';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getTimeZone: GetTimeZone = new GetTimeZone();

describe('testing GetTimeZone', () => {
    test('Time as Object', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000Z']]));

        expect((await getTimeZone.execute(fep)).allResults()[0].getResult().get('zoneId')).toBe(
            'UTC',
        );
    });
    test('Time as Object', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000+05:30']]));

        expect((await getTimeZone.execute(fep)).allResults()[0].getResult().get('zoneId')).toBe(
            'GMT+05:30',
        );
    });
});

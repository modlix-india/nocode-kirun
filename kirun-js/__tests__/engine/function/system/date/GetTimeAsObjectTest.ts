import {
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    Namespaces,
} from '../../../../../src';
import { GetTimeAsObject } from '../../../../../src/engine/function/system/date/GetTimeAsObject';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getTimeAsObject: GetTimeAsObject = new GetTimeAsObject();

describe('testing GetTimeAsObject', () => {
    test('Time as Object', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000+05:30']]));

        expect((await getTimeAsObject.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '2026-10-10T00:35:00.000Z',
        );
    });
});

import {
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    Namespaces,
} from '../../../../../src';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { GetTimeAsArray } from '../../../../../src/engine/function/system/date/GetTimeAsArray';

const getTimeAsArray: GetTimeAsArray = new GetTimeAsArray();

describe('testing GetTimeAsArray', () => {
    test('Time as Array', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000Z']]));

        expect((await getTimeAsArray.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '2026-10-10T00:35:00.000Z',
        );
    });
});

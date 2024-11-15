import { KIRunSchemaRepository } from '../../../../../src';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { TimestampToEpoch } from '../../../../../src/engine/function/system/date/TimestampToEpoch';

import { Settings } from 'luxon';

Settings.defaultZone = 'Asia/Kolkata';

describe('TimestampToEpoch', () => {
    test('should return the epoch time in seconds', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000+05:30'],
            ]),
        );

        const result = await (await new TimestampToEpoch('TimestampToEpoch', true).execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(1704047400);
    });

    test('should return the epoch time in milliseconds', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000+05:30'],
            ]),
        );

        const result = await (await new TimestampToEpoch('TimestampToEpoch', false).execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(1704047400000);
    });
});

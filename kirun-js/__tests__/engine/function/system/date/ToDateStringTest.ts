import { FunctionExecutionParameters } from '../../../../../src';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src';
import { ToDateString } from '../../../../../src/engine/function/system/date/ToDateString';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';

import { Settings } from 'luxon';

Settings.defaultZone = 'Asia/Kolkata';

describe('ToDateString', () => {
    test('should return the date string in the given format', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [ToDateString.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000+05:30'],
                [ToDateString.PARAMETER_FORMAT_NAME, 'yyyy-MM-dd'],
            ]),
        );

        const result = await (await new ToDateString().execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe('2024-01-01');
    });

    test('should return the date string in the given format with locale', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [ToDateString.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000+05:30'],
                [ToDateString.PARAMETER_FORMAT_NAME, 'DDD'],
                [ToDateString.PARAMETER_LOCALE_NAME, 'fr-CA'],
            ]),
        );

        const result = await (await new ToDateString().execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe('1 janvier 2024');
    });

    test('should return the date string in the given format and no locale', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [ToDateString.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000+05:30'],
                [ToDateString.PARAMETER_FORMAT_NAME, 'DDD'],
            ]),
        );

        const result = await (await new ToDateString().execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result == '1 January 2024' || result == 'January 1, 2024').toBeTruthy();
    });
});

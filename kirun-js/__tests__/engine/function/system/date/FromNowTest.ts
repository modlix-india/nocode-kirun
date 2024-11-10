import { Settings } from 'luxon';
import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { FromNow } from '../../../../../src/engine/function/system/date/FromNow';

Settings.defaultZone = 'Asia/Kolkata';

describe('From Now', () => {
    test('should return the relative date', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01'],
                [FromNow.PARAMETER_FROM_NAME, '2024-04-25'],
            ]),
        );

        const result = await new FromNow().execute(fep);

        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            'in 8 months',
        );
    });

    test('should return the relative date with units', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01'],
                [FromNow.PARAMETER_FROM_NAME, '2023-04-25'],
                [FromNow.PARAMETER_UNIT_NAME, ['MONTHS', 'DAYS']],
                [FromNow.PARAMETER_FORMAT_NAME, 'SHORT'],
            ]),
        );

        const result = await new FromNow().execute(fep);

        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            'in 20 mo',
        );
    });

    test('should return the relative date with locale', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01'],
                [FromNow.PARAMETER_FROM_NAME, '2023-04-25'],
                [FromNow.PARAMETER_UNIT_NAME, ['DAYS']],
                [FromNow.PARAMETER_FORMAT_NAME, 'LONG'],
                [FromNow.PARAMETER_LOCALE_NAME, 'fr'],
            ]),
        );

        const result = await new FromNow().execute(fep);

        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            'dans 617 jours',
        );
    });
});

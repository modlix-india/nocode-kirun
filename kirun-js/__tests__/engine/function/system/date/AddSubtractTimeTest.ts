import { DateTime, Settings } from 'luxon';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { AddSubtractTime } from '../../../../../src/engine/function/system/date/AddSubtractTime';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const addFunction = new AddSubtractTime(true);
const subtractFunction = new AddSubtractTime(false);

Settings.defaultZone = 'Asia/Kolkata';

describe('AddSubtractTime', () => {
    test('should add time with no values', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01T10:20:35+05:30'],
            ]),
        );

        const result = await addFunction.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2025-01-01T10:20:35.000+05:30');
    });

    test('shoudl subtract time with no values', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01T10:20:35+05:30'],
            ]),
        );

        const result = await subtractFunction.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2025-01-01T10:20:35.000+05:30');
    });

    test('should add time with all values', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01T10:20:35+05:30'],
                [AddSubtractTime.PARAMETER_YEARS_NAME, 1],
                [AddSubtractTime.PARAMETER_MONTHS_NAME, 1],
                [AddSubtractTime.PARAMETER_DAYS_NAME, 1],
                [AddSubtractTime.PARAMETER_HOURS_NAME, 1],
                [AddSubtractTime.PARAMETER_MINUTES_NAME, 1],
                [AddSubtractTime.PARAMETER_SECONDS_NAME, 1],
            ]),
        );

        const result = await addFunction.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2026-02-02T11:21:36.000+05:30');
    });

    test('should subtract time with all values', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2025-01-01T10:20:35+05:30'],
                [AddSubtractTime.PARAMETER_YEARS_NAME, 1],
                [AddSubtractTime.PARAMETER_MONTHS_NAME, 1],
                [AddSubtractTime.PARAMETER_DAYS_NAME, 1],
                [AddSubtractTime.PARAMETER_HOURS_NAME, 1],
                [AddSubtractTime.PARAMETER_MINUTES_NAME, 1],
                [AddSubtractTime.PARAMETER_SECONDS_NAME, 1],
                [AddSubtractTime.PARAMETER_MILLISECONDS_NAME, 8],
            ]),
        );

        const result = await subtractFunction.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2023-11-30T09:19:33.992+05:30');
    });
});

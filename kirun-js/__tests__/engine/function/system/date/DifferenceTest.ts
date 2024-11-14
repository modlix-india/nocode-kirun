import { Settings } from 'luxon';
import { KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { FunctionExecutionParameters } from '../../../../../src';
import { KIRunFunctionRepository } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';

Settings.defaultZone = 'Asia/Kolkata';

describe('Difference of dates', () => {
    test('should return the difference in default', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, '2025-01-01'],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, '2024-04-25'],
            ]),
        );
        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'Difference',
        ))!.execute(fep);
        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME),
        ).toMatchObject({ milliseconds: 21686400000 });
    });

    test('should return the difference in days', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, '2025-01-01T10:20:30+05:30'],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, '2024-04-25T10:20:30-05:00'],
                [AbstractDateFunction.PARAMETER_UNIT_NAME, ['DAYS']],
            ]),
        );
        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'Difference',
        ))!.execute(fep);
        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME),
        ).toMatchObject({ days: 250.5625 });
    });

    test('should return the difference in months,days,hours,minutes', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, '2025-01-01T10:20:30+05:30'],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, '2024-04-25T10:20:30-05:00'],
                [AbstractDateFunction.PARAMETER_UNIT_NAME, ['MONTHS', 'DAYS', 'HOURS', 'MINUTES']],
            ]),
        );

        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'Difference',
        ))!.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME),
        ).toMatchObject({ months: 8, days: 6, hours: 13, minutes: 30 });
    });

    test('should return the difference in negative months,days,hours,minutes', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, '2024-01-01T10:20:30+05:30'],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, '2025-04-25T10:20:30-05:00'],
                [AbstractDateFunction.PARAMETER_UNIT_NAME, ['MONTHS', 'DAYS', 'HOURS', 'MINUTES']],
            ]),
        );

        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'Difference',
        ))!.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME),
        ).toMatchObject({ months: -15, days: -24, hours: -10, minutes: -30 });
    });

    test('should return the difference in fractions', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, '2024-01-01T10:20:30+05:30'],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, '2025-04-25T10:20:30-05:00'],
                [AbstractDateFunction.PARAMETER_UNIT_NAME, ['DAYS']],
            ]),
        );

        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'Difference',
        ))!.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME),
        ).toMatchObject({ days: -480.4375 });
    });
});

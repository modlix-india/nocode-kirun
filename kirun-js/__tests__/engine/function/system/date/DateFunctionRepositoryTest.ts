import { Settings } from 'luxon';
import { Namespaces } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const repo = new DateFunctionRepository();

Settings.defaultZone = 'Asia/Kolkata';

describe('DateFunctionRepository', () => {
    test('should return Date by using GetDate', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01']]));

        const result = (await (await repo.find(Namespaces.DATE, 'GetDay'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(1);
    });

    test('should return the days in month using GetDaysInMonth', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01']]));

        const result = (await (await repo.find(Namespaces.DATE, 'GetDaysInMonth'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(31);
    });

    test('should return the days in year using GetDaysInYear', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01']]));

        const result = (await (await repo.find(Namespaces.DATE, 'GetDaysInYear'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(366);
    });

    test('should return the day of week using GetDayOfWeek', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01']]));

        const result = (await (await repo.find(Namespaces.DATE, 'GetDayOfWeek'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(1);
    });

    test('should return the days in a non leap year using GetDaysInYear', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2023-01-01']]));

        const result = (await (await repo.find(Namespaces.DATE, 'GetDaysInYear'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe(365);
    });

    test('should return the date with the day set using SetDay', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01'],
                [AbstractDateFunction.PARAMETER_NUMBER_NAME, 2],
            ]),
        );

        const result = (await (await repo.find(Namespaces.DATE, 'SetDay'))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
        expect(result).toBe('2024-01-02T00:00:00.000+05:30');
    });
});

describe('DateFunctionRepository comparisons', () => {
    async function compare(name: string, t1: string, t2: string): Promise<boolean> {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, t1],
                [AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, t2],
            ]),
        );
        return (await (await repo.find(Namespaces.DATE, name))!.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractDateFunction.EVENT_RESULT_NAME);
    }

    test('IsSame is true for identical timestamps', async () => {
        expect(
            await compare('IsSame', '2024-01-01T10:00:00.000Z', '2024-01-01T10:00:00.000Z'),
        ).toBe(true);
    });

    test('IsSame is true for the same instant in different offsets', async () => {
        expect(
            await compare('IsSame', '2024-01-01T10:00:00.000Z', '2024-01-01T15:30:00.000+05:30'),
        ).toBe(true);
    });

    test('IsSame is false for different instants', async () => {
        expect(
            await compare('IsSame', '2024-01-01T10:00:00.000Z', '2024-01-01T10:00:00.001Z'),
        ).toBe(false);
        expect(await compare('IsSame', '2024-01-01', '2024-01-02')).toBe(false);
    });

    test('IsBefore, IsAfter, IsSameOrBefore, IsSameOrAfter agree with IsSame', async () => {
        const a = '2024-01-01T10:00:00.000Z';
        const sameAsA = '2024-01-01T15:30:00.000+05:30';
        const b = '2024-01-01T11:00:00.000Z';

        expect(await compare('IsBefore', a, b)).toBe(true);
        expect(await compare('IsBefore', a, sameAsA)).toBe(false);
        expect(await compare('IsAfter', b, a)).toBe(true);
        expect(await compare('IsAfter', a, sameAsA)).toBe(false);
        expect(await compare('IsSameOrBefore', a, sameAsA)).toBe(true);
        expect(await compare('IsSameOrBefore', b, a)).toBe(false);
        expect(await compare('IsSameOrAfter', a, sameAsA)).toBe(true);
        expect(await compare('IsSameOrAfter', a, b)).toBe(false);
    });
});

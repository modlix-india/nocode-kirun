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

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { SubtractTime } from '../../../../../src/engine/function/system/date/SubtractTime';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const subtractTime: SubtractTime = new SubtractTime();

describe('testing SubtractTime', () => {
    test('Subtract Years Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 2],
                ['unit', 'YEARS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2022-10-10T00:35:00.000Z',
        );
    });

    test('Subtract Months Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 11],
                ['unit', 'MONTHS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2023-11-10T00:35:00.000Z',
        );
    });

    test('Subtract Days Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 11],
                ['unit', 'DAYS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-09-29T00:35:00.000Z',
        );
    });

    test('Subtract Hours Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 27],
                ['unit', 'HOURS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-08T21:35:00.000Z',
        );
    });

    test('Subtract Minutes Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 27],
                ['unit', 'MINUTES'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T00:08:00.000Z',
        );
    });

    test('Subtract Seconds Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 420],
                ['unit', 'SECONDS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T00:28:00.000Z',
        );
    });

    test('Subtract Millis Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['subtract', 2040],
                ['unit', 'MILLIS'],
            ]),
        );

        expect((await subtractTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T00:34:57.960Z',
        );
    });
});

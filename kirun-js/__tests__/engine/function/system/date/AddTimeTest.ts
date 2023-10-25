import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { AddTime } from '../../../../../src/engine/function/system/date/AddTime';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const addTime: AddTime = new AddTime();

describe('testing AddTime', () => {
    test('Add Years Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 2],
                ['unit', 'YEARS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2026-10-10T00:35:00.000Z',
        );
    });

    test('Add Months Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 11],
                ['unit', 'MONTHS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2025-09-10T00:35:00.000Z',
        );
    });

    test('Add Days Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 11],
                ['unit', 'DAYS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-21T00:35:00.000Z',
        );
    });

    test('Add Hours Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 27],
                ['unit', 'HOURS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-11T03:35:00.000Z',
        );
    });

    test('Add Minutes Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 27],
                ['unit', 'MINUTES'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T01:02:00.000Z',
        );
    });

    test('Add Seconds Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 420],
                ['unit', 'SECONDS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T00:42:00.000Z',
        );
    });

    test('Add Millis Test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-10-10T00:35:00.000Z'],
                ['add', 2040],
                ['unit', 'MILLIS'],
            ]),
        );

        expect((await addTime.execute(fep)).allResults()[0].getResult().get('dateTime')).toBe(
            '2024-10-10T00:35:02.040Z',
        );
    });
});

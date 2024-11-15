import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { Settings } from 'luxon';

Settings.defaultZone = 'Asia/Kolkata';

describe('testing EpochToDateFunction', () => {
    test('Epoch To Date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', 1694072117]]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('2023-09-07T07:35:17.000Z');
    });

    test('Epoch To Date 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochMilliseconds', 1694072117000]]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochMillisecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('2023-09-07T07:35:17.000Z');
    });

    test('Epoch To Date 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', 169407211700]]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('7338-04-20T14:48:20.000Z');
    });
    test('epoch string to date 1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', '1696489387']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('2023-10-05T07:03:07.000Z');
    });

    test('epoch string to date 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', '1696489387']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('2023-10-05T07:03:07.000Z');
    });

    test('epoch string to date 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochMilliseconds', '1696489386']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochMillisecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('1970-01-20T15:14:49.386Z');
    });

    test('epoch string to date 4 all chars', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', 'abcdef']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(async () =>
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).rejects.toThrow('Please provide a valid value for epochSeconds.');
    });

    test('epoch test for large string function', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', '1696494131']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('2023-10-05T08:22:11.000Z');
    });

    test('epoch test for small string function', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epochSeconds', '169640']]));
        const epochToDate = await new DateFunctionRepository().find(
            Namespaces.DATE,
            'EpochSecondsToTimestamp',
        );
        expect(
            (await epochToDate!.execute(fep)).allResults()[0].getResult().get('isoTimeStamp'),
        ).toBe('1970-01-02T23:07:20.000Z');
    });
});

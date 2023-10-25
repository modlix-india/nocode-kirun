import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getTimeOffset: GetTimeZoneOffset = new GetTimeZoneOffset();

describe('testing GetTimeTest', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getTimeOffset.execute(fep))?.allResults()[0]?.getResult()?.get('timeZoneOffset'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getTimeOffset.execute(fep))?.allResults()[0]?.getResult()?.get('timeZoneOffset'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect(
            (await getTimeOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
        ).toBe(0);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '7765-04-20T14:48:20.000+05:30']]));

        expect(
            (await getTimeOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
        ).toBe(-330);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T23:10:30.700-02:30']]));

        expect(
            (await getTimeOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
        ).toBe(150);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1994-10-24T02:10:30.700+00:00']]));

        expect(
            (await getTimeOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
        ).toBe(0);
    });
});

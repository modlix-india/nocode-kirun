import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetMilliSeconds } from '../../../../../src/engine/function/system/date/GetMilliSeconds';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getMilliSeconds: GetMilliSeconds = new GetMilliSeconds();

describe('testing GetFullYearTest', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getMilliSeconds.execute(fep))?.allResults()[0]?.getResult()?.get('milliSeconds'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getMilliSeconds.execute(fep))?.allResults()[0]?.getResult()?.get('milliSeconds'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect(
            (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
        ).toBe(939);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

        expect(
            (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
        ).toBe(0);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1383-10-04T14:10:50.700+00:00']]));

        expect(
            (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
        ).toBe(700);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1994-10-24T14:05:30.406+00:00']]));

        expect(
            (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
        ).toBe(406);
    });
});

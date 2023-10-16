import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetSeconds } from '../../../../../src/engine/function/system/date/GetSeconds';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getSeconds: GetSeconds = new GetSeconds();

describe('testing GetSecondsTest', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getSeconds.execute(fep))?.allResults()[0]?.getResult()?.get('seconds'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getSeconds.execute(fep))?.allResults()[0]?.getResult()?.get('seconds'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(38);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

        expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(20);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1383-10-04T14:10:50.700+00:00']]));

        expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(50);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1994-10-24T14:05:30.700+00:00']]));

        expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(30);
    });
});

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetDate } from '../../../../../src/engine/function/system/date/GetDate';
import { GetDay } from '../../../../../src/engine/function/system/date/GetDay';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getDay: GetDay = new GetDay();

describe('testing GetDateFunction', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getDay.execute(fep))?.allResults()[0]?.getResult()?.get('day'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getDay.execute(fep))?.allResults()[0]?.getResult()?.get('day'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(3);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-04-20T14:48:20.000Z']]));

        expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(4);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T14:10:30.700+00:00']]));

        expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(3);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-24T14:10:30.700+00:00']]));

        expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(2);
    });

    test('Test 6', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2053-10-04T14:10:50.70000+00:00']]));

        expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(6);
    });
});

import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetDate } from '../../../../../src/engine/function/system/date/GetDate';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getDate: GetDate = new GetDate();

describe('testing GetDateFunction', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getDate.execute(fep))?.allResults()[0]?.getResult()?.get('value'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getDate.execute(fep))?.allResults()[0]?.getResult()?.get('value'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(4);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-04-20T14:48:20.000Z']]));

        expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(20);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T14:10:30.700+00:00']]));

        expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(4);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-24T14:10:30.700+00:00']]));

        expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(24);
    });

    test('Test 6', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-02-31T07:35:17.000Z']]));

        expect(
            (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
        ).rejects.toThrowError('Please provide a valid value for epoch');
    });

    test('Test 7', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-02-35T07:35:17.000Z']]));

        expect(
            (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });
});

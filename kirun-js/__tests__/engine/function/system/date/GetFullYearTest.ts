import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetFullYear } from '../../../../../src/engine/function/system/date/GetFullYear';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getFullYear: GetFullYear = new GetFullYear();

describe('testing GetFullYearTest', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getFullYear.execute(fep))?.allResults()[0]?.getResult()?.get('year'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getFullYear.execute(fep))?.allResults()[0]?.getResult()?.get('year'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await getFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(2023);
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

        expect((await getFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(7765);
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1383-10-04T14:10:30.700+00:00']]));

        expect((await getFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(1383);
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1994-10-24T14:10:30.700+00:00']]));

        expect((await getFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(1994);
    });
});

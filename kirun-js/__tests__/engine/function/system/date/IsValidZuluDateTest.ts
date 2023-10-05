import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { IsValidISODate } from '../../../../../src/engine/function/system/date/IsValidISODate';

const isValidISODate: IsValidISODate = new IsValidISODate();

describe('testing IsValidZuluDateFunction', () => {
    test('Test 1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect((await isValidISODate.execute(fep)).allResults()[0].getResult().get('output')).toBe(
            false,
        );
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await isValidISODate.execute(fep)).allResults()[0].getResult().get('output')).toBe(
            true,
        );
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-4-20T14:48:20.000Z']]));

        expect((await isValidISODate.execute(fep)).allResults()[0].getResult().get('output')).toBe(
            false,
        );
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T14:10:30.700+00:00']]));

        expect((await isValidISODate.execute(fep)).allResults()[0].getResult().get('output')).toBe(
            true,
        );
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T14:10:30.700+00:00']]));

        expect((await isValidISODate.execute(fep)).allResults()[0].getResult().get('output')).toBe(
            false,
        );
    });
});

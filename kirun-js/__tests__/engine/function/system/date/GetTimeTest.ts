import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetTime } from '../../../../../src/engine/function/system/date/GetTime';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getTime: GetTime = new GetTime();

describe('testing GetTimeTest', () => {
    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', 'abc']]));

        expect(async () =>
            (await getTime.execute(fep))?.allResults()[0]?.getResult()?.get('time'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('should throw an error', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

        expect(async () =>
            (await getTime.execute(fep))?.allResults()[0]?.getResult()?.get('time'),
        ).rejects.toThrowError('Invalid ISO 8601 Date format.');
    });

    test('Test 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

        expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
            1696419938939,
        );
    });

    test('Test 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

        expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
            182882069300000,
        );
    });

    test('Test 4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1383-10-04T23:10:30.700+00:00']]));

        expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
            -18499970969300,
        );
    });

    test('Test 5', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isodate', '1994-10-24T02:10:30.700+00:00']]));

        expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
            782964630700,
        );
    });
});

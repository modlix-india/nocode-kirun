import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetEndOfTimestamp } from '../../../../../src/engine/function/system/date/GetEndOfTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getEndOfTimestamp: GetEndOfTimestamp = new GetEndOfTimestamp();

describe('testing GetEndOfTimestamp', () => {
    test('test1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2024-02-27T15:13:51.123Z'],
                ['unit', 'week'],
            ]),
        );

        expect(
            (await getEndOfTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
        ).toBe('UTC');
    });
    // test('test2', async () => {
    //     let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    //         new KIRunFunctionRepository(),
    //         new KIRunSchemaRepository(),
    //     ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000+05:30']]));

    //     expect(
    //         (await getEndOfTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
    //     ).toBe('GMT+05:30');
    // });
});

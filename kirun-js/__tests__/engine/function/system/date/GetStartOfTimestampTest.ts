import {
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    Namespaces,
} from '../../../../../src';
import { GetStartOfTimestamp } from '../../../../../src/engine/function/system/date/GetStartOfTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getStartOfTimestamp: GetStartOfTimestamp = new GetStartOfTimestamp();

describe('testing GetStartOfTimestamp', () => {
    test('test1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2023-11-14T02:35:56.125+06:31'],
                ['unit', 'week'],
            ]),
        );

        expect(
            (await getStartOfTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
        ).toBe('UTC');
    });
    // test('test2', async () => {
    //     let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    //         new KIRunFunctionRepository(),
    //         new KIRunSchemaRepository(),
    //     ).setArguments(new Map<string, any>([['isodate', '2024-10-10T00:35:00.000+05:30']]));

    //     expect(
    //         (await getStartOfTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
    //     ).toBe('GMT+05:30');
    // });
});

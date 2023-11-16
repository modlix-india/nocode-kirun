import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { MinimumTimestamp } from '../../../../../src/engine/function/system/date/MinimumTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const minimumTimestamp: MinimumTimestamp = new MinimumTimestamp();

describe('testing MinimumTimestamp', () => {
    test('test1', async () => {
        let arr = [
            '2023-10-25T13:30:04.970Z',
            '-202023-10-29T15:13:51.122Z',
            '2023-10-29T15:13:51.123-05:30',
            '-202023-10-29T15:13:51.123+06:57',
        ];
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodates', arr]]));

        expect(
            (await minimumTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
        ).toBe('UTC');
    });
});

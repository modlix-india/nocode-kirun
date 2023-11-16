import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { MaximumTimestamp } from '../../../../../src/engine/function/system/date/MaximumTimestamp';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const maximumTimestamp: MaximumTimestamp = new MaximumTimestamp();

describe('testing MaximumTimestamp', () => {
    test('test1', async () => {
        let arr = [
            '2023-10-25T13:30:04.970Z',
            '-202023-10-29T15:13:51.123Z',
            '2023-10-29T15:13:51.123-05:30',
            '-202023-10-29T15:13:51.123+06:57',
        ];
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map<string, any>([['isodates', arr]]));

        expect(
            (await maximumTimestamp.execute(fep)).allResults()[0].getResult().get('result'),
        ).toBe('UTC');
    });
});

import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { FromNow } from '../../../../../src/engine/function/system/date/FromNow';

const fromNow: FromNow = new FromNow();

describe('testing FromNow', () => {
    test('test', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodate', '2023-10-25T19:30:04.970+01:30'],
            ]),
        );

        expect((await fromNow.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '5 days ago',
        );

    });
});

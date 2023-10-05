import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { EpochToDate } from '../../../../../src/engine/function/system/date/EpochToDate';

const epochToDate: EpochToDate = new EpochToDate();

describe('testing EpochToDateFunction', () => {
    test('Epoch To Date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', 1694072117]]));

        expect((await epochToDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
            '2023-09-07T07:35:17.000Z',
        );
    });

    test('Epoch To Date 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', 1694072117000]]));

        expect((await epochToDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
            '2023-09-07T07:35:17.000Z',
        );
    });

    test('Epoch To Date 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', 169407211700]]));

        expect((await epochToDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
            '7338-04-20T14:48:20.000Z',
        );
    });
});

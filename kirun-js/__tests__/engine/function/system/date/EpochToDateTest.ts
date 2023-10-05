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
    test('epoch string to date 1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', '1696489387']]));

        expect((await epochToDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
            '2023-10-05T07:03:07.000Z',
        );
    });

    test('epoch string to date 2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', 'a1696489387']]));

        expect(async () =>
            (await epochToDate.execute(fep)).allResults()[0].getResult().get('date'),
        ).rejects.toThrowError('Please provide a valid value for epoch.');
    });

    test('epoch string to date 3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', '169648938as7']]));

        expect((await epochToDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
            '1975-05-18T12:42:18.000Z',
        );
    });

    test('epoch string to date 4 all chars', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['epoch', 'abcdef']]));

        expect(async () =>
            (await epochToDate.execute(fep)).allResults()[0].getResult().get('date'),
        ).rejects.toThrow('Please provide a valid value for epoch.');
    });
});

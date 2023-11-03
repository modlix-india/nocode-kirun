import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetDateFunction', async () => {
    let getMonth = await dateFunctionRepo.find(Namespaces.DATE, 'getMonth');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getMonth) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect((await getMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(10);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

    expect((await getMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(4);

    fep.setArguments(new Map([['isodate', '1383-10-04T14:10:30.700+00:00']]));

    expect((await getMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(10);

    fep.setArguments(new Map([['isodate', '1994-12-24T14:10:30.700+00:00']]));

    expect((await getMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(12);
});

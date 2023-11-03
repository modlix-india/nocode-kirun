import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetDayFunction', async () => {
    let getDay = await dateFunctionRepo.find(Namespaces.DATE, 'getDay');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getDay) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(3);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2023-04-20T14:48:20.000Z']]));

    expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(4);

    fep.setArguments(new Map([['isodate', '2023-10-04T14:10:30.700+00:00']]));

    expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(3);

    fep.setArguments(new Map([['isodate', '2023-10-24T14:10:30.700+00:00']]));

    expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(2);

    fep.setArguments(new Map([['isodate', '2053-10-04T14:10:50.70000+00:00']]));

    expect((await getDay.execute(fep)).allResults()[0].getResult().get('day')).toBe(6);
});

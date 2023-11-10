import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetMinutesFunction', async () => {
    let getMinutes = await dateFunctionRepo.find(Namespaces.DATE, 'getMinutes');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getMinutes) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect((await getMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(45);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

    expect((await getMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(48);

    fep.setArguments(new Map([['isodate', '1383-10-04T14:33:30.700+00:20']]));

    expect((await getMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(33);

    fep.setArguments(new Map([['isodate', '1994-10-24T14:10:30.700+00:00']]));

    expect((await getMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(10);
});

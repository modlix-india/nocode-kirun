import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetMilliSecondsFunction', async () => {
    let getMilliSeconds = await dateFunctionRepo.find(Namespaces.DATE, 'getMilliSeconds');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getMilliSeconds) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect(
        (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe(939);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

    expect(
        (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe(0);

    fep.setArguments(new Map([['isodate', '1383-10-04T14:33:30.700+00:00']]));

    expect(
        (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe(700);

    fep.setArguments(new Map([['isodate', '1994-10-24T14:10:15.609+00:00']]));

    expect(
        (await getMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe(609);
});

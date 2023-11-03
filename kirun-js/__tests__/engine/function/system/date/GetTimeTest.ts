import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTime } from '../../../../../src/engine/function/system/date/GetTime';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetTimeFunction', async () => {
    let getTime = await dateFunctionRepo.find(Namespaces.DATE, 'getTime');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getTime) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
        1696419938939,
    );

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

    expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
        182882069300000,
    );

    fep.setArguments(new Map([['isodate', '1383-10-04T23:10:30.700+00:00']]));

    expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
        -18499970969300,
    );

    fep.setArguments(new Map([['isodate', '1994-10-24T02:10:30.700+00:00']]));

    expect((await getTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(782964630700);
});

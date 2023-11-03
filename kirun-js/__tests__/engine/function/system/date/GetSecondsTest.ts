import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetSecondsFunction', async () => {
    let getSeconds = await dateFunctionRepo.find(Namespaces.DATE, 'getSeconds');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getSeconds) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(38);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000Z']]));

    expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(20);

    fep.setArguments(new Map([['isodate', '1383-10-04T14:33:30.700+00:00']]));

    expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(30);

    fep.setArguments(new Map([['isodate', '1994-10-24T14:10:15.700+00:00']]));

    expect((await getSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(15);
});

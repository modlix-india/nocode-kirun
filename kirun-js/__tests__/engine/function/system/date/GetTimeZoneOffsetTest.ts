import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetTimeZoneOffsetFunction', async () => {
    let getTimeZoneOffset = await dateFunctionRepo.find(Namespaces.DATE, 'getTimeZoneOffset');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getTimeZoneOffset) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '2023-10-04T11:45:38.939Z']]));

    expect(
        (await getTimeZoneOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
    ).toBe(0);

    // fep.setArguments(new Map([['isodate', 'abc']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    // fep.setArguments(new Map([['isodate', '2023-10-4T11:45:38.939Z']]));

    // expect(
    //     (await getDate.execute(fep)).allResults()[0].getResult().get('date'),
    // ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '7765-04-20T14:48:20.000+05:30']]));

    expect(
        (await getTimeZoneOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
    ).toBe(-330);

    fep.setArguments(new Map([['isodate', '2023-10-04T23:10:30.700-02:30']]));

    expect(
        (await getTimeZoneOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
    ).toBe(150);

    fep.setArguments(new Map([['isodate', '1994-10-24T02:10:30.700+00:00']]));

    expect(
        (await getTimeZoneOffset.execute(fep)).allResults()[0].getResult().get('timeZoneOffset'),
    ).toBe(0);
});


import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetDateFunction', async () => {
    let setDate = await dateFunctionRepo.find(Namespaces.DATE, 'setDate');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setDate) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '-092023-10-04T11:45:38.939-04:00'],
            ['dateValue', 12],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '-092023-10-12T11:45:38.939-04:00',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.000Z'],
            ['dateValue', 18],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '2023-09-18T17:35:17.000Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['dateValue', 31],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '1970-01-31T15:58:57.561Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615-05:12'],
            ['dateValue', 32],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '2023-11-01T06:44:11.615-05:12',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+05:09'],
            ['dateValue', 40],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '2023-11-09T14:10:30.700+05:09',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['dateValue', 76],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '1994-12-15T14:05:30.406-18:00',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['dateValue', 130],
        ]),
    );

    expect((await setDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(
        '1301-02-07T05:42:10.435+14:00',
    );
});

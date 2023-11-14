import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetDateFunction', async () => {
    let setMonth = await dateFunctionRepo.find(Namespaces.DATE, 'setMonth');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setMonth) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-04T11:45:38.939Z'],
            ['monthValue', 12],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(0);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.000Z'],
            ['monthValue', 18],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(6);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['monthValue', 31],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(7);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['monthValue', 100],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(4);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+15:02'],
            ['monthValue', 1000],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(4);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['monthValue', 10000],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(4);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['monthValue', 144],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(0);
});

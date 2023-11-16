import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
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

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2024-01-04T11:45:38.939Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.000Z'],
            ['monthValue', 18],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2024-07-03T17:35:17.000Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['monthValue', 31],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '1972-08-20T15:58:57.561Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['monthValue', 100],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2031-05-19T06:44:11.615Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', 12],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2024-01-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', 0],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2023-01-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', -1],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2022-12-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', -5],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2022-08-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', -11],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2022-02-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', -14],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2021-11-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-11-15T04:57:14.970Z'],
            ['monthValue', -19],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2021-06-15T04:57:14.970Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+15:02'],
            ['monthValue', -100],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2014-09-24T14:10:30.700+15:02',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['monthValue', 10000],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2827-05-24T14:05:30.406-18:00',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['monthValue', 144],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '1312-01-25T05:42:10.435+14:00',
    );
});

test('testing leap year', async () => {
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
            ['isodate', '2023-10-31T11:45:38.939Z'],
            ['monthValue', 2],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2023-03-31T11:45:38.939Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-31T11:45:38.939Z'],
            ['monthValue', 1],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2023-03-03T11:45:38.939Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2024-12-29T11:45:38.939Z'],
            ['monthValue', 0],
        ]),
    );

    expect((await setMonth.execute(fep)).allResults()[0].getResult().get('month')).toBe(
        '2024-01-29T11:45:38.939Z',
    );
});

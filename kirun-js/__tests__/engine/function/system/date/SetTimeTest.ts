import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetDateFunction', async () => {
    let setTime = await dateFunctionRepo.find(Namespaces.DATE, 'setTime');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setTime) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-04T11:45:38.939Z'],
            ['timeValue', 1000],
        ]),
    );

    expect((await setTime.execute(fep)).allResults()[0].getResult().get('time'));

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '2023-09-03T17:35:17.000Z'],
    //         ['timeValue', 18],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(18);

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '1970-01-20T15:58:57.561Z'],
    //         ['timeValue', 31],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(31);

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '2023-10-19T06:44:11.615Z'],
    //         ['timeValue', 32],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(1);

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '2023-10-24T14:10:30.700+12:00'],
    //         ['timeValue', 40],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(9);

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '1994-10-24T14:05:30.406-18:00'],
    //         ['timeValue', 76],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(15);

    // fep.setArguments(
    //     new Map<string, any>([
    //         ['isodate', '1300-10-25T05:42:10.435+14:00'],
    //         ['timeValue', 130],
    //     ]),
    // );

    // expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(7);
});

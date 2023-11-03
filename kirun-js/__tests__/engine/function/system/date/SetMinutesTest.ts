import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetMinutesFunction', async () => {
    let setMinutes = await dateFunctionRepo.find(Namespaces.DATE, 'setMinutes');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setMinutes) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-07T17:35:17.123Z'],
            ['minutesValue', 68],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(8);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.980Z'],
            ['minutesValue', 100],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(40);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['minutesValue', 1000],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(40);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['minutesValue', 10000],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(40);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+12:00'],
            ['minutesValue', 100],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(40);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['minutesValue', -100],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(20);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['minutesValue', -10000],
        ]),
    );

    expect((await setMinutes.execute(fep)).allResults()[0].getResult().get('minutes')).toBe(20);
});

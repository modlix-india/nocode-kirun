import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetSecondsFunction', async () => {
    let setSeconds = await dateFunctionRepo.find(Namespaces.DATE, 'setSeconds');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setSeconds) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-07T17:35:17.123Z'],
            ['secondsValue', -10000],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.980Z'],
            ['secondsValue', 100],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['secondsValue', 1000],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['secondsValue', 10000],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+12:00'],
            ['secondsValue', 100],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['secondsValue', -100],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['secondsValue', -10000],
        ]),
    );

    expect(async () =>
        (await setSeconds?.execute(fep))?.allResults()[0].getResult().get('seconds'),
    ).rejects.toThrowError('Seconds should be in the range of 0 and 59');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['secondsValue', 10],
        ]),
    );

    expect((await setSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(10);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['secondsValue', 59],
        ]),
    );

    expect((await setSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(59);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['secondsValue', 34],
        ]),
    );

    expect((await setSeconds.execute(fep)).allResults()[0].getResult().get('seconds')).toBe(34);
});

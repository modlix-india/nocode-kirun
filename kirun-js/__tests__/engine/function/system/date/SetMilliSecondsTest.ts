import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetMilliSecondsFunction', async () => {
    let setMilliSeconds = await dateFunctionRepo.find(Namespaces.DATE, 'setMilliSeconds');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setMilliSeconds) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-07T17:35:17.123Z'],
            ['milliSecondsValue', -10000],
        ]),
    );

    expect(async () =>
        (await setMilliSeconds?.execute(fep))?.allResults()[0].getResult().get('milliSeconds'),
    ).rejects.toThrowError('Milliseconds should be in the range of 0 and 999');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.980Z'],
            ['milliSecondsValue', 100],
        ]),
    );

    expect(
        (await setMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe('2023-09-03T17:35:17.100Z');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['milliSecondsValue', 1000],
        ]),
    );

    expect(async () =>
        (await setMilliSeconds?.execute(fep))?.allResults()[0].getResult().get('milliSeconds'),
    ).rejects.toThrowError('Milliseconds should be in the range of 0 and 999');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['milliSecondsValue', 10000],
        ]),
    );

    expect(async () =>
        (await setMilliSeconds?.execute(fep))?.allResults()[0].getResult().get('milliSeconds'),
    ).rejects.toThrowError('Milliseconds should be in the range of 0 and 999');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+12:00'],
            ['milliSecondsValue', 100],
        ]),
    );

    expect(
        (await setMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe('2023-10-24T14:10:30.100+12:00');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['milliSecondsValue', 546],
        ]),
    );

    expect(
        (await setMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe('1994-10-24T14:05:30.546-18:00');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '-121300-10-25T05:42:10.435+14:00'],
            ['milliSecondsValue', 123],
        ]),
    );

    expect(
        (await setMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe('-121300-10-25T05:42:10.123+14:00');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '+001300-10-25T05:42:10.435+14:00'],
            ['milliSecondsValue', 456],
        ]),
    );

    expect(
        (await setMilliSeconds.execute(fep)).allResults()[0].getResult().get('milliSeconds'),
    ).toBe('+001300-10-25T05:42:10.456+14:00');
});

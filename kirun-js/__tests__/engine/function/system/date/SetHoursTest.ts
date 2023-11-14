import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetHoursFunction', async () => {
    let setHours = await dateFunctionRepo.find(Namespaces.DATE, 'setHours');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setHours) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-04T11:45:38.939Z'],
            ['hoursValue', -109],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.000Z'],
            ['hoursValue', 100],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['hoursValue', 1000],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['hoursValue', -0],
        ]),
    );

    expect((await setHours.execute(fep)).allResults()[0].getResult().get('hours')).toBe(0);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+12:00'],
            ['hoursValue', 100],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['hoursValue', -100],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['hoursValue', -10000],
        ]),
    );

    expect(async () =>
        (await setHours?.execute(fep))?.allResults()[0].getResult().get('hour'),
    ).rejects.toThrowError('Hours should be in the range of 0 and 23');

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['hoursValue', 10],
        ]),
    );

    expect((await setHours.execute(fep)).allResults()[0].getResult().get('hours')).toBe(10);
});

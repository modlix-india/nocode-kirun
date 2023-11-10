import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('testing SetFullYearFunction', async () => {
    let setFullYear = await dateFunctionRepo.find(Namespaces.DATE, 'setFullYear');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!setFullYear) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-04T11:45:38.939Z'],
            ['yearValue', 8347],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(8347);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '-002023-09-03T17:35:17.000Z'],
            ['yearValue', -10],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(-10);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1970-01-20T15:58:57.561Z'],
            ['yearValue', 1997],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(1997);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-19T06:44:11.615Z'],
            ['yearValue', 2030],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(2030);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-10-24T14:10:30.700+12:00'],
            ['yearValue', 9999],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(9999);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1994-10-24T14:05:30.406-18:00'],
            ['yearValue', 8575],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(8575);

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '1300-10-25T05:42:10.435+14:00'],
            ['yearValue', 1998],
        ]),
    );

    expect((await setFullYear.execute(fep)).allResults()[0].getResult().get('year')).toBe(1998);
});

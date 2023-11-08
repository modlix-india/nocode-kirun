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
            ['timeValue', 100000],
        ]),
    );

    expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
        '1970-01-01T00:01:40.000Z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isodate', '2023-09-03T17:35:17.980Z'],
            ['timeValue', 100],
        ]),
    );

    expect((await setTime.execute(fep)).allResults()[0].getResult().get('time')).toBe(
        '1970-01-01T00:00:00.100Z',
    );
});

import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetTimeZoneOffset } from '../../../../../src/engine/function/system/date/GetTimeZoneOffset';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateFunctionRepo = new DateFunctionRepository();

test('test1 IsBeforeFunction', async () => {
    let isBefore = await dateFunctionRepo.find(Namespaces.DATE, 'IsBefore');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!isBefore) {
        throw new Error('Function not available');
    }

    let arr = [];
    arr.push('year');
    arr.push('day');

    fep.setArguments(
        new Map<string, any>([
            ['dateone', '2023-10-31T17:14:21.798Z'],
            ['datetwo', '2023-10-31T17:14:20.789Z'],
            ['unit', arr],
        ]),
    );

    expect((await isBefore.execute(fep)).allResults()[0].getResult().get('result')).toBe(true);
});

test('test2 IsBeforeFunction', async () => {
    let isBefore = await dateFunctionRepo.find(Namespaces.DATE, 'IsBefore');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!isBefore) {
        throw new Error('Function not available');
    }

    let arr = [];
    arr.push('year');

    fep.setArguments(
        new Map<string, any>([
            ['dateone', '2023-10-31T17:14:21.798Z'],
            ['datetwo', '2000-10-31T17:14:20.789Z'],
            ['unit', arr],
        ]),
    );

    expect((await isBefore.execute(fep)).allResults()[0].getResult().get('result')).toBe(false);
});

import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const subtractTimeFunction = await dfr.find(Namespaces.DATE, 'SubtractTime');

    if (!subtractTimeFunction) {
        throw new Error("Function not found");
    }
   
    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2029-15-05T06:04:18.073Z'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 10],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'SECOND']
    ]));

    await expect( () => subtractTimeFunction.execute(fep)).rejects.toThrow();
});

test('Subtract Time 1', async () => {

    const subtractTimeFunction = await dfr.find(Namespaces.DATE, 'SubtractTime');

    if (!subtractTimeFunction) {
        throw new Error("Function not found");
    }
    
    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 10],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'MINUTE']
    ]));

    expect((await subtractTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2024-09-13T23:42:34.633-05:30');
});

test('Subtract Time 2', async () => {

    const subtractTimeFunction = await dfr.find(Namespaces.DATE, 'SubtractTime');

    if (!subtractTimeFunction) {
        throw new Error("Function not found");
    }
    
    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 13],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'MONTH']
    ]));

    expect((await subtractTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2023-08-13T23:52:34.633-05:30');
});

test('Subtract Time 3', async () => {

    const subtractTimeFunction = await dfr.find(Namespaces.DATE, 'SubtractTime');

    if (!subtractTimeFunction) {
        throw new Error("Function not found");
    }
    
    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 70],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'SECOND']
    ]));

    expect((await subtractTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2024-09-13T23:51:24.633-05:30');
});

test('Subtract Time 4', async () => {

    const subtractTimeFunction = await dfr.find(Namespaces.DATE, 'SubtractTime');

    if (!subtractTimeFunction) {
        throw new Error("Function not found");
    }
    
    fep.setArguments(new Map<string, any>([ 
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 5],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'YEAR']
    ]));

    expect((await subtractTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2019-09-13T23:52:34.633-05:30');
});

import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }
   
    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2029-15-05T06:04:18.073Z'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 10],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'SECOND']
    ]));

    await expect( () => addTimeFunction.execute(fep)).rejects.toThrow();
});

test('Add Time 1', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 10],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'MINUTE']
    ]));
    
    expect((await addTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2024-09-14T00:02:34.633-05:30');
})

test('Add Time 2', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 13],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'MONTH']
    ]));
    
    expect((await addTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2025-10-13T23:52:34.633-05:30');
})


test('Add Time 2', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 3],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'MONTH']
    ]));
    
    expect((await addTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2024-12-13T23:52:34.633-05:30');
})

test('Add Time 3', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 10],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'SECOND']
    ]));
    
    expect((await addTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2024-09-13T23:52:44.633-05:30');
    
})


test('Add Time 4', async () => {

    const addTimeFunction = await dfr.find(Namespaces.DATE, 'AddTime');

    if (!addTimeFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map<string, any>([
        [AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30'],
        [AbstractDateFunction.PARAMETER_INT_NAME, 5],
        [AbstractDateFunction.PARAMETER_UNIT_NAME, 'YEAR']
    ]));
    
    expect((await addTimeFunction.execute(fep)).allResults()[0].getResult().get('result')).toBe('2029-09-13T23:52:34.633-05:30');
})
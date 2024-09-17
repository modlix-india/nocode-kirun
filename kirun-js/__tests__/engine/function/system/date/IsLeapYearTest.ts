import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const isLeapFunction = await dfr.find(Namespaces.DATE, 'IsLeapYear');

    if (!isLeapFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2029-15-05T06:04:18.073Z' ]]));

    await expect( () => isLeapFunction.execute(fep)).rejects.toThrow();
    
});


test('check for invalid dates', async () => {

    const isLeapFunction = await dfr.find(Namespaces.DATE, 'IsLeapYear');

    if (!isLeapFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, false ]]));

    await expect( () => isLeapFunction.execute(fep)).rejects.toThrow();
    
});

test('fetching for valid date' , async () => {

    const isLeapFunction = await dfr.find(Namespaces.DATE, 'IsLeapYear');

    if (!isLeapFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30' ]]));


    expect( (await isLeapFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBeTruthy();

})

test('fetching for valid date' , async () => {

    const isLeapFunction = await dfr.find(Namespaces.DATE, 'IsLeapYear');

    if (!isLeapFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2023-09-13T23:52:34.633Z' ]]));


    expect( (await isLeapFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBeFalsy();

})


test('fetching for valid date' , async () => {

    const isLeapFunction = await dfr.find(Namespaces.DATE, 'IsLeapYear');

    if (!isLeapFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2020-02-29T07:35:17.000-12:00' ]]));


    expect( (await isLeapFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBeTruthy();

})

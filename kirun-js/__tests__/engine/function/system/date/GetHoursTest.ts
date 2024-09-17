import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const getHoursFunction = await dfr.find(Namespaces.DATE, 'GetHours');

    if (!getHoursFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2029-02-00T06:04:18.073Z' ]]));

    await expect( () => getHoursFunction.execute(fep)).rejects.toThrow();
    
});


test('check for invalid dates', async () => {

    const getHoursFunction = await dfr.find(Namespaces.DATE, 'GetHours');

    if (!getHoursFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, 'cvb' ]]));

    await expect( () => getHoursFunction.execute(fep)).rejects.toThrow();
    
});

test('fetching for valid date' , async () => {

    const getHoursFunction = await dfr.find(Namespaces.DATE, 'GetHours');

    if (!getHoursFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-01T23:52:34.633-05:30' ]]));


    expect( (await getHoursFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(5);

})

test('fetching for valid date' , async () => {

    const getHoursFunction = await dfr.find(Namespaces.DATE, 'GetHours');

    if (!getHoursFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2019-11-13T00:52:34.633Z' ]]));


    expect( (await getHoursFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(0);

})


test('fetching for valid date' , async () => {

    const getHoursFunction = await dfr.find(Namespaces.DATE, 'GetHours');

    if (!getHoursFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2023-12-31T07:35:17.000-12:00' ]]));


    expect( (await getHoursFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(19);

})

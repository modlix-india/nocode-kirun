import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const getSecondsFunction = await dfr.find(Namespaces.DATE, 'GetSeconds');

    if (!getSecondsFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2029-02-00T06:04:18.073Z' ]]));

    await expect( () => getSecondsFunction.execute(fep)).rejects.toThrow();
    
});


test('check for invalid dates', async () => {

    const getSecondsFunction = await dfr.find(Namespaces.DATE, 'GetSeconds');

    if (!getSecondsFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, 'water packet' ]]));

    await expect( () => getSecondsFunction.execute(fep)).rejects.toThrow();
    
});

test('fetching for valid date' , async () => {

    const getSecondsFunction = await dfr.find(Namespaces.DATE, 'GetSeconds');

    if (!getSecondsFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-01T23:52:53.633-05:30' ]]));


    expect( (await getSecondsFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(53);

})

test('fetching for valid date' , async () => {

    const getSecondsFunction = await dfr.find(Namespaces.DATE, 'GetSeconds');

    if (!getSecondsFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2019-11-13T00:52:34.633Z' ]]));


    expect( (await getSecondsFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(34);

})


test('fetching for valid date' , async () => {

    const getSecondsFunction = await dfr.find(Namespaces.DATE, 'GetSeconds');

    if (!getSecondsFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2023-12-31T07:59:17.000-12:00' ]]));


    expect( (await getSecondsFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(17);

})

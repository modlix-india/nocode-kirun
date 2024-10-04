import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from "../../../../../src";
import { AbstractDateFunction } from "../../../../../src/engine/function/system/date/AbstractDateFunction";
import { DateFunctionRepository } from "../../../../../src/engine/function/system/date/DateFunctionRepository";


const dfr : DateFunctionRepository = new DateFunctionRepository();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());

test('check for invalid dates', async () => {

    const getDayFunction = await dfr.find(Namespaces.DATE, 'GetDay');

    if (!getDayFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2029-05-95T06:04:18.073Z' ]]));

    await expect( () => getDayFunction.execute(fep)).rejects.toThrow();
    
});


test('check for invalid dates', async () => {

    const getDayFunction = await dfr.find(Namespaces.DATE, 'GetDay');

    if (!getDayFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, 1234 ]]));

    await expect( () => getDayFunction.execute(fep)).rejects.toThrow();
    
});

test('fetching for valid date' , async () => {

    const getDayFunction = await dfr.find(Namespaces.DATE, 'GetDay');

    if (!getDayFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-13T23:52:34.633-05:30' ]]));


    expect( (await getDayFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(6);

})

test('fetching for valid date' , async () => {

    const getDayFunction = await dfr.find(Namespaces.DATE, 'GetDay');

    if (!getDayFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2024-09-10T23:52:34.633Z' ]]));


    expect( (await getDayFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(2);

})


test('fetching for valid date' , async () => {

    const getDayFunction = await dfr.find(Namespaces.DATE, 'GetDay');

    if (!getDayFunction) {
        throw new Error("Function not found");
    }

    fep.setArguments(new Map([[AbstractDateFunction.PARAMETER_DATE_NAME, '2023-12-31T07:35:17.000-12:00' ]]));


    expect( (await getDayFunction.execute(fep)).allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(0);

})

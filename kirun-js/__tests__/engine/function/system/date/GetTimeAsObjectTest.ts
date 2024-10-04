import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { GetTimeAsObject } from "../../../../../src/engine/function/system/date/GetTimeAsObject";

const gto : GetTimeAsObject = new GetTimeAsObject();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());


describe('invalid date', () => {

    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', 'surendhar']]));

        await expect( () => gto.execute(fep)).rejects.toThrow();
        
    })


    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-02-30T12:12:12.123Z']]));

        await expect( () => gto.execute(fep)).rejects.toThrow();
        
    })

})

describe('valid date', () => {

    test('valid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-10-10T10:02:54.959-12:12']]));

        expect((await gto.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject({
            "year": 2023,
            "month": 10,
            "day": 10,
            "hours": 22,
            "minutes": 14,
            "seconds": 54,
            "milliseconds": 959
        });
        
    })

    test('valid date two', async () => {

        fep.setArguments(new Map([['isoDate', '2024-09-20T15:13:51.000Z']]));

        expect((await gto.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject({
            "year": 2024,
            "month": 9,
            "day": 20,
            "hours": 15,
            "minutes": 13,
            "seconds": 51,
            "milliseconds": 0
        });
        
    })


    test('valid date three', async () => {

        fep.setArguments(new Map([['isoDate', '2023-06-10T10:02:54.959+02:11']]));

        expect((await gto.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject({
            "year": 2023,
            "month": 6,
            "day": 10,
            "hours": 7,
            "minutes": 51,
            "seconds": 54,
            "milliseconds": 959
        });
        
    })

});
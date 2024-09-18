import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { GetTimeAsArray } from "../../../../../src/engine/function/system/date/GetTimeAsArray";

const gta : GetTimeAsArray = new GetTimeAsArray();

const fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository());


describe('invalid date', () => {

    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', 'surendhar']]));

        await expect( () => gta.execute(fep)).rejects.toThrow();
        
    })


    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-02-30T12:12:12.123Z']]));

        await expect( () => gta.execute(fep)).rejects.toThrow();
        
    })

})

describe('valid date', () => {

    test('valid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-10-10T10:02:54.959-12:12']]));

        expect((await gta.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject([2023, 10, 10, 22, 14, 54, 959]);
        
    })

    test('valid date two', async () => {

        fep.setArguments(new Map([['isoDate', '2024-09-20T15:13:51.000Z']]));

        expect((await gta.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject([2024, 9, 20, 15, 13, 51, 0]);
        
    })


    test('valid date three', async () => {

        fep.setArguments(new Map([['isoDate', '2023-06-10T10:02:54.959+02:11']]));

        expect((await gta.execute(fep)).allResults()[0].getResult().get('result')).toMatchObject([2023, 6, 10, 7, 51, 54, 959]);
        
    })

});
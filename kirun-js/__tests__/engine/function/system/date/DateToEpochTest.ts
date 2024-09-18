import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { DateToEpoch } from "../../../../../src/engine/function/system/date/DateToEpoch";

const dte: DateToEpoch = new DateToEpoch();

const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository()
);

describe('invalid date', () => {

    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', 'surendhar']]));

        await expect( () => dte.execute(fep)).rejects.toThrow();
        
    })


    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-02-30T12:12:12.123Z']]));

        await expect( () => dte.execute(fep)).rejects.toThrow();
        
    })

})

describe('valid date', () => {

    test('valid date one', async () => {

        fep.setArguments(new Map([['isoDate', '2023-10-21T16:11:50.978Z']]));
        
        expect((await dte.execute(fep)).allResults()[0].getResult().get('result')).toBe(1697904710978);

    })

    test('valid date two', async () => {

        fep.setArguments(new Map([['isoDate', '2507-08-07T11:41:50.000Z']]));
        
        expect((await dte.execute(fep)).allResults()[0].getResult().get('result')).toBe(16964941310000);

    })
    
    test('valid date three', async () => {

        fep.setArguments(new Map([['isoDate', '1970-01-20T15:13:51.000Z']]));
        
        expect((await dte.execute(fep)).allResults()[0].getResult().get('result')).toBe(1696431000);

    })

    test('valid date four', async () => {

        fep.setArguments(new Map([['isoDate', '2024-02-29T12:13:41.189-12:01']]));
        
        expect((await dte.execute(fep)).allResults()[0].getResult().get('result')).toBe(1709252081189);

    })

    test('valid date five', async () => {

        fep.setArguments(new Map([['isoDate', '2028-02-29T12:13:49.200+02:01']]));
        
        expect((await dte.execute(fep)).allResults()[0].getResult().get('result')).toBe(1835431969200);

    })

});

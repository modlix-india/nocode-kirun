import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { IsValidISODate } from "../../../../../src/engine/function/system/date/IsValidISODate";


const validIso: IsValidISODate = new IsValidISODate();

describe('checking the validity for not string types ',() => {

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    test('number test' , async () => {

        fep.setArguments(new Map([['isoDate',1234]]))
        await expect( validIso.execute(fep)).rejects.toThrow();

    });

    test('boolean test' , async () => {

        fep.setArguments(new Map([['isoDate',true]]))
        await expect(validIso.execute(fep)).rejects.toThrow();

    })
})

describe('checking validity for string date types' , () => {

    test('Date1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "aws"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('output')).toBeFalsy();
    });
});

describe('checking validity', () => {

    test('Date1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "2023-10-04T11:45:38.939ss"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('output')).toBeFalsy();
    });

    test('Date2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "2023-10-04T11:45:38.939Z"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('output')).toBeTruthy();
    });

    test('Date3', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "2023-10-10T10:02:54.959Z"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('output')).toBeTruthy();
    });

    test('Date4', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "2023-10-10T10:02:54.959-12:12"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('output')).toBeTruthy();
    });
    
})

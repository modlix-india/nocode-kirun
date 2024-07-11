import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository, KIRuntimeException } from "../../../../../src";
import { IsValidISODate } from "../../../../../src/engine/function/system/date/IsValidISODate";


const validIso: IsValidISODate = new IsValidISODate();

describe('checking the validity for not string types ',() => {

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    test('number test' , () => {

        fep.setArguments(new Map([['isoDate',1234]]))

        validIso.execute(fep).then();

        expect(async () =>
            (await validIso.execute(fep)).allResults()[0].getResult().get('output'),
        ).rejects.toThrowError(KIRuntimeException);
    })

    test('boolean test' , () => {

        fep.setArguments(new Map([['isoDate',true]]))

        validIso.execute(fep).then();

        expect(async () =>
            (await validIso.execute(fep)).allResults()[0].getResult().get('output'),
        ).rejects.toThrowError(KIRuntimeException);
    })
});

describe('checking validity for string date types' , () => {

    test('Date1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['isoDate', "aws"]]));

        expect((await validIso.execute(fep)).allResults()[0].getResult().get('date')).toBeFalsy();
    });
});

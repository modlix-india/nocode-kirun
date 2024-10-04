import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { MinimumTimestamp } from "../../../../../src/engine/function/system/date/MinimumTimestamp";


const mt: MinimumTimestamp = new MinimumTimestamp();

let fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

describe('MinimumTimestamp valid', () => {

    test('test1', async () => {

        const arr = ["2023-10-25T13:30:04.970Z","2023-10-25T12:30:04.970Z","2023-10-25T19:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T12:30:04.970Z");
    });

    test('test2', async () => { 

        const arr = ["2023-10-25T12:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T12:30:04.970Z");
    });

    test('test3', async () => { 

        const arr = ["2023-10-25T13:30:04.970+07:00","2023-10-25T12:30:04.970-1:00","2023-10-25T19:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));
        
        await expect(mt.execute(fep)).rejects.toThrow();
    });

    test('test4', async () => { 

        const arr = ["2023-10-25T13:30:04.970+07:00","2023-10-25T12:30:04.970-11:00","2023-10-25T19:30:04.970Z","2023-10-25T13:30:04.970+09:00","2023-10-25T19:30:04.970+01:30"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T13:30:04.970+09:00");
    });

    test('test5', async () => { 

        const arr = ["2023-10-25T02:30:04.970Z","2023-10-25T03:30:04.970Z","2023-10-25T04:30:04.970Z","2023-10-25T05:30:04.970Z","2023-10-25T06:30:04.970Z","2023-10-25T07:30:04.970Z","2023-10-25T08:30:04.970Z","2023-10-25T09:30:04.970Z","2023-10-25T10:30:04.970Z","2023-10-25T11:30:04.970Z","2023-10-25T12:30:04.970Z","2023-10-25T13:30:04.970Z","2023-10-25T14:30:04.970Z","2023-10-25T15:30:04.970Z","2023-10-25T16:30:04.970Z","2023-10-25T17:30:04.970Z","2023-10-25T18:30:04.970Z","2023-10-25T19:30:04.970Z","2023-10-25T01:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T01:30:04.970Z");
    });
    
});
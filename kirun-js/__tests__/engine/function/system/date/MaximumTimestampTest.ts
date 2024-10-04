import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { MaximumTimestamp } from "../../../../../src/engine/function/system/date/MaximumTimestamp";


const mt : MaximumTimestamp = new MaximumTimestamp();

let fep : FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

describe('MaximumTimestamp valid', () => {

    test('test1', async () => {

        const arr = ["2023-10-25T13:30:04.970+07:00","2023-10-25T12:30:04.970-1:00","2023-10-25T19:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        await expect(mt.execute(fep)).rejects.toThrow();
    });

    test('test2', async () => {

        const arr = ["2023-10-25T13:30:04.970Z", "2023-10-25T12:30:04.970Z","2023-10-25T19:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T19:30:04.970Z");
    });

    test('test3', async () => {

        const arr = ["2023-10-25T12:30:04.970Z"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T12:30:04.970Z");
    });

    test('test4', async () => {

        const arr = ["2023-10-25T13:30:04.970+07:00","2023-10-25T12:30:04.970-11:00","2023-10-25T19:30:04.970Z","2023-10-25T13:30:04.970+09:00","2023-10-25T19:30:04.970+01:30"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T12:30:04.970-11:00");
    });

    test('test5', async () => {

        const arr = ["2023-10-25T13:30:04.100+01:00","2023-10-25T13:30:04.101+02:00","2023-10-25T13:30:04.102+03:00","2023-10-25T13:30:04.103+04:00","2023-10-25T13:30:04.104+05:00"];
        fep.setArguments(new Map([['isoDates', arr]]));

        expect((await mt.execute(fep)).allResults()[0].getResult().get("result")).toBe("2023-10-25T13:30:04.100+01:00");
    });

});


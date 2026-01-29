import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../src';
import { Wait } from '../../../../src/engine/function/system/Wait';

const waitFunction = new Wait();

test('wait test', async () => {
    const waitTime = 1000;
    const startTime = new Date().getTime();
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    await waitFunction.execute(fep.setArguments(new Map([['millis', waitTime]])));

    const endTime = new Date().getTime();
    expect(endTime - startTime).toBeGreaterThanOrEqual(waitTime);
});

test('wait test 3500ms', async () => {
    const waitTime = 3500;
    const startTime = new Date().getTime();
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    await waitFunction.execute(fep.setArguments(new Map([['millis', waitTime]])));

    const endTime = new Date().getTime();
    expect(endTime - startTime).toBeGreaterThanOrEqual(waitTime);
});

test('wait test immediately', async () => {
    const startTime = new Date().getTime();
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    await waitFunction.execute(fep.setArguments(new Map()));

    const endTime = new Date().getTime();
    expect(endTime - startTime).toBeLessThan(50);
});

test('wait test error with negative number', async () => {
    const waitTime = -3500;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    expect(
        waitFunction.execute(fep.setArguments(new Map([['millis', waitTime]]))),
    ).rejects.toThrow();
});

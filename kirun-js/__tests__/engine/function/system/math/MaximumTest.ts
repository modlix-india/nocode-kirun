import { Maximum } from "../../../../../src/engine/function/system/math/Maximum";
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const max = new Maximum();

test('Minimum Test 1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([['value', [3, 2, 3, 5, 3]]]),
    )

    expect((await max.execute(fep)).allResults()[0].getResult().get('value')).toBe(5);
})

test('Minimum Test 2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([['value', [-1, -1, 0, -1, -2]]]),
    )

    expect((await max.execute(fep)).allResults()[0].getResult().get('value')).toBe(0);
})

test('Minimum Test 3', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([['value', ["-1", -1, 0, -1, -2]]]),
    )

    expect(async () => (await max.execute(fep)).allResults()[0].getResult().get('value')).rejects.toThrowError("Value \"-1\" is not of valid type(s)\n-1 is not a Integer\n-1 is not a Long\n-1 is not a Float\n-1 is not a Double");
})
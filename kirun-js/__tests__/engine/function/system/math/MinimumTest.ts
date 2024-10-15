import { Minimum } from '../../../../../src/engine/function/system/math/Minimum';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const min = new Minimum();

test('Minimum Test 1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', [3, 2, 3, 5, 3]]]));

    expect((await min.execute(fep)).allResults()[0].getResult().get('value')).toBe(2);
});

test('Minimum Test 2', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', ['3', 2, 3, 5, 3]]]));

    expect(async () =>
        (await min.execute(fep)).allResults()[0].getResult().get('value'),
    ).rejects.toThrowError(
        "Error while executing the function System.Math.Minimum's parameter value with step name 'Unknown Step' with error : Value \"3\" is not of valid type(s)\n3 is not a Double\n3 is not a Float\n3 is not a Long\n3 is not a Integer",
    );
});

test('Minimum Test 3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', [-1, -2, -3, -5, -3]]]));

    expect((await min.execute(fep)).allResults()[0].getResult().get('value')).toBe(-5);
});

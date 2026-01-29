import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { MathFunctionRepository } from '../../../../../src/engine/function/system/math/MathFunctionRepository';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const MathFunction: MathFunctionRepository = new MathFunctionRepository();

test('Test Math Functions 1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', 1.2]]));

    expect(
        (await (await MathFunction.find(Namespaces.MATH, 'Ceiling'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).toBe(2);
});

test('Test Math Functions 2', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', '-1.2']]));

    expect(async () =>
        (await (await MathFunction.find(Namespaces.MATH, 'Absolute'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).rejects.toThrow(
        'Error while executing the function System.Math.Absolute\'s parameter value [Expected: Integer | Long | Float | Double]: Value \"-1.2\" is not of valid type(s)\nExpected a Double but found \"-1.2\"\nExpected a Float but found \"-1.2\"\nExpected a Long but found \"-1.2\"\nExpected a Integer but found \"-1.2\"',
    );
});

test('Test Math Functions 3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', 90]]));

    const func = await MathFunction.find(Namespaces.MATH, 'ACosine');
    if (!func)
        expect(func).toBe(undefined);
});

test('Test Math Functions 4', async () => {
    expect(await MathFunction.find(Namespaces.STRING, 'ASine')).toBe(undefined);
});

test('test Math Functions 5', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', '-1']]));

    const func = await MathFunction.find(Namespaces.MATH, 'ATangent');
    if (!func)
        expect(func).toBe(undefined);
});

test('test Math Functions 6', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', 1]]));

    expect(
        (await (await MathFunction.find(Namespaces.MATH, 'Cosine'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).toBe(0.5403023058681398);
});

test('test Math Functions 7', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map([
            ['value1', 2],
            ['value2', 3],
        ]),
    );

    expect(
        (await (await MathFunction.find(Namespaces.MATH, 'Power'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).toBe(8);
});

test('test Math Functions 8', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map([
            ['value1', '1'],
            ['value2', '1'],
        ]),
    );

    expect(async () =>
        (await (await MathFunction.find(Namespaces.MATH, 'Power'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).rejects.toThrow(
        'Error while executing the function System.Math.Power\'s parameter value1 [Expected: Integer | Long | Float | Double]: Value "1" is not of valid type(s)\nExpected a Double but found "1"\nExpected a Float but found "1"\nExpected a Long but found "1"\nExpected a Integer but found "1"',
    );
});

test('test Math Functions 9', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', [3, 2, 3, 5, 3]]]));
    expect(
        (await (await MathFunction.find(Namespaces.MATH, 'Add'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).toBe(16);
});

test('test Math Functions 10', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['value', [3, 2]]]));
    expect(
        (await (await MathFunction.find(Namespaces.MATH, 'Hypotenuse'))?.execute(fep))
            ?.allResults()[0]
            ?.getResult()
            ?.get('value'),
    ).toBe(3.605551275463989);
});

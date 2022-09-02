import { KIRunFunctionRepository, KIRunSchemaRepository } from "../../../../../src";
import { GenericMathFunction } from "../../../../../src/engine/function/system/math/GenericMathFunction";
import { MathFunctionRepository } from "../../../../../src/engine/function/system/math/MathFunctionRepository";
import { Namespaces } from "../../../../../src/engine/namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../../../src/engine/runtime/FunctionExecutionParameters";

const MathFunction: MathFunctionRepository = new MathFunctionRepository();

test("Test Math Functions 1", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', 1.2]]),
    );

    expect((await MathFunction.find(Namespaces.MATH, "Ceiling")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(2);
})

test("Test Math Functions 2", () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', "-1.2"]]),
    );

    expect(async () => (await (MathFunction.find(Namespaces.MATH, "Absolute")?.execute(fep)))?.allResults()[0]?.getResult()?.get("value")).rejects.toThrow();
})

test("Test Math Functions 3", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', 90]]),
    );

    expect((await MathFunction.find(Namespaces.MATH, "ACosine")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(NaN);
})

test("Test Math Functions 4", () => {
    expect(MathFunction.find(Namespaces.STRING, "ASine")).toBe(undefined);
})

test("test Math Functions 5", () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', "-1"]]),
    );

    expect(async () => (await MathFunction.find(Namespaces.MATH, "ATangent")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).rejects.toThrowError("Value \"-1\" is not of valid type(s)\n1 is not a Integer\n1 is not a Long\n1 is not a Float\n1 is not a Double");
})

test("test Math Functions 6", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', 1]]),
    );

    expect((await MathFunction.find(Namespaces.MATH, "Cosine")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(0.5403023058681398);
})

test("test Math Functions 7", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value1', 2], ['value2', 3]]),
    );

    expect((await MathFunction.find(Namespaces.MATH, "Power")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(8);
})

test("test Math Functions 8", () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value1', '1'], ['value2', '1']]),
    );

    expect(async () => (await MathFunction.find(Namespaces.MATH, "Power")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).rejects.toThrowError("Value \"1\" is not of valid type(s)\n1 is not a Integer\n1 is not a Long\n1 is not a Float\n1 is not a Double");
})

test("test Math Functions 9", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', [3, 2, 3, 5, 3]]]),
    );
    expect((await MathFunction.find(Namespaces.MATH, "Add")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(16);
})

test("test Math Functions 10", async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository()
    ).setArguments(
        new Map([['value', [3, 2]]]),
    );
    expect((await MathFunction.find(Namespaces.MATH, "Hypotenuse")?.execute(fep))?.allResults()[0]?.getResult()?.get("value")).toBe(3.605551275463989);
})

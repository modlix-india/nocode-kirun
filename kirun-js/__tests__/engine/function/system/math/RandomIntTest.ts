import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { MathFunctionRepository } from '../../../../../src/engine/function/system/math/MathFunctionRepository';

test(' rand int 1', async () => {
    const rand = (await new MathFunctionRepository().find(Namespaces.MATH, 'RandomInt'))!;
    let min = 100,
        max = 1000123;
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map([
            ['minValue', min],
            ['maxValue', max],
        ]),
    );
    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
});

test(' rand int 2', async () => {
    const rand = (await new MathFunctionRepository().find(Namespaces.MATH, 'RandomInt'))!;
    let min = 100;
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(new Map([['minValue', min]]));
    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(2147483647);
    expect(num).toBeGreaterThanOrEqual(min);
});

test(' rand int 3', async () => {
    const rand = (await new MathFunctionRepository().find(Namespaces.MATH, 'RandomInt'))!;
    let min = 100,
        max = 101;
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    ).setArguments(
        new Map([
            ['minValue', min],
            ['maxValue', max],
        ]),
    );
    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
});

import { RandomInt } from '../../../../../src/engine/function/system/math/RandomInt';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const rand = new RandomInt();

test(' rand int 1', async () => {
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

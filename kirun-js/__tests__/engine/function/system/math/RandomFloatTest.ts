import { RandomFloat } from "../../../../../src/engine/function/system/math/RandomFloat";
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const rand = new RandomFloat();

test('rand Float 1', async () => {
    let min = 1,
        max = 10;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([
            ['minValue', min],
            ['maxValue', max],
        ]),
    );

    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
})

test('rand Float 2', async () => {
    let min = 0.1,
        max = 0.9;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([
            ['minValue', min],
            ['maxValue', max],
        ]),
    );

    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
})

test('rand Float 3', async () => {
    let min = 1, max = 3.4028235E38;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([
            ['minValue', min],
        ]),
    );

    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
})

test('rand Float 4', async () => {
    let min = 1.1,
        max = 1.2;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository,
        new KIRunSchemaRepository
    ).setArguments(
        new Map([
            ['minValue', min],
            ['maxValue', max],
        ]),
    );

    let num: number = (await rand.execute(fep)).allResults()[0].getResult().get('value');

    expect(num).toBeLessThanOrEqual(max);
    expect(num).toBeGreaterThanOrEqual(min);
})
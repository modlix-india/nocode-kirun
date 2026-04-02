import { Reverse } from '../../../../../src/engine/function/system/string/Reverse';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const reve: Reverse = new Reverse();

test('reverse test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', ' mr"ofta"lp edoc on a si sihT'));

    let reveresed: string = 'This is a no code pl"atfo"rm ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe(reveresed);
});

test('reverse test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', ' '));

    let reveresed: string = ' ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe(reveresed);
});

test('reverse test - empty string', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', ''));

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe('');
});

test('reverse test - single character', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', 'a'));

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe('a');
});

test('reverse test - palindrome', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', 'racecar'));

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe('racecar');
});

test('reverse test - special characters', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', '!@#$%'));

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe('%$#@!');
});

test('reverse test - numbers in string', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('value', '12345'));

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe('54321');
});

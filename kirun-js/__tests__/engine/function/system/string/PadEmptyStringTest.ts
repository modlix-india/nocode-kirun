import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { PostPad } from '../../../../../src/engine/function/system/string/PostPad';
import { PrePad } from '../../../../../src/engine/function/system/string/PrePad';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

// An empty pad string used to loop forever: the padded length never grew past `length`.

const fep = (args: [string, any][]) =>
    new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
        .setArguments(new Map(args))
        .setContext(new Map())
        .setSteps(new Map());

test('PrePad with an empty pad string returns the input', async () => {
    const out = await new PrePad().execute(
        fep([
            ['string', '5'],
            ['prepadString', ''],
            ['length', 3],
        ]),
    );
    expect(out.allResults()[0].getResult().get('result')).toBe('5');
}, 2000);

test('PostPad with an empty pad string returns the input', async () => {
    const out = await new PostPad().execute(
        fep([
            ['string', '5'],
            ['postpadString', ''],
            ['length', 3],
        ]),
    );
    expect(out.allResults()[0].getResult().get('result')).toBe('5');
}, 2000);

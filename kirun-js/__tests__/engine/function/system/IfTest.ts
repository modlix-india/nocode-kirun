import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../src';
import { If } from '../../../../src/engine/function/system/If';

const ifFunction = new If();

test('if test', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    let fo = await ifFunction.execute(fep.setArguments(new Map([['condition', true]])));
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', false]])));
    expect(fo.allResults()[0].getName()).toBe('false');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', undefined]])));
    expect(fo.allResults()[0].getName()).toBe('false');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', null]])));
    expect(fo.allResults()[0].getName()).toBe('false');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', '']]))); // empty string
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', ' ']]))); // space
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', 'abc']])));
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', 0]])));
    expect(fo.allResults()[0].getName()).toBe('false');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', 1]])));
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', -1]])));
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', {}]])));
    expect(fo.allResults()[0].getName()).toBe('true');

    fo = await ifFunction.execute(fep.setArguments(new Map([['condition', []]])));
    expect(fo.allResults()[0].getName()).toBe('true');
});

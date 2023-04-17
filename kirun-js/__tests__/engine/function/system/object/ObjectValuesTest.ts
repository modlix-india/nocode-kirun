import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
} from '../../../../../src';
import { ObjectValues } from '../../../../../src/engine/function/system/object/ObjectValues';

const objVals: ObjectValues = new ObjectValues();

test('entry test 1', async () => {
    let obj = { a: 1, b: 2, d: ['a', 'b', 'c'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res = [1, 2, ['a', 'b', 'c']];

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        res,
    );
});

test('entry null test ', async () => {
    let obj = null;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        [],
    );
});

test('entry undefined test ', async () => {
    let obj;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        [],
    );
});

test('entry primitive number test ', async () => {
    let obj = 423;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        [],
    );
});

test('entry primitive boolean test ', async () => {
    let obj = false;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        [],
    );
});

test('entry primitive String test ', async () => {
    let obj = 'surendhar';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual([
        's',
        'u',
        'r',
        'e',
        'n',
        'd',
        'h',
        'a',
        'r',
    ]);
});

test('entry nested object test', async () => {
    let obj = { a: { b: { c: { d: { e: [1, 2, 4, 5] } } } }, c: ['q', 'w', 'e', 'r'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res: any[] = [{ b: { c: { d: { e: [1, 2, 4, 5] } } } }, ['q', 'w', 'e', 'r']];

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        res,
    );
});

test(' entry array test ', async () => {
    let obj = [1, 2, 3, 4, 1, 12, 3, 4, 5];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res: any[] = [1, 2, 3, 4, 1, 12, 3, 4, 5];

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        res,
    );
});

test(' entry duplicate entry test ', async () => {
    let parent = { a: { b: 'c' }, k: { b: 'c' } };

    let obj = { ...parent, a: 'overridden', k: ' so only child objects are returned from child ' }; // trying to replicate duplicate

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let childRes: any[] = ['overridden', ' so only child objects are returned from child '];

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        childRes,
    );

    let parentRes: any[] = [{ b: 'c' }, { b: 'c' }];

    fep.setArguments(MapUtil.of('source', parent));

    expect((await objVals.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        parentRes,
    );
});

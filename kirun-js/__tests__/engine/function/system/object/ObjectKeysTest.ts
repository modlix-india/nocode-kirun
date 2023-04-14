import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
} from '../../../../../src';
import { ObjectKeys } from '../../../../../src/engine/function/system/object/ObjectKeys';

const objKeys: ObjectKeys = new ObjectKeys();

test('keys test 1', async () => {
    let obj = { a: 1, b: 2, d: ['a', 'b', 'c'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res = ['a', 'b', 'd'];

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual([
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
    ]);
});

test('entry nested object test', async () => {
    let obj = { a: { b: { c: { d: { e: [1, 2, 4, 5] } } } }, c: ['q', 'w', 'e', 'r'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res: any[] = ['a', 'c'];

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    let res: any[] = ['0', '1', '2', '3', '4', '5', '6', '7', '8'];

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    let childRes: any[] = ['a', 'k'];

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        childRes,
    );

    fep.setArguments(MapUtil.of('source', parent));

    expect((await objKeys.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        childRes,
    );
});

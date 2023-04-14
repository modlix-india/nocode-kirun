import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
} from '../../../../../src';
import { ObjectEntries } from '../../../../../src/engine/function/system/object/ObjectEntries';

const objEnt: ObjectEntries = new ObjectEntries();

test('entry test 1', async () => {
    let obj = { a: 1, b: 2, d: ['a', 'b', 'c'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res = [
        ['a', 1],
        ['b', 2],
        ['d', ['a', 'b', 'c']],
    ];

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual([
        ['0', 's'],
        ['1', 'u'],
        ['2', 'r'],
        ['3', 'e'],
        ['4', 'n'],
        ['5', 'd'],
        ['6', 'h'],
        ['7', 'a'],
        ['8', 'r'],
    ]);
});

test('entry nested object test', async () => {
    let obj = { a: { b: { c: { d: { e: [1, 2, 4, 5] } } } }, c: ['q', 'w', 'e', 'r'] };

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of('source', obj));

    let res: any[] = [
        ['a', { b: { c: { d: { e: [1, 2, 4, 5] } } } }],
        ['c', ['q', 'w', 'e', 'r']],
    ];

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    let res: any[] = [
        ['0', 1],
        ['1', 2],
        ['2', 3],
        ['3', 4],
        ['4', 1],
        ['5', 12],
        ['6', 3],
        ['7', 4],
        ['8', 5],
    ];

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
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

    let childRes: any[] = [
        ['a', 'overridden'],
        ['k', ' so only child objects are returned from child '],
    ];

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        childRes,
    );

    let parentRes: any[] = [
        ['a', { b: 'c' }],
        ['k', { b: 'c' }],
    ];

    fep.setArguments(MapUtil.of('source', parent));

    expect((await objEnt.execute(fep)).allResults()[0]?.getResult()?.get('value')).toStrictEqual(
        parentRes,
    );
});

import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
} from '../../../../../src';

import { ObjectPutValue } from '../../../../../src/engine/function/system/object/ObjectPutValue';

const objput: ObjectPutValue = new ObjectPutValue();

test('simple test', async () => {

    let obj= {a: 1, b: 2, d: ['a', 'b', 'c'] };
    let obj1={a: 1, b: 2, d: ['a', 'b', 'c'],"newOne":"100" };
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

const map =      new  Map<string, any> ([
        ['source', obj],
['key', 'newOne'],
[ 'value' , '100']
      ]);

    fep.setArguments(map);
    
    const result = await objput.execute(fep);
    expect((await objput.execute(fep)).allResults()[0].getResult().get('value')).toEqual(obj1);
    

});


test('test2', async () => {

    let obj= {a: 1, b: 2, d: ['a', 'b', 'c'] };
    let obj1={a: 1, b: 2, d: '1',};
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

const map = new  Map<string, any> ([
        ['source', obj],
['key', 'd'],
[ 'value' , '1'],
['overwrite',true],
['deleteKeyOnNull',false]
      ]);

    fep.setArguments(map);
    
    const result = await objput.execute(fep);
    expect((await objput.execute(fep)).allResults()[0].getResult().get('value')).toEqual(obj1);
    

});


test('test2', async () => {

    let obj= {a: 1, b: 2, d: ['a', 'b', 'c'] };
    let obj1={a: 1, b: 2,};
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

const map = new  Map<string, any> ([
        ['source', obj],
['key', 'd'],
[ 'value' , null],
['overwrite',true],
['deleteKeyOnNull',true]
      ]);

    fep.setArguments(map);
    
    const result = await objput.execute(fep);
    expect((await objput.execute(fep)).allResults()[0].getResult().get('value')).toEqual(obj1);
    

});


test('test2', async () => {

    let obj= {name: "kirun", "addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]} ;
    let obj1={name: "kirun", "addresses":{"city":"Bangalore","state":"Karnataka","country":"India"},"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]} ;
   
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

const map = new  Map<string, any> ([
        ['source', obj],
['key', 'addresses'],
[ 'value' ,{"city":"Bangalore","state":"Karnataka","country":"India"} ],
['overwrite',true],
['deleteKeyOnNull',true]
      ]);

    fep.setArguments(map);
    
    const result = await objput.execute(fep);
    expect((await objput.execute(fep)).allResults()[0].getResult().get('value')).toEqual(obj1);
    

});


test('test3', async () => {

    let obj= {name: "kirun", "addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]} ;
    let obj1={name: "kirun", "addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":989898,"plain":[1,2,3,4]}
   
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

const map = new  Map<string, any> ([
        ['source', obj],
['key', 'phone'],
[ 'value' ,989898],
['overwrite',true],
['deleteKeyOnNull',true]
      ]);

    fep.setArguments(map);
    
    const result = await objput.execute(fep);
    expect((await objput.execute(fep)).allResults()[0].getResult().get('value')).toEqual(obj1);
    

});
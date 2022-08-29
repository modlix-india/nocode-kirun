import { Reverse } from '../../../../../src/engine/function/system/string/Reverse';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const reve: Reverse = new Reverse();

test('reverse test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(MapUtil.of('value', ' mr"ofta"lp edoc on a si sihT'));

    let reveresed: string = 'This is a no code pl"atfo"rm ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe(reveresed);
});

test('reverse test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(MapUtil.of('value', ' '));

    let reveresed: string = ' ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe(reveresed);
});

// test('reverse test3', async () => {
//     let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

//     fep.setArguments(MapUtil.of('value', null));

//     let reveresed: string = ' ';

//     expect((await reve.execute(fep)).allResults()[0].getResult().get('value')).toBe(reveresed);
// });

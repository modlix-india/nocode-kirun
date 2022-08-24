import { PostPad } from '../../../../../src/engine/function/system/string/PostPad';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapEntry, MapUtil } from '../../../../../src/engine/util/MapUtil';

const reve: PostPad = new PostPad();

test('postpad test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            ['string', ' THIScompatY IS A NOcoDE plATFNORM'],
            ['postpadString', 'hiran'],
            ['length', 12],
        ]),
    );

    let padded: string = ' THIScompatY IS A NOcoDE plATFNORMhiranhiranhi';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(padded);
});

test('postpad test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            ['string', ' THIScompatY IS A NOcoDE plATFNORM'],
            ['postpadString', ' h '],
            ['length', 15],
        ]),
    );

    let reveresed: string = ' THIScompatY IS A NOcoDE plATFNORM h  h  h  h  h ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(reveresed);
});

test('postpad test3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            ['string', ' THIScompatY IS A NOcoDE plATFNORM'],
            ['postpadString', ' surendhar '],
            ['length', 100],
        ]),
    );

    let reveresed: string =
        ' THIScompatY IS A NOcoDE plATFNORM surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  ';

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(reveresed);
});

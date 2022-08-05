import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const repo = new StringFunctionRepository();

test('StringFunctionRepository - Trim', () => {
    let fun = repo.find(Namespaces.STRING, 'Trim');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(MapUtil.of(AbstractStringFunction.PARAMETER_STRING_NAME, ' Kiran '));
    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe('Kiran');
});

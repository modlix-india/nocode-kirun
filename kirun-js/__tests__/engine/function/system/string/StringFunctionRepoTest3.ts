import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const stringRepo = new StringFunctionRepository();

test('StringRepo3 - EqualsIgnoreCase', () => {
    let fun = stringRepo.find(Namespaces.STRING, 'EqualsIgnoreCase');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    if (!fun) {
        throw new Error('Function not available');
    }

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' THIS IS A NOcoDE plATFORM		',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            ' THIS IS A NOCODE PLATFORM		',
        ),
    );
    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBeTruthy();

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '    20934 123 123 245-0 34" 3434 " 123',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            '  w20934 123 123 245-0 34" 3434 " 123   ',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '			no code  Kirun  PLATform		',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            '			NO CODE  KIRUN  PLATFORM	',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

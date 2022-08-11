import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const stringRepo = new StringFunctionRepository();

test('StringRepo - contains', () => {
    let fun = stringRepo.find(Namespaces.STRING, 'Contains');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '			no code  Kirun  PLATform		',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            'no code',
        ),
    );
    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '   ',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            ' ',
        ),
    );
    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '{20934 123 1[[23 245-0 34\\\\\\" 3434 \\\\\\" 123]]}',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            '4 123 1[[23 245-0 34',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '{20934 123 1[[23 245-0 34\\\\\\" 3434 \\\\\\" 123]]}',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            '2093(.*)',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

test('string function repo 2', () => {
    let fun = stringRepo.find(Namespaces.STRING, 'EndsWith');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();
    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '			no code  Kirun  PLATform		',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            'PLATform		',
        ),
    );
    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            'this is a new job\t',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            'job\t',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '{20934 123 1[[23 245-0 34\\\\\\" 3434 \\\\\\" 123]]}',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            '" 123]]}',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '{20934 123 1[[23 245-0 34\\\\\\" 3434 \\\\\\" 123]]}',
            AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,
            ']]20934}',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

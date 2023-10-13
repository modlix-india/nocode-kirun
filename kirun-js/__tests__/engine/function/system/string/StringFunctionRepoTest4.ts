import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const repo = new StringFunctionRepository();

test('StringFunctionRepository - Replace', async () => {
    let fun = await repo.find(Namespaces.STRING, 'Replace');

    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            '  new elemenet  ',
            AbstractStringFunction.PARAMETER_SECOND_STRING_NAME,
            ' ',
            AbstractStringFunction.PARAMETER_THIRD_STRING_NAME,
            '',
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe('newelemenet');

    fep.setArguments(
        MapUtil.of(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            'thereisnospace',
            AbstractStringFunction.PARAMETER_SECOND_STRING_NAME,
            '   ',
            AbstractStringFunction.PARAMETER_THIRD_STRING_NAME,
            '  ',
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe('thereisnospace');
});

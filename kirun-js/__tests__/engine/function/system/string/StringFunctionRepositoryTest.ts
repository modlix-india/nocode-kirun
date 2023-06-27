import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
import { Namespaces } from '../../../../../src/engine/namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

const repo = new StringFunctionRepository();

test('StringFunctionRepository - Trim', async () => {
    let fun = await repo.find(Namespaces.STRING, 'Trim');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );
    fep.setArguments(MapUtil.of(AbstractStringFunction.PARAMETER_STRING_NAME, ' Kiran '));
    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe('Kiran');
});

test('StringFunctionRepo -Repeat', async () => {
    let fun = await repo.find(Namespaces.STRING, 'Repeat');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' surendhar ',
            AbstractStringFunction.PARAMETER_INDEX_NAME,
            3,
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' surendhar  surendhar  surendhar ');
});

test('StringFunctionRepo -Lowercase', async () => {
    let fun = await repo.find(Namespaces.STRING, 'LowerCase');
    if (!fun) {
        throw new Error('Function not available');
    }

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' SURENDHAR ',
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' surendhar ');
});

test('StringFunctionRepo -UpperCase', async () => {
    let fun = await repo.find(Namespaces.STRING, 'UpperCase');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' surendhar ',
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' SURENDHAR ');
});

test('StringFunctionRepo -Blank1', async () => {
    let fun = await repo.find(Namespaces.STRING, 'IsBlank');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(MapUtil.of<string, string>(AbstractStringFunction.PARAMETER_STRING_NAME, ''));

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);
});

test('StringFunctionRepo -Blank2', async () => {
    let fun = await repo.find(Namespaces.STRING, 'IsBlank');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' this is a string',
        ),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

test('StringFunctionRepo -Empty1', async () => {
    let fun = await repo.find(Namespaces.STRING, 'IsEmpty');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(AbstractStringFunction.PARAMETER_STRING_NAME, ''),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);
});

test('StringFunctionRepo -Empty2', async () => {
    let fun = await repo.find(Namespaces.STRING, 'IsEmpty');
    if (!fun) {
        throw new Error('Function not available');
    }
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        MapUtil.of<string, string | number>(AbstractStringFunction.PARAMETER_STRING_NAME, ' '),
    );

    expect(
        (await fun.execute(fep))
            .allResults()[0]
            .getResult()
            .get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

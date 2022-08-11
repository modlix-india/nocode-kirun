import { AbstractStringFunction } from '../../../../../src/engine/function/system/string/AbstractStringFunction';
import { StringFunctionRepository } from '../../../../../src/engine/function/system/string/StringFunctionRepository';
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

test('StringFunctionRepo -Repeat', () => {
    let fun = repo.find(Namespaces.STRING, 'Repeat');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' surendhar ',
            AbstractStringFunction.PARAMETER_INDEX_NAME,
            3,
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' surendhar  surendhar  surendhar ');
});

test('StringFunctionRepo -Lowercase', () => {
    let fun = repo.find(Namespaces.STRING, 'LowerCase');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' SURENDHAR ',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' surendhar ');
});

test('StringFunctionRepo -UpperCase', () => {
    let fun = repo.find(Namespaces.STRING, 'UpperCase');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' surendhar ',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(' SURENDHAR ');
});

test('StringFunctionRepo -Blank1', () => {
    let fun = repo.find(Namespaces.STRING, 'Blank');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(AbstractStringFunction.PARAMETER_STRING_NAME, ''),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);
});

test('StringFunctionRepo -Blank2', () => {
    let fun = repo.find(Namespaces.STRING, 'Blank');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(
            AbstractStringFunction.PARAMETER_STRING_NAME,
            ' this is a string',
        ),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

test('StringFunctionRepo -Empty1', () => {
    let fun = repo.find(Namespaces.STRING, 'Empty');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(AbstractStringFunction.PARAMETER_STRING_NAME, ''),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(true);
});

test('StringFunctionRepo -Empty2', () => {
    let fun = repo.find(Namespaces.STRING, 'Empty');

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        MapUtil.of<string, string | number>(AbstractStringFunction.PARAMETER_STRING_NAME, ' '),
    );

    expect(
        fun.execute(fep).allResults()[0].getResult().get(AbstractStringFunction.EVENT_RESULT_NAME),
    ).toBe(false);
});

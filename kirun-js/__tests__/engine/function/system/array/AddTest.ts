import { AbstractArrayFunction } from '../../../../../src/engine/function/system/array/AbstractArrayFunction';
import { Add } from '../../../../../src/engine/function/system/array/Add';
import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { FunctionOutput } from '../../../../../src/engine/model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

test('Add Test 1', () => {
    let add: Add = new Add();

    let source: any[] = [2, 2, 3, 4, 5];

    let secondSource: any[] = [2, 2, 2, 3, 4, 5];

    let temp1: any[] = [2, 2, 3, 4, 5];

    let temp2: any[] = [2, 2, 2, 3, 4, 5];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Add.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Add.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    add.execute(fep);

    expect(source).toStrictEqual(temp1);
});

test('Add test 2', () => {
    let add: Add = new Add();

    let source: any[] = ['nocode', 'platform'];

    let secondSource: any[] = [];

    let temp1: any[] = ['nocode', 'platform'];

    let temp2: any[] = [];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Add.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Add.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    add.execute(fep);

    expect(source).toStrictEqual(temp1);
});

test('Add test 3', () => {
    let add: Add = new Add();

    let source: any[] = [];

    let secondSource: any[] = [];

    let temp1: any[] = [];

    let temp2: any[] = [];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Add.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Add.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    add.execute(fep);

    expect(source).toStrictEqual(temp1);
});

test('Add test 4', () => {
    let add: Add = new Add();

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Add.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
                [Add.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    expect(() => add.execute(fep)).toThrow('');
});

test('Add test 5', () => {
    let add: Add = new Add();

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Add.PARAMETER_ARRAY_SOURCE.getParameterName(), secondSource],
                [Add.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    expect(() => add.execute(fep)).toThrow(
        'Value undefined is not of valid type(s)\nExpected an array but found null',
    );
    // expect(() => add.execute(fep)).toThrowError(
    //     new SchemaValidationException(Schema.ofString("source"),'Value undefined is not of valid type(s)\nExpected an array but found null',       );
});

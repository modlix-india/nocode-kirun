import { Concatenate } from '../../../../../src/engine/function/system/array/Concatenate';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';

test('Concatenate Test 1', async () => {
    let add: Concatenate = new Concatenate();

    let source: any[] = [2, 2, 3, 4, 5];

    let secondSource: any[] = [2, 2, 2, 3, 4, 5];

    let temp1: any[] = [2, 2, 3, 4, 5];

    let temp2: any[] = [2, 2, 2, 3, 4, 5];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Concatenate.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Concatenate.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    expect((await add.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(temp1);
});

test('Concatenate test 2', async () => {
    let add: Concatenate = new Concatenate();

    let source: any[] = ['nocode', 'platform'];

    let secondSource: any[] = [];

    let temp1: any[] = ['nocode', 'platform'];

    let temp2: any[] = [];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Concatenate.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Concatenate.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await add.execute(fep);

    expect(source).toStrictEqual(temp1);
});

test('Concatenate test 3', async () => {
    let add: Concatenate = new Concatenate();

    let source: any[] = [];

    let secondSource: any[] = [];

    let temp1: any[] = [];

    let temp2: any[] = [];

    temp1.splice(temp1.length, 0, ...temp2);

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Concatenate.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [Concatenate.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await add.execute(fep);

    expect(source).toStrictEqual(temp1);
});

test('Concatenate test 4', async () => {
    let add: Concatenate = new Concatenate();

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Concatenate.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
                [Concatenate.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(add.execute(fep)).rejects.toThrow('');
});

test('Concatenate test 5', async () => {
    let add: Concatenate = new Concatenate();

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(
            new Map([
                [Concatenate.PARAMETER_ARRAY_SOURCE.getParameterName(), secondSource],
                [Concatenate.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), undefined],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(add.execute(fep)).rejects.toThrow("Error while executing the function System.Array.Concatenate's parameter secondSource with step name 'Unknown Step' with error : Expected an array but found null");
    // await expect(add.execute(fep)).rejects.toThrowError(
    //     new SchemaValidationException(Schema.ofString("source"),'Value undefined is not of valid type(s)\nExpected an array but found null',       );
});

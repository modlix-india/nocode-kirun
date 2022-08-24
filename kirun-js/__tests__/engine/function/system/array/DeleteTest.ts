import { Delete } from '../../../../../src/engine/function/system/array/Delete';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Delete Test 1', async () => {
    let delet: Delete = new Delete();

    let source: any[] = [12, 14, 15, 9];

    let secondSource: any[] = [14, 15];

    let temp: any[] = [12, 9];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), source],
                [Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('Delete Test 2', async () => {
    let delet: Delete = new Delete();

    let source: any[] = ['nocode', 'platform', 14];

    let secondSource: any[] = ['platform'];

    let temp: any[] = ['nocode', 14];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), source],
                [Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await delet.execute(fep);

    expect(source).toStrictEqual(temp);
});

test('Delete Test 3', async () => {
    let delet: Delete = new Delete();

    let source = undefined;

    let secondSource: any[] = ['platform'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), source],
                [Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('Delete Test 3', async () => {
    let delet: Delete = new Delete();

    let source: any[] = ['platform'];

    let secondSource = undefined;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), source],
                [Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

test('Delete Test 4', async () => {
    let delet: Delete = new Delete();

    let source: any[] = ['platform'];

    let secondSource: any[] = [];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), source],
                [Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), secondSource],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(delet.execute(fep)).rejects.toThrow();
});

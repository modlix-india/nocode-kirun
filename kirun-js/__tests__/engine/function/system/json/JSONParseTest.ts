import { JSONParse } from '../../../../../src/engine/function/system/json/JSONParse';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('should return the parsed JSON Object', async () => {
    const jsonParse = new JSONParse();

    const result = await jsonParse.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', '{"name":"John","age":30}']])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual({ name: 'John', age: 30 });
});

test('should return the parsed JSON Array', async () => {
    const jsonParse = new JSONParse();

    const result = await jsonParse.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', '[1,2,3,4,5]']])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual([1, 2, 3, 4, 5]);
});

test('should return null if source is null', async () => {
    const jsonParse = new JSONParse();

    const result = await jsonParse.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', 'null']])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual(null);
});

test('should return null if source is not a valid JSON', async () => {
    const jsonParse = new JSONParse();

    const result = await jsonParse.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', 'invalid JSON']])),
    );

    expect(result.allResults()[0].getResult().get('errorMessage')).toEqual(
        'Unexpected token \'i\', "invalid JSON" is not valid JSON',
    );
});

test('should return null if source is undefined', async () => {
    const jsonParse = new JSONParse();

    const result = await jsonParse.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', '']])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual(null);
});

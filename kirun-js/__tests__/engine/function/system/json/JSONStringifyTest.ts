import { JSONStringify } from '../../../../../src/engine/function/system/json/JSONStringify';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('should return the stringified JSON', async () => {
    const jsonStringify = new JSONStringify();

    const result = await jsonStringify.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', { name: 'John', age: 30 }]])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual('{"name":"John","age":30}');
});

test('should return the stringified if source is undefined', async () => {
    const jsonStringify = new JSONStringify();

    const result = await jsonStringify.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['source', undefined]])),
    );

    expect(result.allResults()[0].getResult().get('value')).toEqual('null');
});

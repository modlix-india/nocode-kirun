import { KIRunSchemaRepository } from "../../../../../src";
import { Frequency } from "../../../../../src/engine/function/system/string/Frequency";
import { KIRunFunctionRepository } from "../../../../../src/engine/repository/KIRunFunctionRepository";
import { FunctionExecutionParameters } from "../../../../../src/engine/runtime/FunctionExecutionParameters";

const reve: Frequency = new Frequency();

test('Frequency test1', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [Frequency.PARAMETER_STRING_NAME, ' wowwowwowwow'],
            [Frequency.PARAMETER_SEARCH_STRING_NAME, 'wow'],
        ]),
    );

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(4);
});


test('Frequency test2', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [Frequency.PARAMETER_STRING_NAME, ' THIS IS A NOcoDE plATFNORM'],
            [Frequency.PARAMETER_SEARCH_STRING_NAME, ''],
        ]),
    );

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(0);
});


test('Frequency test3', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string | number>([
            [Frequency.PARAMETER_STRING_NAME, ' THIS IS A NOcoDE plATFNORM'],
            [Frequency.PARAMETER_SEARCH_STRING_NAME, 'NO'],
        ]),
    );

    expect((await reve.execute(fep)).allResults()[0].getResult().get('result')).toBe(2);
});
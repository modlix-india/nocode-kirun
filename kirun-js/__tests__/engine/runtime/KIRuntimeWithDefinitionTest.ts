import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';
import { EventResult } from '../../../src/engine/model/EventResult';
import { Event } from '../../../src/engine/model/Event';
import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { Parameter } from '../../../src/engine/model/Parameter';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { Create } from '../../../src/engine/function/system/context/Create';
import { SetFunction } from '../../../src/engine/function/system/context/SetFunction';
import { GenerateEvent } from '../../../src/engine/function/system/GenerateEvent';
import { If } from '../../../src/engine/function/system/If';
import { RangeLoop } from '../../../src/engine/function/system/loop/RangeLoop';
import { Statement } from '../../../src/engine/model/Statement';
import { ParameterReference } from '../../../src/engine/model/ParameterReference';
import { Schema } from '../../../src/engine/json/schema/Schema';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { Namespaces } from '../../../src/engine/namespaces/Namespaces';
import { FunctionSignature } from '../../../src/engine/model/FunctionSignature';
import { AbstractFunction } from '../../../src/engine/function/AbstractFunction';
import { FunctionOutput } from '../../../src/engine/model/FunctionOutput';
import { HybridRepository } from '../../../src/engine/HybridRepository';
import { Function } from '../../../src/engine/function/Function';

test('KIRuntime With Definition 1', async () => {
    var def = {
        name: 'getAppData',
        namespace: 'UIApp',
        parameters: {
            a: { parameterName: 'a', schema: { name: 'integer', type: 'Integer' } },
            b: { parameterName: 'b', schema: { name: 'integer', type: 'Integer' } },
            c: { parameterName: 'c', schema: { name: 'integer', type: 'Integer' } },
        },
        events: {
            output: {
                name: 'output',
                parameters: { additionResult: { name: 'additionResult', type: 'Integer' } },
            },
        },
        steps: {
            add: {
                statementName: 'add',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Arguments.a' },
                        two: { key: 'two', type: 'EXPRESSION', expression: '10 + 1' },
                        three: { key: 'three', type: 'EXPRESSION', expression: 'Arguments.c' },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                parameterMap: {
                    eventName: { one: { key: 'one', type: 'VALUE', value: 'output' } },
                    results: {
                        one: {
                            key: 'one',
                            type: 'VALUE',
                            value: {
                                name: 'additionResult',
                                value: { isExpression: true, value: 'Steps.add.output.value' },
                            },
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    let result = await new KIRuntime(fd).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([
                ['a', 7],
                ['b', 11],
                ['c', 13],
            ]),
        ),
    );

    expect(result.allResults()[0].getResult().get('additionResult')).toBe(31);
});

test('KIRuntime With Definition 2', async () => {
    var def = {
        name: 'checkWithNoParamsOrEvents',
        namespace: 'UIApp',
        steps: {
            add: {
                statementName: 'add',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'VALUE', value: 2 },
                        two: { key: 'two', type: 'VALUE', value: 5 },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                dependentStatements: { 'Steps.add.output': true },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    let result = await new KIRuntime(fd).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map()),
    );

    expect(result.allResults()[0].getResult()).toMatchObject({});
});

test('KIRuntime With Definition 3', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            add: {
                statementName: 'add',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'VALUE', value: 'X' },
                        two: { key: 'two', type: 'VALUE', value: 5 },
                    },
                },
            },
            genOutput: {
                statementName: 'genOutput',
                namespace: Namespaces.SYSTEM,
                name: 'GenerateEvent',
                dependentStatements: { 'Steps.add.output': true },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    try {
        await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
    } catch (e: any) {
        expect(e.message).toMatch('error');
    }
});

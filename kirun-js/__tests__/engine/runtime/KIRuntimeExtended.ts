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

test('KIRuntime Extended Definition', async () => {
    var def = {
        name: 'checkExtendedFunction',
        namespace: 'Test',
        parameters: {
            a: { parameterName: 'a', schema: { name: 'INTEGER', type: 'INTEGER' } },
        },
        events: {
            output: {
                name: 'output',
                parameters: { additionResult: { name: 'additionResult', type: 'INTEGER' } },
            },
        },
        steps: {
            add1: {
                statementName: 'add1',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: { key: 'one', type: 'EXPRESSION', expression: 'Arguments.a' },
                        two: { key: 'two', type: 'VALUE', value: 1 },
                    },
                },
            },
            if1: {
                statementName: 'if1',
                namespace: Namespaces.SYSTEM,
                name: 'If',
                parameterMap: {
                    condition: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add1.output.value % 2 = 0',
                        },
                    },
                },
            },
            add2: {
                statementName: 'add2',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add1.output.value',
                        },
                        two: { key: 'two', type: 'VALUE', value: 2 },
                    },
                },
                dependentStatements: {
                    'Steps.if1.true': true,
                },
            },
            add3: {
                statementName: 'add3',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add2.output.value',
                        },
                        two: { key: 'two', type: 'VALUE', value: 2 },
                    },
                },
            },
            add4: {
                statementName: 'add4',
                namespace: Namespaces.MATH,
                name: 'Add',
                parameterMap: {
                    value: {
                        one: {
                            key: 'one',
                            type: 'EXPRESSION',
                            expression: 'Steps.add1.output.value',
                        },
                        two: { key: 'two', type: 'VALUE', value: 1 },
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
                                value: { isExpression: true, value: 'Steps.add4.output.value' },
                            },
                        },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    let result = await new KIRuntime(fd, true).execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([['a', 7]])),
    );

    expect(result.allResults()[0].getResult().get('additionResult')).toBe(9);
});

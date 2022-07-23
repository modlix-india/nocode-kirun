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

test('KIRuntime Test 1', () => {
    let start = new Date().getTime();
    let num: number = 7000;
    let array: number[] = [];
    let a = 0,
        b = 1;
    array.push(a);
    array.push(b);

    for (let i = 2; i < num; i++) {
        array.push(array[i - 2] + array[i - 1]);
    }

    console.log('Normal Logic : ' + (new Date().getTime() - start));

    var create = new Create().getSignature();
    var integerSchema = { name: 'EachElement', type: 'INTEGER' };
    var arrayOfIntegerSchema = {
        name: 'ArrayType',
        type: 'ARRAY',
        defaultValue: new Array(),
        items: integerSchema,
    };
    var createArray = new Statement('createArray')
        .setNamespace(create.getNamespace())
        .setName(create.getName())
        .setParameterMap(
            new Map([
                ['name', [ParameterReference.ofValue('a')]],
                ['schema', [ParameterReference.ofValue(arrayOfIntegerSchema)]],
            ]),
        );

    var rangeLoop = new RangeLoop().getSignature();
    var loop = new Statement('loop')
        .setNamespace(rangeLoop.getNamespace())
        .setName(rangeLoop.getName())
        .setParameterMap(
            new Map([
                ['from', [ParameterReference.ofValue(0)]],
                ['to', [ParameterReference.ofExpression('Arguments.Count')]],
            ]),
        )
        .setDependentStatements(['Steps.createArray.output']);

    var resultObj = { name: 'result', value: { isExpression: true, value: 'Context.a' } };

    var generate = new GenerateEvent().getSignature();
    var outputGenerate = new Statement('outputStep')
        .setNamespace(generate.getNamespace())
        .setName(generate.getName())
        .setParameterMap(
            new Map([
                ['eventName', [ParameterReference.ofValue('output')]],
                ['results', [ParameterReference.ofValue(resultObj)]],
            ]),
        )
        .setDependentStatements(['Steps.loop.output']);

    var ifFunction = new If().getSignature();
    var ifStep = new Statement('if')
        .setNamespace(ifFunction.getNamespace())
        .setName(ifFunction.getName())
        .setParameterMap(
            new Map([
                [
                    'condition',
                    [
                        ParameterReference.ofExpression(
                            'Steps.loop.iteration.index = 0 or Steps.loop.iteration.index = 1',
                        ),
                    ],
                ],
            ]),
        );

    var set = new SetFunction().getSignature();
    var set1 = new Statement('setOnTrue')
        .setNamespace(set.getNamespace())
        .setName(set.getName())
        .setParameterMap(
            new Map([
                ['name', [ParameterReference.ofValue('Context.a[Steps.loop.iteration.index]')]],
                ['value', [ParameterReference.ofExpression('Steps.loop.iteration.index')]],
            ]),
        )
        .setDependentStatements(['Steps.if.true']);
    var set2 = new Statement('setOnFalse')
        .setNamespace(set.getNamespace())
        .setName(set.getName())
        .setParameterMap(
            new Map([
                ['name', [ParameterReference.ofValue('Context.a[Steps.loop.iteration.index]')]],
                [
                    'value',
                    [
                        ParameterReference.ofExpression(
                            'Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]',
                        ),
                    ],
                ],
            ]),
        )
        .setDependentStatements(['Steps.if.false']);

    start = new Date().getTime();
    let out: EventResult[] = new KIRuntime(
        new FunctionDefinition()
            .setSteps(
                new Map([
                    Statement.ofEntry(createArray),
                    Statement.ofEntry(loop),
                    Statement.ofEntry(outputGenerate),
                    Statement.ofEntry(ifStep),
                    Statement.ofEntry(set1),
                    Statement.ofEntry(set2),
                ]),
            )
            .setNamespace('Test')
            .setName('Fibonacci')
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        new Map([['result', Schema.ofArray('result', Schema.ofInteger('result'))]]),
                    ),
                ]),
            )
            .setParameters(
                new Map([
                    [
                        'Count',
                        new Parameter()
                            .setParameterName('Count')
                            .setSchema(Schema.ofInteger('Count')),
                    ],
                ]),
            ) as FunctionDefinition,
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .execute(new FunctionExecutionParameters().setArguments(new Map([['Count', num]])))
        .allResults();
    console.log('KIRunt Logic : ' + (new Date().getTime() - start));
    expect(out[0].getResult().get('result')).toStrictEqual(array);
});

test('KIRuntime Test 2', () => {
    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.first.output.value' };

    let resultObj = { name: 'result', value: expression };

    let out: EventResult[] = new KIRuntime(
        (
            new FunctionDefinition()
                .setNamespace('Test')
                .setName('SingleCall')
                .setParameters(
                    new Map([
                        [
                            'Value',
                            new Parameter()
                                .setParameterName('Value')
                                .setSchema(Schema.ofInteger('Value')),
                        ],
                    ]),
                ) as FunctionDefinition
        ).setSteps(
            new Map([
                Statement.ofEntry(
                    new Statement('first')
                        .setNamespace(Namespaces.MATH)
                        .setName('Absolute')
                        .setParameterMap(
                            new Map([
                                ['value', [ParameterReference.ofExpression('Arguments.Value')]],
                            ]),
                        ),
                ),
                Statement.ofEntry(
                    new Statement('second')
                        .setNamespace(genEvent.getNamespace())
                        .setName(genEvent.getName())
                        .setParameterMap(
                            new Map([
                                ['eventName', [ParameterReference.ofValue('output')]],
                                ['results', [ParameterReference.ofValue(resultObj)]],
                            ]),
                        ),
                ),
            ]),
        ),
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .execute(new FunctionExecutionParameters().setArguments(new Map([['Value', -10]])))
        .allResults();

    expect(out[0].getResult().get('result')).toBe(10);
});
test('KIRuntime Test 3', () => {
    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.first.output.value' };

    let resultObj = { name: 'result', value: expression };

    let out: EventResult[] = new KIRuntime(
        (
            new FunctionDefinition()
                .setNamespace('Test')
                .setName('SingleCall')
                .setParameters(
                    new Map([
                        [
                            'Value',
                            new Parameter()
                                .setParameterName('Value')
                                .setSchema(Schema.ofInteger('Value')),
                        ],
                    ]),
                ) as FunctionDefinition
        ).setSteps(
            new Map([
                Statement.ofEntry(
                    new Statement('first')
                        .setNamespace(Namespaces.MATH)
                        .setName('CubeRoot')
                        .setParameterMap(
                            new Map([
                                ['value', [ParameterReference.ofExpression('Arguments.Value')]],
                            ]),
                        ),
                ),
                Statement.ofEntry(
                    new Statement('second')
                        .setNamespace(genEvent.getNamespace())
                        .setName(genEvent.getName())
                        .setParameterMap(
                            new Map([
                                ['eventName', [ParameterReference.ofValue('output')]],
                                ['results', [ParameterReference.ofValue(resultObj)]],
                            ]),
                        ),
                ),
            ]),
        ),
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .execute(new FunctionExecutionParameters().setArguments(new Map([['Value', 27]])))
        .allResults();

    expect(out[0].getResult().get('result')).toBe(3);
});

test('KIRuntime Test 4', () => {
    let start = new Date().getTime();
    const num = 7;
    let array = new Array(num);
    let a = 0,
        b = 1;
    array[0] = a;
    array[1] = b;

    for (let i = 2; i < num; i++) {
        array[i] = array[i - 2] + array[i - 1];
    }

    console.log('Normal Logic : ' + (new Date().getTime() - start));

    var fibFunctionSignature = new FunctionSignature()
        .setName('FibFunction')
        .setNamespace('FibSpace')
        .setParameters(
            new Map([
                [
                    'value',
                    new Parameter().setParameterName('value').setSchema(Schema.ofInteger('value')),
                ],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    new Map([['value', Schema.ofArray('value', Schema.ofInteger('value'))]]),
                ),
            ]),
        );
    class FibFunction extends AbstractFunction {
        public getSignature(): FunctionSignature {
            return fibFunctionSignature;
        }

        protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
            let count = context.getArguments().get('value');
            let a = new Array(count);
            for (let i = 0; i < count; i++) a[i] = i < 2 ? i : a[i - 1] + a[i - 2];
            return new FunctionOutput([EventResult.outputOf(new Map([['value', a]]))]);
        }
    }

    var fibFunction = new FibFunction();

    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.fib.output.value' };
    let resultObj = { name: 'result', value: expression };

    var hybrid = new HybridRepository(new KIRunFunctionRepository(), {
        find(namespace, name): Function {
            return fibFunction;
        },
    });

    start = new Date().getTime();
    let out: EventResult[] = new KIRuntime(
        (
            new FunctionDefinition()
                .setNamespace('Test')
                .setName('CustomFunction')
                .setParameters(
                    new Map([
                        [
                            'Value',
                            new Parameter()
                                .setParameterName('Value')
                                .setSchema(Schema.ofInteger('Value')),
                        ],
                    ]),
                ) as FunctionDefinition
        ).setSteps(
            new Map([
                Statement.ofEntry(
                    new Statement('fib')
                        .setNamespace(fibFunctionSignature.getNamespace())
                        .setName('asdf')
                        .setParameterMap(
                            new Map([
                                ['value', [ParameterReference.ofExpression('Arguments.Value')]],
                            ]),
                        ),
                ),
                Statement.ofEntry(
                    new Statement('fiboutput')
                        .setNamespace(genEvent.getNamespace())
                        .setName(genEvent.getName())
                        .setParameterMap(
                            new Map([
                                ['eventName', [ParameterReference.ofValue('output')]],
                                ['results', [ParameterReference.ofValue(resultObj)]],
                            ]),
                        ),
                ),
            ]),
        ),
        hybrid,
        new KIRunSchemaRepository(),
    )
        .execute(new FunctionExecutionParameters().setArguments(new Map([['Value', num]])))
        .allResults();
    console.log('KIRun Logic : ' + (new Date().getTime() - start));
    expect(out[0].getResult().get('result')).toStrictEqual(array);
});

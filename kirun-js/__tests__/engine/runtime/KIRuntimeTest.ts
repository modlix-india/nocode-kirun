import { KIRuntime, PerfTimer } from '../../../src/engine/runtime/KIRuntime';
import { ExprPerfTimer } from '../../../src/engine/runtime/expression/ExpressionEvaluator';
import { TVEPerfTimer } from '../../../src/engine/runtime/expression/tokenextractor/TokenValueExtractor';
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
import { MapUtil } from '../../../src';

test('KIRuntime Test 1', async () => {
    // Enable performance timing
    PerfTimer.enable();
    ExprPerfTimer.enable();
    TVEPerfTimer.enable();
    PerfTimer.reset();
    ExprPerfTimer.reset();
    TVEPerfTimer.reset();
    
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

    //console.log('Normal Logic : ' + (new Date().getTime() - start));

    var create = new Create().getSignature();
    var integerSchema = { name: 'EachElement', type: 'INTEGER' };
    var arrayOfIntegerSchema = {
        name: 'ArrayType',
        type: 'ARRAY',
        defaultValue: new Array(),
        items: integerSchema,
    };
    var createArray = new Statement(
        'createArray',
        create.getNamespace(),
        create.getName(),
    ).setParameterMap(
        new Map([
            ['name', MapUtil.ofEntriesArray(ParameterReference.ofValue('a'))],
            ['schema', MapUtil.ofEntriesArray(ParameterReference.ofValue(arrayOfIntegerSchema))],
        ]),
    );

    var rangeLoop = new RangeLoop().getSignature();
    var loop = new Statement('loop', rangeLoop.getNamespace(), rangeLoop.getName())
        .setParameterMap(
            new Map([
                ['from', MapUtil.ofEntriesArray(ParameterReference.ofValue(0))],
                ['to', MapUtil.ofEntriesArray(ParameterReference.ofExpression('Arguments.Count'))],
            ]),
        )
        .setDependentStatements(MapUtil.of('Steps.createArray.output', true));

    var resultObj = { name: 'result', value: { isExpression: true, value: 'Context.a' } };

    var generate = new GenerateEvent().getSignature();
    var outputGenerate = new Statement('outputStep', generate.getNamespace(), generate.getName())
        .setParameterMap(
            new Map([
                ['eventName', MapUtil.ofEntriesArray(ParameterReference.ofValue('output'))],
                ['results', MapUtil.ofEntriesArray(ParameterReference.ofValue(resultObj))],
            ]),
        )
        .setDependentStatements(MapUtil.of('Steps.loop.output', true));

    var ifFunction = new If().getSignature();
    var ifStep = new Statement(
        'if',
        ifFunction.getNamespace(),
        ifFunction.getName(),
    ).setParameterMap(
        new Map([
            [
                'condition',
                MapUtil.ofEntriesArray(
                    ParameterReference.ofExpression(
                        'Steps.loop.iteration.index = 0 or Steps.loop.iteration.index = 1',
                    ),
                ),
            ],
        ]),
    );

    var set = new SetFunction().getSignature();
    var set1 = new Statement('setOnTrue', set.getNamespace(), set.getName())
        .setParameterMap(
            new Map([
                [
                    'name',
                    MapUtil.ofEntriesArray(
                        ParameterReference.ofValue('Context.a[Steps.loop.iteration.index]'),
                    ),
                ],
                [
                    'value',
                    MapUtil.ofEntriesArray(
                        ParameterReference.ofExpression('Steps.loop.iteration.index'),
                    ),
                ],
            ]),
        )
        .setDependentStatements(MapUtil.of('Steps.if.true', true));
    var set2 = new Statement('setOnFalse', set.getNamespace(), set.getName())
        .setParameterMap(
            new Map([
                [
                    'name',
                    MapUtil.ofEntriesArray(
                        ParameterReference.ofValue('Context.a[Steps.loop.iteration.index]'),
                    ),
                ],
                [
                    'value',
                    MapUtil.ofEntriesArray(
                        ParameterReference.ofExpression(
                            'Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]',
                        ),
                    ),
                ],
            ]),
        )
        .setDependentStatements(MapUtil.of('Steps.if.false', true));

    start = new Date().getTime();
    let out: EventResult[] = (
        await new KIRuntime(
            new FunctionDefinition('Fibonacci')
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
                .setEvents(
                    new Map([
                        Event.outputEventMapEntry(
                            new Map([
                                ['result', Schema.ofArray('result', Schema.ofInteger('result'))],
                            ]),
                        ),
                    ]),
                )
                .setParameters(
                    new Map([['Count', new Parameter('Count', Schema.ofInteger('Count'))]]),
                ) as FunctionDefinition,
        ).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['Count', num]])),
        )
    ).allResults();
    console.log('KIRuntime Logic : ' + (new Date().getTime() - start) + 'ms');
    
    // Print performance reports
    PerfTimer.report();
    ExprPerfTimer.report();
    TVEPerfTimer.report();
    
    // Disable timing
    PerfTimer.disable();
    ExprPerfTimer.disable();
    TVEPerfTimer.disable();
    
    expect(out[0].getResult().get('result')).toStrictEqual(array);
});

test('KIRuntime Test 2', async () => {
    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.first.output.value' };

    let resultObj = { name: 'result', value: expression };

    let out: EventResult[] = (
        await new KIRuntime(
            (
                new FunctionDefinition('SingleCall')
                    .setNamespace('Test')
                    .setParameters(
                        new Map([['Value', new Parameter('Value', Schema.ofInteger('Value'))]]),
                    ) as FunctionDefinition
            ).setSteps(
                new Map([
                    Statement.ofEntry(
                        new Statement('first', Namespaces.MATH, 'Absolute').setParameterMap(
                            new Map([
                                [
                                    'value',
                                    MapUtil.ofEntriesArray(
                                        ParameterReference.ofExpression('Arguments.Value'),
                                    ),
                                ],
                            ]),
                        ),
                    ),
                    Statement.ofEntry(
                        new Statement(
                            'second',
                            genEvent.getNamespace(),
                            genEvent.getName(),
                        ).setParameterMap(
                            new Map([
                                [
                                    'eventName',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue('output')),
                                ],
                                [
                                    'results',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue(resultObj)),
                                ],
                            ]),
                        ),
                    ),
                ]),
            ),
        ).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['Value', -10]])),
        )
    ).allResults();

    expect(out[0].getResult().get('result')).toBe(10);
});
test('KIRuntime Test 3', async () => {
    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.first.output.value' };

    let resultObj = { name: 'result', value: expression };

    let out: EventResult[] = (
        await new KIRuntime(
            (
                new FunctionDefinition('SingleCall')
                    .setNamespace('Test')
                    .setParameters(
                        new Map([['Value', new Parameter('Value', Schema.ofInteger('Value'))]]),
                    ) as FunctionDefinition
            ).setSteps(
                new Map([
                    Statement.ofEntry(
                        new Statement('first', Namespaces.MATH, 'CubeRoot').setParameterMap(
                            new Map([
                                [
                                    'value',
                                    MapUtil.ofEntriesArray(
                                        ParameterReference.ofExpression('Arguments.Value'),
                                    ),
                                ],
                            ]),
                        ),
                    ),
                    Statement.ofEntry(
                        new Statement(
                            'second',
                            genEvent.getNamespace(),
                            genEvent.getName(),
                        ).setParameterMap(
                            new Map([
                                [
                                    'eventName',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue('output')),
                                ],
                                [
                                    'results',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue(resultObj)),
                                ],
                            ]),
                        ),
                    ),
                ]),
            ),
        ).execute(
            new FunctionExecutionParameters(
                new KIRunFunctionRepository(),
                new KIRunSchemaRepository(),
            ).setArguments(new Map([['Value', 27]])),
        )
    ).allResults();

    expect(out[0].getResult().get('result')).toBe(3);
});

test('KIRuntime Test 4', async () => {
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

    //console.log('Normal Logic : ' + (new Date().getTime() - start));

    var fibFunctionSignature = new FunctionSignature('FibFunction')
        .setNamespace('FibSpace')
        .setParameters(new Map([['value', new Parameter('value', Schema.ofInteger('value'))]]))
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

        protected async internalExecute(
            context: FunctionExecutionParameters,
        ): Promise<FunctionOutput> {
            let count = context.getArguments()?.get('value');
            let a = new Array(count);
            for (let i = 0; i < count; i++) a[i] = i < 2 ? i : a[i - 1] + a[i - 2];
            return new FunctionOutput([EventResult.outputOf(new Map([['value', a]]))]);
        }
    }

    var fibFunction = new FibFunction();

    var genEvent = new GenerateEvent().getSignature();

    var expression = { isExpression: true, value: 'Steps.fib.output.value' };
    let resultObj = { name: 'result', value: expression };

    class X {
        async find(namespace: string, name: string): Promise<Function> {
            return fibFunction;
        }

        async filter(name: string): Promise<string[]> {
            return [fibFunction.getSignature().getFullName()].filter((e) =>
                e.toLowerCase().includes(name.toLowerCase()),
            );
        }
    }

    var hybrid = new HybridRepository(new KIRunFunctionRepository(), new X());

    start = new Date().getTime();
    let out: EventResult[] = (
        await new KIRuntime(
            (
                new FunctionDefinition('CustomFunction')
                    .setNamespace('Test')
                    .setParameters(
                        new Map([['Value', new Parameter('Value', Schema.ofInteger('Value'))]]),
                    ) as FunctionDefinition
            ).setSteps(
                new Map([
                    Statement.ofEntry(
                        new Statement(
                            'fib',
                            fibFunctionSignature.getNamespace(),
                            'asdf',
                        ).setParameterMap(
                            new Map([
                                [
                                    'value',
                                    MapUtil.ofEntriesArray(
                                        ParameterReference.ofExpression('Arguments.Value'),
                                    ),
                                ],
                            ]),
                        ),
                    ),
                    Statement.ofEntry(
                        new Statement(
                            'fiboutput',
                            genEvent.getNamespace(),
                            genEvent.getName(),
                        ).setParameterMap(
                            new Map([
                                [
                                    'eventName',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue('output')),
                                ],
                                [
                                    'results',
                                    MapUtil.ofEntriesArray(ParameterReference.ofValue(resultObj)),
                                ],
                            ]),
                        ),
                    ),
                ]),
            ),
        ).execute(
            new FunctionExecutionParameters(hybrid, new KIRunSchemaRepository()).setArguments(
                new Map([['Value', num]]),
            ),
        )
    ).allResults();
    //console.log('KIRun Logic : ' + (new Date().getTime() - start));
    expect(out[0].getResult().get('result')).toStrictEqual(array);
});

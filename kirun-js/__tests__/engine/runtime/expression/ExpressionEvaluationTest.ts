import {
    ArgumentsTokenValueExtractor,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    TokenValueExtractor,
} from '../../../../src';
import { Schema } from '../../../../src/engine/json/schema/Schema';
import { ContextElement } from '../../../../src/engine/runtime/ContextElement';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';
import { FunctionExecutionParameters } from '../../../../src/engine/runtime/FunctionExecutionParameters';

test('Expression Test', () => {
    let phone = { phone1: '1234', phone2: '5678', phone3: '5678' };

    let address = {
        line1: 'Flat 202, PVR Estates',
        line2: 'Nagvara',
        city: 'Benguluru',
        pin: '560048',
        phone: phone,
    };

    let arr = [10, 20, 30];

    let obj = {
        studentName: 'Kumar',
        math: 20,
        isStudent: true,
        address: address,
        array: arr,
        num: 1,
    };

    let inMap: Map<string, any> = new Map();
    inMap.set('name', 'Kiran');
    inMap.set('obj', obj);

    let output: Map<string, Map<string, Map<string, any>>> = new Map([
        ['step1', new Map([['output', inMap]])],
        ['loop', new Map([['iteration', new Map([['index', 2]])]])],
    ]);

    let parameters: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map())
        .setContext(
            new Map([
                [
                    'a',
                    new ContextElement(
                        Schema.ofArray('numbers', Schema.ofNumber('number')),
                        [1, 2],
                    ),
                ],
            ]),
        )
        .setSteps(output);

    expect(
        new ExpressionEvaluator(
            'Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(3);

    expect(new ExpressionEvaluator('3 + 7').evaluate(parameters.getValuesMap())).toBe(10);
    expect(new ExpressionEvaluator('"asdf"+333').evaluate(parameters.getValuesMap())).toBe(
        'asdf333',
    );
    expect(new ExpressionEvaluator('34 >> 2 = 8 ').evaluate(parameters.getValuesMap())).toBe(true);
    expect(new ExpressionEvaluator('10*11+12*13*14/7').evaluate(parameters.getValuesMap())).toBe(
        422,
    );

    expect(
        new ExpressionEvaluator('Steps.step1.output.name1').evaluate(parameters.getValuesMap()),
    ).toBeUndefined();

    expect(
        new ExpressionEvaluator('"Kiran" = Steps.step1.output.name ').evaluate(
            parameters.getValuesMap(),
        ),
    ).toBe(true);

    expect(
        new ExpressionEvaluator('null = Steps.step1.output.name1 ').evaluate(
            parameters.getValuesMap(),
        ),
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj.address.phone.phone2').evaluate(
            parameters.getValuesMap(),
        ),
    ).toBe('5678');

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.address.phone.phone2 = Steps.step1.output.obj.address.phone.phone2 ',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(true);

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 ',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(true);

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(32);

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(60);

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(60);

    expect(
        new ExpressionEvaluator(
            'Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2',
        ).evaluate(parameters.getValuesMap()),
    ).toBe(32);

    expect(new ExpressionEvaluator('2.43*4.22+7.0987').evaluate(parameters.getValuesMap())).toBe(
        17.3533,
    );
});

test('ExpressionEvaluation deep tests', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', { a: 2, b: [true, false], c: { x: 'kiran' } }],
            ['d', { a: 2, b: [true, false], c: { x: 'kiran' } }],
        ]),
    );
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator('Arguments.a = Arugments.b');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.c = Arguments.d');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.e = null');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.e != null');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.e = false');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.c != null');
    expect(ev.evaluate(valuesMap)).toBeTruthy();
});

test('Expression Evaluation nullish coalescing', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['b1', 4],
            ['c', { a: 2, b: [true, false], c: { x: 'kiran' } }],
            ['d', { a: 2, b: [true, false], c: { x: 'kiran' } }],
        ]),
    );
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator('(Arguments.e ?? Arguments.b ?? Arguments.b1) + 4');
    expect(ev.evaluate(valuesMap)).toBe(6);

    ev = new ExpressionEvaluator('(Arguments.e ?? Arguments.b2 ?? Arguments.b1) + 4');
    expect(ev.evaluate(valuesMap)).toBe(8);
});

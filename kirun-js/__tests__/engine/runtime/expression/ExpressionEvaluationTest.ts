import {
    ArgumentsTokenValueExtractor,
    Expression,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    OutputMapTokenValueExtractor,
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

test('Expression Evaluation with Square Access Bracket', () => {
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
    ]);

    let parameters: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    )
        .setArguments(new Map())
        .setContext(new Map())
        .setSteps(output);

    // New test cases for square access bracket using parameters value map
    expect(
        new ExpressionEvaluator('Steps.step1.output.obj.phone.phone2 = Steps.step1.output.obj["phone"]["phone2"]')
            .evaluate(parameters.getValuesMap())
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj["phone"]["phone2"] = Steps.step1.output.obj["phone"]["phone2"]')
            .evaluate(parameters.getValuesMap())
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj["address"].phone["phone2"] != Steps.step1.output.address.obj.phone.phone1')
            .evaluate(parameters.getValuesMap())
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj["address"]["phone"]["phone2"] != Steps.step1.output.address.obj.phone.phone1')
            .evaluate(parameters.getValuesMap())
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj["address"]["phone"]["phone2"] != Steps.step1.output["address"]["phone"]["phone2"]')
            .evaluate(parameters.getValuesMap())
    ).toBe(true);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj.array[Steps.step1.output.obj["num"] + 1] + 2')
            .evaluate(parameters.getValuesMap())
    ).toBe(32);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj.array[Steps.step1.output.obj["num"] + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj["num"] + 1]')
            .evaluate(parameters.getValuesMap())
    ).toBe(60);

    expect(
        new ExpressionEvaluator('Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1]')
            .evaluate(parameters.getValuesMap())
    ).toBe(60);
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

test('Expression Evaluation nesting expression', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['b1', 4],
            ['b2', 4],
            ['c', { a: 2, b: [true, false], c: { x: 'Arguments.b2' } }],
            ['d', 'c'],
        ]),
    );
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator(
        'Arguments.{{Arguments.d}}.a + {{Arguments.{{Arguments.d}}.c.x}}',
    );
    expect(ev.evaluate(valuesMap)).toBe(6);

    ev = new ExpressionEvaluator(
        "'There are {{{{Arguments.{{Arguments.d}}.c.x}}}} boys in the class room...' * Arguments.b",
    );
    expect(ev.evaluate(valuesMap)).toBe(
        'There are 4 boys in the class room...There are 4 boys in the class room...',
    );
});

test('Partial path evaluation', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 1],
            ['b1', 4],
            ['b2', 4],
            [
                'c',
                { a: 0, b: [true, false], c: { x: 'Arguments.b2' }, keys: ['a', 'e', { val: 5 }] },
            ],
            ['d', 'c'],
            [
                'e',
                [
                    { name: 'Kiran', num: 1 },
                    { name: 'Good', num: 2 },
                ],
            ],
        ]),
    );

    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator('Arguments.c.keys[2].val + 3');
    expect(ev.evaluate(valuesMap)).toBe(8);
    ev = new ExpressionEvaluator('(Arguments.f ?? Arguments.e)[1+1-1].num');
    expect(ev.evaluate(valuesMap)).toBe(2);
});

// test('Expression with consecutive negative operators', () => {
//     let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
//         new Map<string, any>([
//             ['a', 'kirun '],
//             ['b', 1],
//             ['b1', 4],
//             ['b2', 4],
//         ]),
//     );

//     let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

//     let ev = new ExpressionEvaluator('Arguments.b - Arguments.b1 - Arguments.b2');
//     expect(ev.evaluate(valuesMap)).toBe(-7);
// });

// test('Expression with multiple coalesce operator', () => {
//     let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
//         new Map<string, any>([
//             ['a', 'kirun '],
//             ['b', 1],
//             ['b1', 4],
//             ['b2', 4],
//         ]),
//     );

//     let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

//     let ev = new ExpressionEvaluator('Arguments.b3 ?? (Arguments.b - 3) ?? Arguments.b5 ?? 4');
//     expect(ev.evaluate(valuesMap)).toBe(-2);

//     ev = new ExpressionEvaluator('Arguments.b3 ?? Arguments.b - 3 ?? Arguments.b5 ?? 4');
//     expect(ev.evaluate(valuesMap)).toBe(-2);
// });

test('Expression with logical operators and all value types including object', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['string', 'kirun '],
            ['stringEmpty', ''],
            ['number', 122.2],
            ['number0', 0],
            ['booleanTrue', true],
            ['booleanFalse', false],
            ['null', null],
            ['undefined', undefined],
            ['object', { a: 1, b: '2', c: true, d: null, e: undefined }],
            ['array', [1, '2', true, null, undefined]],
            ['array2', [1, '2', true, null, undefined]],
            ['emptyArray', []],
        ]),
    );

    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator('not not Arguments.object');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('not not Arguments.stringEmpty');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('not not Arguments.number');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('not not Arguments.number0');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('not not Arguments.booleanTrue');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('not not Arguments.booleanFalse');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('not not Arguments.null');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('not not Arguments.undefined');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('not not Arguments.array');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('not not Arguments.emptyArray');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.object = true');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.object != true');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.stringEmpty = true');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.stringEmpty != false');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.number0 = true');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.number0 = false');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.array.length');
    expect(ev.evaluate(valuesMap)).toBe(5);

    ev = new ExpressionEvaluator('Arguments.object.length');
    expect(ev.evaluate(valuesMap)).toBe(5);

    ev = new ExpressionEvaluator('Arguments.object and Arguments.array');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.object or Arguments.null');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.object and Arguments.null');
    expect(ev.evaluate(valuesMap)).toBeFalsy();

    ev = new ExpressionEvaluator('Arguments.object ? 3 : 4');
    expect(ev.evaluate(valuesMap)).toBe(3);

    ev = new ExpressionEvaluator('not Arguments.object ? 3 : 4');
    expect(ev.evaluate(valuesMap)).toBe(4);

    ev = new ExpressionEvaluator('Arguments.array = Arguments.array2');
    expect(ev.evaluate(valuesMap)).toBeTruthy();

    ev = new ExpressionEvaluator('Arguments.number0 ? 3 : 4');
    expect(ev.evaluate(valuesMap)).toBe(4);
});

class TestTokenValueExtractor extends TokenValueExtractor {
    private store: any;

    constructor(store: any) {
        super();
        this.store = store;
    }

    protected getValueInternal(token: string): any {
        return this.retrieveElementFrom(token, token.split('.'), 1, this.store);
    }
    public getPrefix(): string {
        return 'Test.';
    }
    public getStore(): any {
        return this.store;
    }
}

test('Full Store Test', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', { a: 2, b: [true, false], c: { x: 'kiran' } }],
            ['d', { a: 2, b: [true, false], c: { x: 'kiran' } }],
        ]),
    );

    expect(atv.getStore()).toMatchObject({
        a: 'kirun ',
        b: 2,
        c: { a: 2, b: [true, false], c: { x: 'kiran' } },
        d: { a: 2, b: [true, false], c: { x: 'kiran' } },
    });

    let obv: OutputMapTokenValueExtractor = new OutputMapTokenValueExtractor(
        new Map<string, Map<string, Map<string, any>>>([
            ['step1', new Map([['output', new Map([['name', 'Kiran']])]])],
            ['loop', new Map([['iteration', new Map([['index', 2]])]])],
        ]),
    );

    expect(obv.getStore()).toMatchObject({
        step1: { output: { name: 'Kiran' } },
        loop: { iteration: { index: 2 } },
    });

    let ttv: TestTokenValueExtractor = new TestTokenValueExtractor({
        a: 'kirun',
        b: 2,
        c: { a: 2, b: [true, false], c: { x: 'kiran' } },
        d: { a: 2, b: [true, false], c: { x: 'kiran' } },
    });

    let ev: ExpressionEvaluator = new ExpressionEvaluator('Test.a');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('kirun');

    ttv = new TestTokenValueExtractor(20);

    ev = new ExpressionEvaluator('Test');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(20);

    ev = new ExpressionEvaluator('Test > 10');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toStrictEqual(true);
});

test('Full Store Test when undefined', () => {
    let ttv = new TestTokenValueExtractor(undefined);

    let ev = new ExpressionEvaluator('Test');

    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBeUndefined();
});

test('index retrieval', () => {
    let ttv: TestTokenValueExtractor = new TestTokenValueExtractor({
        a: 'kirun',
        b: 2,
        c: { a: 2, b: [true, false], c: { x: 'kiran' } },
        d: { a: 2, b: [true, false], c: { x: 'kiran' } },
    });

    let ev: ExpressionEvaluator = new ExpressionEvaluator('Test.a');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('kirun');

    ev = new ExpressionEvaluator('Test.b.__index');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('b');

    ev = new ExpressionEvaluator('Test.c.c.x.__index');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('x');

    ev = new ExpressionEvaluator('Test.c.b[1].__index');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(1);

    ev = new ExpressionEvaluator('Test.x.c.__index');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('c');
});

test('backslash escape', () => {
    let ev = new ExpressionEvaluator("'\\maza'");
    expect(ev.evaluate(new Map())).toBe('\\maza');
});

test('ternary expression with displayValue equality check', () => {
    // Test when displayValue = "0" -> should return "1"
    let ttv = new TestTokenValueExtractor({ displayValue: '0' });
    let ev = new ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('1');

    // Test when displayValue = "1" -> should return "11"
    ttv = new TestTokenValueExtractor({ displayValue: '1' });
    ev = new ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('11');

    // Test when displayValue = "5" -> should return "51"
    ttv = new TestTokenValueExtractor({ displayValue: '5' });
    ev = new ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('51');

    // Test when displayValue = "10" -> should return "101"
    ttv = new TestTokenValueExtractor({ displayValue: '10' });
    ev = new ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('101');

    // Test ((Test.displayValue = '0') ? '1' : (Test.displayValue + '1'))
    ttv = new TestTokenValueExtractor({ displayValue: '1' });
    ev = new ExpressionEvaluator("((Test.displayValue = '0') ? '1' : (Test.displayValue + '1'))");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('11');

    // Test (((Test.displayValue = '0') ? '1' : (Test.displayValue + '1')))
    ttv = new TestTokenValueExtractor({ displayValue: '1' });
    ev = new ExpressionEvaluator("(((Test.displayValue = '0') ? '1' : (Test.displayValue + '1')))");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('11');

    // Test (((Test.displayValue = '0')) ? '1' : (((Test.displayValue) + '1')))
    ttv = new TestTokenValueExtractor({ displayValue: '1' });
    ev = new ExpressionEvaluator("(((Test.displayValue = '0')) ? '1' : (((Test.displayValue) + '1')))");
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('11');
});

class StoreTokenValueExtractor extends TokenValueExtractor {
    private store: any;

    constructor(store: any) {
        super();
        this.store = store;
    }

    protected getValueInternal(token: string): any {
        return this.retrieveElementFrom(token, token.split('.'), 1, this.store);
    }
    public getPrefix(): string {
        return 'Store.';
    }
    public getStore(): any {
        return this.store;
    }
}

test('String vs Number equality with nested expression substitution', () => {
    // Setup store with string "413" and number 413
    let stv = new StoreTokenValueExtractor({ strNumber: '413', number: 413 });
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(stv.getPrefix(), stv);

    // Direct comparison: Store.strNumber = Store.number should return false
    // because "413" (string) != 413 (number) - different types
    let ev = new ExpressionEvaluator('Store.strNumber = Store.number');
    expect(ev.evaluate(valuesMap)).toBe(false);

    // Nested expression substitution: {{Store.strNumber}} = Store.number should return true
    // because {{Store.strNumber}} is replaced with "413" making the expression "413 = Store.number"
    // which evaluates to 413 = 413 (both numbers) and returns true
    ev = new ExpressionEvaluator('{{Store.strNumber}} = Store.number');
    expect(ev.evaluate(valuesMap)).toBe(true);

    // Additional test: verify the string value directly
    ev = new ExpressionEvaluator('Store.strNumber');
    expect(ev.evaluate(valuesMap)).toBe('413');

    // Additional test: verify the number value directly
    ev = new ExpressionEvaluator('Store.number');
    expect(ev.evaluate(valuesMap)).toBe(413);

    // Additional test: string to string comparison should return true
    ev = new ExpressionEvaluator("Store.strNumber = '413'");
    expect(ev.evaluate(valuesMap)).toBe(true);

    // Additional test: number to number comparison should return true
    ev = new ExpressionEvaluator('Store.number = 413');
    expect(ev.evaluate(valuesMap)).toBe(true);
});

test('unary minus operator', () => {
    // Simple negative number
    let ev = new ExpressionEvaluator('-5');
    expect(ev.evaluate(new Map())).toBe(-5);

    // Negative variable using subtraction from zero
    let ttv = new TestTokenValueExtractor({ value: 10 });
    ev = new ExpressionEvaluator('0 - Test.value');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(-10);

    // Negative in expression: (a + (0 - b)) = 5 + (-3) = 2
    ttv = new TestTokenValueExtractor({ a: 5, b: 3 });
    ev = new ExpressionEvaluator('Test.a + (0 - Test.b)');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(2);

    // Multiplication with negative: a * -1
    ttv = new TestTokenValueExtractor({ a: 5 });
    ev = new ExpressionEvaluator('Test.a * -1');
    expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(-5);

    // Parenthesized negative number
    ev = new ExpressionEvaluator('(-5)');
    expect(ev.evaluate(new Map())).toBe(-5);

    // Negative number in complex expression
    ev = new ExpressionEvaluator('10 + (-5) * 2');
    expect(ev.evaluate(new Map())).toBe(0);
});

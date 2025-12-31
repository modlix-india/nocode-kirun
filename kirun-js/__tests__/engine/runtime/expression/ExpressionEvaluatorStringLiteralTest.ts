import {
    ArgumentsTokenValueExtractor,
    Expression,
    MapUtil,
    TokenValueExtractor,
} from '../../../../src';
import { ExpressionEvaluator } from '../../../../src';

test('Expression with String Literal - 1 ', () => {
    let ex: Expression = new Expression("'ki/run'+'ab'");

    let ev: ExpressionEvaluator = new ExpressionEvaluator(ex);

    expect(ev.evaluate(MapUtil.of())).toBe('ki/runab');

    let evt: ExpressionEvaluator = new ExpressionEvaluator('"Steps.a');

    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', true],
            ['d', 1.5],
        ]),
    );
    const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    expect(() => evt.evaluate(valuesMap)).toThrow();

    ev = new ExpressionEvaluator("Arguments.a+'kiran'");

    expect(ev.evaluate(valuesMap)).toBe('kirun kiran');

    ev = new ExpressionEvaluator("Arguments.b+'kiran'");

    expect(ev.evaluate(valuesMap)).toBe('2kiran');

    ev = new ExpressionEvaluator('Arguments.c+\'k"ir"an\'');

    expect(ev.evaluate(valuesMap)).toBe('truek"ir"an');

    ev = new ExpressionEvaluator("Arguments.b+\"'kir\" + ' an'");

    expect(ev.evaluate(valuesMap)).toBe("2'kir an");

    ev = new ExpressionEvaluator("Arguments.a+'kiran'+ Arguments.b");

    expect(ev.evaluate(valuesMap)).toBe('kirun kiran2');
});

test('Testing for length expression in string', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', { a: 'hello', b: '' }],
            ['d', 1.5],
        ]),
    );
    const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);
    let ev: ExpressionEvaluator = new ExpressionEvaluator('Arguments.a.length');
    expect(ev.evaluate(valuesMap)).toBe(6);
    ev = new ExpressionEvaluator('Arguments.b.length');
    expect(() => ev.evaluate(valuesMap)).toThrowError();
    ev = new ExpressionEvaluator('Arguments.c.a.length * "f"');
    expect(ev.evaluate(valuesMap)).toBe('fffff');
    ev = new ExpressionEvaluator('Arguments.c.b.length ? "f" : "t"');
    expect(ev.evaluate(valuesMap)).toBe('t');
});

test('Expression with String Literal - 2 ', () => {
    let ev: ExpressionEvaluator = new ExpressionEvaluator("'a' * 10");

    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', true],
            ['d', 1.5],
        ]),
    );
    const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    expect(ev.evaluate(valuesMap)).toBe('aaaaaaaaaa');

    ev = new ExpressionEvaluator('2.5*Arguments.a');
    expect(ev.evaluate(valuesMap)).toBe('kirun kirun kir');

    ev = new ExpressionEvaluator('-0.5*Arguments.a');
    expect(ev.evaluate(valuesMap)).toBe('rik');

    ev = new ExpressionEvaluator("'asdf' * -1");
    expect(ev.evaluate(valuesMap)).toBe('fdsa');

    ev = new ExpressionEvaluator("'asdf' * 0");
    expect(ev.evaluate(valuesMap)).toBe('');

    ev = new ExpressionEvaluator('2.val');
    expect(ev.evaluate(valuesMap)).toBe(undefined);
});

test('Testing for string length with object', () => {
    const jsonObj = {
        greeting: 'hello',
        name: 'surendhar'
    };

    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'surendhar '],
            ['b', 2],
            ['c', true],
            ['d', 1.5],
            ['obj', jsonObj],
        ]),
    );
    const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev: ExpressionEvaluator = new ExpressionEvaluator('Arguments.a.length');
    expect(ev.evaluate(valuesMap)).toBe(10);

    ev = new ExpressionEvaluator('Arguments.b.length');
    expect(() => ev.evaluate(valuesMap)).toThrow();

    ev = new ExpressionEvaluator('Arguments.obj.greeting.length * "S"');
    expect(ev.evaluate(valuesMap)).toBe('SSSSS');

    ev = new ExpressionEvaluator('Arguments.obj.greeting.length * "SP"');
    expect(ev.evaluate(valuesMap)).toBe('SPSPSPSPSP');

    ev = new ExpressionEvaluator('Arguments.obj.name.length ? "fun" : "not Fun"');
    expect(ev.evaluate(valuesMap)).toBe('fun');
});

test('Testing for string length with square brackets', () => {
    const jsonObj = {
        greeting: 'hello',
        name: 'surendhar'
    };

    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'surendhar '],
            ['b', 2],
            ['c', true],
            ['d', 1.5],
            ['obj', jsonObj],
        ]),
    );
    const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev: ExpressionEvaluator = new ExpressionEvaluator('Arguments.a["length"]');
    expect(ev.evaluate(valuesMap)).toBe(10);

    ev = new ExpressionEvaluator('Arguments.b["length"]');
    expect(() => ev.evaluate(valuesMap)).toThrow();

    ev = new ExpressionEvaluator('Arguments.obj.greeting["length"] * "S"');
    expect(ev.evaluate(valuesMap)).toBe('SSSSS');

    ev = new ExpressionEvaluator('Arguments.obj.greeting["length"] * "SP"');
    expect(ev.evaluate(valuesMap)).toBe('SPSPSPSPSP');

    ev = new ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "S"');
    expect(ev.evaluate(valuesMap)).toBe('SSSSS');

    ev = new ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "SP"');
    expect(ev.evaluate(valuesMap)).toBe('SPSPSPSPSP');

    ev = new ExpressionEvaluator('Arguments.obj.name["length"] ? "fun" : "not Fun"');
    expect(ev.evaluate(valuesMap)).toBe('fun');
});

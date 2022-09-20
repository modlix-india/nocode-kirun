import {
    ArgumentsTokenValueExtractor,
    Expression,
    MapUtil,
    TokenValueExtractor,
} from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

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
});

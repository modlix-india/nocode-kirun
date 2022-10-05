import {
    ArgumentsTokenValueExtractor,
    Expression,
    MapUtil,
    TokenValueExtractor,
} from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

test('Expression with Ternary Operator - 1 ', () => {
    var exp = new Expression('a > 10 ?  a - 2 : a + 3'.replace(' ', ''));
    expect(exp.toString()).toBe('((a>10)?(a-2):(a+3))');

    exp = new Expression('a > 10 ?  a - 2 : a + 3');
    expect(exp.toString()).toBe('((a>10)?(a-2):(a+3))');

    exp = new Expression('a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3');
    expect(exp.toString()).toBe('((a>10)?((a>15)?(a+2):(a-2)):(a+3))');
});

test('Expression Evaluation with Ternary Operator - 1 ', () => {
    let x = { a: 2, b: [true, false], c: { x: 'Arguments.b2' } };
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['b1', 4],
            ['b2', 4],
            ['c', x],
            ['d', 'c'],
        ]),
    );
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    var ev = new ExpressionEvaluator('Arguments.e = null ? Arguments.c.a : 3 ');
    expect(ev.evaluate(valuesMap)).toBe(2);

    ev = new ExpressionEvaluator('Arguments.f ? Arguments.c.a : 3 ');
    expect(ev.evaluate(valuesMap)).toBe(3);

    ev = new ExpressionEvaluator('Arguments.e = null ? Arguments.c : 3 ');
    expect(ev.evaluate(valuesMap)).toMatchObject(x);
});

import {
    ArgumentsTokenValueExtractor,
    ExpressionEvaluator,
    MapUtil,
    TokenValueExtractor,
} from '../../../../src';

test('Expression String Index', () => {
    let atv: ArgumentsTokenValueExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['a', 'kirun '],
            ['b', 2],
            ['c', { a: 2, b: [true, false], c: { x: 'kiran' } }],
            ['d', { a: 2, b: [true, false], c: { x: 'kiran' } }],
            ['arr', [0, 1, 2, 3, 4, 5, 6]],
        ]),
    );
    let valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);

    let ev = new ExpressionEvaluator('Arguments.a');
    expect(ev.evaluate(valuesMap)).toBe('kirun ');

    ev = new ExpressionEvaluator('Arguments.a[2]');
    expect(ev.evaluate(valuesMap)).toBe('r');

    ev = new ExpressionEvaluator('Arguments.a[-2]');
    expect(ev.evaluate(valuesMap)).toBe('n');

    ev = new ExpressionEvaluator('Arguments.arr[2..4]');
    expect(ev.evaluate(valuesMap)).toMatchObject([2, 3]);

    ev = new ExpressionEvaluator('Arguments.a[2..4]');
    expect(ev.evaluate(valuesMap)).toBe('ru');

    ev = new ExpressionEvaluator('Arguments.a[(4-2)..(6-2)]');
    expect(ev.evaluate(valuesMap)).toBe('ru');

    ev = new ExpressionEvaluator('Arguments.a[..4]');
    expect(ev.evaluate(valuesMap)).toBe('kiru');

    ev = new ExpressionEvaluator('Arguments.a[2..]');
    expect(ev.evaluate(valuesMap)).toBe('run ');

    ev = new ExpressionEvaluator('Arguments.arr[..4]');
    expect(ev.evaluate(valuesMap)).toMatchObject([0, 1, 2, 3]);

    ev = new ExpressionEvaluator('Arguments.arr[(4-2)..7]');
    expect(ev.evaluate(valuesMap)).toMatchObject([2, 3, 4, 5, 6]);

    ev = new ExpressionEvaluator('Arguments.a[..-4]');
    expect(ev.evaluate(valuesMap)).toBe('ki');

    ev = new ExpressionEvaluator('Arguments.a[..(-8+4)]');
    expect(ev.evaluate(valuesMap)).toBe('ki');

    ev = new ExpressionEvaluator('Arguments.a[-4..-1]');
    expect(ev.evaluate(valuesMap)).toBe('run');

    ev = new ExpressionEvaluator('Arguments.a[-4..]');
    expect(ev.evaluate(valuesMap)).toBe('run ');

    ev = new ExpressionEvaluator('Arguments.arr[..-1]');
    expect(ev.evaluate(valuesMap)).toMatchObject([0, 1, 2, 3, 4, 5]);

    ev = new ExpressionEvaluator('Arguments.arr[-2..]');
    expect(ev.evaluate(valuesMap)).toMatchObject([5, 6]);
});

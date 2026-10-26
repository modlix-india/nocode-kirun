import { ArgumentsTokenValueExtractor, MapUtil, TokenValueExtractor } from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

// The reference for KIRun arithmetic (JavaScript numbers). kirun-rust, kirun-java and kirun-py
// run the same cases: whole numbers never wrap, `/` is true division, `//` floors and `%`
// keeps the dividend's sign.
const atv = new ArgumentsTokenValueExtractor(
    new Map<string, any>([
        ['a', 40000],
        ['b', 12],
        ['c', 10000],
        ['p', 480000],
        ['q', 6000000],
        ['n', -40000],
        ['f', 1.5],
        ['s', 'ab'],
    ]),
);
const valuesMap: Map<string, TokenValueExtractor> = MapUtil.of(atv.getPrefix(), atv);
const ev = (e: string) => new ExpressionEvaluator(e).evaluate(valuesMap);

test('Arithmetic parity: big products and negatives', () => {
    expect(ev('40000 * 12 * 10000')).toBe(4800000000);
    expect(ev('Arguments.a * Arguments.b * Arguments.c')).toBe(4800000000);
    expect(ev('Arguments.n * Arguments.b * Arguments.c')).toBe(-4800000000);
    expect(ev('65536 * 65536')).toBe(4294967296);
    expect(ev('2147483647 + 1')).toBe(2147483648);
    expect(ev('-2147483648 - 1')).toBe(-2147483649);
    expect(ev('-(-2147483648)')).toBe(2147483648);
});

test('Arithmetic parity: division, integer division and modulus', () => {
    expect(ev('480000 / 6000000')).toBe(0.08);
    expect(ev('Arguments.p / Arguments.q')).toBe(0.08);
    expect(ev('7 / 2')).toBe(3.5);
    expect(ev('6 / 3')).toBe(2);
    expect(ev('1 / 3')).toBe(1 / 3);
    expect(ev('4800000000 / 1000')).toBe(4800000);
    expect(ev('7 // 2')).toBe(3);
    expect(ev('-7 // 2')).toBe(-4);
    expect(ev('-7 % 3')).toBe(-1);
    expect(ev('7 % -3')).toBe(1);
    expect(ev('-7.5 % 2')).toBe(-1.5);
    // `*` and `/` group from the right: a / b * c is a / (b * c).
    expect(ev('Arguments.p / Arguments.q * 100')).toBe(0.0008);
});

test('Arithmetic parity: mixed, comparisons and strings', () => {
    expect(ev('Arguments.f * Arguments.a')).toBe(60000);
    expect(ev('0.1 + 0.2')).toBe(0.1 + 0.2);
    expect(ev('40000 * 12 * 10000 > 4000000000')).toBe(true);
    expect(ev('480000 / 6000000 = 0.08')).toBe(true);
    expect(ev('6 / 3 = 2')).toBe(true);
    expect(ev("'ab' + 40000 * 12 * 10000")).toBe('ab4800000000');
    expect(ev("'x' + 7 / 2")).toBe('x3.5');
    expect(ev("1 + 2 + 'a'")).toBe('3a');
    expect(ev("'a' + 1 + 2")).toBe('a12');
    expect(ev('Arguments.s * 2')).toBe('abab');
});

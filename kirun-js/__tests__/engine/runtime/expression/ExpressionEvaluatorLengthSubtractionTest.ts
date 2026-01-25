import { MapUtil, TokenValueExtractor } from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

class TestTokenValueExtractor extends TokenValueExtractor {
    private readonly store: any;

    constructor(store: any) {
        super();
        this.store = store;
    }

    protected getValueInternal(token: string): any {
        return this.retrieveElementFrom(token, token.split('.'), 1, this.store);
    }

    public getPrefix(): string {
        return 'Page.';
    }

    public getStore(): any {
        return this.store;
    }
}

describe('Expression Evaluator - Array length with subtraction (working patterns)', () => {
    test('array.length alone should work', () => {
        const ttv = new TestTokenValueExtractor({
            conditions: [1, 2, 3, 4, 5],
        });

        const ev = new ExpressionEvaluator('Page.conditions.length');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(5);
    });

    test('array.length - 1 with spaces should work', () => {
        const ttv = new TestTokenValueExtractor({
            conditions: [1, 2, 3, 4, 5],
        });

        const ev = new ExpressionEvaluator('Page.conditions.length - 1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(4);
    });

    test('array.length-1 without spaces should work', () => {
        const ttv = new TestTokenValueExtractor({
            conditions: [1, 2, 3, 4, 5],
        });

        const ev = new ExpressionEvaluator('Page.conditions.length-1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(4);
    });

    test('nested array access with length-1 without spaces', () => {
        const ttv = new TestTokenValueExtractor({
            mainFilterCondition: {
                condition: {
                    conditions: [
                        {
                            conditions: [1, 2, 3, 4],
                        },
                        {
                            conditions: [5, 6],
                        },
                    ],
                },
            },
        });

        // This is the exact expression pattern from the bug report
        const ev = new ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length-1',
        );
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(3);
    });

    test('nested array access with length - 1 with spaces', () => {
        const ttv = new TestTokenValueExtractor({
            mainFilterCondition: {
                condition: {
                    conditions: [
                        {
                            conditions: [1, 2, 3, 4],
                        },
                        {
                            conditions: [5, 6],
                        },
                    ],
                },
            },
        });

        const ev = new ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length - 1',
        );
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(3);
    });

    test('array length with addition', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        const ev = new ExpressionEvaluator('Page.items.length+2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(5);
    });

    test('array length with multiplication', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        const ev = new ExpressionEvaluator('Page.items.length*2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(6);
    });

    test('array length in complex expression', () => {
        const ttv = new TestTokenValueExtractor({
            arr1: [1, 2, 3],
            arr2: [4, 5, 6, 7],
        });

        // arr1.length + arr2.length - 1 = 3 + 4 - 1 = 6
        const ev = new ExpressionEvaluator('Page.arr1.length + Page.arr2.length - 1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(6);
    });

    test('using length-1 as array index', () => {
        const ttv = new TestTokenValueExtractor({
            items: [10, 20, 30, 40, 50],
        });

        // Access last element: items[items.length - 1]
        const ev = new ExpressionEvaluator('Page.items[Page.items.length - 1]');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(50);
    });

    test('using length-1 without spaces as array index', () => {
        const ttv = new TestTokenValueExtractor({
            items: [10, 20, 30, 40, 50],
        });

        // Access last element: items[items.length-1]
        const ev = new ExpressionEvaluator('Page.items[Page.items.length-1]');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(50);
    });

    test('nested object with array length subtraction', () => {
        const ttv = new TestTokenValueExtractor({
            data: {
                nested: {
                    list: ['a', 'b', 'c', 'd'],
                },
            },
        });

        const ev = new ExpressionEvaluator('Page.data.nested.list.length-1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(3);
    });

    test('string length with subtraction', () => {
        const ttv = new TestTokenValueExtractor({
            text: 'hello',
        });

        const ev = new ExpressionEvaluator('Page.text.length-1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(4);
    });

    test('object keys length with subtraction', () => {
        const ttv = new TestTokenValueExtractor({
            obj: { a: 1, b: 2, c: 3, d: 4 },
        });

        // Object with 4 keys, length should be 4
        const ev = new ExpressionEvaluator('Page.obj.length-1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(3);
    });

    test('empty array length minus 1', () => {
        const ttv = new TestTokenValueExtractor({
            empty: [],
        });

        const ev = new ExpressionEvaluator('Page.empty.length-1');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(-1);
    });

    test('multiple length operations in same expression', () => {
        const ttv = new TestTokenValueExtractor({
            list1: [1, 2, 3],
            list2: [4, 5],
        });

        // (list1.length-1) * (list2.length-1) = 2 * 1 = 2
        const ev = new ExpressionEvaluator('(Page.list1.length-1) * (Page.list2.length-1)');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(2);
    });

    test('length with division', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3, 4, 5, 6],
        });

        const ev = new ExpressionEvaluator('Page.items.length/2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(3);
    });

    test('length with modulus', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3, 4, 5],
        });

        const ev = new ExpressionEvaluator('Page.items.length%3');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(2);
    });

    test('simple greater than comparison (no subtraction)', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // Page.items.length > 0 = 3 > 0 = true
        const ev = new ExpressionEvaluator('Page.items.length > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });
});

describe('Expression Evaluator - Alternative patterns with parentheses', () => {
    // These tests demonstrate alternative patterns using parentheses
    // These patterns work, but parentheses are no longer required (see "Previously known issues" section)

    test('Alternative: length comparison with parentheses and spaces', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // Using parentheses: (Page.items.length - 1) > 0
        const ev = new ExpressionEvaluator('(Page.items.length - 1) > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('Alternative: length comparison with parentheses no spaces', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // Using parentheses: (Page.items.length-1) > 0
        const ev = new ExpressionEvaluator('(Page.items.length-1) > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('Alternative: literal subtraction and comparison with parentheses', () => {
        const ttv = new TestTokenValueExtractor({});

        // Using parentheses: (5 - 1) > 0
        const ev = new ExpressionEvaluator('(5 - 1) > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('Alternative: chained subtraction with parentheses', () => {
        const ttv = new TestTokenValueExtractor({});

        // Using parentheses: (5 - 1) - 2 = 4 - 2 = 2
        const ev = new ExpressionEvaluator('(5 - 1) - 2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(2);
    });

    test('Alternative: length in ternary with parentheses', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3, 4],
        });

        // Using parentheses: (Page.items.length - 1) > 2 ? 'big' : 'small'
        const ev = new ExpressionEvaluator("(Page.items.length - 1) > 2 ? 'big' : 'small'");
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('big');
    });

    test('Alternative: subtraction with equality using parentheses', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // Using parentheses: (Page.items.length - 1) = 2
        const ev = new ExpressionEvaluator('(Page.items.length - 1) = 2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });
});

describe('Expression Evaluator - Previously known issues (now fixed)', () => {
    // These tests document expressions that previously failed without parentheses
    // These issues have been fixed and now work correctly without parentheses

    test('FIXED: length comparison without parentheses now works', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // This now works: Page.items.length - 1 > 0
        const ev = new ExpressionEvaluator('Page.items.length - 1 > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('FIXED: simple subtraction and comparison without parentheses now works', () => {
        const ttv = new TestTokenValueExtractor({});

        // This now works: 5 - 1 > 0
        const ev = new ExpressionEvaluator('5 - 1 > 0');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('FIXED: chained subtraction now has correct associativity', () => {
        const ttv = new TestTokenValueExtractor({});

        // This now correctly evaluates as (5-1)-2 = 2
        const ev = new ExpressionEvaluator('5 - 1 - 2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(2);
    });

    test('FIXED: subtraction then equality without parentheses now works', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3],
        });

        // This now works: Page.items.length - 1 = 2
        const ev = new ExpressionEvaluator('Page.items.length - 1 = 2');
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe(true);
    });

    test('FIXED: length in ternary without parentheses now works', () => {
        const ttv = new TestTokenValueExtractor({
            items: [1, 2, 3, 4],
        });

        // This now works: Page.items.length - 1 > 2 ? 'big' : 'small'
        const ev = new ExpressionEvaluator("Page.items.length - 1 > 2 ? 'big' : 'small'");
        expect(ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv))).toBe('big');
    });
});

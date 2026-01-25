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

describe('Expression Evaluator - Length Subtraction Bug Reproduction', () => {
    /**
     * These tests reproduce the bug where:
     * Page.mainFilterCondition.condition.conditions[0].conditions.length-1
     * fails with: "Cannot evaluate expression [object Object] - 1"
     * 
     * The issue occurs when accessing .length on an object/array and then subtracting.
     */

    describe('Bug reproduction - nested array length subtraction', () => {
        test('nested array access with length-1 (exact failing case from bug report)', () => {
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

            // This is the exact expression from the bug report
            // NOTE: This test currently PASSES, suggesting the bug may be data-dependent
            // or related to specific object structures with length properties
            const ev = new ExpressionEvaluator(
                'Page.mainFilterCondition.condition.conditions[0].conditions.length-1',
            );
            
            // Expected result: 3 (array length 4 - 1)
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });

        test('nested array access with length - 1 (with spaces)', () => {
            const ttv = new TestTokenValueExtractor({
                mainFilterCondition: {
                    condition: {
                        conditions: [
                            {
                                conditions: [1, 2, 3, 4],
                            },
                        ],
                    },
                },
            });

            const ev = new ExpressionEvaluator(
                'Page.mainFilterCondition.condition.conditions[0].conditions.length - 1',
            );
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });
    });

    describe('Edge cases - object with length property', () => {
        test('FIXED: object with length property that is an object should return Object.keys length', () => {
            const ttv = new TestTokenValueExtractor({
                obj: {
                    length: { nested: 'object' }, // Object has a length property
                },
            });

            // When accessing obj.length, it should return Object.keys(obj).length
            // (ignoring the length property on the object)
            // Then obj.length - 1 should work correctly
            const ev = new ExpressionEvaluator('Page.obj.length-1');
            
            // Should return Object.keys(obj).length - 1 = 1 - 1 = 0
            // (obj has one key: 'length')
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(0);
        });

        test('Nested object with length property in path', () => {
            // This might be closer to the actual bug scenario
            const ttv = new TestTokenValueExtractor({
                mainFilterCondition: {
                    condition: {
                        conditions: [
                            {
                                conditions: [1, 2, 3, 4],
                                length: { some: 'object' }, // Object has length property
                            },
                        ],
                    },
                },
            });

            // If conditions[0] has a length property, accessing conditions.length might return that
            const ev = new ExpressionEvaluator(
                'Page.mainFilterCondition.condition.conditions[0].conditions.length-1',
            );
            
            // This might fail if the parser gets confused
            // The issue: conditions[0] has a length property, but we want conditions.length (array length)
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            // Should still return 3 (array length 4 - 1), not affected by the length property on the parent
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });

        test('object with length property that is a number should work', () => {
            const ttv = new TestTokenValueExtractor({
                obj: {
                    length: 5, // length property exists and is a number
                },
            });

            // When obj has a length property, it should return that value
            const ev = new ExpressionEvaluator('Page.obj.length-1');
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            
            // Should return 5 - 1 = 4 (the length property value minus 1)
            expect(result).toBe(4);
        });

        test('array length should always work (no length property conflict)', () => {
            const ttv = new TestTokenValueExtractor({
                arr: [1, 2, 3, 4, 5],
            });

            const ev = new ExpressionEvaluator('Page.arr.length-1');
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            
            // Arrays always have .length, should return 5 - 1 = 4
            expect(result).toBe(4);
        });
    });

    describe('Complex nested scenarios', () => {
        test('deeply nested structure with multiple array accesses', () => {
            const ttv = new TestTokenValueExtractor({
                level1: {
                    level2: {
                        level3: {
                            items: [
                                {
                                    subItems: ['a', 'b', 'c', 'd', 'e'],
                                },
                                {
                                    subItems: ['f', 'g'],
                                },
                            ],
                        },
                    },
                },
            });

            const ev = new ExpressionEvaluator(
                'Page.level1.level2.level3.items[0].subItems.length-1',
            );
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(4); // 5 - 1
        });

        test('FIXED: multiple length operations in same expression should work', () => {
            const ttv = new TestTokenValueExtractor({
                arr1: [1, 2, 3],
                arr2: [4, 5, 6, 7, 8],
            });

            const ev = new ExpressionEvaluator(
                'Page.arr1.length-1 + Page.arr2.length-1',
            );
            
            // Should work correctly now
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            // Expected result: (3-1) + (5-1) = 2 + 4 = 6
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(6);
        });

        test('length-1 used as array index', () => {
            const ttv = new TestTokenValueExtractor({
                items: [10, 20, 30, 40, 50],
            });

            // Access last element using length-1
            const ev = new ExpressionEvaluator('Page.items[Page.items.length-1]');
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(50);
        });
    });

    describe('Different data types with length', () => {
        test('string length subtraction', () => {
            const ttv = new TestTokenValueExtractor({
                text: 'hello world',
            });

            const ev = new ExpressionEvaluator('Page.text.length-1');
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            
            expect(result).toBe(10); // 11 - 1
        });

        test('empty array length minus 1', () => {
            const ttv = new TestTokenValueExtractor({
                empty: [],
            });

            const ev = new ExpressionEvaluator('Page.empty.length-1');
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            
            expect(result).toBe(-1); // 0 - 1
        });

        test('object with no length property (should use Object.keys)', () => {
            const ttv = new TestTokenValueExtractor({
                obj: { a: 1, b: 2, c: 3 },
            });

            const ev = new ExpressionEvaluator('Page.obj.length-1');
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            
            expect(result).toBe(2); // 3 keys - 1
        });
    });

    describe('Specific bug scenarios', () => {
        test('scenario where intermediate object has length property', () => {
            // This test checks if having a length property on an intermediate object
            // causes issues when accessing array.length further down the path
            const ttv = new TestTokenValueExtractor({
                mainFilterCondition: {
                    condition: {
                        length: { conflict: true }, // This might cause issues
                        conditions: [
                            {
                                conditions: [1, 2, 3, 4],
                            },
                        ],
                    },
                },
            });

            // Accessing conditions.length should still work (it's an array)
            const ev = new ExpressionEvaluator(
                'Page.mainFilterCondition.condition.conditions[0].conditions.length-1',
            );
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });

        test('scenario where array element has length property', () => {
            // What if one of the array elements has a length property?
            const ttv = new TestTokenValueExtractor({
                mainFilterCondition: {
                    condition: {
                        conditions: [
                            {
                                conditions: [1, 2, 3, 4],
                                length: { nested: 'value' }, // Array element has length
                            },
                        ],
                    },
                },
            });

            const ev = new ExpressionEvaluator(
                'Page.mainFilterCondition.condition.conditions[0].conditions.length-1',
            );
            
            // This should still work - we're accessing conditions.length (the array)
            // not the length property of the object
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });
    });

    describe('Expression parsing edge cases', () => {
        test('length-1 without spaces in complex path', () => {
            const ttv = new TestTokenValueExtractor({
                data: {
                    nested: {
                        list: [1, 2, 3, 4, 5, 6],
                    },
                },
            });

            const ev = new ExpressionEvaluator('Page.data.nested.list.length-1');
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(5);
        });

        test('length - 1 with spaces in complex path', () => {
            const ttv = new TestTokenValueExtractor({
                data: {
                    nested: {
                        list: [1, 2, 3, 4, 5, 6],
                    },
                },
            });

            const ev = new ExpressionEvaluator('Page.data.nested.list.length - 1');
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(5);
        });

        test('parentheses around length-1', () => {
            const ttv = new TestTokenValueExtractor({
                items: [1, 2, 3, 4],
            });

            const ev = new ExpressionEvaluator('(Page.items.length-1)');
            
            expect(() => {
                ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            }).not.toThrow();
            
            const result = ev.evaluate(MapUtil.of(ttv.getPrefix(), ttv));
            expect(result).toBe(3);
        });
    });
});

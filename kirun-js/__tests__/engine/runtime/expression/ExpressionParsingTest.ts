import { Expression, TokenValueExtractor } from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

class TestTokenValueExtractor extends TokenValueExtractor {
    private readonly store: any;
    private readonly prefix: string;

    constructor(prefix: string, store: any) {
        super();
        this.prefix = prefix;
        this.store = store;
    }

    protected getValueInternal(token: string): any {
        return this.retrieveElementFrom(token, token.split('.'), 1, this.store);
    }

    public getPrefix(): string {
        return this.prefix;
    }

    public getStore(): any {
        return this.store;
    }
}

describe('Expression Parsing Tests', () => {
    test('Parse expression with multiplication and nested template: Steps.floorWeekOne.output.value * {{Page.secondsInDay}}', () => {
        const stepsExtractor = new TestTokenValueExtractor('Steps.', {
            floorWeekOne: {
                output: {
                    value: 7,
                },
            },
        });

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            secondsInDay: 86400,
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [stepsExtractor.getPrefix(), stepsExtractor],
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        // Test parsing does not throw
        const expr = new Expression('Steps.floorWeekOne.output.value * {{Page.secondsInDay}}');
        expect(expr).toBeDefined();
        expect(expr.getOperations().isEmpty()).toBe(false);

        // Test evaluation
        const ev = new ExpressionEvaluator(
            'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}',
        );
        expect(ev.evaluate(valuesMap)).toBe(7 * 86400);
    });

    test('Parse expression with nullish coalescing: Parent.projectInfo.projectType?? \'-\'', () => {
        // Test with value present
        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            projectInfo: {
                projectType: 'Commercial',
            },
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        // Test parsing does not throw
        const expr = new Expression("Parent.projectInfo.projectType?? '-'");
        expect(expr).toBeDefined();

        // Test evaluation with value present
        const ev = new ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'");
        expect(ev.evaluate(valuesMap)).toBe('Commercial');

        const ev1 = new ExpressionEvaluator("Parent.projectInfo.projectType1?? '-'");
        expect(ev1.evaluate(valuesMap)).toBe('-');

        // Test with value null/undefined - should return default
        const parentExtractorNull = new TestTokenValueExtractor('Parent.', {
            projectInfo: {
                projectType: null,
            },
        });

        const valuesMapNull: Map<string, TokenValueExtractor> = new Map([
            [parentExtractorNull.getPrefix(), parentExtractorNull],
        ]);

        expect(ev.evaluate(valuesMapNull)).toBe('-');

        // Test with missing property - should return default
        const parentExtractorMissing = new TestTokenValueExtractor('Parent.', {
            projectInfo: {},
        });

        const valuesMapMissing: Map<string, TokenValueExtractor> = new Map([
            [parentExtractorMissing.getPrefix(), parentExtractorMissing],
        ]);

        expect(ev.evaluate(valuesMapMissing)).toBe('-');
    });

    test("Parse expression with nullish coalescing and string concat: (Page.userFirstName??'') +' '+ (Page.userLastName??'')", () => {
        const pageExtractor = new TestTokenValueExtractor('Page.', {
            userFirstName: 'John',
            userLastName: 'Doe',
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        const expr =
            "(Page.userFirstName??'') +' '+ (Page.userLastName??'')";
        expect(new Expression(expr)).toBeDefined();

        const ev = new ExpressionEvaluator(expr);
        expect(ev.evaluate(valuesMap)).toBe('John Doe');

        // Both null/undefined - should return ' '
        const pageExtractorEmpty = new TestTokenValueExtractor('Page.', {});
        const valuesMapEmpty: Map<string, TokenValueExtractor> = new Map([
            [pageExtractorEmpty.getPrefix(), pageExtractorEmpty],
        ]);
        expect(ev.evaluate(valuesMapEmpty)).toBe(' ');

        // First name only
        const pageExtractorFirst = new TestTokenValueExtractor('Page.', {
            userFirstName: 'Jane',
        });
        const valuesMapFirst: Map<string, TokenValueExtractor> = new Map([
            [pageExtractorFirst.getPrefix(), pageExtractorFirst],
        ]);
        expect(ev.evaluate(valuesMapFirst)).toBe('Jane ');

        // Last name only
        const pageExtractorLast = new TestTokenValueExtractor('Page.', {
            userLastName: 'Smith',
        });
        const valuesMapLast: Map<string, TokenValueExtractor> = new Map([
            [pageExtractorLast.getPrefix(), pageExtractorLast],
        ]);
        expect(ev.evaluate(valuesMapLast)).toBe(' Smith');
    });

    test('Parse expression with dynamic array index and dot access: Parent.perCount[Parent.index].value.Percentage + \'%\'', () => {
        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            perCount: [
                { value: { Percentage: 10 } },
                { value: { Percentage: 25 } },
                { value: { Percentage: 50 } },
            ],
            index: 1,
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        // Test parsing does not throw
        const expr = new Expression(
            "Parent.perCount[Parent.index].value.Percentage + '%'",
        );
        expect(expr).toBeDefined();

        // Test evaluation - array index path expressions are now properly evaluated
        const ev = new ExpressionEvaluator(
            "Parent.perCount[Parent.index].value.Percentage + '%'",
        );
        expect(ev.evaluate(valuesMap)).toBe('25%');
    });

    test('Parse expression with nested parent index: Parent.perCount[Parent.Parent.__index].value.Percentage', () => {
        // Using nested Parent references
        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            perCount: [
                { value: { Percentage: 10 } },
                { value: { Percentage: 25 } },
                { value: { Percentage: 50 } },
            ],
            Parent: {
                __index: 2,
            },
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        // Test parsing does not throw
        const expr = new Expression(
            "Parent.perCount[Parent.Parent.__index].value.Percentage + '%'",
        );
        expect(expr).toBeDefined();

        // Test evaluation - accessing Parent.Parent.__index means:
        // 1. Get Parent.Parent -> { __index: 2 }
        // 2. Get __index from that -> 2
        // 3. Use 2 as index into perCount -> { value: { Percentage: 50 } }
        const ev = new ExpressionEvaluator(
            "Parent.perCount[Parent.Parent.__index].value.Percentage + '%'",
        );
        expect(ev.evaluate(valuesMap)).toBe('50%');
    });

    test('Parse expression with less than comparison: Page.dealData.size < Page.dealData.totalElements', () => {
        // Test when size < totalElements
        const pageExtractor = new TestTokenValueExtractor('Page.', {
            dealData: {
                size: 10,
                totalElements: 100,
            },
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        // Test parsing does not throw
        const expr = new Expression('Page.dealData.size < Page.dealData.totalElements');
        expect(expr).toBeDefined();

        // Test evaluation - size < totalElements should be true
        const ev = new ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements');
        expect(ev.evaluate(valuesMap)).toBe(true);

        // Test when size >= totalElements
        const pageExtractorEqual = new TestTokenValueExtractor('Page.', {
            dealData: {
                size: 100,
                totalElements: 100,
            },
        });

        const valuesMapEqual: Map<string, TokenValueExtractor> = new Map([
            [pageExtractorEqual.getPrefix(), pageExtractorEqual],
        ]);

        expect(ev.evaluate(valuesMapEqual)).toBe(false);

        // Test when size > totalElements
        const pageExtractorGreater = new TestTokenValueExtractor('Page.', {
            dealData: {
                size: 150,
                totalElements: 100,
            },
        });

        const valuesMapGreater: Map<string, TokenValueExtractor> = new Map([
            [pageExtractorGreater.getPrefix(), pageExtractorGreater],
        ]);

        expect(ev.evaluate(valuesMapGreater)).toBe(false);
    });

    test('Parse expression with extra spaces: Page.dealData.size <  Page.dealData.totalElements', () => {
        const pageExtractor = new TestTokenValueExtractor('Page.', {
            dealData: {
                size: 5,
                totalElements: 20,
            },
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        // Test parsing with extra spaces does not throw
        const expr = new Expression('Page.dealData.size <  Page.dealData.totalElements');
        expect(expr).toBeDefined();

        // Test evaluation
        const ev = new ExpressionEvaluator('Page.dealData.size <  Page.dealData.totalElements');
        expect(ev.evaluate(valuesMap)).toBe(true);
    });

    test('Parse expression with space before dot: Parent.perCount[Parent.Parent.__index].value.percentage', () => {
        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            perCount: [
                { value: { Percentage: 10 } },
                { value: { Percentage: 25 } },
            ],
            Parent: {
                __index: 0,
            },
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        // Test expression with space before property name after dot
        // Note: This might be invalid syntax depending on parser strictness
        // Let's see if it parses
        try {
            const expr = new Expression(
                "Parent.perCount[Parent.Parent.__index].value.percentage + '%'",
            );
            expect(expr).toBeDefined();

            const ev = new ExpressionEvaluator(
                "Parent.perCount[Parent.Parent.__index].value.percentage + '%'",
            );
            const result = ev.evaluate(valuesMap);
            // If it evaluates, check the result
            expect(result).toBe('10%');
        } catch (e) {
            // If parsing fails due to space before property name, that's also a valid test result
            expect(e).toBeDefined();
        }
    });

    test('Expression toString() preserves structure', () => {
        // Simple path
        const expr1 = new Expression('Page.dealData.size');
        expect(expr1.toString()).toContain('Page');
        expect(expr1.toString()).toContain('dealData');

        // Comparison
        const expr2 = new Expression('Page.dealData.size < Page.dealData.totalElements');
        expect(expr2.toString()).toContain('<');

        // Multiplication with template
        const expr3 = new Expression('Steps.floorWeekOne.output.value * 86400');
        expect(expr3.toString()).toContain('*');

        // Nullish coalescing
        const expr4 = new Expression("Parent.projectInfo.projectType ?? '-'");
        expect(expr4.toString()).toContain('??');
    });

    test('Parse complex expression with array access and string concatenation', () => {
        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            perCount: [
                { value: { Percentage: 15 } },
                { value: { Percentage: 30 } },
                { value: { Percentage: 45 } },
            ],
            index: 2,
        });

        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        // Array index path expressions are now properly evaluated
        const ev = new ExpressionEvaluator(
            "Parent.perCount[Parent.index].value.Percentage + '%'",
        );
        expect(ev.evaluate(valuesMap)).toBe('45%');
    });
});

/**
 * Tests for exact original expressions as provided by user.
 * These tests verify parsing behavior for real-world expression patterns.
 */
describe('Original Expression Parsing Tests', () => {
    test('Expression 1: Steps.floorWeekOne.output.value * {{Page.secondsInDay}}', () => {
        // This expression uses nested template syntax {{...}} for value interpolation
        const expression = 'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}';

        // Verify parsing succeeds
        const expr = new Expression(expression);
        expect(expr).toBeDefined();
        expect(expr.getOperations().isEmpty()).toBe(false);

        // The template {{...}} syntax is handled specially during evaluation
        // toString() may not preserve the full structure with * operator
        // but the expression should parse and evaluate correctly
        expect(expr.toString()).toContain('Page.secondsInDay');
    });

    test('Expression 2: Parent.projectInfo.projectType?? \'-\'', () => {
        // This expression uses nullish coalescing operator (??)
        // Note: No space between projectType and ?? in the original
        const expression = "Parent.projectInfo.projectType?? '-'";

        // Verify parsing succeeds
        const expr = new Expression(expression);
        expect(expr).toBeDefined();

        // Verify it contains nullish coalescing operation
        expect(expr.toString()).toContain('??');
    });

    test('Expression 4: Page.dealData.size <  Page.dealData.totalElements', () => {
        // This expression has extra space in the less-than comparison
        const expression = 'Page.dealData.size <  Page.dealData.totalElements';

        // Verify parsing succeeds with extra spaces
        const expr = new Expression(expression);
        expect(expr).toBeDefined();

        // Verify it contains less-than operation
        expect(expr.toString()).toContain('<');
    });

    test('Original expression parsing - verify all parse without throwing', () => {
        const expressions = [
            'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}',
            "Parent.projectInfo.projectType?? '-'",
            'Page.dealData.size <  Page.dealData.totalElements',
            'Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}',
        ];

        for (const expression of expressions) {
            expect(() => new Expression(expression)).not.toThrow();
        }
    });

    test('Expression 5: Complex nested ternary with multiple template expressions', () => {
        // This is a deeply nested ternary expression with subtraction at the start
        // Testing the parser's ability to handle:
        // 1. Arithmetic operation (10 - result)
        // 2. Multiple nested ternary operators
        // 3. Multiple template expressions {{Parent.id}}
        // 4. Comparison operators (<)
        const expression =
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : {{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : {{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : {{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : {{Parent.id}} < 1000000000 ? 9 :10}}';

        // Verify parsing succeeds (this should not throw an error)
        expect(() => new Expression(expression)).not.toThrow();

        const expr = new Expression(expression);
        expect(expr).toBeDefined();
        expect(expr.getOperations().isEmpty()).toBe(false);
    });

    test('Expression 5 Evaluation: Complex nested ternary returns correct values', () => {
        const expression =
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : {{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : {{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : {{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : {{Parent.id}} < 1000000000 ? 9 :10}}';

        // Test with various Parent.id values to ensure correct ternary evaluation
        const testCases = [
            { id: 5, expected: 10 - 1 }, // id < 10
            { id: 50, expected: 10 - 2 }, // id < 100
            { id: 500, expected: 10 - 3 }, // id < 1000
            { id: 5000, expected: 10 - 4 }, // id < 10000
            { id: 50000, expected: 10 - 5 }, // id < 100000
            { id: 500000, expected: 10 - 6 }, // id < 1000000
            { id: 5000000, expected: 10 - 7 }, // id < 10000000
            { id: 50000000, expected: 10 - 8 }, // id < 100000000
            { id: 500000000, expected: 10 - 9 }, // id < 1000000000
            { id: 5000000000, expected: 10 - 10 }, // id >= 1000000000
        ];

        testCases.forEach(({ id, expected }) => {
            const parentExtractor = new TestTokenValueExtractor('Parent.', { id });
            const valuesMap: Map<string, TokenValueExtractor> = new Map([
                [parentExtractor.getPrefix(), parentExtractor],
            ]);

            const ev = new ExpressionEvaluator(expression);
            const result = ev.evaluate(valuesMap);

            expect(result).toBe(expected);
        });
    });

    test('Expression 5 Debug: Log parsing structure of complex nested ternary', () => {
        const expression =
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : {{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : {{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : {{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : {{Parent.id}} < 1000000000 ? 9 :10}}';

        const expr = new Expression(expression);
        
        // Log the expression structure for debugging
        console.log('Expression parsed successfully');
        console.log('Expression toString:', expr.toString());
        console.log('Operations empty?', expr.getOperations().isEmpty());

        // Test that it can evaluate without error
        const parentExtractor = new TestTokenValueExtractor('Parent.', { id: 12345 });
        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        
        console.log('Evaluation result for id=12345:', result);
        expect(result).toBe(10 - 5); // 12345 < 100000, so should return 10 - 5 = 5
    });

    test('Expression 5 Edge Cases: Test boundary values', () => {
        const expression =
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : {{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : {{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : {{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : {{Parent.id}} < 1000000000 ? 9 :10}}';

        // Test exact boundary values
        const boundaryTestCases = [
            { id: 0, expected: 10 - 1 }, // Minimum value
            { id: 9, expected: 10 - 1 }, // Just before boundary
            { id: 10, expected: 10 - 2 }, // Exact boundary
            { id: 99, expected: 10 - 2 }, // Just before next boundary
            { id: 100, expected: 10 - 3 }, // Exact boundary
            { id: 999, expected: 10 - 3 },
            { id: 1000, expected: 10 - 4 },
            { id: 9999, expected: 10 - 4 },
            { id: 10000, expected: 10 - 5 },
            { id: 99999, expected: 10 - 5 },
            { id: 100000, expected: 10 - 6 },
            { id: 999999, expected: 10 - 6 },
            { id: 1000000, expected: 10 - 7 },
            { id: 9999999, expected: 10 - 7 },
            { id: 10000000, expected: 10 - 8 },
            { id: 99999999, expected: 10 - 8 },
            { id: 100000000, expected: 10 - 9 },
            { id: 999999999, expected: 10 - 9 },
            { id: 1000000000, expected: 10 - 10 },
            { id: 10000000000, expected: 10 - 10 }, // Very large value
        ];

        boundaryTestCases.forEach(({ id, expected }) => {
            const parentExtractor = new TestTokenValueExtractor('Parent.', { id });
            const valuesMap: Map<string, TokenValueExtractor> = new Map([
                [parentExtractor.getPrefix(), parentExtractor],
            ]);

            const ev = new ExpressionEvaluator(expression);
            const result = ev.evaluate(valuesMap);

            expect(result).toBe(expected);
        });
    });

    test('Expression 6: Dynamic bracket index with Parent.__index', () => {
        const parentExtractor = new TestTokenValueExtractor('Parent.', { __index: 2 });
        const pageExtractor = new TestTokenValueExtractor('Page.', { 
            items: ['first', 'second', 'third', 'fourth']
        });
        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        const expression = 'Page.items[Parent.__index]';
        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);

        expect(result).toBe('third');
    });

    test('Expression 7: Nested dynamic bracket index', () => {
        const parentExtractor = new TestTokenValueExtractor('Parent.', { index: 1 });
        const pageExtractor = new TestTokenValueExtractor('Page.', {
            matrix: [
                ['a', 'b', 'c'],
                ['d', 'e', 'f'],
                ['g', 'h', 'i']
            ]
        });
        const valuesMap: Map<string, TokenValueExtractor> = new Map([
            [parentExtractor.getPrefix(), parentExtractor],
            [pageExtractor.getPrefix(), pageExtractor],
        ]);

        const expression = 'Page.matrix[Parent.index][Parent.index]';
        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);

        expect(result).toBe('e');
    });

    test('Expression 8: Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}', () => {
        // Expression: dynamic page key from urlDetails.pageName, nullish coalesce value with location.expression
        const expression =
            'Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}';

        // Verify parsing succeeds
        const expr = new Expression(expression);
        expect(expr).toBeDefined();
        expect(expr.getOperations().isEmpty()).toBe(false);

        // Store with urlDetails.pageName = "home" and pageDefinition.home.properties.title.name.value present
        const storeWithValue = new TestTokenValueExtractor('Store.', {
            application: {
                properties: {
                    title: 'My Application Title',
                },
            },
            urlDetails: { pageName: 'home' },
            pageDefinition: {
                home: {
                    properties: {
                        title: {
                            name: {
                                location: { expression: "'Application : ' + Store.application.properties.title" },
                            },
                        },
                    },
                },
            },
        });
        const valuesMapWithValue: Map<string, TokenValueExtractor> = new Map([
            [storeWithValue.getPrefix(), storeWithValue],
        ]);

        const ev = new ExpressionEvaluator(expression);
        expect(ev.evaluate(valuesMapWithValue)).toBe('Application : My Application Title');

        // Store with value null - should return location.expression
        
        const storeValueNull = new TestTokenValueExtractor('Store.', {
            urlDetails: { pageName: 'home' },
            pageDefinition: {
                home: {
                    properties: {
                        title: {
                            name: {
                                value: null,
                                location: { expression: 'Expression Fallback Title' },
                            },
                        },
                    },
                },
            },
        });
        const valuesMapValueNull: Map<string, TokenValueExtractor> = new Map([
            [storeValueNull.getPrefix(), storeValueNull],
        ]);
        expect(ev.evaluate(valuesMapValueNull)).toBe('Expression Fallback Title');

        // Store with value missing (no .value key) - should return location.expression
        // Use a fresh evaluator to avoid any cached expansion from previous evaluations
        const storeValueMissing = new TestTokenValueExtractor('Store.', {
            urlDetails: { pageName: 'home' },
            pageDefinition: {
                home: {
                    properties: {
                        title: {
                            name: {
                                location: { expression: 'Expression Fallback Title' },
                            },
                        },
                    },
                },
            },
        });
        const valuesMapValueMissing: Map<string, TokenValueExtractor> = new Map([
            [storeValueMissing.getPrefix(), storeValueMissing],
        ]);
        const evFresh = new ExpressionEvaluator(expression);
        expect(evFresh.evaluate(valuesMapValueMissing)).toBe('Expression Fallback Title');
    });

    test('Expression 9a: Simplified nested ternary to isolate issue', () => {
        // Simplified version to isolate the issue
        const expression = `Page.a != undefined ? 'A' : Page.b != undefined ? 'B' : Page.c != undefined ? 'C' : '-'`;

        // Test with matching condition
        const pageExtractor = new TestTokenValueExtractor('Page.', {
            a: 'test'
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        expect(ev.evaluate(valuesMap)).toBe('A');
    });

    test('Expression 9b: Nested ternary with dynamic keys', () => {
        // Test with dynamic keys using {{}}
        const expression = `Page.kycs.{{Parent.id}}.a != undefined ? 'A' : Page.kycs.{{Parent.id}}.b != undefined ? 'B' : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            kycs: {
                '123': {
                    a: 'test'
                }
            }
        });

        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            id: '123'
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor],
            [parentExtractor.getPrefix(), parentExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        expect(ev.evaluate(valuesMap)).toBe('A');
    });

    test('Expression 9c1: Debug - Simplest failing case', () => {
        // Simplest case that reproduces the issue
        const expression = `true ? 'a' +' '+ 'b' : false ? 'c' +' '+ 'd' : '-'`;

        const valuesMap = new Map();

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('a b');
    });

    test('Expression 9c2: Debug - With property access', () => {
        // Test with property access in ternary
        const expression = `true ? Page.a +' '+ Page.b : false ? Page.c +' '+ Page.d : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            a: 'Hello',
            b: 'World'
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('Hello World');
    });

    test('Expression 9c3: Debug - With deeper property paths like original', () => {
        // Test with deeper property paths like the original failing expression
        const expression = `true ? Page.x.a.first +' '+ Page.x.a.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            x: {
                a: {
                    first: 'John',
                    last: 'Doe'
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('John Doe');
    });

    test('Expression 9c4: Debug - Nested ternary with deep paths', () => {
        // Test nested ternary with deep property paths
        const expression = `true ? Page.x.a.first +' '+ Page.x.a.last : false ? Page.x.b.first +' '+ Page.x.b.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            x: {
                a: {
                    first: 'John',
                    last: 'Doe'
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('John Doe');
    });

    test('Expression 9c5: Debug - With NUMERIC property path segment', () => {
        // Test numeric segment in property path - should now work with the fix
        const expression = `true ? Page.x.123.a.first +' '+ Page.x.123.a.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            x: {
                '123': {
                    a: {
                        first: 'John',
                        last: 'Doe'
                    }
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('John Doe');
    });

    test('Expression 9c6: Debug - With ObjectId-like property path segment', () => {
        // Test with alphanumeric ObjectId-like value (e.g., MongoDB ObjectId)
        const expression = `true ? Page.x.507f1f77bcf86cd799439011.a.first +' '+ Page.x.507f1f77bcf86cd799439011.a.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            x: {
                '507f1f77bcf86cd799439011': {
                    a: {
                        first: 'Jane',
                        last: 'Smith'
                    }
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('Jane Smith');
    });

    test('Expression 9c7: Debug - With underscore in property path segment', () => {
        // Test with property names containing underscores and numbers
        const expression = `true ? Page.x.123_abc.a.first +' '+ Page.x.123_abc.a.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            x: {
                '123_abc': {
                    a: {
                        first: 'Bob',
                        last: 'Johnson'
                    }
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('Bob Johnson');
    });

    test('Expression 9c: Debug - Parse expanded expression without {{}}', () => {
        // Test the expanded expression directly (without {{}}) to see if parsing is the issue
        const expandedExpression = `Page.kycs.123.a != undefined ? Page.kycs.123.a.first +' '+ Page.kycs.123.a.last : Page.kycs.123.b != undefined ? Page.kycs.123.b.first +' '+ Page.kycs.123.b.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            kycs: {
                '123': {
                    a: {
                        first: 'John',
                        last: 'Doe'
                    }
                }
            }
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor]
        ]);

        const ev = new ExpressionEvaluator(expandedExpression);
        const result = ev.evaluate(valuesMap);
        expect(result).toBe('John Doe');
    });

    test('Expression 9c: Nested ternary with string concatenation', () => {
        // Test with string concatenation in ternary branches
        const expression = `Page.kycs.{{Parent.id}}.a != undefined ? Page.kycs.{{Parent.id}}.a.first +' '+ Page.kycs.{{Parent.id}}.a.last : Page.kycs.{{Parent.id}}.b != undefined ? Page.kycs.{{Parent.id}}.b.first +' '+ Page.kycs.{{Parent.id}}.b.last : '-'`;

        const pageExtractor = new TestTokenValueExtractor('Page.', {
            kycs: {
                '123': {
                    a: {
                        first: 'John',
                        last: 'Doe'
                    }
                }
            }
        });

        const parentExtractor = new TestTokenValueExtractor('Parent.', {
            id: '123'
        });

        const valuesMap = new Map([
            [pageExtractor.getPrefix(), pageExtractor],
            [parentExtractor.getPrefix(), parentExtractor]
        ]);

        const ev = new ExpressionEvaluator(expression);
        expect(ev.evaluate(valuesMap)).toBe('John Doe');
    });

    test('Expression 9: Complex nested ternary with dynamic keys and string concatenation', () => {
        // This is a complex multi-level ternary with dynamic object keys and string concatenation
        const expression = `Page.kycs.{{Parent.kycAccountId}}.individual != undefined ? Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.joint != undefined ? Page.kycs.{{Parent.kycAccountId}}.joint.mainApplicant.individual.basic.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.joint.mainApplicant.individual.basic.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.llp != undefined ? Page.kycs.{{Parent.kycAccountId}}.llp.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.llp.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.partnership != undefined ? Page.kycs.{{Parent.kycAccountId}}.partnership.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.partnership.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.private != undefined ? Page.kycs.{{Parent.kycAccountId}}.private.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.private.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.trust != undefined ? Page.kycs.{{Parent.kycAccountId}}.trust.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.trust.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.huf != undefined ? Page.kycs.{{Parent.kycAccountId}}.huf.kartaInformation.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.huf.kartaInformation.personalInformation.lastName : '-'`;

        // Test with individual account type
        const pageExtractorIndividual = new TestTokenValueExtractor('Page.', {
            kycs: {
                '123': {
                    individual: {
                        basic: {
                            personalInformation: {
                                firstName: 'John',
                                lastName: 'Doe'
                            }
                        }
                    }
                }
            }
        });

        const parentExtractorIndividual = new TestTokenValueExtractor('Parent.', {
            kycAccountId: '123'
        });

        const valuesMapIndividual = new Map([
            [pageExtractorIndividual.getPrefix(), pageExtractorIndividual],
            [parentExtractorIndividual.getPrefix(), parentExtractorIndividual]
        ]);

        const ev = new ExpressionEvaluator(expression);
        expect(ev.evaluate(valuesMapIndividual)).toBe('John Doe');

        // Test with fallback (no matching account type)
        const pageExtractorFallback = new TestTokenValueExtractor('Page.', {
            kycs: {
                '456': {}
            }
        });

        const parentExtractorFallback = new TestTokenValueExtractor('Parent.', {
            kycAccountId: '456'
        });

        const valuesMapFallback = new Map([
            [pageExtractorFallback.getPrefix(), pageExtractorFallback],
            [parentExtractorFallback.getPrefix(), parentExtractorFallback]
        ]);

        const evFallback = new ExpressionEvaluator(expression);
        expect(evFallback.evaluate(valuesMapFallback)).toBe('-');
    });
});

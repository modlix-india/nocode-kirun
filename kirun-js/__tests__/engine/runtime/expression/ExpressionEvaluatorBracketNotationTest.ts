import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';
import { TokenValueExtractor } from '../../../../src/engine/runtime/expression/tokenextractor/TokenValueExtractor';

class TestExtractor extends TokenValueExtractor {
    private data: any;

    constructor(data: any) {
        super();
        this.data = data;
    }

    protected getValueInternal(token: string): any {
        const prefix = this.getPrefix();
        const path = token.substring(prefix.length);
        const parts = TokenValueExtractor['splitPath'](path);
        return this.retrieveElementFrom(token, parts, 0, this.data);
    }

    public getPrefix(): string {
        return 'Context.';
    }

    public getStore(): any {
        return this.data;
    }

    // Expose retrieveElementFrom for testing
    public retrieveElementFrom(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): any {
        return super.retrieveElementFrom(token, parts, partNumber, jsonElement);
    }
}

describe('ExpressionEvaluator with Bracket Notation', () => {
    let testData: any;
    let extractor: TestExtractor;
    let evaluatorMap: Map<string, TokenValueExtractor>;

    beforeEach(() => {
        testData = {
            obj: {
                'mail.props.port': 587,
                'mail.props.host': 'smtp.example.com',
                'api.key.secret': 'secret123',
                simple: 'value',
                count: 100,
            },
            arr: [10, 20, 30, 40, 50],
            nested: {
                'field.with.dots': 'nestedValue',
                regular: 'regularValue',
            },
        };
        extractor = new TestExtractor(testData);
        evaluatorMap = new Map([['Context.', extractor]]);
    });

    describe('Basic bracket notation with dotted keys', () => {
        test('should access property with double quotes', () => {
            const result = new ExpressionEvaluator('Context.obj["mail.props.port"]').evaluate(
                evaluatorMap,
            );
            expect(result).toBe(587);
        });

        test('should access property with single quotes', () => {
            const result = new ExpressionEvaluator("Context.obj['mail.props.host']").evaluate(
                evaluatorMap,
            );
            expect(result).toBe('smtp.example.com');
        });

        test('should access nested property with dotted key', () => {
            const result = new ExpressionEvaluator(
                "Context.nested['field.with.dots']",
            ).evaluate(evaluatorMap);
            expect(result).toBe('nestedValue');
        });
    });

    describe('Comparison operators with bracket notation', () => {
        test('should work with equality operator (=)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] = 587',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with not equal operator (!=)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] != 500',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with greater than operator (>)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] > 500',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with greater than or equal operator (>=)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] >= 587',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with less than operator (<)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] < 600',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with less than or equal operator (<=)', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] <= 587',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });
    });

    describe('Arithmetic operators with bracket notation', () => {
        test('should work with addition', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] + 13',
            ).evaluate(evaluatorMap);
            expect(result).toBe(600);
        });

        test('should work with subtraction', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] - 87',
            ).evaluate(evaluatorMap);
            expect(result).toBe(500);
        });

        test('should work with multiplication', () => {
            const result = new ExpressionEvaluator('Context.obj["count"] * 2').evaluate(
                evaluatorMap,
            );
            expect(result).toBe(200);
        });

        test('should work with division', () => {
            const result = new ExpressionEvaluator('Context.obj["count"] / 4').evaluate(
                evaluatorMap,
            );
            expect(result).toBe(25);
        });
    });

    describe('Ternary operator with bracket notation', () => {
        test('should work with ternary operator', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] > 500 ? "high" : "low"',
            ).evaluate(evaluatorMap);
            expect(result).toBe('high');
        });

        test('should return false branch of ternary', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] < 500 ? "high" : "low"',
            ).evaluate(evaluatorMap);
            expect(result).toBe('low');
        });
    });

    describe('Logical operators with bracket notation', () => {
        test('should work with logical AND', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] > 500 and Context.obj["count"] = 100',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with logical OR', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] < 500 or Context.obj["count"] = 100',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with logical NOT', () => {
            const result = new ExpressionEvaluator(
                'not Context.obj["mail.props.port"] < 500',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });
    });

    describe('Mixed bracket and dot notation', () => {
        test('should work with bracket notation followed by dot notation', () => {
            testData.obj['mail.props.port'] = { subfield: 'subvalue' };
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"].subfield',
            ).evaluate(evaluatorMap);
            expect(result).toBe('subvalue');
        });

        test('should work with dot notation followed by bracket notation', () => {
            const result = new ExpressionEvaluator(
                "Context.nested['field.with.dots']",
            ).evaluate(evaluatorMap);
            expect(result).toBe('nestedValue');
        });
    });

    describe('Array bracket notation (pre-existing functionality)', () => {
        test('should work with array index access', () => {
            const result = new ExpressionEvaluator('Context.arr[0]').evaluate(evaluatorMap);
            expect(result).toBe(10);
        });

        test('should work with array index and comparison', () => {
            const result = new ExpressionEvaluator('Context.arr[0] = 10').evaluate(
                evaluatorMap,
            );
            expect(result).toBe(true);
        });

        test('should work with array index and arithmetic', () => {
            const result = new ExpressionEvaluator('Context.arr[1] + Context.arr[2]').evaluate(
                evaluatorMap,
            );
            expect(result).toBe(50);
        });
    });

    describe('Complex expressions', () => {
        test('should work with multiple bracket notations in one expression', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.port"] + Context.obj["count"]',
            ).evaluate(evaluatorMap);
            expect(result).toBe(687);
        });

        test('should work with bracket notation in nested expression', () => {
            const result = new ExpressionEvaluator(
                '(Context.obj["mail.props.port"] > 500) and (Context.obj["count"] < 200)',
            ).evaluate(evaluatorMap);
            expect(result).toBe(true);
        });

        test('should work with string concatenation', () => {
            const result = new ExpressionEvaluator(
                'Context.obj["mail.props.host"] + ":587"',
            ).evaluate(evaluatorMap);
            expect(result).toBe('smtp.example.com:587');
        });
    });

    describe('Edge cases', () => {
        test('should handle property with multiple dots', () => {
            testData.obj['a.b.c.d'] = 'deepValue';
            const result = new ExpressionEvaluator('Context.obj["a.b.c.d"]').evaluate(
                evaluatorMap,
            );
            expect(result).toBe('deepValue');
        });

        test('should handle empty string key (though unusual)', () => {
            testData.obj[''] = 'emptyKey';
            const result = new ExpressionEvaluator('Context.obj[""]').evaluate(evaluatorMap);
            expect(result).toBe('emptyKey');
        });

        test('should handle key with special characters', () => {
            testData.obj['key@#$%'] = 'specialValue';
            const result = new ExpressionEvaluator('Context.obj["key@#$%"]').evaluate(
                evaluatorMap,
            );
            expect(result).toBe('specialValue');
        });
    });
});

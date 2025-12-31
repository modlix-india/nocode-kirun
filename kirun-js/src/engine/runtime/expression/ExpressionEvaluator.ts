import { ExecutionException } from '../../exception/ExecutionException';
import { LinkedList } from '../../util/LinkedList';

// Simple performance timer for expression evaluation
class ExprPerfTimer {
    private static timings: Map<string, { count: number; total: number }> = new Map();
    private static enabled = false;
    
    static enable() { this.enabled = true; }
    static disable() { this.enabled = false; }
    static isEnabled() { return this.enabled; }
    
    static start(label: string): number {
        return this.enabled ? performance.now() : 0;
    }
    
    static end(label: string, startTime: number) {
        if (!this.enabled) return;
        const elapsed = performance.now() - startTime;
        const existing = this.timings.get(label) || { count: 0, total: 0 };
        existing.count++;
        existing.total += elapsed;
        this.timings.set(label, existing);
    }
    
    static report() {
        if (!this.enabled) return;
        console.log('\n=== Expression Evaluator Performance ===');
        const sorted = Array.from(this.timings.entries())
            .sort((a, b) => b[1].total - a[1].total);
        for (const [label, { count, total }] of sorted) {
            console.log(`${label}: ${total.toFixed(2)}ms (${count} calls, avg ${(total/count).toFixed(3)}ms)`);
        }
        console.log('=========================================\n');
    }
    
    static reset() { this.timings.clear(); }
}

export { ExprPerfTimer };
import { StringFormatter } from '../../util/string/StringFormatter';
import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';
import { Expression } from './Expression';
import { ExpressionToken } from './ExpressionToken';
import { ExpressionTokenValue } from './ExpressionTokenValue';
import { Operation } from './Operation';
import { LogicalNullishCoalescingOperator } from './operators/binary';
import {
    ArithmeticAdditionOperator,
    ArithmeticDivisionOperator,
    ArithmeticIntegerDivisionOperator,
    ArithmeticModulusOperator,
    ArithmeticMultiplicationOperator,
    ArithmeticSubtractionOperator,
    ArrayOperator,
    ArrayRangeOperator,
    BinaryOperator,
    BitwiseAndOperator,
    BitwiseLeftShiftOperator,
    BitwiseOrOperator,
    BitwiseRightShiftOperator,
    BitwiseUnsignedRightShiftOperator,
    BitwiseXorOperator,
    LogicalAndOperator,
    LogicalEqualOperator,
    LogicalGreaterThanEqualOperator,
    LogicalGreaterThanOperator,
    LogicalLessThanEqualOperator,
    LogicalLessThanOperator,
    LogicalNotEqualOperator,
    LogicalOrOperator,
    ObjectOperator,
} from './operators/binary/';
import {
    ArithmeticUnaryMinusOperator,
    ArithmeticUnaryPlusOperator,
    BitwiseComplementOperator,
    LogicalNotOperator,
    UnaryOperator,
} from './operators/unary';
import { LiteralTokenValueExtractor } from './tokenextractor/LiteralTokenValueExtractor';
import { TokenValueExtractor } from './tokenextractor/TokenValueExtractor';
import { Tuple2 } from '../../util/Tuples';
import { ConditionalTernaryOperator } from './operators/ternary';
import { TernaryOperator } from './operators/ternary/TernaryOperator';
import { ExpressionInternalValueExtractor } from './tokenextractor/ExpressionInternalValueExtractor';

export class ExpressionEvaluator {
    // Static cache for parsed expressions to avoid re-parsing the same expression
    private static expressionCache: Map<string, Expression> = new Map();
    
    // Counter for generating unique keys (faster than Date.now() + random)
    private static keyCounter = 0;

    private static getCachedExpression(expressionString: string): Expression {
        let exp = ExpressionEvaluator.expressionCache.get(expressionString);
        if (!exp) {
            exp = new Expression(expressionString);
            ExpressionEvaluator.expressionCache.set(expressionString, exp);
        }
        return exp;
    }

    private static readonly UNARY_OPERATORS_MAP: Map<Operation, UnaryOperator> = new Map([
        [Operation.UNARY_BITWISE_COMPLEMENT, new BitwiseComplementOperator()],
        [Operation.UNARY_LOGICAL_NOT, new LogicalNotOperator()],
        [Operation.UNARY_MINUS, new ArithmeticUnaryMinusOperator()],
        [Operation.UNARY_PLUS, new ArithmeticUnaryPlusOperator()],
    ]);

    private static readonly TERNARY_OPERATORS_MAP: Map<Operation, TernaryOperator> = new Map([
        [Operation.CONDITIONAL_TERNARY_OPERATOR, new ConditionalTernaryOperator()],
    ]);

    private static readonly BINARY_OPERATORS_MAP: Map<Operation, BinaryOperator> = new Map([
        [Operation.ADDITION, new ArithmeticAdditionOperator()],
        [Operation.DIVISION, new ArithmeticDivisionOperator()],
        [Operation.INTEGER_DIVISION, new ArithmeticIntegerDivisionOperator()],
        [Operation.MOD, new ArithmeticModulusOperator()],
        [Operation.MULTIPLICATION, new ArithmeticMultiplicationOperator()],
        [Operation.SUBTRACTION, new ArithmeticSubtractionOperator()],

        [Operation.BITWISE_AND, new BitwiseAndOperator()],
        [Operation.BITWISE_LEFT_SHIFT, new BitwiseLeftShiftOperator()],
        [Operation.BITWISE_OR, new BitwiseOrOperator()],
        [Operation.BITWISE_RIGHT_SHIFT, new BitwiseRightShiftOperator()],
        [Operation.BITWISE_UNSIGNED_RIGHT_SHIFT, new BitwiseUnsignedRightShiftOperator()],
        [Operation.BITWISE_XOR, new BitwiseXorOperator()],

        [Operation.AND, new LogicalAndOperator()],
        [Operation.EQUAL, new LogicalEqualOperator()],
        [Operation.GREATER_THAN, new LogicalGreaterThanOperator()],
        [Operation.GREATER_THAN_EQUAL, new LogicalGreaterThanEqualOperator()],
        [Operation.LESS_THAN, new LogicalLessThanOperator()],
        [Operation.LESS_THAN_EQUAL, new LogicalLessThanEqualOperator()],
        [Operation.OR, new LogicalOrOperator()],
        [Operation.NOT_EQUAL, new LogicalNotEqualOperator()],
        [Operation.NULLISH_COALESCING_OPERATOR, new LogicalNullishCoalescingOperator()],

        [Operation.ARRAY_OPERATOR, new ArrayOperator()],
        [Operation.ARRAY_RANGE_INDEX_OPERATOR, new ArrayRangeOperator()],
        [Operation.OBJECT_OPERATOR, new ObjectOperator()],
    ]);

    private static readonly UNARY_OPERATORS_MAP_KEY_SET: Set<Operation> = new Set(
        ExpressionEvaluator.UNARY_OPERATORS_MAP.keys(),
    );

    // ==================== FAST PATH DETECTION CACHES ====================
    
    // Expression pattern types for fast path routing
    private static readonly PATTERN_UNKNOWN = 0;
    private static readonly PATTERN_LITERAL = 1;
    private static readonly PATTERN_SIMPLE_PATH = 2;
    private static readonly PATTERN_SIMPLE_ARRAY_ACCESS = 3;
    private static readonly PATTERN_SIMPLE_COMPARISON = 4;
    private static readonly PATTERN_SIMPLE_TERNARY = 5;
    
    // Cache for expression pattern detection
    private static patternCache: Map<string, number> = new Map();
    
    // Regex patterns for fast detection (compiled once)
    private static readonly LITERAL_TRUE = 'true';
    private static readonly LITERAL_FALSE = 'false';
    private static readonly LITERAL_NULL = 'null';
    private static readonly LITERAL_UNDEFINED = 'undefined';
    private static readonly NUMBER_REGEX = /^-?\d+(\.\d+)?$/;
    private static readonly SINGLE_QUOTE_STRING_REGEX = /^'([^'\\]|\\.)*'$/;
    private static readonly DOUBLE_QUOTE_STRING_REGEX = /^"([^"\\]|\\.)*"$/;
    
    // Simple path regex: Store.path.to.value or Store.path[0].value (no nested expressions)
    private static readonly SIMPLE_PATH_REGEX = /^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*|\[\d+\])*$/;
    
    // Detect expression pattern type for fast path routing
    private static detectPattern(exp: Expression): number {
        const expStr = exp.getExpression();
        
        // Check cache
        const cached = ExpressionEvaluator.patternCache.get(expStr);
        if (cached !== undefined) return cached;
        
        let pattern = ExpressionEvaluator.PATTERN_UNKNOWN;
        
        // 1. Check for literals (no parsing needed)
        if (expStr === ExpressionEvaluator.LITERAL_TRUE || 
            expStr === ExpressionEvaluator.LITERAL_FALSE ||
            expStr === ExpressionEvaluator.LITERAL_NULL ||
            expStr === ExpressionEvaluator.LITERAL_UNDEFINED ||
            ExpressionEvaluator.NUMBER_REGEX.test(expStr) ||
            ExpressionEvaluator.SINGLE_QUOTE_STRING_REGEX.test(expStr) ||
            ExpressionEvaluator.DOUBLE_QUOTE_STRING_REGEX.test(expStr)) {
            pattern = ExpressionEvaluator.PATTERN_LITERAL;
        }
        // 2. Check for simple path (must have dot, no complex operators)
        // Exclude expressions with range operator '..' or nested expressions '{{'
        else if (expStr.includes('.') && !expStr.includes('{{') && !expStr.includes('..')) {
            const ops = exp.getOperationsArray();
            const tokens = exp.getTokensArray();
            
            // Must have only OBJECT_OPERATOR or ARRAY_OPERATOR
            let isSimplePath = ops.length > 0;
            for (const op of ops) {
                if (op !== Operation.OBJECT_OPERATOR && op !== Operation.ARRAY_OPERATOR) {
                    isSimplePath = false;
                    break;
                }
            }
            
            // No tokens can be nested expressions
            if (isSimplePath) {
                for (const token of tokens) {
                    if (token instanceof Expression) {
                        isSimplePath = false;
                        break;
                    }
                }
            }
            
            if (isSimplePath) {
                pattern = ExpressionEvaluator.PATTERN_SIMPLE_PATH;
            } else {
                // 3. Check for simple ternary: condition ? value1 : value2
                pattern = ExpressionEvaluator.detectTernaryOrComparison(exp, ops);
            }
        }
        // Check for range operator expressions (need full evaluation)
        else if (expStr.includes('..')) {
            pattern = ExpressionEvaluator.PATTERN_UNKNOWN;
        }
        // 4. Check ternary/comparison for expressions without dots
        else if (!expStr.includes('{{')) {
            const ops = exp.getOperationsArray();
            pattern = ExpressionEvaluator.detectTernaryOrComparison(exp, ops);
        }
        
        ExpressionEvaluator.patternCache.set(expStr, pattern);
        return pattern;
    }
    
    // Detect simple ternary or comparison patterns
    private static detectTernaryOrComparison(exp: Expression, ops: Operation[]): number {
        const tokens = exp.getTokensArray();
        
        // Check for nested expressions in tokens
        for (const token of tokens) {
            if (token instanceof Expression) {
                return ExpressionEvaluator.PATTERN_UNKNOWN;
            }
        }
        
        // Check for simple ternary: exactly one CONDITIONAL_TERNARY_OPERATOR and simple tokens
        if (ops.length === 1 && ops[0] === Operation.CONDITIONAL_TERNARY_OPERATOR) {
            return ExpressionEvaluator.PATTERN_SIMPLE_TERNARY;
        }
        
        // Check for simple comparison: exactly one EQUAL or NOT_EQUAL
        if (ops.length === 1 && (ops[0] === Operation.EQUAL || ops[0] === Operation.NOT_EQUAL)) {
            return ExpressionEvaluator.PATTERN_SIMPLE_COMPARISON;
        }
        
        return ExpressionEvaluator.PATTERN_UNKNOWN;
    }

    // ==================== FAST PATH EVALUATORS ====================
    
    // Fast path for literals
    private static evaluateLiteral(expStr: string): any {
        if (expStr === ExpressionEvaluator.LITERAL_TRUE) return true;
        if (expStr === ExpressionEvaluator.LITERAL_FALSE) return false;
        if (expStr === ExpressionEvaluator.LITERAL_NULL) return null;
        if (expStr === ExpressionEvaluator.LITERAL_UNDEFINED) return undefined;
        
        // Number
        if (ExpressionEvaluator.NUMBER_REGEX.test(expStr)) {
            return expStr.includes('.') ? Number.parseFloat(expStr) : Number.parseInt(expStr, 10);
        }
        
        // Quoted string - just remove quotes (no escape processing to match original behavior)
        // The LiteralTokenValueExtractor also doesn't process escapes for non-standard sequences
        if (ExpressionEvaluator.SINGLE_QUOTE_STRING_REGEX.test(expStr)) {
            return expStr.slice(1, -1);
        }
        if (ExpressionEvaluator.DOUBLE_QUOTE_STRING_REGEX.test(expStr)) {
            return expStr.slice(1, -1);
        }
        
        return undefined;
    }
    
    // Fast path for simple paths (Store.path.to.value)
    private evaluateSimplePath(exp: Expression, valuesMap: Map<string, TokenValueExtractor>): any {
        const pathStr = exp.getExpression();
        const dotIdx = pathStr.indexOf('.');
        if (dotIdx === -1) return undefined;
        
        const prefix = pathStr.substring(0, dotIdx + 1);
        const extractor = valuesMap.get(prefix);
        if (!extractor) return undefined;
        
        return extractor.getValue(pathStr);
    }
    
    // Fast path for simple comparisons (path = value, path != value)
    private evaluateSimpleComparison(
        exp: Expression, 
        valuesMap: Map<string, TokenValueExtractor>
    ): any {
        const ops = exp.getOperationsArray();
        const tokens = exp.getTokensArray();
        
        if (tokens.length !== 2 || ops.length !== 1) return undefined;
        
        const v1 = this.getTokenValue(tokens[1], valuesMap);
        const v2 = this.getTokenValue(tokens[0], valuesMap);
        
        if (ops[0] === Operation.EQUAL) {
            return v1 == v2;
        } else if (ops[0] === Operation.NOT_EQUAL) {
            return v1 != v2;
        }
        
        return undefined;
    }
    
    // Fast path for simple ternary (condition ? value1 : value2)
    private evaluateSimpleTernary(
        exp: Expression, 
        valuesMap: Map<string, TokenValueExtractor>
    ): any {
        const tokens = exp.getTokensArray();
        
        if (tokens.length !== 3) return undefined;
        
        const condition = this.getTokenValue(tokens[2], valuesMap);
        const trueValue = this.getTokenValue(tokens[1], valuesMap);
        const falseValue = this.getTokenValue(tokens[0], valuesMap);
        
        return condition ? trueValue : falseValue;
    }
    
    // Helper to get value from a token (for fast paths)
    private getTokenValue(token: ExpressionToken, valuesMap: Map<string, TokenValueExtractor>): any {
        if (token instanceof ExpressionTokenValue) {
            return token.getElement();
        }
        
        const tokenStr = token.getExpression();
        
        // Check if it's a literal
        const literalVal = ExpressionEvaluator.evaluateLiteral(tokenStr);
        if (literalVal !== undefined || tokenStr === 'undefined' || tokenStr === 'null') {
            return literalVal;
        }
        
        // Check if it's a path
        const dotIdx = tokenStr.indexOf('.');
        if (dotIdx !== -1) {
            const prefix = tokenStr.substring(0, dotIdx + 1);
            const extractor = valuesMap.get(prefix);
            if (extractor) {
                return extractor.getValue(tokenStr);
            }
        }
        
        // Fall back to literal extractor
        return LiteralTokenValueExtractor.INSTANCE.getValue(tokenStr);
    }

    private expression: string;
    private exp?: Expression;
    private internalTokenValueExtractor: ExpressionInternalValueExtractor =
        new ExpressionInternalValueExtractor();

    public constructor(exp: Expression | string) {
        if (exp instanceof Expression) {
            this.exp = exp;
            this.expression = this.exp.getExpression();
        } else {
            this.expression = exp;
        }
    }

    public evaluate(valuesMap: Map<string, TokenValueExtractor>): any {
        const t0 = ExprPerfTimer.start('evaluate');
        
        const t1 = ExprPerfTimer.start('processNestingExpression');
        const tuple: Tuple2<string, Expression> = this.processNestingExpression(
            this.expression,
            valuesMap,
        );
        ExprPerfTimer.end('processNestingExpression', t1);
        
        this.expression = tuple.getT1();
        this.exp = tuple.getT2();
        
        // Detect pattern type for fast path routing
        const pattern = ExpressionEvaluator.detectPattern(this.exp);
        
        // Fast path 1: Literals (true, false, numbers, strings)
        if (pattern === ExpressionEvaluator.PATTERN_LITERAL) {
            const t2 = ExprPerfTimer.start('evaluateLiteral');
            const result = ExpressionEvaluator.evaluateLiteral(this.expression);
            ExprPerfTimer.end('evaluateLiteral', t2);
            ExprPerfTimer.end('evaluate', t0);
            return result;
        }
        
        // Fast path 2: Simple paths (Store.path.to.value)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_PATH) {
            const t2 = ExprPerfTimer.start('evaluateSimplePath');
            const result = this.evaluateSimplePath(this.exp, valuesMap);
            ExprPerfTimer.end('evaluateSimplePath', t2);
            ExprPerfTimer.end('evaluate', t0);
            return result;
        }
        
        // Fast path 3: Simple comparison (path = value)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_COMPARISON) {
            const t2 = ExprPerfTimer.start('evaluateSimpleComparison');
            const result = this.evaluateSimpleComparison(this.exp, valuesMap);
            ExprPerfTimer.end('evaluateSimpleComparison', t2);
            ExprPerfTimer.end('evaluate', t0);
            return result;
        }
        
        // Fast path 4: Simple ternary (condition ? value1 : value2)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_TERNARY) {
            const t2 = ExprPerfTimer.start('evaluateSimpleTernary');
            const result = this.evaluateSimpleTernary(this.exp, valuesMap);
            ExprPerfTimer.end('evaluateSimpleTernary', t2);
            ExprPerfTimer.end('evaluate', t0);
            return result;
        }
        
        // Full evaluation path for complex expressions
        valuesMap = new Map(valuesMap.entries());
        valuesMap.set(
            this.internalTokenValueExtractor.getPrefix(),
            this.internalTokenValueExtractor,
        );

        const result = this.evaluateExpression(this.exp, valuesMap);
        ExprPerfTimer.end('evaluate', t0);
        return result;
    }

    private processNestingExpression(
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
    ): Tuple2<string, Expression> {
        let start = 0;
        let i = 0;

        const tuples: LinkedList<Tuple2<number, number>> = new LinkedList();

        while (i < expression.length - 1) {
            if (expression.charAt(i) == '{' && expression.charAt(i + 1) == '{') {
                if (start == 0) tuples.push(new Tuple2(i + 2, -1));

                start++;
                i++;
            } else if (expression.charAt(i) == '}' && expression.charAt(i + 1) == '}') {
                start--;

                if (start < 0)
                    throw new ExpressionEvaluationException(
                        expression,
                        'Expecting {{ nesting path operator to be started before closing',
                    );

                if (start == 0) {
                    tuples.push(tuples.pop().setT2(i));
                }
                i++;
            }
            i++;
        }

        let newExpression = this.replaceNestingExpression(expression, valuesMap, tuples);

        return new Tuple2(newExpression, ExpressionEvaluator.getCachedExpression(newExpression));
    }

    private replaceNestingExpression(
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
        tuples: LinkedList<Tuple2<number, number>>,
    ): string {
        let newExpression = expression;

        for (let tuple of tuples.toArray()) {
            if (tuple.getT2() == -1)
                throw new ExpressionEvaluationException(
                    expression,
                    'Expecting }} nesting path operator to be closed',
                );

            let expStr: string = new ExpressionEvaluator(
                newExpression.substring(tuple.getT1(), tuple.getT2()),
            ).evaluate(valuesMap);

            newExpression =
                newExpression.substring(0, tuple.getT1() - 2) +
                expStr +
                newExpression.substring(tuple.getT2() + 2);
        }
        return newExpression;
    }

    public getExpression(): Expression {
        if (!this.exp) this.exp = ExpressionEvaluator.getCachedExpression(this.expression);

        return this.exp;
    }

    public getExpressionString(): string {
        return this.expression;
    }

    private evaluateExpression(exp: Expression, valuesMap: Map<string, TokenValueExtractor>): any {
        const t0 = ExprPerfTimer.start('evaluateExpression');
        
        // Use cached arrays for fast evaluation (no LinkedList traversal)
        const opsArray: Operation[] = exp.getOperationsArray();
        const tokensSource: ExpressionToken[] = exp.getTokensArray();
        const workingStack: ExpressionToken[] = [];
        
        // Context for tracking indices - passed to helper methods
        const ctx = { opIdx: 0, srcIdx: 0 };
        
        // Pop from working stack first (LIFO for results), then from source array
        const popToken = (): ExpressionToken => {
            if (workingStack.length > 0) {
                return workingStack.pop()!;
            }
            return tokensSource[ctx.srcIdx++];
        };
        
        // Pop operation from source array
        const popOp = (): Operation | undefined => {
            if (ctx.opIdx >= opsArray.length) return undefined;
            return opsArray[ctx.opIdx++];
        };
        
        // Peek at next operation without consuming
        const peekOp = (): Operation | undefined => {
            if (ctx.opIdx >= opsArray.length) return undefined;
            return opsArray[ctx.opIdx];
        };
        
        // Check if there are more tokens available
        const hasMoreTokens = (): boolean => {
            return workingStack.length > 0 || ctx.srcIdx < tokensSource.length;
        };

        while (ctx.opIdx < opsArray.length) {
            let operator: Operation = popOp()!;
            let token: ExpressionToken = popToken();

            if (ExpressionEvaluator.UNARY_OPERATORS_MAP_KEY_SET.has(operator)) {
                const t1 = ExprPerfTimer.start('op.unary');
                workingStack.push(
                    this.applyUnaryOperation(operator, this.getValueFromToken(valuesMap, token)),
                );
                ExprPerfTimer.end('op.unary', t1);
            } else if (
                operator == Operation.OBJECT_OPERATOR ||
                operator == Operation.ARRAY_OPERATOR
            ) {
                const t2 = ExprPerfTimer.start('op.objectArray');
                this.processObjectOrArrayOperatorIndexed(
                    valuesMap, opsArray, tokensSource, workingStack, ctx, operator, token, popToken, popOp, peekOp, hasMoreTokens
                );
                ExprPerfTimer.end('op.objectArray', t2);
            } else if (operator == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                const t3 = ExprPerfTimer.start('op.ternary');
                const token2: ExpressionToken = popToken();
                const token3: ExpressionToken = popToken();
                let v1 = this.getValueFromToken(valuesMap, token3);
                let v2 = this.getValueFromToken(valuesMap, token2);
                let v3 = this.getValueFromToken(valuesMap, token);
                workingStack.push(this.applyTernaryOperation(operator, v1, v2, v3));
                ExprPerfTimer.end('op.ternary', t3);
            } else {
                const t4 = ExprPerfTimer.start('op.binary');
                const token2: ExpressionToken = popToken();
                let v1 = this.getValueFromToken(valuesMap, token2);
                let v2 = this.getValueFromToken(valuesMap, token);
                workingStack.push(this.applyBinaryOperation(operator, v1, v2));
                ExprPerfTimer.end('op.binary', t4);
            }
        }
        
        // Collect remaining source tokens
        while (ctx.srcIdx < tokensSource.length) {
            workingStack.push(tokensSource[ctx.srcIdx++]);
        }

        if (workingStack.length === 0)
            throw new ExecutionException(
                StringFormatter.format('Expression : $ evaluated to null', exp),
            );

        if (workingStack.length !== 1)
            throw new ExecutionException(
                StringFormatter.format('Expression : $ evaluated multiple values $', exp, workingStack),
            );

        const token: ExpressionToken = workingStack[0];
        let result: any;
        if (token instanceof ExpressionTokenValue) result = token.getElement();
        else if (token instanceof Expression) result = this.evaluateExpression(token, valuesMap);
        else result = this.getValueFromToken(valuesMap, token);
        
        ExprPerfTimer.end('evaluateExpression', t0);
        return result;
    }

    private processObjectOrArrayOperatorIndexed(
        valuesMap: Map<string, TokenValueExtractor>,
        opsArray: Operation[],
        tokensSource: ExpressionToken[],
        workingStack: ExpressionToken[],
        ctx: { opIdx: number; srcIdx: number },
        operator: Operation | undefined,
        token: ExpressionToken | undefined,
        popToken: () => ExpressionToken,
        popOp: () => Operation | undefined,
        peekOp: () => Operation | undefined,
        hasMoreTokens: () => boolean,
    ): void {
        const objTokens: ExpressionToken[] = [];
        const objOperations: Operation[] = [];

        if (!operator || !token) return;

        do {
            objOperations.push(operator);
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token, valuesMap),
                    ),
                );
            else if (token) objTokens.push(token);
            
            token = hasMoreTokens() ? popToken() : undefined;
            operator = popOp();
        } while (operator == Operation.OBJECT_OPERATOR || operator == Operation.ARRAY_OPERATOR);

        if (token) {
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token, valuesMap),
                    ),
                );
            else objTokens.push(token);
        }

        // If we consumed an operator that's not OBJECT/ARRAY, put the index back
        if (operator !== undefined) {
            ctx.opIdx--;
        }

        // Process collected tokens and operations (in reverse order since we used push)
        let objTokenIdx = objTokens.length - 1;
        let objOpIdx = objOperations.length - 1;
        
        let objToken: ExpressionToken = objTokens[objTokenIdx--];

        if (
            objToken instanceof ExpressionTokenValue &&
            typeof objToken.getTokenValue() === 'object'
        ) {
            const key = '_k' + (ExpressionEvaluator.keyCounter++);
            this.internalTokenValueExtractor.addValue(key, objToken.getTokenValue());
            objToken = new ExpressionToken(ExpressionInternalValueExtractor.PREFIX + key);
        }

        // Use string concatenation instead of StringBuilder (V8 optimizes this well)
        let str: string = objToken instanceof ExpressionTokenValue
            ? objToken.getTokenValue()
            : objToken.toString();

        while (objTokenIdx >= 0) {
            objToken = objTokens[objTokenIdx--];
            operator = objOperations[objOpIdx--];
            const tokenVal = objToken instanceof ExpressionTokenValue
                ? objToken.getTokenValue()
                : objToken.toString();
            str = str + operator!.getOperator() + tokenVal + (operator == Operation.ARRAY_OPERATOR ? ']' : '');
        }
        let key: string = str.substring(0, str.indexOf('.') + 1);
        if (key.length > 2 && valuesMap.has(key))
            workingStack.push(new ExpressionTokenValue(str, this.getValue(str, valuesMap)));
        else {
            let v: any;
            try {
                v = LiteralTokenValueExtractor.INSTANCE.getValue(str);
            } catch (err) {
                v = str;
            }
            workingStack.push(new ExpressionTokenValue(str, v));
        }
    }

    private applyTernaryOperation(operator: Operation, v1: any, v2: any, v3: any): ExpressionToken {
        let op: TernaryOperator | undefined =
            ExpressionEvaluator.TERNARY_OPERATORS_MAP.get(operator);

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format('No operator found to evaluate $', this.getExpression()),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2, v3));
    }

    private applyBinaryOperation(operator: Operation, v1: any, v2: any): ExpressionToken {
        let typv1: string = typeof v1;
        let typv2: string = typeof v2;

        let op: BinaryOperator | undefined = ExpressionEvaluator.BINARY_OPERATORS_MAP.get(operator);

        if (
            (typv1 === 'object' || typv2 === 'object') &&
            operator !== Operation.EQUAL &&
            operator !== Operation.NOT_EQUAL &&
            operator !== Operation.NULLISH_COALESCING_OPERATOR &&
            operator !== Operation.AND &&
            operator !== Operation.OR
        )
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'Cannot evaluate expression $ $ $',
                    v1,
                    operator.getOperator(),
                    v2,
                ),
            );

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'No operator found to evaluate $ $ $',
                    v1,
                    operator.getOperator(),
                    v2,
                ),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2));
    }

    private applyUnaryOperation(operator: Operation, value: any): ExpressionToken {
        let typv: string = typeof value;

        if (
            operator.getOperator() != Operation.NOT.getOperator() &&
            operator.getOperator() != Operation.UNARY_LOGICAL_NOT.getOperator() &&
            (typv === 'object' || Array.isArray(value))
        )
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'The operator $ cannot be applied to $',
                    operator.getOperator(),
                    value,
                ),
            );

        let op: UnaryOperator | undefined = ExpressionEvaluator.UNARY_OPERATORS_MAP.get(operator);

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'No Unary operator $ is found to apply on $',
                    operator.getOperator(),
                    value,
                ),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(value));
    }

    private getValueFromToken(
        valuesMap: Map<string, TokenValueExtractor>,
        token: ExpressionToken,
    ): any {
        if (token instanceof Expression) {
            return this.evaluateExpression(token, valuesMap);
        } else if (token instanceof ExpressionTokenValue) {
            return token.getElement();
        }
        return this.getValue(token.getExpression(), valuesMap);
    }

    private getValue(path: string, valuesMap: Map<string, TokenValueExtractor>): any {
        const pathPrefix: string = path.substring(0, path.indexOf('.') + 1);
        if (valuesMap.has(pathPrefix)) {
            return valuesMap.get(pathPrefix)!.getValue(path);
        }

        return LiteralTokenValueExtractor.INSTANCE.getValueFromExtractors(path, valuesMap);
    }
}



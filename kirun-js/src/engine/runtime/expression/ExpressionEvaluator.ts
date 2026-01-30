import { ExecutionException } from '../../exception/ExecutionException';
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

    // Dedupe error logging (same expression+inner can error many times in array contexts)
    private static loggedErrorKeys: Set<string> = new Set();

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
        try{
        // Set valuesMap on all extractors so they can resolve dynamic bracket indices
        valuesMap.forEach(extractor => {
            extractor.setValuesMap(valuesMap);
        });

        const tuple: Tuple2<string, Expression> = this.processNestingExpression(
            this.expression,
            valuesMap,
        );
        // Use local expanded form so each evaluate() re-expands with current valuesMap (do not mutate this.expression)
        const expandedExpression = tuple.getT1();
        const expandedExp = tuple.getT2();

        // Detect pattern type for fast path routing
        const pattern = ExpressionEvaluator.detectPattern(expandedExp);

        // Fast path 1: Literals (true, false, numbers, strings)
        if (pattern === ExpressionEvaluator.PATTERN_LITERAL) {
            return ExpressionEvaluator.evaluateLiteral(expandedExpression);
        }

        // Fast path 2: Simple paths (Store.path.to.value)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_PATH) {
            return this.evaluateSimplePath(expandedExp, valuesMap);
        }

        // Fast path 3: Simple comparison (path = value)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_COMPARISON) {
            return this.evaluateSimpleComparison(expandedExp, valuesMap);
        }

        // Fast path 4: Simple ternary (condition ? value1 : value2)
        if (pattern === ExpressionEvaluator.PATTERN_SIMPLE_TERNARY) {
            return this.evaluateSimpleTernary(expandedExp, valuesMap);
        }

        // Full evaluation path for complex expressions
        valuesMap = new Map(valuesMap.entries());
        valuesMap.set(
            this.internalTokenValueExtractor.getPrefix(),
            this.internalTokenValueExtractor,
        );
        // Also set valuesMap on the internal extractor
        this.internalTokenValueExtractor.setValuesMap(valuesMap);

        return this.evaluateExpression(expandedExp, valuesMap);
        }
        catch(err) {
            if (!(err as any)._exprErrorLogged) {
                const errKey = `top|${this.expression}`;
                if (!ExpressionEvaluator.loggedErrorKeys.has(errKey)) {
                    ExpressionEvaluator.loggedErrorKeys.add(errKey);
                    console.error('[EXPR ERROR : ]', JSON.stringify({ EXPRESSION: this.expression, ERROR: String(err) }, null, 2));
                }
            }
            throw err;
        }
    }

    private processNestingExpression(
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
    ): Tuple2<string, Expression> {
        // Recursively expand innermost {{ }} first, then step back: evaluate innermost,
        // substitute into expression, repeat until no {{ }} left.
        let current = expression;
        while (current.includes('{{')) {
            const innermost = this.findInnermostPair(current);
            if (!innermost) break;
            current = this.replaceOneNesting(current, innermost, valuesMap);
        }
        return new Tuple2(current, ExpressionEvaluator.getCachedExpression(current));
    }

    /**
     * Finds the innermost {{ }} pair: one whose content does not contain {{.
     * Returns { start, end, content } where start/end are indices (start of {{, end after }}).
     */
    private findInnermostPair(expr: string): { start: number; end: number; content: string } | null {
        let i = 0;
        while (i < expr.length - 1) {
            if (expr.charAt(i) !== '{' || expr.charAt(i + 1) !== '{') {
                i++;
                continue;
            }
            const openPos = i;
            i += 2;
            let depth = 1;
            while (i < expr.length - 1 && depth > 0) {
                if (expr.charAt(i) === '{' && expr.charAt(i + 1) === '{') {
                    depth++;
                    i += 2;
                    continue;
                }
                if (expr.charAt(i) === '}' && expr.charAt(i + 1) === '}') {
                    depth--;
                    if (depth === 0) {
                        const closePos = i + 2;
                        const content = expr.substring(openPos + 2, i);
                        if (!content.includes('{{')) {
                            return { start: openPos, end: closePos, content };
                        }
                        const inner = this.findInnermostPair(content);
                        if (!inner) {
                            i += 2;
                            continue;
                        }
                        return {
                            start: openPos + 2 + inner.start,
                            end: openPos + 2 + inner.end,
                            content: inner.content,
                        };
                    }
                    i += 2;
                    continue;
                }
                i++;
            }
            i = openPos + 1;
        }
        return null;
    }

    private replaceOneNesting(
        expression: string,
        innermost: { start: number; end: number; content: string },
        valuesMap: Map<string, TokenValueExtractor>,
    ): string {
        const { start: startPos, end: endPos, content: innerExpr } = innermost;
        const afterContext = expression.substring(endPos, Math.min(endPos + 1, expression.length));
        const isInPath =
            afterContext === '.' ||
            afterContext === '[' ||
            (startPos > 0 && expression.charAt(startPos - 1) === '[') ||
            (startPos > 0 && expression.charAt(startPos - 1) === '.');
        let singleQuotes = 0;
        let doubleQuotes = 0;
        for (let idx = 0; idx < startPos; idx++) {
            if (expression.charAt(idx) === "'" && (idx === 0 || expression.charAt(idx - 1) !== '\\')) {
                singleQuotes++;
            } else if (expression.charAt(idx) === '"' && (idx === 0 || expression.charAt(idx - 1) !== '\\')) {
                doubleQuotes++;
            }
        }
        const isInStringLiteral = (singleQuotes % 2 === 1) || (doubleQuotes % 2 === 1);

        let evaluatedValue: any;
        try {
            const nestedEvaluator = new ExpressionEvaluator(innerExpr);
            (nestedEvaluator as any).internalTokenValueExtractor = this.internalTokenValueExtractor;
            evaluatedValue = nestedEvaluator.evaluate(valuesMap);
        } catch (err) {
            const errKey = `${expression}|${innerExpr}`;
            if (!ExpressionEvaluator.loggedErrorKeys.has(errKey)) {
                ExpressionEvaluator.loggedErrorKeys.add(errKey);
                console.error(
                    '[EXPR ERROR : ]',
                    JSON.stringify(
                        { ORIGINAL: expression, FAILED_INNER: innerExpr, ERROR: String(err) },
                        null,
                        2,
                    ),
                );
            }
            (err as any)._exprErrorLogged = true;
            throw err;
        }

        const isPathReference =
            typeof evaluatedValue === 'string' &&
            Array.from(valuesMap.keys()).some((prefix) => evaluatedValue.startsWith(prefix));
        let replacement: string;
        if (isInPath || isInStringLiteral || isPathReference) {
            replacement = String(evaluatedValue);
        } else {
            const key = `__nested_${ExpressionEvaluator.keyCounter++}__`;
            this.internalTokenValueExtractor.addValue(key, evaluatedValue);
            replacement = `${this.internalTokenValueExtractor.getPrefix()}${key}`;
        }

        return expression.substring(0, startPos) + replacement + expression.substring(endPos);
    }


    public getExpression(): Expression {
        if (!this.exp) this.exp = ExpressionEvaluator.getCachedExpression(this.expression);

        return this.exp;
    }

    public getExpressionString(): string {
        return this.expression;
    }

    private evaluateExpression(exp: Expression, valuesMap: Map<string, TokenValueExtractor>): any {
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
            if (ctx.srcIdx >= tokensSource.length) {
                // Only throw if we're actively processing operations and need a token
                // This indicates a malformed expression (not enough tokens for operations)
                // Check if we have more operations to process
                if (ctx.opIdx < opsArray.length) {
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        'Not enough tokens to evaluate expression',
                    );
                }
                // If we're done with operations but need a token, this is an error
                // This can happen with malformed expressions
                throw new ExpressionEvaluationException(
                    exp.getExpression(),
                    'Expression evaluation incomplete: missing token',
                );
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
                workingStack.push(
                    this.applyUnaryOperation(operator, this.getValueFromToken(valuesMap, token)),
                );
            } else if (
                operator == Operation.OBJECT_OPERATOR ||
                operator == Operation.ARRAY_OPERATOR
            ) {
                this.processObjectOrArrayOperatorIndexed(
                    valuesMap, opsArray, tokensSource, workingStack, ctx, operator, token, popToken, popOp, peekOp, hasMoreTokens
                );
            } else if (operator == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                const token2: ExpressionToken = popToken();
                const token3: ExpressionToken = popToken();
                let v1 = this.getValueFromToken(valuesMap, token3);
                let v2 = this.getValueFromToken(valuesMap, token2);
                let v3 = this.getValueFromToken(valuesMap, token);
                workingStack.push(this.applyTernaryOperation(operator, v1, v2, v3));
            } else {
                const token2: ExpressionToken = popToken();
                let v1 = this.getValueFromToken(valuesMap, token2);
                let v2 = this.getValueFromToken(valuesMap, token);
                workingStack.push(this.applyBinaryOperation(operator, v1, v2, valuesMap));
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
        if (token instanceof ExpressionTokenValue) return token.getElement();
        if (token instanceof Expression) return this.evaluateExpression(token, valuesMap);
        return this.getValueFromToken(valuesMap, token);
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
            if (token instanceof Expression) {
                // For ARRAY_OPERATOR, the token is an array index that should always be evaluated
                // to get its numeric value (e.g., Parent.index should resolve to a number).
                // For OBJECT_OPERATOR, path expressions should be kept as path strings.
                const shouldEvaluate = operator === Operation.ARRAY_OPERATOR || !this.isPathExpression(token);

                if (shouldEvaluate) {
                    const evaluatedValue = this.evaluateExpression(token, valuesMap);
                    objTokens.push(
                        new ExpressionTokenValue(
                            token.toString(),
                            evaluatedValue,
                        ),
                    );
                } else {
                    // Build path string without parentheses for OBJECT_OPERATOR path components
                    const tokenStr = this.buildPathString(token);
                    objTokens.push(new ExpressionToken(tokenStr));
                }
            }
            else if (token) objTokens.push(token);
            
            // Match Java logic: check workingStack first, then tokensSource
            if (workingStack.length > 0) {
                token = workingStack.pop();
            } else if (ctx.srcIdx < tokensSource.length) {
                token = tokensSource[ctx.srcIdx++];
            } else {
                token = undefined;
            }
            operator = popOp();
        } while (operator == Operation.OBJECT_OPERATOR || operator == Operation.ARRAY_OPERATOR);

        if (token) {
            if (token instanceof Expression) {
                // Same logic: path components use buildPathString(), value expressions are evaluated
                if (this.isPathExpression(token)) {
                    const tokenStr = this.buildPathString(token);
                    objTokens.push(new ExpressionToken(tokenStr));
                } else {
                    objTokens.push(
                        new ExpressionTokenValue(
                            token.toString(),
                            this.evaluateExpression(token, valuesMap),
                        ),
                    );
                }
            }
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
        // Preserve quotes for bracket notation with quoted keys containing dots (like ["mail.props.port"])
        let str: string;
        if (objToken instanceof ExpressionTokenValue) {
            const originalExpr = objToken.getExpression();
            const evaluatedValue = objToken.getTokenValue();
            // Preserve quotes when the key contains dots to distinguish from simple bracket access
            if (originalExpr && originalExpr.length > 0 &&
                (originalExpr.charAt(0) == '"' || originalExpr.charAt(0) == "'") &&
                typeof evaluatedValue === 'string' && evaluatedValue.includes('.')) {
                str = originalExpr;
            } else {
                str = typeof evaluatedValue === 'string' ? evaluatedValue : String(evaluatedValue);
            }
        } else {
            str = objToken.toString();
        }

        while (objTokenIdx >= 0) {
            objToken = objTokens[objTokenIdx--];
            operator = objOperations[objOpIdx--];
            let tokenVal: string;
            if (objToken instanceof ExpressionTokenValue) {
                const originalExpr = objToken.getExpression();
                const evaluatedValue = objToken.getTokenValue();
                // Preserve quotes when the key contains dots
                if (operator == Operation.ARRAY_OPERATOR && originalExpr && originalExpr.length > 0 &&
                    (originalExpr.charAt(0) == '"' || originalExpr.charAt(0) == "'") &&
                    typeof evaluatedValue === 'string' && evaluatedValue.includes('.')) {
                    tokenVal = originalExpr;
                } else {
                    tokenVal = typeof evaluatedValue === 'string' ? evaluatedValue : String(evaluatedValue);
                }
            } else {
                tokenVal = objToken.toString();
            }
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
                // Check if this is a literal (number, string, boolean, null) with property access
                // e.g., "2.val" should evaluate to undefined (accessing .val on number 2)
                v = this.evaluateLiteralPropertyAccess(str);
            }
            workingStack.push(new ExpressionTokenValue(str, v));
        }
    }

    /**
     * Handle cases like "2.val" where we have a literal with property access.
     * Numbers, booleans, null don't have custom properties, so accessing them returns undefined.
     * Strings might have properties like .length
     */
    private evaluateLiteralPropertyAccess(str: string): any {
        const dotIdx = str.indexOf('.');
        if (dotIdx === -1) {
            // No property access, just return the string as-is
            return str;
        }
        
        const basePart = str.substring(0, dotIdx);
        const propPart = str.substring(dotIdx + 1);
        
        // Try to parse the base as a literal
        let baseValue: any;
        try {
            baseValue = LiteralTokenValueExtractor.INSTANCE.getValue(basePart);
        } catch (err) {
            // Not a valid literal, return the original string
            return str;
        }
        
        // If baseValue is null or undefined, property access returns undefined
        if (baseValue === null || baseValue === undefined) {
            return undefined;
        }
        
        // For primitives (number, boolean, string), access the property
        // This handles cases like "2.val" -> undefined, or potentially "hello".length -> 5
        // But we need to handle chained access like "2.val.something"
        const propParts = this.splitPropertyPath(propPart);
        let result: any = baseValue;
        
        for (const prop of propParts) {
            if (result === null || result === undefined) {
                return undefined;
            }
            // Handle bracket notation within the property path
            if (prop.includes('[')) {
                result = this.accessPropertyWithBrackets(result, prop);
            } else {
                result = result[prop];
            }
        }
        
        return result;
    }
    
    /**
     * Split a property path like "val.something" into ["val", "something"]
     * Handles bracket notation like "arr[0].value"
     */
    private splitPropertyPath(path: string): string[] {
        const parts: string[] = [];
        let current = '';
        let inBracket = false;
        
        for (let i = 0; i < path.length; i++) {
            const ch = path.charAt(i);
            if (ch === '[') {
                if (current.length > 0) {
                    parts.push(current);
                    current = '';
                }
                inBracket = true;
                current += ch;
            } else if (ch === ']') {
                current += ch;
                inBracket = false;
                parts.push(current);
                current = '';
            } else if (ch === '.' && !inBracket) {
                if (current.length > 0) {
                    parts.push(current);
                    current = '';
                }
            } else {
                current += ch;
            }
        }
        
        if (current.length > 0) {
            parts.push(current);
        }
        
        return parts;
    }
    
    /**
     * Access a property that may contain bracket notation like "[0]" or '["key"]'
     */
    private accessPropertyWithBrackets(obj: any, prop: string): any {
        // Handle bracket notation like "[0]" or '["key"]'
        const bracketMatch = prop.match(/^\[(.+)\]$/);
        if (bracketMatch) {
            let key = bracketMatch[1];
            // Remove quotes if present
            if ((key.startsWith('"') && key.endsWith('"')) || 
                (key.startsWith("'") && key.endsWith("'"))) {
                key = key.substring(1, key.length - 1);
            }
            // Try to parse as number
            const numKey = parseInt(key);
            if (!isNaN(numKey)) {
                return obj[numKey];
            }
            return obj[key];
        }
        return obj[prop];
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

    private applyBinaryOperation(
        operator: Operation,
        v1: any,
        v2: any,
        valuesMap?: Map<string, TokenValueExtractor>,
    ): ExpressionToken {
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

        let result = op.apply(v1, v2);

        // When ?? yields a string that looks like an expression (e.g. from location.expression), evaluate it
        if (
            operator === Operation.NULLISH_COALESCING_OPERATOR &&
            typeof result === 'string' &&
            valuesMap &&
            result.trim().length > 0 &&
            this.looksLikeExpression(result)
        ) {
            try {
                result = new ExpressionEvaluator(result).evaluate(valuesMap);
            } catch {
                // Keep original string if sub-expression fails
            }
        }

        return new ExpressionTokenValue(operator.toString(), result);
    }

    private looksLikeExpression(str: string): boolean {
        const trimmed = str.trim();
        if (trimmed.length === 0) return false;
        if (/[+\-*/%=<>!&|?:]/.test(trimmed)) return true;
        const pathPrefixes = ['Store.', 'Context.', 'Arguments.', 'Steps.', 'Page.', 'Parent.'];
        return pathPrefixes.some((prefix) => trimmed.includes(prefix));
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
    
    /**
     * Build a path string from a path Expression, without parentheses.
     * E.g., Expression for "a.(b.c)" returns "a.b.c"
     */
    private buildPathString(expr: Expression): string {
        const ops = expr.getOperationsArray();
        const tokens = expr.getTokensArray();
        
        // Leaf expression - just return the token string
        if (ops.length === 0) {
            if (tokens.length === 1) {
                const token = tokens[0];
                if (token instanceof Expression) {
                    return this.buildPathString(token);
                }
                // For ExpressionTokenValue, use getExpression() not toString()
                // (toString() returns "expr: value" format which is wrong for paths)
                return this.getTokenExpressionString(token);
            }
            return expr.getExpression() || '';
        }
        
        // Binary expression - build path from tokens and operators
        // With push() order: tokens[0]=right, tokens[1]=left
        if (tokens.length >= 2 && ops.length >= 1) {
            const right = tokens[0];
            const left = tokens[1];
            const op = ops[0];
            
            const leftStr = left instanceof Expression ? this.buildPathString(left) : this.getTokenExpressionString(left);
            const rightStr = right instanceof Expression ? this.buildPathString(right) : this.getTokenExpressionString(right);
            
            if (op === Operation.OBJECT_OPERATOR) {
                return leftStr + '.' + rightStr;
            } else if (op === Operation.ARRAY_OPERATOR) {
                return leftStr + '[' + rightStr + ']';
            } else if (op === Operation.ARRAY_RANGE_INDEX_OPERATOR) {
                return leftStr + '..' + rightStr;
            }
        }
        
        // Fallback to toString() with parens stripped
        return this.stripOuterParens(expr.toString());
    }
    
    /**
     * Get the expression string from a token, handling ExpressionTokenValue specially.
     */
    private getTokenExpressionString(token: ExpressionToken): string {
        // For ExpressionTokenValue, use getExpression() not toString()
        // because toString() returns "expr: value" format
        if (token instanceof ExpressionTokenValue) {
            return token.getExpression();
        }
        return token.getExpression();
    }
    
    /**
     * Strip outer parentheses from a string if they exist.
     * E.g., "(Context.obj)" -> "Context.obj"
     */
    private stripOuterParens(str: string): string {
        if (str.length >= 2 && str.charAt(0) === '(' && str.charAt(str.length - 1) === ')') {
            // Count parentheses to ensure we only strip matching outer parens
            let depth = 0;
            for (let i = 0; i < str.length; i++) {
                if (str.charAt(i) === '(') depth++;
                else if (str.charAt(i) === ')') depth--;
                // If depth becomes 0 before the last char, the outer parens don't match
                if (depth === 0 && i < str.length - 1) {
                    return str; // Don't strip, the parens don't match
                }
            }
            return str.substring(1, str.length - 1);
        }
        return str;
    }
    
    /**
     * Check if an Expression is a path component (identifier, OBJECT_OPERATOR, or ARRAY_OPERATOR).
     * Path components should use toString() for path building, not be evaluated as values.
     * Expressions with other operators (like +, -, etc.) should be evaluated.
     *
     * IMPORTANT: ARRAY_OPERATOR expressions with non-static indices (like [Parent.index])
     * must be evaluated, not treated as path strings.
     */
    private isPathExpression(expr: Expression): boolean {
        const ops = expr.getOperationsArray();
        const tokens = expr.getTokensArray();

        // No operations = leaf identifier - use toString()
        if (ops.length === 0) return true;

        // Check if all operations are path-related
        for (const op of ops) {
            if (!this.isPathOperator(op)) return false;

            // For ARRAY_OPERATOR, check if the index is static
            if (op === Operation.ARRAY_OPERATOR && tokens.length > 0 && !this.isStaticArrayIndex(tokens[0])) {
                return false;
            }
        }

        // Also check nested expressions in tokens
        return tokens.every(token => !(token instanceof Expression) || this.isPathExpression(token));
    }

    private isPathOperator(op: Operation): boolean {
        return op === Operation.OBJECT_OPERATOR ||
               op === Operation.ARRAY_OPERATOR ||
               op === Operation.ARRAY_RANGE_INDEX_OPERATOR;
    }

    /**
     * Check if a token represents a static array index (number or string literal).
     */
    private isStaticArrayIndex(token: ExpressionToken): boolean {
        if (token instanceof Expression) {
            return this.isStaticArrayIndexExpression(token);
        }
        return this.isStaticLiteral(token.getExpression());
    }

    private isStaticArrayIndexExpression(expr: Expression): boolean {
        const ops = expr.getOperationsArray();
        const tokens = expr.getTokensArray();

        // Leaf expression with single token - check if it's a literal
        if (ops.length === 0 && tokens.length === 1) {
            return this.isStaticLiteral(tokens[0].getExpression());
        }

        // Range expressions are static if both parts are static
        if (ops.length === 1 && ops[0] === Operation.ARRAY_RANGE_INDEX_OPERATOR) {
            return tokens.every(t => this.isStaticArrayIndex(t));
        }

        return false;
    }

    private isStaticLiteral(str: string): boolean {
        // Number: digits, possibly with decimal and negative sign
        if (/^-?\d+(\.\d+)?$/.test(str)) return true;
        // Quoted string
        if ((str.startsWith('"') && str.endsWith('"')) ||
            (str.startsWith("'") && str.endsWith("'"))) return true;
        return false;
    }
}



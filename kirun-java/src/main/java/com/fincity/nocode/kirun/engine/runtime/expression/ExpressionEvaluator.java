package com.fincity.nocode.kirun.engine.runtime.expression;

import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ADDITION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.AND;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ARRAY_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ARRAY_RANGE_INDEX_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_AND;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_LEFT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_OR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_RIGHT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_UNSIGNED_RIGHT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_XOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.CONDITIONAL_TERNARY_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.DIVISION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.GREATER_THAN;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.GREATER_THAN_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.LESS_THAN;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.LESS_THAN_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.MOD;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.MULTIPLICATION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.NOT_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.NULLISH_COALESCING_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OBJECT_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.SUBTRACTION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_BITWISE_COMPLEMENT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_LOGICAL_NOT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_MINUS;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_PLUS;

import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticAdditionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticDivisionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticModulusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticMultiplicationOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticSubtractionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArrayOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArrayRangeOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BinaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseAndOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseLeftShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseOrOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseRightShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseUnsignedRightShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseXorOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalAndOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalGreaterThanEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalGreaterThanOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalLessThanEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalLessThanOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalNotEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalNullishCoalescingOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalOrOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ObjectOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.ternary.ConditionalTernaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.ternary.TernaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryMinusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryPlusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.BitwiseComplementOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.LogicalNotOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.UnaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.LiteralTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ExpressionEvaluator {

    // Static cache for parsed expressions to avoid re-parsing
    private static final Map<String, Expression> expressionCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Counter for generating unique keys for internal values
    private static int keyCounter = 0;
    
    private static Expression getCachedExpression(String expressionString) {
        return expressionCache.computeIfAbsent(expressionString, Expression::new);
    }

    private static final Map<Operation, UnaryOperator> UNARY_OPERATORS_MAP = new EnumMap<>(Map.of(
            UNARY_BITWISE_COMPLEMENT, new BitwiseComplementOperator(), UNARY_LOGICAL_NOT, new LogicalNotOperator(),
            UNARY_MINUS, new ArithmeticUnaryMinusOperator(), UNARY_PLUS, new ArithmeticUnaryPlusOperator()));

    private static final Map<Operation, BinaryOperator> BINARY_OPERATORS_MAP = new EnumMap<>(Map.ofEntries(
            Map.entry(ADDITION, new ArithmeticAdditionOperator()),
            Map.entry(DIVISION, new ArithmeticDivisionOperator()), Map.entry(MOD, new ArithmeticModulusOperator()),
            Map.entry(MULTIPLICATION, new ArithmeticMultiplicationOperator()),
            Map.entry(SUBTRACTION, new ArithmeticSubtractionOperator()),

            Map.entry(BITWISE_AND, new BitwiseAndOperator()),
            Map.entry(BITWISE_LEFT_SHIFT, new BitwiseLeftShiftOperator()),
            Map.entry(BITWISE_OR, new BitwiseOrOperator()),
            Map.entry(BITWISE_RIGHT_SHIFT, new BitwiseRightShiftOperator()),
            Map.entry(BITWISE_UNSIGNED_RIGHT_SHIFT, new BitwiseUnsignedRightShiftOperator()),
            Map.entry(BITWISE_XOR, new BitwiseXorOperator()),

            Map.entry(AND, new LogicalAndOperator()), Map.entry(EQUAL, new LogicalEqualOperator()),
            Map.entry(GREATER_THAN, new LogicalGreaterThanOperator()),
            Map.entry(GREATER_THAN_EQUAL, new LogicalGreaterThanEqualOperator()),
            Map.entry(LESS_THAN, new LogicalLessThanOperator()),
            Map.entry(LESS_THAN_EQUAL, new LogicalLessThanEqualOperator()), Map.entry(OR, new LogicalOrOperator()),
            Map.entry(NOT_EQUAL, new LogicalNotEqualOperator()),
            Map.entry(NULLISH_COALESCING_OPERATOR, new LogicalNullishCoalescingOperator()),

            Map.entry(ARRAY_OPERATOR, new ArrayOperator()),
            Map.entry(ARRAY_RANGE_INDEX_OPERATOR, new ArrayRangeOperator()),

            Map.entry(OBJECT_OPERATOR, new ObjectOperator())));

    private static final Map<Operation, TernaryOperator> TERNARY_OPERATORS_MAP = new EnumMap<>(
            Map.ofEntries(Map.entry(CONDITIONAL_TERNARY_OPERATOR, new ConditionalTernaryOperator())));

    private static final Set<Operation> UNARY_OPERATORS_MAP_KEY_SET = UNARY_OPERATORS_MAP.keySet();

    // ==================== FAST PATH DETECTION CACHES ====================
    
    // Expression pattern types for fast path routing
    private static final int PATTERN_UNKNOWN = 0;
    private static final int PATTERN_LITERAL = 1;
    private static final int PATTERN_SIMPLE_PATH = 2;
    private static final int PATTERN_SIMPLE_ARRAY_ACCESS = 3;
    private static final int PATTERN_SIMPLE_COMPARISON = 4;
    private static final int PATTERN_SIMPLE_TERNARY = 5;
    
    // Cache for expression pattern detection
    private static final Map<String, Integer> patternCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Regex patterns for fast detection (compiled once)
    private static final String LITERAL_TRUE = "true";
    private static final String LITERAL_FALSE = "false";
    private static final String LITERAL_NULL = "null";
    private static final String LITERAL_UNDEFINED = "undefined";
    private static final java.util.regex.Pattern NUMBER_REGEX = java.util.regex.Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final java.util.regex.Pattern SINGLE_QUOTE_STRING_REGEX = java.util.regex.Pattern.compile("^'([^'\\\\]|\\\\.)*'$");
    private static final java.util.regex.Pattern DOUBLE_QUOTE_STRING_REGEX = java.util.regex.Pattern.compile("^\"([^\"\\\\]|\\\\.)*\"$");

    private String expression;
    private Expression exp;
    private ExpressionInternalValueExtractor internalTokenValueExtractor = new ExpressionInternalValueExtractor();

    public ExpressionEvaluator(String expression) {
        this.expression = expression;
    }

    public ExpressionEvaluator(Expression exp) {
        this.exp = exp;
        this.expression = exp.getExpression();
    }

    public JsonElement evaluate(Map<String, TokenValueExtractor> valuesMap) {
        Tuple2<String, Expression> tuple = this.processNestingExpression(this.expression, valuesMap);
        this.expression = tuple.getT1();
        this.exp = tuple.getT2();
        
        // Detect pattern type for fast path routing
        int pattern = detectPattern(this.exp);
        
        // Fast path 1: Literals (true, false, numbers, strings)
        if (pattern == PATTERN_LITERAL) {
            return evaluateLiteral(this.expression);
        }
        
        // Fast path 2: Simple paths (Store.path.to.value)
        if (pattern == PATTERN_SIMPLE_PATH) {
            return evaluateSimplePath(this.exp, valuesMap);
        }
        
        // Fast path 3: Simple comparison (path = value)
        if (pattern == PATTERN_SIMPLE_COMPARISON) {
            return evaluateSimpleComparison(this.exp, valuesMap);
        }
        
        // Fast path 4: Simple ternary (condition ? value1 : value2)
        if (pattern == PATTERN_SIMPLE_TERNARY) {
            return evaluateSimpleTernary(this.exp, valuesMap);
        }
        
        // Full evaluation path for complex expressions
        valuesMap = new HashMap<>(valuesMap);
        valuesMap.put(this.internalTokenValueExtractor.getPrefix(), this.internalTokenValueExtractor);

        return this.evaluateExpression(exp, valuesMap);
    }

    private Tuple2<String, Expression> processNestingExpression(String expression,
                                                                Map<String, TokenValueExtractor> valuesMap) {

        int start = 0;
        int i = 0;

        LinkedList<Tuple2<Integer, Integer>> tuples = new LinkedList<>();

        while (i < expression.length() - 1) {

            if (expression.charAt(i) == '{' && expression.charAt(i + 1) == '{') {

                if (start == 0)
                    tuples.push(Tuples.of(i + 2, -1));

                start++;
                i++;
            } else if (expression.charAt(i) == '}' && expression.charAt(i + 1) == '}') {
                start--;

                if (start < 0)
                    throw new ExpressionEvaluationException(expression,
                            "Expecting {{ nesting path operator to be started before closing");

                if (start == 0) {

                    final int index = i;
                    tuples.push(tuples.pop()
                            .mapT2(e -> index));
                }
                i++;
            }
            i++;
        }

        String newExpression = replaceNestingExpression(expression, valuesMap, tuples);

        return Tuples.of(newExpression, getCachedExpression(newExpression));
    }

    private String replaceNestingExpression(String expression, Map<String, TokenValueExtractor> valuesMap,
                                            LinkedList<Tuple2<Integer, Integer>> tuples) {

        String newExpression = expression;

        for (var tuple : tuples) {

            if (tuple.getT2() == -1)
                throw new ExpressionEvaluationException(expression, "Expecting }} nesting path operator to be closed");

            String expStr = (new ExpressionEvaluator(newExpression.substring(tuple.getT1(), tuple.getT2())))
                    .evaluate(valuesMap)
                    .getAsString();

            newExpression = newExpression.substring(0, tuple.getT1() - 2) + expStr
                    + newExpression.substring(tuple.getT2() + 2);
        }
        return newExpression;
    }

    public Expression getExpression() {

        if (this.exp == null)
            this.exp = getCachedExpression(this.expression);

        return this.exp;
    }

    public String getExpressionString() {
        return this.expression;
    }
    
    // ==================== FAST PATH DETECTION ====================
    
    /**
     * Detect expression pattern type for fast path routing
     */
    private static int detectPattern(Expression exp) {
        String expStr = exp.getExpression();
        
        // Check cache
        Integer cached = patternCache.get(expStr);
        if (cached != null) return cached;
        
        int pattern = PATTERN_UNKNOWN;
        
        // 1. Check for literals (no parsing needed)
        if (expStr.equals(LITERAL_TRUE) || 
            expStr.equals(LITERAL_FALSE) ||
            expStr.equals(LITERAL_NULL) ||
            expStr.equals(LITERAL_UNDEFINED) ||
            NUMBER_REGEX.matcher(expStr).matches() ||
            SINGLE_QUOTE_STRING_REGEX.matcher(expStr).matches() ||
            DOUBLE_QUOTE_STRING_REGEX.matcher(expStr).matches()) {
            pattern = PATTERN_LITERAL;
        }
        // 2. Check for simple path (must have dot, no complex operators)
        // Exclude expressions with range operator '..' or nested expressions '{{'
        else if (expStr.contains(".") && !expStr.contains("{{") && !expStr.contains("..")) {
            Operation[] ops = exp.getOpsArray();
            ExpressionToken[] tokens = exp.getTokensArray();
            
            // Must have only OBJECT_OPERATOR or ARRAY_OPERATOR
            boolean isSimplePath = ops.length > 0;
            for (Operation op : ops) {
                if (op != OBJECT_OPERATOR && op != ARRAY_OPERATOR) {
                    isSimplePath = false;
                    break;
                }
            }
            
            // No tokens can be nested expressions
            if (isSimplePath) {
                for (ExpressionToken token : tokens) {
                    if (token instanceof Expression) {
                        isSimplePath = false;
                        break;
                    }
                }
            }
            
            if (isSimplePath) {
                pattern = PATTERN_SIMPLE_PATH;
            } else {
                // 3. Check for simple ternary: condition ? value1 : value2
                pattern = detectTernaryOrComparison(exp, ops);
            }
        }
        // Check for range operator expressions (need full evaluation)
        else if (expStr.contains("..")) {
            pattern = PATTERN_UNKNOWN;
        }
        // 4. Check ternary/comparison for expressions without dots
        else if (!expStr.contains("{{")) {
            Operation[] ops = exp.getOpsArray();
            pattern = detectTernaryOrComparison(exp, ops);
        }
        
        patternCache.put(expStr, pattern);
        return pattern;
    }
    
    /**
     * Detect simple ternary or comparison patterns
     */
    private static int detectTernaryOrComparison(Expression exp, Operation[] ops) {
        ExpressionToken[] tokens = exp.getTokensArray();
        
        // Check for nested expressions in tokens
        for (ExpressionToken token : tokens) {
            if (token instanceof Expression) {
                return PATTERN_UNKNOWN;
            }
        }
        
        // Check for simple ternary: exactly one CONDITIONAL_TERNARY_OPERATOR and simple tokens
        if (ops.length == 1 && ops[0] == CONDITIONAL_TERNARY_OPERATOR) {
            return PATTERN_SIMPLE_TERNARY;
        }
        
        // Check for simple comparison: exactly one EQUAL or NOT_EQUAL
        if (ops.length == 1 && (ops[0] == EQUAL || ops[0] == NOT_EQUAL)) {
            return PATTERN_SIMPLE_COMPARISON;
        }
        
        return PATTERN_UNKNOWN;
    }
    
    // ==================== FAST PATH EVALUATORS ====================
    
    /**
     * Fast path for literals
     */
    private static JsonElement evaluateLiteral(String expStr) {
        if (expStr.equals(LITERAL_TRUE)) return new JsonPrimitive(true);
        if (expStr.equals(LITERAL_FALSE)) return new JsonPrimitive(false);
        if (expStr.equals(LITERAL_NULL)) return JsonNull.INSTANCE;
        if (expStr.equals(LITERAL_UNDEFINED)) return JsonNull.INSTANCE;
        
        // Number
        if (NUMBER_REGEX.matcher(expStr).matches()) {
            if (expStr.contains(".")) {
                return new JsonPrimitive(Double.parseDouble(expStr));
            } else {
                return new JsonPrimitive(Long.parseLong(expStr));
            }
        }
        
        // Quoted string - just remove quotes (no escape processing to match original behavior)
        if (SINGLE_QUOTE_STRING_REGEX.matcher(expStr).matches()) {
            return new JsonPrimitive(expStr.substring(1, expStr.length() - 1));
        }
        if (DOUBLE_QUOTE_STRING_REGEX.matcher(expStr).matches()) {
            return new JsonPrimitive(expStr.substring(1, expStr.length() - 1));
        }
        
        return null;
    }
    
    /**
     * Fast path for simple paths (Store.path.to.value)
     */
    private JsonElement evaluateSimplePath(Expression exp, Map<String, TokenValueExtractor> valuesMap) {
        String pathStr = exp.getExpression();
        int dotIdx = pathStr.indexOf('.');
        if (dotIdx == -1) return null;
        
        String prefix = pathStr.substring(0, dotIdx + 1);
        TokenValueExtractor extractor = valuesMap.get(prefix);
        if (extractor == null) {
            // If no extractor found, fall back to literal extractor
            // This handles cases like "2.val" which should be treated as a literal string
            return LiteralTokenValueExtractor.INSTANCE.getValue(pathStr);
        }
        
        // If extractor is found, use it - don't catch exceptions here
        // Exceptions should propagate (e.g., Arguments.b.length where b is a number)
        return extractor.getValue(pathStr);
    }
    
    /**
     * Fast path for simple comparisons (path = value, path != value)
     */
    private JsonElement evaluateSimpleComparison(Expression exp, Map<String, TokenValueExtractor> valuesMap) {
        Operation[] ops = exp.getOpsArray();
        ExpressionToken[] tokens = exp.getTokensArray();
        
        if (tokens.length != 2 || ops.length != 1) return null;
        
        JsonElement v1 = getTokenValue(tokens[1], valuesMap);
        JsonElement v2 = getTokenValue(tokens[0], valuesMap);
        
        if (ops[0] == EQUAL) {
            return new JsonPrimitive(equalsJsonElement(v1, v2));
        } else if (ops[0] == NOT_EQUAL) {
            return new JsonPrimitive(!equalsJsonElement(v1, v2));
        }
        
        return null;
    }
    
    /**
     * Fast path for simple ternary (condition ? value1 : value2)
     */
    private JsonElement evaluateSimpleTernary(Expression exp, Map<String, TokenValueExtractor> valuesMap) {
        ExpressionToken[] tokens = exp.getTokensArray();
        
        if (tokens.length != 3) return null;
        
        JsonElement condition = getTokenValue(tokens[2], valuesMap);
        JsonElement trueValue = getTokenValue(tokens[1], valuesMap);
        JsonElement falseValue = getTokenValue(tokens[0], valuesMap);
        
        // Check if condition is truthy
        boolean isTruthy = condition != null && condition != JsonNull.INSTANCE && 
                          (!condition.isJsonPrimitive() || 
                           (condition.getAsJsonPrimitive().isBoolean() && condition.getAsBoolean()) ||
                           (condition.getAsJsonPrimitive().isNumber() && condition.getAsDouble() != 0) ||
                           (condition.getAsJsonPrimitive().isString() && !condition.getAsString().isEmpty()));
        
        return isTruthy ? trueValue : falseValue;
    }
    
    /**
     * Helper to get value from a token (for fast paths)
     */
    private JsonElement getTokenValue(ExpressionToken token, Map<String, TokenValueExtractor> valuesMap) {
        if (token instanceof ExpressionTokenValue etv) {
            return etv.getElement();
        }
        
        String tokenStr = token.getExpression();
        
        // Check if it's a literal
        JsonElement literalVal = evaluateLiteral(tokenStr);
        if (literalVal != null || tokenStr.equals("undefined") || tokenStr.equals("null")) {
            return literalVal != null ? literalVal : JsonNull.INSTANCE;
        }
        
        // Check if it's a path
        int dotIdx = tokenStr.indexOf('.');
        if (dotIdx != -1) {
            String prefix = tokenStr.substring(0, dotIdx + 1);
            TokenValueExtractor extractor = valuesMap.get(prefix);
            if (extractor != null) {
                return extractor.getValue(tokenStr);
            }
        }
        
        // Fall back to literal extractor
        return LiteralTokenValueExtractor.INSTANCE.getValue(tokenStr);
    }
    
    /**
     * Compare two JsonElements for equality
     */
    private boolean equalsJsonElement(JsonElement v1, JsonElement v2) {
        if (v1 == v2) return true;
        if (v1 == null || v2 == null) return false;
        if (v1 == JsonNull.INSTANCE && v2 == JsonNull.INSTANCE) return true;
        if (v1 == JsonNull.INSTANCE || v2 == JsonNull.INSTANCE) return false;
        
        if (v1.isJsonPrimitive() && v2.isJsonPrimitive()) {
            JsonPrimitive p1 = v1.getAsJsonPrimitive();
            JsonPrimitive p2 = v2.getAsJsonPrimitive();
            
            if (p1.isNumber() && p2.isNumber()) {
                return p1.getAsDouble() == p2.getAsDouble();
            }
            if (p1.isString() && p2.isString()) {
                return p1.getAsString().equals(p2.getAsString());
            }
            if (p1.isBoolean() && p2.isBoolean()) {
                return p1.getAsBoolean() == p2.getAsBoolean();
            }
        }
        
        return v1.equals(v2);
    }

    private JsonElement evaluateExpression(Expression exp, Map<String, TokenValueExtractor> valuesMap) {
        return evaluateExpression(exp, valuesMap, null, -1);
    }
    
    private JsonElement evaluateExpression(Expression exp, Map<String, TokenValueExtractor> valuesMap, 
                                          ExpressionToken[] parentTokens, int parentTokenOffset) {
        // Use cached arrays for non-destructive evaluation
        Operation[] opsArray = exp.getOpsArray();
        ExpressionToken[] tokensSource = exp.getTokensArray();
        Deque<ExpressionToken> workingStack = new LinkedList<>();
        
        // Context for tracking indices
        int[] ctx = {0, 0}; // opIdx, srcIdx
        
        // Store parent tokens for nested expression evaluation
        final ExpressionToken[] parentTokensForNested = parentTokens;
        
        // Pop from working stack first (LIFO for results), then from source array
        // This matches the TypeScript implementation pattern
        // Also check parent context if available (for handling parser bugs)
        final int[] parentOffset = {parentTokenOffset}; // Make effectively final for lambda
        final ExpressionToken[] parentTokensFinal = parentTokens; // Make effectively final for lambda
        java.util.function.Supplier<ExpressionToken> popToken = () -> {
            if (!workingStack.isEmpty()) {
                return workingStack.pop();
            }
            if (ctx[1] >= tokensSource.length) {
                // Check if we can get token from parent context (workaround for parser bug)
                if (parentTokensFinal != null && parentOffset[0] >= 0 && parentOffset[0] < parentTokensFinal.length) {
                    // Use token from parent context - this handles cases where the parser
                    // incorrectly split an expression and the missing token is in the parent
                    return parentTokensFinal[parentOffset[0]++];
                }
                
                // Only throw if we're actively processing operations and need a token
                // This indicates a malformed expression (not enough tokens for operations)
                if (ctx[0] < opsArray.length) {
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        "Not enough tokens to evaluate expression (ops: " + opsArray.length + 
                        ", tokens: " + tokensSource.length + ", opIdx: " + ctx[0] + ", srcIdx: " + ctx[1] + ")");
                }
                // If we're done with operations but need a token, check if we're in the "collect remaining tokens" phase
                // In that case, it's okay - we just don't have any more tokens to collect
                // But if we're trying to get a token for an operation, that's an error
                // The issue is that this can happen when evaluating nested expressions that have structural issues
                throw new ExpressionEvaluationException(
                    exp.getExpression(),
                    "Expression evaluation incomplete: missing token (this may indicate a nested expression structure issue)");
            }
            return tokensSource[ctx[1]++];
        };
        
        while (ctx[0] < opsArray.length) {
            Operation operator = opsArray[ctx[0]++];
            ExpressionToken token;
            try {
                token = popToken.get();
            } catch (ExpressionEvaluationException e) {
                // If we can't get a token, this might be because the expression structure is malformed
                // This can happen with nested expressions created by the old parser
                // Check if we have any tokens in the working stack that we can use
                if (!workingStack.isEmpty()) {
                    // Use a token from the stack as a fallback
                    token = workingStack.pop();
                } else {
                    // No tokens available - this is a structural issue
                    // Re-throw with more context
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        "Cannot get token for operation " + operator.getOperator() + 
                        " (ops: " + opsArray.length + ", tokens: " + tokensSource.length + 
                        ", opIdx: " + ctx[0] + ", srcIdx: " + ctx[1] + "). " + e.getMessage());
                }
            }
            

            if (UNARY_OPERATORS_MAP_KEY_SET.contains(operator)) {
                // Pass parent context to nested expression evaluation
                JsonElement value = getValueFromToken(valuesMap, token, tokensSource, ctx[1]);
                workingStack.push(applyOperation(operator, value));
            } else if (operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR) {
                processObjectOrArrayOperatorIndexed(valuesMap, opsArray, tokensSource, workingStack, ctx, operator, token, popToken);
            } else if (operator == CONDITIONAL_TERNARY_OPERATOR) {
                ExpressionToken token2;
                ExpressionToken token3;
                try {
                    token2 = popToken.get();
                    token3 = popToken.get();
                } catch (ExpressionEvaluationException e) {
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        "Cannot get tokens for ternary operation. " + e.getMessage());
                }

                var v1 = getValueFromToken(valuesMap, token3, tokensSource, ctx[1]);
                var v2 = getValueFromToken(valuesMap, token2, tokensSource, ctx[1]);
                var v3 = getValueFromToken(valuesMap, token, tokensSource, ctx[1]);
                workingStack.push(applyOperation(operator, v1, v2, v3));
            } else {
                // Binary operation - need at least one more token
                ExpressionToken token2 = null;
                try {
                    token2 = popToken.get();
                } catch (ExpressionEvaluationException e) {
                    // If we can't get the second token, check if the first token is a nested Expression
                    // that might have been incorrectly parsed by the old parser
                    if (token instanceof Expression nestedExpr) {
                        Operation[] nestedOps = nestedExpr.getOpsArray();
                        ExpressionToken[] nestedTokens = nestedExpr.getTokensArray();
                        
                        // Check if the nested expression has operations but not enough tokens
                        int nestedRequiredTokens = nestedOps.length;
                        for (Operation op : nestedOps) {
                            if (!UNARY_OPERATORS_MAP_KEY_SET.contains(op) && 
                                op != Operation.CONDITIONAL_TERNARY_OPERATOR &&
                                op != Operation.OBJECT_OPERATOR && 
                                op != Operation.ARRAY_OPERATOR) {
                                nestedRequiredTokens++; // Binary operations need one more token
                            } else if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                                nestedRequiredTokens += 2; // Ternary needs 2 more tokens
                            }
                        }
                        
                        // If the nested expression is missing tokens, this is a parser bug
                        // For Arguments.b+"'kir" + ' an':
                        // - Parent tokens: [' an' (token[0]), "'kir" (nested, token[1]), Arguments.b (nested, token[2])]
                        // - We get token[0] = ' an' as token (right operand for parent)
                        // - We try to get token2 for left operand, but can't (because we're out of tokens)
                        // - token[1] is the nested "'kir" which needs ' an' as its second token
                        // - The ' an' we already have (as token) should be used to complete the nested expression
                        // - Then we need Arguments.b as the left operand for the parent
                        if (nestedTokens.length < nestedRequiredTokens) {
                            // The nested expression needs tokens that are in the parent context
                            // Since we can't get token2, check if we can use tokens from the source array
                            // that we haven't processed yet
                            if (ctx[1] < tokensSource.length) {
                                // There are still tokens in source - the next one might be what we need
                                // But actually, we need to look at what we've already processed
                                // The token we have (' an') should be used to complete the nested expression
                                // But we also need it for the parent operation...
                                // This is a complex case - let's try a different approach
                                // Check if the next token in source can complete the nested expression
                                ExpressionToken nextToken = tokensSource[ctx[1]];
                                if (nestedRequiredTokens - nestedTokens.length == 1) {
                                    // Complete nested with nextToken
                                    Expression completeNested = new Expression("", 
                                        nestedTokens[0], nextToken, nestedOps[0], false);
                                    var nestedValue = evaluateExpression(completeNested, valuesMap);
                                    ctx[1]++; // Consume nextToken
                                    // Now we need the left operand for parent - it should be the next token
                                    if (ctx[1] < tokensSource.length) {
                                        ExpressionToken leftOperand = tokensSource[ctx[1]++];
                                        var v1 = getValueFromToken(valuesMap, leftOperand, tokensSource, ctx[1]);
                                        var v2 = nestedValue;
                                        workingStack.push(applyOperation(operator, v1, v2));
                                        continue;
                                    }
                                }
                            }
                            
                            // If we can't reconstruct, throw error
                            throw new ExpressionEvaluationException(
                                exp.getExpression(),
                                "Cannot get second token for binary operation " + operator.getOperator() + 
                                ". The nested expression has " + nestedOps.length + " operations but only " + 
                                nestedTokens.length + " tokens (needs " + nestedRequiredTokens + "). " + e.getMessage());
                        }
                    }
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        "Cannot get second token for binary operation " + operator.getOperator() + 
                        ". " + e.getMessage());
                }
                
                if (token2 == null) {
                    throw new ExpressionEvaluationException(
                        exp.getExpression(),
                        "Second token is null for binary operation " + operator.getOperator());
                }
                
                // Check if token2 is a nested expression that needs tokens from token
                if (token2 instanceof Expression nestedExpr2) {
                    Operation[] nestedOps2 = nestedExpr2.getOpsArray();
                    ExpressionToken[] nestedTokens2 = nestedExpr2.getTokensArray();
                    
                    int nestedRequiredTokens2 = nestedOps2.length;
                    for (Operation op : nestedOps2) {
                        if (!UNARY_OPERATORS_MAP_KEY_SET.contains(op) && 
                            op != Operation.CONDITIONAL_TERNARY_OPERATOR &&
                            op != Operation.OBJECT_OPERATOR && 
                            op != Operation.ARRAY_OPERATOR) {
                            nestedRequiredTokens2++;
                        } else if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                            nestedRequiredTokens2 += 2;
                        }
                    }
                    
                    // If nestedExpr2 needs tokens, and token might be the missing token
                    if (nestedTokens2.length < nestedRequiredTokens2 && 
                        nestedRequiredTokens2 - nestedTokens2.length == 1) {
                        // Complete nestedExpr2 with token
                        Expression completeNested2 = new Expression("", 
                            nestedTokens2[0], token, nestedOps2[0], false);
                        var nestedValue2 = evaluateExpression(completeNested2, valuesMap);
                        // For parent operation: we need left operand
                        // token was used to complete nestedExpr2, so we need another token for left operand
                        if (ctx[1] < tokensSource.length) {
                            ExpressionToken leftOperand = tokensSource[ctx[1]++];
                            var v1 = getValueFromToken(valuesMap, leftOperand, tokensSource, ctx[1]);
                            var v2 = nestedValue2;
                            workingStack.push(applyOperation(operator, v1, v2));
                            continue;
                        }
                    }
                }
                
                // Normal case: both tokens are available
                var v1 = getValueFromToken(valuesMap, token2, tokensSource, ctx[1]);
                var v2 = getValueFromToken(valuesMap, token, tokensSource, ctx[1]);
                workingStack.push(applyOperation(operator, v1, v2));
            }
        }
        
        // Collect remaining source tokens
        // However, if we have remaining tokens but no more operations, this might indicate
        // a parser bug where tokens weren't properly grouped. 
        // If we have exactly 2 remaining tokens and the last operation was binary,
        // they might be the operands that should have been processed.
        // Otherwise, evaluate remaining tokens individually.
        int remainingTokensCount = tokensSource.length - ctx[1];
        if (remainingTokensCount > 0 && opsArray.length > 0) {
            // Check if we might have missed processing some tokens due to parser bug
            // If we have remaining tokens, try to evaluate them as a group
            // But only if we haven't already processed all operations correctly
            // For now, evaluate them individually and let the error handler catch issues
        }
        
        while (ctx[1] < tokensSource.length) {
            ExpressionToken remainingToken = tokensSource[ctx[1]++];
            // If it's a nested expression, evaluate it first
            if (remainingToken instanceof Expression nestedExpr) {
                JsonElement nestedValue = evaluateExpression(nestedExpr, valuesMap);
                workingStack.push(new ExpressionTokenValue("", nestedValue));
            } else {
                // For non-expression tokens, get their value
                JsonElement value = getValueFromToken(valuesMap, remainingToken, tokensSource, ctx[1]);
                workingStack.push(new ExpressionTokenValue("", value));
            }
        }

        if (workingStack.isEmpty())
            throw new ExecutionException(StringFormatter.format("Expression : $ evaluated to null", exp));

        if (workingStack.size() != 1)
            throw new ExecutionException(
                    StringFormatter.format("Expression : $ evaluated multiple values $", exp, workingStack));

        ExpressionToken token = workingStack.peek();
        // Use getValueFromToken which handles nested expressions correctly
        return getValueFromToken(valuesMap, token);
    }

    private void processObjectOrArrayOperatorIndexed(Map<String, TokenValueExtractor> valuesMap, 
            Operation[] opsArray, ExpressionToken[] tokensSource, Deque<ExpressionToken> workingStack,
            int[] ctx, Operation operator, ExpressionToken token, java.util.function.Supplier<ExpressionToken> popToken) {
        
        Deque<ExpressionToken> objTokens = new LinkedList<>();
        Deque<Operation> objOperations = new LinkedList<>();

        do {
            objOperations.push(operator);
            if (token instanceof Expression ex) {
                // For path components (identifiers with OBJECT_OPERATOR, ARRAY_OPERATOR, or no operations),
                // build the path string without parentheses - don't evaluate as a value.
                // For expressions with other operators (like +, -, etc.), evaluate to get the actual value.
                if (isPathExpression(ex)) {
                    // Build path string without parentheses
                    String tokenStr = buildPathString(ex);
                    objTokens.push(new ExpressionToken(tokenStr));
                } else {
                    objTokens.push(new ExpressionTokenValue(ex.getExpression(), this.evaluateExpression(ex, valuesMap)));
                }
            } else if (token != null) {
                objTokens.push(token);
            }
            // Use popToken function for consistent token access
            // Check if we have more tokens before trying to get one
            if (ctx[1] < tokensSource.length || !workingStack.isEmpty()) {
                try {
                    token = popToken.get();
                } catch (ExpressionEvaluationException e) {
                    // If we can't get a token, set to null and break the loop
                    token = null;
                }
            } else {
                token = null;
            }
            operator = ctx[0] < opsArray.length ? opsArray[ctx[0]++] : null;
        } while ((operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR) && token != null);

        if (token != null) {
            if (token instanceof Expression ex) {
                // Same logic: path components use buildPathString(), value expressions are evaluated
                if (isPathExpression(ex)) {
                    String tokenStr = buildPathString(ex);
                    objTokens.push(new ExpressionToken(tokenStr));
                } else {
                    objTokens.push(new ExpressionTokenValue(ex.getExpression(), this.evaluateExpression(ex, valuesMap)));
                }
            } else {
                objTokens.push(token);
            }
        }
        // If we consumed an operator that's not OBJECT/ARRAY, put the index back
        if (operator != null)
            ctx[0]--;

        ExpressionToken objToken = objTokens.pop();

        if (objToken instanceof ExpressionTokenValue vtoken && !vtoken.getElement().isJsonPrimitive()) {
            final String key = "_k" + (keyCounter++);
            this.internalTokenValueExtractor.addValue(key, vtoken.getElement());
            objToken = new ExpressionToken(ExpressionInternalValueExtractor.PREFIX + key);
        }

        StringBuilder sb = new StringBuilder((objToken instanceof ExpressionTokenValue etv ? etv.getTokenValue()
                .getAsString() : objToken.toString()));

        while (!objTokens.isEmpty()) {
            objToken = objTokens.pop();
            operator = objOperations.pop();
            String tokenStr;
            if (objToken instanceof ExpressionTokenValue etv) {
                String originalExpr = etv.getExpression();
                String evaluatedValue = etv.getTokenValue().getAsString();
                // Preserve quotes for bracket notation with quoted keys containing dots (like ["mail.props.port"])
                // Only preserve when the key contains dots, to distinguish from simple bracket access like ["length"]
                if (operator == ARRAY_OPERATOR && originalExpr != null && !originalExpr.isEmpty()
                        && (originalExpr.charAt(0) == '"' || originalExpr.charAt(0) == '\'')
                        && evaluatedValue.contains(".")) {
                    tokenStr = originalExpr;
                } else {
                    tokenStr = evaluatedValue;
                }
            } else {
                tokenStr = objToken.toString();
            }
            sb.append(operator.getOperator()).append(tokenStr);
            if (operator == ARRAY_OPERATOR)
                sb.append(']');
        }

        String str = sb.toString();
        String key = str.substring(0, str.indexOf('.') + 1);
        if (key.length() > 2 && valuesMap.containsKey(key)) {
            workingStack.push(new ExpressionTokenValue(str, getValue(str, valuesMap)));
        } else {
            JsonElement v = null;
            try {
                v = LiteralTokenValueExtractor.INSTANCE.getValue(str);
            } catch (Exception ex) {
                v = new JsonPrimitive(str);
            }
            workingStack.push(new ExpressionTokenValue(str, v));
        }
    }

    private ExpressionToken applyOperation(Operation operator, JsonElement v1, JsonElement v2, JsonElement v3) {

        if (v1 == null)
            v1 = JsonNull.INSTANCE;
        if (v2 == null)
            v2 = JsonNull.INSTANCE;
        if (v3 == null)
            v3 = JsonNull.INSTANCE;

        TernaryOperator op = TERNARY_OPERATORS_MAP.get(operator);

        if (op == null)
            throw new ExpressionEvaluationException(this.expression,
                    StringFormatter.format("No operator found to evaluate $ $ $", v1, operator.getOperator(), v2));

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2, v3));
    }

    private ExpressionToken applyOperation(Operation operator, JsonElement v1, JsonElement v2) {

        if (v1 == null)
            v1 = JsonNull.INSTANCE;
        if (v2 == null)
            v2 = JsonNull.INSTANCE;

        if ((v1 != JsonNull.INSTANCE && !v1.isJsonPrimitive()) && (v2 != JsonNull.INSTANCE && !v2.isJsonPrimitive())
                && operator != EQUAL && operator != NOT_EQUAL && operator != NULLISH_COALESCING_OPERATOR
                && operator != AND && operator != OR)

            throw new ExpressionEvaluationException(this.expression,
                    StringFormatter.format("Cannot evaluate expression $ $ $", v1, operator.getOperator(), v2));

        BinaryOperator op = BINARY_OPERATORS_MAP.get(operator);

        if (op == null)
            throw new ExpressionEvaluationException(this.expression,
                    StringFormatter.format("No operator found to evaluate $ $ $", v1, operator.getOperator(), v2));

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2));
    }

    private ExpressionToken applyOperation(Operation operator, JsonElement value) {

        if (value != null && (value != JsonNull.INSTANCE && !value.isJsonPrimitive() && operator != Operation.NOT &&
                operator != Operation.UNARY_LOGICAL_NOT))
            throw new ExpressionEvaluationException(this.expression,
                    StringFormatter.format("The operator $ cannot be applied to $", operator.getOperator(), value));

        UnaryOperator op = UNARY_OPERATORS_MAP.get(operator);

        if (op == null)
            throw new ExpressionEvaluationException(this.expression, StringFormatter
                    .format("No Unary operator $ is found to apply on $", operator.getOperator(), value));

        return new ExpressionTokenValue(operator.toString(), op.apply(value));
    }

    private JsonElement getValueFromToken(Map<String, TokenValueExtractor> valuesMap, ExpressionToken token) {
        return getValueFromToken(valuesMap, token, null, -1);
    }
    
    private JsonElement getValueFromToken(Map<String, TokenValueExtractor> valuesMap, ExpressionToken token,
                                         ExpressionToken[] parentTokens, int parentTokenOffset) {

        if (token instanceof Expression ex) {
            // Check if the nested expression has a valid structure before evaluating
            // The old parser sometimes creates nested expressions with operations but not enough tokens
            Operation[] nestedOps = ex.getOpsArray();
            ExpressionToken[] nestedTokens = ex.getTokensArray();
            
            // Count required tokens for operations
            int requiredTokens = nestedOps.length;
            for (Operation op : nestedOps) {
                if (!UNARY_OPERATORS_MAP_KEY_SET.contains(op) && 
                    op != Operation.CONDITIONAL_TERNARY_OPERATOR &&
                    op != Operation.OBJECT_OPERATOR && 
                    op != Operation.ARRAY_OPERATOR) {
                    requiredTokens++; // Binary operations need one more token
                } else if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                    requiredTokens += 2; // Ternary needs 2 more tokens (3 total)
                }
            }
            
            // If the nested expression doesn't have enough tokens, it's malformed
            // This happens when the old parser creates expressions incorrectly
            // The parser might have split an expression like "'kir" + ' an' incorrectly,
            // creating a nested expression with only part of it
            // Pass parent context so the evaluator can find missing tokens
            if (nestedTokens.length < requiredTokens) {
                // Pass parent tokens to nested evaluator so it can find missing tokens
                return this.evaluateExpression(ex, valuesMap, parentTokens, parentTokenOffset);
            }
            
            return this.evaluateExpression(ex, valuesMap, parentTokens, parentTokenOffset);
        } else if (token instanceof ExpressionTokenValue v) {
            return v.getElement();
        }
        return getValue(token.getExpression(), valuesMap);
    }

    private JsonElement getValue(String path, Map<String, TokenValueExtractor> valuesMap) {

        if (path.length() <= 5)
            return LiteralTokenValueExtractor.INSTANCE.getValueFromExtractors(path, valuesMap);

        String pathPrefix = path.substring(0, path.indexOf('.') + 1);
        if (valuesMap.containsKey(pathPrefix)) {
            return valuesMap.get(pathPrefix)
                    .getValue(path);
        }

        return LiteralTokenValueExtractor.INSTANCE.getValueFromExtractors(path, valuesMap);
    }
    
    /**
     * Build a path string from a path Expression, without parentheses.
     * E.g., Expression for "a.(b.c)" returns "a.b.c"
     */
    private String buildPathString(Expression expr) {
        Operation[] ops = expr.getOpsArray();
        ExpressionToken[] tokens = expr.getTokensArray();
        
        // Leaf expression - just return the token string
        if (ops.length == 0) {
            if (tokens.length == 1) {
                ExpressionToken token = tokens[0];
                if (token instanceof Expression ex) {
                    return buildPathString(ex);
                }
                // For ExpressionTokenValue, use getExpression() not toString()
                // (toString() returns "expr: value" format which is wrong for paths)
                return getTokenExpressionString(token);
            }
            return expr.getExpression() != null ? expr.getExpression() : "";
        }
        
        // Binary expression - build path from tokens and operators
        // With push() order: tokens[0]=right, tokens[1]=left
        if (tokens.length >= 2 && ops.length >= 1) {
            ExpressionToken right = tokens[0];
            ExpressionToken left = tokens[1];
            Operation op = ops[0];
            
            String leftStr = left instanceof Expression ? buildPathString((Expression) left) : getTokenExpressionString(left);
            String rightStr = right instanceof Expression ? buildPathString((Expression) right) : getTokenExpressionString(right);
            
            if (op == OBJECT_OPERATOR) {
                return leftStr + "." + rightStr;
            } else if (op == ARRAY_OPERATOR) {
                return leftStr + "[" + rightStr + "]";
            } else if (op == ARRAY_RANGE_INDEX_OPERATOR) {
                return leftStr + ".." + rightStr;
            }
        }
        
        // Fallback to toString() with parens stripped
        return stripOuterParens(expr.toString());
    }
    
    /**
     * Get the expression string from a token, handling ExpressionTokenValue specially.
     */
    private String getTokenExpressionString(ExpressionToken token) {
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
    private String stripOuterParens(String str) {
        if (str.length() >= 2 && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
            // Count parentheses to ensure we only strip matching outer parens
            int depth = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '(') depth++;
                else if (str.charAt(i) == ')') depth--;
                // If depth becomes 0 before the last char, the outer parens don't match
                if (depth == 0 && i < str.length() - 1) {
                    return str; // Don't strip, the parens don't match
                }
            }
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
    
    /**
     * Check if an Expression is a path component (identifier, OBJECT_OPERATOR, or ARRAY_OPERATOR).
     * Path components should use toString() for path building, not be evaluated as values.
     * Expressions with other operators (like +, -, etc.) should be evaluated.
     */
    private boolean isPathExpression(Expression expr) {
        Operation[] ops = expr.getOpsArray();
        
        // No operations = leaf identifier - use toString()
        if (ops.length == 0) {
            return true;
        }
        
        // Check if all operations are path-related (OBJECT_OPERATOR or ARRAY_OPERATOR)
        // ARRAY_RANGE_INDEX_OPERATOR should NOT be treated as a path expression
        // because it needs to be evaluated to produce the range string "start..end"
        for (Operation op : ops) {
            if (op != OBJECT_OPERATOR &&
                op != ARRAY_OPERATOR) {
                // Has non-path operator - needs evaluation
                return false;
            }
        }
        
        // Also check nested expressions in tokens
        ExpressionToken[] tokens = expr.getTokensArray();
        for (ExpressionToken token : tokens) {
            if (token instanceof Expression ex) {
                if (!isPathExpression(ex)) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
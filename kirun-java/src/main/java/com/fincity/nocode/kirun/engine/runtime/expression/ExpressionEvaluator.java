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

    private JsonElement evaluateExpression(Expression exp, Map<String, TokenValueExtractor> valuesMap) {
        // Use cached arrays for non-destructive evaluation
        Operation[] opsArray = exp.getOpsArray();
        ExpressionToken[] tokensSource = exp.getTokensArray();
        Deque<ExpressionToken> workingStack = new LinkedList<>();
        
        // Context for tracking indices
        int[] ctx = {0, 0}; // opIdx, srcIdx
        
        while (ctx[0] < opsArray.length) {
            Operation operator = opsArray[ctx[0]++];
            ExpressionToken token = workingStack.isEmpty() ? tokensSource[ctx[1]++] : workingStack.pop();

            if (UNARY_OPERATORS_MAP_KEY_SET.contains(operator)) {
                workingStack.push(applyOperation(operator, getValueFromToken(valuesMap, token)));
            } else if (operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR) {
                processObjectOrArrayOperatorIndexed(valuesMap, opsArray, tokensSource, workingStack, ctx, operator, token);
            } else if (operator == CONDITIONAL_TERNARY_OPERATOR) {
                ExpressionToken token2 = workingStack.isEmpty() ? tokensSource[ctx[1]++] : workingStack.pop();
                ExpressionToken token3 = workingStack.isEmpty() ? tokensSource[ctx[1]++] : workingStack.pop();

                var v1 = getValueFromToken(valuesMap, token3);
                var v2 = getValueFromToken(valuesMap, token2);
                var v3 = getValueFromToken(valuesMap, token);
                workingStack.push(applyOperation(operator, v1, v2, v3));
            } else {
                ExpressionToken token2 = workingStack.isEmpty() ? tokensSource[ctx[1]++] : workingStack.pop();
                var v1 = getValueFromToken(valuesMap, token2);
                var v2 = getValueFromToken(valuesMap, token);
                workingStack.push(applyOperation(operator, v1, v2));
            }
        }
        
        // Collect remaining source tokens
        while (ctx[1] < tokensSource.length) {
            workingStack.push(tokensSource[ctx[1]++]);
        }

        if (workingStack.isEmpty())
            throw new ExecutionException(StringFormatter.format("Expression : $ evaluated to null", exp));

        if (workingStack.size() != 1)
            throw new ExecutionException(
                    StringFormatter.format("Expression : $ evaluated multiple values $", exp, workingStack));

        ExpressionToken token = workingStack.peek();
        if (token instanceof ExpressionTokenValue etv)
            return etv.getElement();
        else if (token instanceof Expression ex)
            return evaluateExpression(ex, valuesMap);
        else
            return getValueFromToken(valuesMap, token);
    }

    private void processObjectOrArrayOperatorIndexed(Map<String, TokenValueExtractor> valuesMap, 
            Operation[] opsArray, ExpressionToken[] tokensSource, Deque<ExpressionToken> workingStack,
            int[] ctx, Operation operator, ExpressionToken token) {
        
        Deque<ExpressionToken> objTokens = new LinkedList<>();
        Deque<Operation> objOperations = new LinkedList<>();

        do {
            objOperations.push(operator);
            if (token instanceof Expression ex) {
                objTokens.push(new ExpressionTokenValue(ex.getExpression(), this.evaluateExpression(ex, valuesMap)));
            } else if (token != null) {
                objTokens.push(token);
            }
            token = (workingStack.isEmpty() && ctx[1] < tokensSource.length) ? tokensSource[ctx[1]++] :
                    (!workingStack.isEmpty() ? workingStack.pop() : null);
            operator = ctx[0] < opsArray.length ? opsArray[ctx[0]++] : null;
        } while (operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR);

        if (token != null) {
            if (token instanceof Expression ex) {
                objTokens.push(new ExpressionTokenValue(ex.getExpression(), this.evaluateExpression(ex, valuesMap)));
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

        if (token instanceof Expression ex) {
            return this.evaluateExpression(ex, valuesMap);
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
}
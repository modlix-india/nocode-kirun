package com.fincity.nocode.kirun.engine.runtime.expression;

import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OPERATORS;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OPERATORS_WITHOUT_SPACE_WRAP;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OPERATOR_PRIORITY;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_MAP;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_OPERATORS;

import java.util.Deque;
import java.util.LinkedList;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class Expression extends ExpressionToken {

    private LinkedList<ExpressionToken> tokens = new LinkedList<>();
    private LinkedList<Operation> ops = new LinkedList<>();
    
    // Cached arrays for non-destructive evaluation
    private ExpressionToken[] tokensArray;
    private Operation[] opsArray;

    public Expression(String expression) {

        super(expression);

        if (expression == null || expression.isBlank())
            throw new ExpressionEvaluationException(expression, "No expression found to evaluate");
        this.evaluate();
        if (!this.ops.isEmpty() && "..".equals(this.ops.peekLast().getOperator()) && this.tokens.size() == 1)
            this.tokens.push(new ExpressionToken(""));
    }

    public Expression(ExpressionToken token, Operation op) {
        super("");
        this.tokens.push(token);
        this.ops.push(op);
    }

    public Expression(ExpressionToken l, ExpressionToken r, Operation op) {

        super("");
        if (op != null && "..".equals(op.getOperator())) {
            if (l == null) l = new ExpressionTokenValue("", new JsonPrimitive(""));
            if (r == null || r.getExpression().isEmpty()) r = new ExpressionTokenValue("", new JsonPrimitive(""));
        }
        this.tokens.push(l);
        this.tokens.push(r);
        this.ops.push(op);
    }

    public LinkedList<ExpressionToken> getTokens() { // NOSONAR - LinkedList is required
        return this.tokens;
    }

    public LinkedList<Operation> getOperations() {// NOSONAR - LinkedList is required
        return this.ops;
    }
    
    /**
     * Get tokens as a cached array for non-destructive evaluation.
     */
    public ExpressionToken[] getTokensArray() {
        if (tokensArray == null) {
            tokensArray = tokens.toArray(new ExpressionToken[0]);
        }
        return tokensArray;
    }
    
    /**
     * Get operations as a cached array for non-destructive evaluation.
     */
    public Operation[] getOpsArray() {
        if (opsArray == null) {
            opsArray = ops.toArray(new Operation[0]);
        }
        return opsArray;
    }

    private void evaluate() {

        final int length = this.expression.length();
        char chr = 0;

        StringBuilder sb = new StringBuilder();
        String buff = null;
        int i = 0;
        boolean isPrevOp = false;


        while (i < length) {

            chr = this.expression.charAt(i);
            buff = sb.toString();

            switch (chr) {
                case ' ': {

                    isPrevOp = processTokenSepearator(sb, buff, isPrevOp);
                    break;
                }
                case '(': {

                    i = processSubExpression(length, sb, buff, i, isPrevOp);
                    isPrevOp = false;
                    break;
                }
                case ')': {
                    throw new ExpressionEvaluationException(this.expression, "Extra closing parenthesis found");
                }
                case ']': {
                    throw new ExpressionEvaluationException(this.expression, "Extra closing square bracket found");
                }
                case '\'', '"': {

                    Tuple2<Integer, Boolean> result = processStringLiteral(length, chr, i);
                    i = result.getT1();
                    isPrevOp = result.getT2();
                    break;
                }
                case '?': {

                    if (i + 1 < length && this.expression.charAt(i + 1) != '?' && i != 0
                            && this.expression.charAt(i - 1) != '?') {
                        i = processTernaryOperator(length, sb, buff, i, isPrevOp);
                    } else {
                        Tuple2<Integer, Boolean> result = processOthers(chr, length, sb, buff, i, isPrevOp);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                        if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
                            result = process(length, sb, i);
                            i = result.getT1();
                            isPrevOp = result.getT2();
                        }
                    }
                    break;
                }
                default:
                    Tuple2<Integer, Boolean> result = processOthers(chr, length, sb, buff, i, isPrevOp);
                    i = result.getT1();
                    isPrevOp = result.getT2();
                    if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
                        result = process(length, sb, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    }
            }

            ++i;
        }

        buff = sb.toString();
        if (!buff.isBlank()) {
            if (OPERATORS.contains(buff)) {
                throw new ExpressionEvaluationException(this.expression, "Expression is ending with an operator");
            } else {
                tokens.push(new ExpressionToken(buff));
            }
        }
    }

    private Tuple2<Integer, Boolean> processStringLiteral(final int length, char chr, int i) {
        StringBuilder strConstant = new StringBuilder();

        int j = i + 1;
        for (; j < length; j++) {

            char nextChar = this.expression.charAt(j);

            if (nextChar == chr && this.expression.charAt(j - 1) != '\\')
                break;

            strConstant.append(nextChar);
        }

        if (j == length && this.expression.charAt(j - 1) != chr) {
            throw new ExpressionEvaluationException(this.expression, "Missing string ending marker " + chr);
        }

        Tuple2<Integer, Boolean> result = Tuples.of(j, false);
        String str = strConstant.toString();
        this.tokens.push(new ExpressionTokenValue(str, new JsonPrimitive(str)));
        return result;
    }

    private Tuple2<Integer, Boolean> process(final int length, StringBuilder sb, int i) {

        int cnt = 1;
        ++i;
        while (i < length && cnt != 0) {
            char c = this.expression.charAt(i);
            if (c == ']')
                --cnt;
            else if (c == '[')
                ++cnt;
            if (cnt != 0) {
                sb.append(c);
                i++;
            }
        }
        this.tokens.push(new Expression(sb.toString()));
        sb.setLength(0);

        return Tuples.of(i, false);
    }

    private Tuple2<Integer, Boolean> processOthers(char chr, final int length, StringBuilder sb, String buff, int i,
                                                   boolean isPrevOp) {

        int start = length - i;
        start = start < Operation.BIGGEST_OPERATOR_SIZE ? start : Operation.BIGGEST_OPERATOR_SIZE;

        for (int size = start; size > 0; size--) {
            String op = this.expression.substring(i, i + size);
            if (OPERATORS_WITHOUT_SPACE_WRAP.contains(op)) {
                if (!buff.isBlank()) {
                    tokens.push(new ExpressionToken(buff));
                    isPrevOp = false;
                } else if ("..".equals(op) && tokens.isEmpty()) {
                    tokens.push(new ExpressionToken("0"));
                    isPrevOp = false;
                }
                checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(op), isPrevOp);
                isPrevOp = true;
                sb.setLength(0);
                return Tuples.of(i + size - 1, isPrevOp);
            }
        }

        sb.append(chr);
        return Tuples.of(i, false);
    }

    private int processTernaryOperator(final int length, StringBuilder sb, String buff, int i, boolean isPrevOp) {

        if (isPrevOp) {
            throw new ExpressionEvaluationException(this.expression, "Ternary operator is followed by an operator");
        }

        if (!buff.isBlank()) {
            this.tokens.push(new Expression(buff));
            sb.setLength(0);
        }

        ++i;
        int cnt = 1;
        char inChr = 0;
        int start = i;
        while (i < length && cnt > 0) {

            inChr = this.expression.charAt(i);
            if (inChr == '?')
                ++cnt;
            else if (inChr == ':')
                --cnt;
            ++i;
        }

        if (inChr != ':') {
            throw new ExpressionEvaluationException(this.expression, "':' operater is missing");
        }

        if (i >= length) {
            throw new ExpressionEvaluationException(this.expression, "Third part of the ternary expression is missing");
        }

        while (!ops.isEmpty() && hasPrecedence(Operation.CONDITIONAL_TERNARY_OPERATOR, ops.peek())) {

            Operation prev = ops.pop();

            if (UNARY_OPERATORS.contains(prev)) {
                ExpressionToken l = tokens.pop();
                tokens.push(new Expression(l, prev));
            } else {
                ExpressionToken r = tokens.pop();
                ExpressionToken l = tokens.pop();

                tokens.push(new Expression(l, r, prev));
            }
        }

        this.ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR);
        this.tokens.push(new Expression(this.expression.substring(start, i - 1)));

        String secondExp = this.expression.substring(i);
        if (secondExp.isBlank()) {
            throw new ExpressionEvaluationException(this.expression, "Third part of the ternary expression is missing");
        }

        this.tokens.push(new Expression(secondExp));

        return length - 1;
    }

    private int processSubExpression(final int length, StringBuilder sb, String buff, int i, boolean isPrevOp) {

        if (OPERATORS.contains(buff)) {
            checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(buff), isPrevOp);
            sb.setLength(0);
        } else if (!buff.isBlank()) {
            throw new ExpressionEvaluationException(this.expression,
                    StringFormatter.format("Unkown token : $ found.", buff));
        }

        int cnt = 1;
        StringBuilder subExp = new StringBuilder();
        char inChr = this.expression.charAt(i);
        i++;
        while (i < length && cnt > 0) {
            inChr = this.expression.charAt(i);
            if (inChr == '(')
                cnt++;
            else if (inChr == ')')
                cnt--;
            if (cnt != 0) {
                subExp.append(inChr);
                i++;
            }
        }

        if (inChr != ')')
            throw new ExpressionEvaluationException(this.expression, "Missing a closed parenthesis");

        // Only remove outer parentheses if they actually match
        while (subExp.length() > 2 && hasMatchingOuterParentheses(subExp.toString())) {
            subExp.deleteCharAt(0);
            subExp.setLength(subExp.length() - 1);
        }

        tokens.push(new Expression(subExp.toString()
                .trim()));
        return i;
    }

    private boolean processTokenSepearator(StringBuilder sb, String buff, boolean isPrevOp) {

        if (!buff.isBlank()) {

            if (OPERATORS.contains(buff)) {
                checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(buff), isPrevOp);
                isPrevOp = true;
            } else {
                tokens.push(new ExpressionToken(buff));
                isPrevOp = false;
            }
        }
        sb.setLength(0);

        return isPrevOp;
    }

    private void checkUnaryOperator(Deque<ExpressionToken> tokens, Deque<Operation> ops, Operation op,
                                    boolean isPrevOp) {
        if (isPrevOp || tokens.isEmpty()) {
            if (UNARY_OPERATORS.contains(op)) {
                ops.push(UNARY_MAP.get(op));

            } else {
                throw new ExpressionEvaluationException(this.expression,
                        StringFormatter.format("Extra operator $ found.", op));
            }
        } else {
            while (!ops.isEmpty() && hasPrecedence(op, ops.peek())) {

                Operation prev = ops.pop();
                if (UNARY_OPERATORS.contains(prev)) {
                    ExpressionToken l = tokens.pop();
                    tokens.push(new Expression(l, prev));
                } else {
                    ExpressionToken r = tokens.pop();
                    ExpressionToken l = tokens.pop();

                    tokens.push(new Expression(l, r, prev));
                }

            }
            ops.push(op);
        }

    }

    private boolean hasPrecedence(Operation op1, Operation op2) {

        int pre1 = OPERATOR_PRIORITY.get(op1);
        int pre2 = OPERATOR_PRIORITY.get(op2);

        return pre2 < pre1;
    }

    private boolean hasMatchingOuterParentheses(String str) {
        if (str.length() < 2 || str.charAt(0) != '(' || str.charAt(str.length() - 1) != ')') {
            return false;
        }
        // Check if the first '(' matches the last ')'
        // by verifying that the nesting level never drops to 0 before the end
        int level = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            char ch = str.charAt(i);
            if (ch == '(') level++;
            else if (ch == ')') level--;
            if (level == 0) return false; // First paren closed before end
        }
        return level == 1; // Should be 1 just before the last ')'
    }

    @Override
    public String toString() {

        if (ops.isEmpty()) {
            if (this.tokens.size() == 1)
                return this.tokens.get(0)
                        .toString();
            return "Error: No tokens";
        }

        StringBuilder sb = new StringBuilder();
        int ind = 0;
        for (int i = 0; i < this.ops.size(); i++) {

            if (this.ops.get(i)
                    .getOperator()
                    .startsWith("UN: ")) {
                sb.append("(")
                        .append(this.ops.get(i)
                                .getOperator()
                                .substring(4))
                        .append(this.tokens.get(ind))
                        .append(")");
                ind++;
            } else if (ops.get(i) == Operation.CONDITIONAL_TERNARY_OPERATOR) {

                sb.insert(0, this.tokens.get(ind++));
                sb.insert(0, ":");
                sb.insert(0, this.tokens.get(ind++));
                sb.insert(0, "?");
                sb.insert(0, this.tokens.get(ind++))
                        .append(")");
                sb.insert(0, "(");

            } else if (ops.get(i) == Operation.ARRAY_RANGE_INDEX_OPERATOR && this.tokens.size() == 1) {

                sb.insert(0, this.ops.get(i)
                        .getOperator());
                sb.insert(0, this.tokens.get(ind++));

                sb.insert(0, "(").append(")");

            } else {

                if (ind == 0) {
                    sb.insert(0, this.tokens.get(ind++));
                }
                sb.insert(0, this.ops.get(i)
                        .getOperator());
                if (ind < this.tokens.size())
                    sb.insert(0, this.tokens.get(ind++));
                sb.insert(0, "(")
                        .append(")");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Expression e)
            return this.expression.equals(e.expression);

        return false;
    }

    @Override
    public int hashCode() {
        return this.expression.hashCode();
    }
}

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

    /**
     * Create a ternary expression with condition, trueExpr, and falseExpr.
     * Using push() to maintain compatibility with evaluator (push adds to head).
     * Tokens will be in order: [falseExpr, trueExpr, condition] from head to tail.
     */
    public static Expression createTernary(ExpressionToken condition, ExpressionToken trueExpr, ExpressionToken falseExpr) {
        Expression expr = new Expression("", null, null, null, true);
        // Push in reverse order so get() returns them correctly
        // push(condition) -> head=condition
        // push(trueExpr) -> head=trueExpr, trueExpr.next=condition  
        // push(falseExpr) -> head=falseExpr, falseExpr.next=trueExpr.next=condition
        // get(0)=falseExpr, get(1)=trueExpr, get(2)=condition
        expr.tokens.push(condition);
        expr.tokens.push(trueExpr);
        expr.tokens.push(falseExpr);
        expr.ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR);
        return expr;
    }

    /**
     * Create a leaf expression (identifier/number) without re-parsing.
     * Used by the parser for leaf nodes.
     */
    public static Expression createLeaf(String value) {
        // Pass the value as the expression string - it will be used by toString()
        // Use skipParsing=true to avoid parsing
        Expression expr = new Expression(value, null, null, null, true);
        // Push a token for the leaf value - the evaluator needs at least one token
        expr.tokens.push(new ExpressionToken(value));
        return expr;
    }

    public Expression(String expression) {
        this(expression, null, null, null, false);
    }

    public Expression(String expression, ExpressionToken l, ExpressionToken r, Operation op, boolean skipParsing) {
        // Trim leading/trailing whitespace before storing in super class
        super(expression != null ? expression.trim() : "");
        
        // If we have left/right tokens and operation, construct directly (for AST building)
        // Using push() to maintain compatibility with evaluator (push adds to head).
        // For binary ops: push(left), push(right) -> get(0)=right, get(1)=left
        // This should happen even if skipParsing is true (parser creates expressions this way)
        if (l != null || r != null || op != null) {
            if (op != null && "..".equals(op.getOperator())) {
                // For range operator, handle missing operands
                // Note: Don't check r.getExpression().isEmpty() for Expression objects
                // because parser-created expressions have empty expression strings
                if (l == null) l = new ExpressionTokenValue("", new JsonPrimitive(""));
                if (r == null || (!(r instanceof Expression) && r.getExpression().isEmpty())) {
                    r = new ExpressionTokenValue("", new JsonPrimitive(""));
                }
            }
            if (l != null) this.tokens.push(l);
            if (r != null) this.tokens.push(r);
            if (op != null) this.ops.push(op);
            // For constructed expressions, don't re-parse
            return;
        }
        
        // If skipParsing is true and we don't have tokens/ops, don't parse (used by createLeaf/createTernary)
        if (skipParsing) {
            return;
        }
        
        // If we have an expression string, parse it
        // Note: expression is already trimmed in super() constructor
        if (expression != null && !expression.isBlank()) {
            // Try the new parser first, fall back to old parser if it fails
            try {
                // Use this.expression which contains the trimmed version from super()
                ExpressionParser parser = new ExpressionParser(this.expression);
                Expression parsed = parser.parse();
                // Copy tokens and operations from parsed expression
                // Create new LinkedLists to avoid sharing references
                this.tokens = new LinkedList<>(parsed.getTokens());
                this.ops = new LinkedList<>(parsed.getOperations());
                // Ensure we have the correct structure - if parser succeeded, we should have ops
                if (this.ops.isEmpty() && !this.tokens.isEmpty()) {
                    // Parser created structure but no ops - this shouldn't happen, but fall back
                    this.tokens.clear();
                    this.evaluate();
                }
            } catch (Exception e) {
                // Fall back to old parser if new one fails (for backward compatibility during migration)
                this.evaluate();
                if (!this.ops.isEmpty() && "..".equals(this.ops.peekLast().getOperator()) && this.tokens.size() == 1) {
                    this.tokens.push(new ExpressionToken(""));
                }
            }
        } else {
            throw new ExpressionEvaluationException(expression, "No expression found to evaluate");
        }
    }

    public Expression(ExpressionToken token, Operation op) {
        this("", token, null, op, false);
    }

    public Expression(ExpressionToken l, ExpressionToken r, Operation op) {
        this("", l, r, op, false);
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
                    // If we're inside a bracket (ARRAY_OPERATOR was just added),
                    // don't treat quotes as string literals - they're part of the bracket notation
                    if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
                        Tuple2<Integer, Boolean> result = process(length, sb, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    } else {
                        Tuple2<Integer, Boolean> result = processStringLiteral(length, chr, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    }
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
        // Preserve quotes in the expression field for bracket notation
        String quotedExpression = chr + str + chr;
        this.tokens.push(new ExpressionTokenValue(quotedExpression, new JsonPrimitive(str)));
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
        
        // For left-associative operators with same precedence, combine the previous
        // operation before adding the new one. This ensures 5 - 1 - 2 is parsed as
        // (5-1) - 2, not 5 - (1-2).
        // Exception: OBJECT_OPERATOR and ARRAY_OPERATOR should NOT combine -
        // they need to stay flat for path building (e.g., a.b.c).
        if (pre2 == pre1) {
            return op2 != Operation.OBJECT_OPERATOR &&
                   op2 != Operation.ARRAY_OPERATOR &&
                   op1 != Operation.OBJECT_OPERATOR &&
                   op1 != Operation.ARRAY_OPERATOR;
        }
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

    /**
     * Simple recursive toString() that walks the AST tree.
     * The tree structure is in the tokens LinkedList:
     * - For Expression('', left, right, op): tokens[0] = left, tokens[1] = right
     * - For leaf nodes: just the expression string
     * 
     * Handles both tree structure (single operation) and flat structure (multiple operations from old parser).
     */
    @Override
    public String toString() {
        // If we have operations, always format from tokens/ops (new parser structure)
        // This ensures proper formatting with parentheses
        if (!this.ops.isEmpty()) {
            // If there's only one operation, use tree-based formatting (new parser structure)
            if (this.ops.size() == 1) {
                Operation op = this.ops.get(0);
                
                // Unary operation: (operator operand)
                if (op.getOperator().startsWith("UN: ")) {
                    return this.formatUnaryOperation(op);
                }
                
                // Ternary operation: (condition ? trueExpr : falseExpr)
                if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                    return this.formatTernaryOperation();
                }
                
                // Binary operation
                return this.formatBinaryOperation(op);
            }
            
            // Multiple operations: use old parser's flat structure logic
            // This handles expressions parsed by the old parser that create a flat list
            return this.formatFlatStructure();
        }
        
        // Leaf node: no operations, just return the token
        if (this.tokens.size() == 1) {
            return this.tokenToString(this.tokens.get(0));
        }
        // Handle special case: expression string without parsed structure
        // Only use expression field if we have no tokens/ops (unparsed expression)
        // This should only happen if the parser completely failed and old parser also failed
        if (this.tokens.isEmpty() && this.ops.isEmpty() && this.expression != null && !this.expression.isEmpty()) {
            return this.expression;
        }
        // If we have tokens but no ops, this shouldn't happen - it means parser failed
        // But old parser should have created ops. Return error to help debug
        return "Error: No operations but has tokens";
    }
    
    /**
     * Format expression with multiple operations (flat structure from old parser).
     * This builds the string by processing operations in order.
     */
    private String formatFlatStructure() {
        // For flat structure from old parser, we need to build the expression with proper parentheses
        // The old parser creates a flat list, but we should still format it with parentheses
        // If we can't format properly, don't fall back to expression field - that loses parentheses
        StringBuilder sb = new StringBuilder();
        int ind = 0;
        for (int i = 0; i < this.ops.size(); i++) {
            Operation op = this.ops.get(i);
            
            if (op.getOperator().startsWith("UN: ")) {
                if (ind >= this.tokens.size()) {
                    return "Error: Not enough tokens for unary operation";
                }
                sb.append("(")
                        .append(op.getOperator().substring(4))
                        .append(this.tokenToString(this.tokens.get(ind)))
                        .append(")");
                ind++;
            } else if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                if (ind + 2 >= this.tokens.size()) {
                    return "Error: Not enough tokens for ternary operation";
                }
                sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                sb.insert(0, ":");
                sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                sb.insert(0, "?");
                sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                sb.insert(0, "(");
                sb.append(")");
            } else if (op == Operation.ARRAY_RANGE_INDEX_OPERATOR && this.tokens.size() == 1) {
                if (ind >= this.tokens.size()) {
                    return "Error: Not enough tokens for range operation";
                }
                sb.insert(0, op.getOperator());
                sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                sb.insert(0, "(");
                sb.append(")");
            } else {
                // Binary operation - always add parentheses
                if (ind == 0 && ind < this.tokens.size()) {
                    sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                }
                sb.insert(0, op.getOperator());
                if (ind < this.tokens.size()) {
                    sb.insert(0, this.tokenToString(this.tokens.get(ind++)));
                }
                sb.insert(0, "(");
                sb.append(")");
            }
        }
        
        // If we couldn't build anything, this is an error
        if (sb.length() == 0) {
            return "Error: Could not format flat structure";
        }
        
        return sb.toString();
    }

    /**
     * Format a unary operation: (operator operand)
     */
    private String formatUnaryOperation(Operation op) {
        ExpressionToken operand = this.tokens.get(0);
        String operandStr = this.tokenToString(operand);
        return "(" + op.getOperator().substring(4) + operandStr + ")";
    }

    /**
     * Format a ternary operation: (condition ? trueExpr : falseExpr)
     * Note: With push() order, tokens are [falseExpr, trueExpr, condition]
     */
    private String formatTernaryOperation() {
        // Safety check: ensure we have at least 3 tokens for ternary operation
        if (this.tokens.size() < 3) {
            // Fall back to expression string if available
            if (this.expression != null && !this.expression.isEmpty()) {
                return this.expression;
            }
            return "Error: Invalid ternary expression";
        }
        
        // With push() order: get(0)=falseExpr, get(1)=trueExpr, get(2)=condition
        ExpressionToken falseExpr = this.tokens.get(0);
        ExpressionToken trueExpr = this.tokens.get(1);
        ExpressionToken condition = this.tokens.get(2);
        
        String conditionStr = this.tokenToString(condition);
        String trueStr = this.tokenToString(trueExpr);
        String falseStr = this.tokenToString(falseExpr);
        
        return "(" + conditionStr + "?" + trueStr + ":" + falseStr + ")";
    }

    /**
     * Format a binary operation based on operator type
     * Note: With push() order, tokens are [right, left] (push(left), push(right) -> get(0)=right)
     */
    private String formatBinaryOperation(Operation op) {
        // Safety check: ensure we have at least 2 tokens for binary operation
        if (this.tokens.size() < 2) {
            // Single token case - just return the token (shouldn't happen for binary ops)
            if (this.tokens.size() == 1) {
                return this.tokenToString(this.tokens.get(0));
            }
            // No tokens - this is an error, but try to format from expression if available
            if (this.expression != null && !this.expression.isEmpty()) {
                return this.expression;
            }
            return "Error: Invalid binary expression";
        }
        
        // With push() order: get(0)=right, get(1)=left
        ExpressionToken right = this.tokens.get(0);
        ExpressionToken left = this.tokens.get(1);
        
        // ARRAY_OPERATOR: left[index] - NO outer parens, brackets are enough
        if (op == Operation.ARRAY_OPERATOR) {
            String leftStr = this.tokenToString(left);
            String indexStr = this.formatArrayIndex(right);
            // Ensure the index string has proper quotes if it's a quoted string
            // If the index is an Expression with a quoted expression field, use that
            if (right instanceof Expression rightExpr) {
                String rightExprStr = rightExpr.getExpression();
                if (rightExprStr != null && rightExprStr.length() > 0) {
                    char firstChar = rightExprStr.charAt(0);
                    if ((firstChar == '"' || firstChar == '\'') && !indexStr.startsWith(String.valueOf(firstChar))) {
                        // The formatArrayIndex lost the quotes, restore them
                        if (rightExprStr.endsWith(String.valueOf(firstChar))) {
                            indexStr = rightExprStr;
                        } else {
                            indexStr = rightExprStr + firstChar;
                        }
                    }
                }
            }
            return leftStr + "[" + indexStr + "]";
        }
        
        // OBJECT_OPERATOR: left.right or left.(right) if right has operations
        if (op == Operation.OBJECT_OPERATOR) {
            String leftStr = this.tokenToString(left);
            String rightStr = this.tokenToString(right);
            
            // Check what operation the right side has
            if (right instanceof Expression) {
                Expression rightExpr = (Expression) right;
                LinkedList<Operation> rightOps = rightExpr.getOperations();
                if (!rightOps.isEmpty()) {
                    Operation rightOp = rightOps.get(0);
                    // ARRAY_OPERATOR doesn't add outer parens, so we need to wrap
                    // OBJECT_OPERATOR and other ops already add parens in their toString()
                    if (rightOp == Operation.ARRAY_OPERATOR) {
                        return "(" + leftStr + ".(" + rightStr + "))";
                    } else {
                        // Other ops (like OBJECT_OPERATOR) already include parens
                        return "(" + leftStr + "." + rightStr + ")";
                    }
                }
            }
            
            // No operations - simple identifier
            return "(" + leftStr + "." + rightStr + ")";
        }
        
        // ARRAY_RANGE_INDEX_OPERATOR: (start..end)
        if (op == Operation.ARRAY_RANGE_INDEX_OPERATOR) {
            String leftStr = this.tokenToString(left);
            String rightStr = this.tokenToString(right);
            return "(" + leftStr + ".." + rightStr + ")";
        }
        
        // Other binary operators: (left op right)
        String leftStr = this.tokenToString(left);
        String rightStr = this.tokenToString(right);
        return "(" + leftStr + op.getOperator() + rightStr + ")";
    }

    /**
     * Convert a token to string, handling nested expressions recursively
     */
    private String tokenToString(ExpressionToken token) {
        if (token instanceof Expression) {
            return token.toString();
        }
        
        // Check if token is an ExpressionTokenValue
        if (token instanceof ExpressionTokenValue) {
            ExpressionTokenValue etv = (ExpressionTokenValue) token;
            String originalExpr = etv.getExpression();
            // If it's a quoted string, use the original expression with quotes
            if (originalExpr != null && originalExpr.length() > 0) {
                char firstChar = originalExpr.charAt(0);
                if (firstChar == '"' || firstChar == '\'') {
                    // Ensure it has the closing quote
                    if (originalExpr.endsWith(String.valueOf(firstChar))) {
                        return originalExpr;
                    } else {
                        // Add missing closing quote
                        return originalExpr + firstChar;
                    }
                }
            }
        }
        
        return token.toString();
    }

    /**
     * Format array index, preserving quotes for bracket notation
     */
    private String formatArrayIndex(ExpressionToken token) {
        if (token instanceof Expression) {
            Expression expr = (Expression) token;
            // First, check if the Expression's original expression string is a quoted string
            // This is the most reliable source since it's the original string passed to the constructor
            String exprStr = expr.getExpression();
            if (exprStr != null && exprStr.length() > 0) {
                char firstChar = exprStr.charAt(0);
                if (firstChar == '"' || firstChar == '\'') {
                    // The original expression string has quotes, use it (ensure it has closing quote)
                    // Always ensure it has the closing quote - the parser might have lost it
                    if (!exprStr.endsWith(String.valueOf(firstChar))) {
                        return exprStr + firstChar;
                    }
                    return exprStr;
                }
            }
            // Check if this is a simple quoted string (no operations, single token)
            if (expr.getOperations().isEmpty() && expr.getTokens().size() == 1) {
                ExpressionToken innerToken = expr.getTokens().get(0);
                // Check if innerToken is an ExpressionTokenValue with quoted expression
                if (innerToken instanceof ExpressionTokenValue) {
                    ExpressionTokenValue etv = (ExpressionTokenValue) innerToken;
                    String originalExpr = etv.getExpression();
                    if (originalExpr != null && originalExpr.length() > 0) {
                        char firstChar = originalExpr.charAt(0);
                        // If it starts with a quote, ensure it ends with the same quote
                        if (firstChar == '"' || firstChar == '\'') {
                            if (!originalExpr.endsWith(String.valueOf(firstChar))) {
                                return originalExpr + firstChar;
                            }
                            return originalExpr;
                        }
                    }
                }
            }
            // For more complex expressions, use toString() but try to preserve quotes
            String result = expr.toString();
            // If result doesn't start with quote but should, try to restore from expression field
            if (exprStr != null && exprStr.length() > 0) {
                char firstChar = exprStr.charAt(0);
                if ((firstChar == '"' || firstChar == '\'') && !result.startsWith(String.valueOf(firstChar))) {
                    // The toString() lost the quotes, restore them from the original expression
                    if (!exprStr.endsWith(String.valueOf(firstChar))) {
                        return exprStr + firstChar;
                    }
                    return exprStr;
                }
            }
            return result;
        }
        
        // Check if token is an ExpressionTokenValue with quoted expression
        if (token instanceof ExpressionTokenValue) {
            ExpressionTokenValue etv = (ExpressionTokenValue) token;
            String originalExpr = etv.getExpression();
            if (originalExpr != null) {
                // Check if it starts and ends with the same quote character
                if ((originalExpr.startsWith("\"") && originalExpr.endsWith("\"")) ||
                    (originalExpr.startsWith("'") && originalExpr.endsWith("'"))) {
                    return originalExpr;
                }
                // If it starts with a quote but doesn't end with one, add the closing quote
                if (originalExpr.startsWith("\"") && !originalExpr.endsWith("\"")) {
                    return originalExpr + "\"";
                }
                if (originalExpr.startsWith("'") && !originalExpr.endsWith("'")) {
                    return originalExpr + "'";
                }
            }
        }
        
        return token.toString();
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

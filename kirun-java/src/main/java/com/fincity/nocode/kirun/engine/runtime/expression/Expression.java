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

	public Expression(String expression) {

		super(expression);
		System.out.println("\n=== New Expression Instance Created ===");
        System.out.println("Expression: " + expression);

		if (expression == null || expression.isBlank())
			throw new ExpressionEvaluationException(expression, "No expression found to evaluate");
			System.out.println("Starting evaluation...");
			this.evaluate();
			System.out.println("After evaluation - Tokens: " + tokens);
			System.out.println("After evaluation - Ops: " + ops);
			System.out.println("=== Expression Instance Creation Complete ===\n");
	}

	public Expression(ExpressionToken token, Operation op) {
		super("");
		this.tokens.push(token);
		this.ops.push(op);
	}

	public Expression(ExpressionToken l, ExpressionToken r, Operation op) {

		super("");
		if (op != null && "..".equals(op.getOperator())) {
            if (l == null) l = new ExpressionTokenValue("",new JsonPrimitive(""));
            if (r == null || r.getExpression().isEmpty()) r = new ExpressionTokenValue("",new JsonPrimitive(""));
        }		
		this.tokens.push(l);
		this.tokens.push(r);
		this.ops.push(op);
		if (!this.ops.isEmpty() && "..".equals(this.ops.peekLast().getOperator()) && this.tokens.size() == 1) {
            this.tokens.push(new ExpressionToken(""));
        }
	}

	public LinkedList<ExpressionToken> getTokens() { // NOSONAR - LinkedList is required
		return this.tokens;
	}

	public LinkedList<Operation> getOperations() {// NOSONAR - LinkedList is required
		return this.ops;
	}

	private void evaluate() {
		System.out.println("\n--- Evaluate Method Start ---");
        System.out.println("Processing expression: " + this.expression);

		final int length = this.expression.length();
		char chr = 0;

		StringBuilder sb = new StringBuilder();
		String buff = null;
		int i = 0;
		boolean isPrevOp = false;
		

		while (i < length) {

			chr = this.expression.charAt(i);
			buff = sb.toString();

			System.out.println("\nEvaluate Loop State:");
            System.out.println("Position: " + i);
            System.out.println("Current char: '" + chr + "'");
            System.out.println("Current buffer: '" + buff + "'");
            System.out.println("StringBuilder content: '" + sb + "'");
            System.out.println("Current tokens: " + tokens);
            System.out.println("Current ops: " + ops);
            System.out.println("isPrevOp: " + isPrevOp);

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
				System.out.println("result after processOthers: " + result);
				i = result.getT1();
				isPrevOp = result.getT2();
				if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
					result = process(length, sb, i);
					//System.out.println("result after process: " + result);
					i = result.getT1();
					isPrevOp = result.getT2();
				}
			}

			++i;
		}

		buff = sb.toString();
		System.out.println("Final buffer: '" + buff + "'");
		if (!buff.isBlank()) {
			if (OPERATORS.contains(buff)) {
				throw new ExpressionEvaluationException(this.expression, "Expression is ending with an operator");
			} else {
				tokens.push(new ExpressionToken(buff));
			}
		}
		System.out.println("--- Evaluate Method End ---\n");
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
		System.out.println("\n=== Process Method Start ===");
        System.out.println("Current position: " + i);
        System.out.println("Expression length: " + length);
        System.out.println("Current expression: " + this.expression);
		int cnt = 1;
		++i;
		System.out.println("Starting bracket processing at position: " + i);
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
		System.out.println("Final buffer content: '" + sb.toString() + "'");
        System.out.println("Creating new Expression with buffer content");
        this.tokens.push(new Expression(sb.toString()));
        System.out.println("Current tokens after push: " + this.tokens);
        sb.setLength(0);
		
        System.out.println("=== Process Method End ===\n");
		return Tuples.of(i, false);
	}

	private Tuple2<Integer, Boolean> processOthers(char chr, final int length, StringBuilder sb, String buff, int i,
	        boolean isPrevOp) {
				System.out.println("\n*** ProcessOthers Method Start ***");
				System.out.println("Processing char: '" + chr + "'");
				System.out.println("Current buffer: '" + buff + "'");
				System.out.println("StringBuilder content: '" + sb + "'");
				System.out.println("Position: " + i);
				System.out.println("isPrevOp: " + isPrevOp);
		int start = length - i;
		start = start < Operation.BIGGEST_OPERATOR_SIZE ? start : Operation.BIGGEST_OPERATOR_SIZE;

		for (int size = start; size > 0; size--) {
			String op = this.expression.substring(i, i + size);
			System.out.println("Checking operator: '" + op + "'");			
			if (OPERATORS_WITHOUT_SPACE_WRAP.contains(op)) {
				System.out.println("Found operator: " + op);
				if (!buff.isBlank()) {
					tokens.push(new ExpressionToken(buff));
					System.out.println("Pushed token: " + buff);
					isPrevOp = false;
				} else if("..".equals(op) && tokens.isEmpty()) {
					tokens.push(new ExpressionToken("0"));
					System.out.println("Pushed default token '0' for range start");
					isPrevOp = false;
				}
				System.out.println("Before operator check - Tokens: " + tokens);
                System.out.println("Before operator check - Ops: " + ops);
				checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(op), isPrevOp);
				System.out.println("After operator check - Tokens: " + tokens);
                System.out.println("After operator check - Ops: " + ops);
				isPrevOp = true;
				sb.setLength(0);
				System.out.println("*** ProcessOthers Method End ***\n");
				return Tuples.of(i + size - 1, isPrevOp);
			}
		}

		sb.append(chr);
		System.out.println("Appended char to buffer: '" + sb + "'");
        System.out.println("*** ProcessOthers Method End ***\n");
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

		while (subExp.length() > 2 && subExp.charAt(0) == '(' && subExp.charAt(subExp.length() - 1) == ')') {
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
				System.out.println("\n+++ CheckUnaryOperator Method Start +++");
				System.out.println("Checking operator: " + op);
				System.out.println("Current tokens: " + tokens);
				System.out.println("Current ops: " + ops);
				System.out.println("isPrevOp: " + isPrevOp);		
			if (isPrevOp || tokens.isEmpty()) {
			if (UNARY_OPERATORS.contains(op)){
				ops.push(UNARY_MAP.get(op));
				System.out.println("Pushed unary operator: " + op);

			}else{
				System.out.println("ERROR: Extra operator found: " + op);
				throw new ExpressionEvaluationException(this.expression,
				        StringFormatter.format("Extra operator $ found.", op));
					}
		} else {
			System.out.println("operators inelseof unary: " + ops);
			System.out.println("op inelseof unary: " + op);
			while (!ops.isEmpty() && hasPrecedence(op, ops.peek())) {
                System.out.println("Processing operator precedence");

				Operation prev = ops.pop();
				System.out.println("Popped operator: " + prev);
								if (UNARY_OPERATORS.contains(prev)) {
					ExpressionToken l = tokens.pop();
					tokens.push(new Expression(l, prev));
					System.out.println("Created unary expression with operator: " + prev);
				} else {
					ExpressionToken r = tokens.pop();
					ExpressionToken l = tokens.pop();

					tokens.push(new Expression(l, r, prev));
					System.out.println("Created binary expression with operator: " + prev);
				}
				System.out.println("Current tokens after operation: " + tokens);

			}
			ops.push(op);
            System.out.println("Pushed operator: " + op);
		}
		
	}

	private boolean hasPrecedence(Operation op1, Operation op2) {

		int pre1 = OPERATOR_PRIORITY.get(op1);
		int pre2 = OPERATOR_PRIORITY.get(op2);

		return pre2 < pre1;
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

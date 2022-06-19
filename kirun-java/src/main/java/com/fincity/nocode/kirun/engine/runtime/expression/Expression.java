package com.fincity.nocode.kirun.engine.runtime.expression;

import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OPERATORS;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OPERATOR_PRIORITY;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_MAP;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_OPERATORS;

import java.util.Deque;
import java.util.LinkedList;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class Expression extends ExpressionToken {

	private LinkedList<ExpressionToken> tokens = new LinkedList<>();
	private LinkedList<Operation> ops = new LinkedList<>();

	public Expression(String expression) {

		super(expression);

		if (expression == null || expression.isBlank())
			throw new KIRuntimeException("No expression found to evaluate");
		this.evaluate();
	}

	public Expression(ExpressionToken token, Operation op) {
		super("");
		this.tokens.push(token);
		this.ops.push(op);
	}

	public Expression(ExpressionToken l, ExpressionToken r, Operation op) {

		super("");
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
				throw new KIRuntimeException("Extra closing parenthesis found");
			}
			default:

				Tuple2<Integer, Boolean> result = processOthers(chr, length, sb, buff, i, isPrevOp);
				i = result.getT1();
				isPrevOp = result.getT2();
			}

			++i;
		}

		buff = sb.toString();
		if (!buff.isBlank()) {
			if (OPERATORS.contains(buff)) {
				throw new KIRuntimeException("Expression is ending with an operator");
			} else {
				tokens.push(new ExpressionToken(buff));
			}
		}
	}

	private Tuple2<Integer, Boolean> processOthers(char chr, final int length, StringBuilder sb, String buff, int i,
	        boolean isPrevOp) {

		if (chr == '!' && i + 1 < length && this.expression.charAt(i + 1) == '=') {
			++i;
			checkUnaryOperator(tokens, ops, Operation.NOT_EQUAL, isPrevOp);
			isPrevOp = true;
			sb.setLength(0);
		} else {

			String op = Character.toString(chr);
			if (OPERATORS.contains(op)) {
				if (!buff.isBlank()) {
					tokens.push(new ExpressionToken(buff));
					isPrevOp = false;
				}
				checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(op), isPrevOp);
				isPrevOp = true;
				sb.setLength(0);
			} else {
				op = sb.toString();
				if (OPERATORS.contains(op)) {
					checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(op), isPrevOp);
					isPrevOp = true;
					sb.setLength(0);
				} else {
					sb.append(chr);
				}
			}
		}

		return Tuples.of(i, isPrevOp);
	}

	private int processSubExpression(final int length, StringBuilder sb, String buff, int i, boolean isPrevOp) {

		if (OPERATORS.contains(buff)) {
			checkUnaryOperator(tokens, ops, Operation.OPERATION_VALUE_OF.get(buff), isPrevOp);
			sb.setLength(0);
		} else if (!buff.isBlank()) {
			throw new KIRuntimeException(StringFormatter.format("Unkown token : $ found.", buff));
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
			throw new KIRuntimeException("Missing a closed parenthesis");

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

		if (isPrevOp || tokens.isEmpty()) {
			if (UNARY_OPERATORS.contains(op))
				ops.push(UNARY_MAP.get(op));
			else
				throw new KIRuntimeException(StringFormatter.format("Extra operator $ found.", op));
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

	@Override
	public String toString() {

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
			} else {

				if (ind == 0) {
					sb.insert(0, this.tokens.get(ind++));
				}
				sb.insert(0, this.ops.get(i)
				        .getOperator())
				        .insert(0, this.tokens.get(ind++))
				        .insert(0, "(")
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

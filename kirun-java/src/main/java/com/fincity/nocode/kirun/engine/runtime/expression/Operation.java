package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Operation {

	MULTIPLICATION("*"), DIVISION("/"), MOD("%"), ADDITION("+"), SUBTRACTION("-"),

	NOT("not", true), AND("and", true), OR("or", true), LESS_THAN("<"), LESS_THAN_EQUAL("<="), GREATER_THAN(">"),
	GREATER_THAN_EQUAL(">="), EQUAL("="), NOT_EQUAL("!="), NULLISH_COALESCING_OPERATOR("??"),

	BITWISE_AND("&"), BITWISE_OR("|"), BITWISE_XOR("^"), BITWISE_COMPLEMENT("~"), BITWISE_LEFT_SHIFT("<<"),
	BITWISE_RIGHT_SHIFT(">>"), BITWISE_UNSIGNED_RIGHT_SHIFT(">>>"),

	UNARY_PLUS("UN: +", "+"), UNARY_MINUS("UN: -", "-"), UNARY_LOGICAL_NOT("UN: not", "not"),
	UNARY_BITWISE_COMPLEMENT("UN: ~", "~"),

	ARRAY_OPERATOR("["), OBJECT_OPERATOR("."), ARRAY_RANGE_INDEX_OPERATOR(".."),

	CONDITIONAL_TERNARY_OPERATOR("?"),

	;

	public static final Set<Operation> UNARY_OPERATORS = Set.of(ADDITION, SUBTRACTION, NOT, BITWISE_COMPLEMENT,
	        UNARY_PLUS, UNARY_MINUS, UNARY_LOGICAL_NOT, UNARY_BITWISE_COMPLEMENT);

	public static final Set<Operation> ARITHMETIC_OPERATORS = Set.of(MULTIPLICATION, DIVISION, MOD, ADDITION,
	        SUBTRACTION);

	public static final Set<Operation> LOGICAL_OPERATORS = Set.of(NOT, AND, OR, LESS_THAN, LESS_THAN_EQUAL,
	        GREATER_THAN, GREATER_THAN_EQUAL, EQUAL, NOT_EQUAL, NULLISH_COALESCING_OPERATOR);

	public static final Set<Operation> CONDITIONAL_OPERATORS = Set.of(CONDITIONAL_TERNARY_OPERATOR);

	public static final Set<Operation> BITWISE_OPERATORS = Set.of(BITWISE_AND, BITWISE_COMPLEMENT, BITWISE_LEFT_SHIFT,
	        BITWISE_OR, BITWISE_RIGHT_SHIFT, BITWISE_UNSIGNED_RIGHT_SHIFT, BITWISE_XOR);

	public static final Map<Operation, Integer> OPERATOR_PRIORITY = Collections.unmodifiableMap(new EnumMap<>(
	        Map.ofEntries(Map.entry(UNARY_PLUS, 1), Map.entry(UNARY_MINUS, 1), Map.entry(UNARY_LOGICAL_NOT, 1),
	                Map.entry(UNARY_BITWISE_COMPLEMENT, 1), Map.entry(ARRAY_OPERATOR, 1), Map.entry(OBJECT_OPERATOR, 1),
					Map.entry(ARRAY_RANGE_INDEX_OPERATOR, 2),
	                Map.entry(MULTIPLICATION, 2), Map.entry(DIVISION, 2), Map.entry(MOD, 2), Map.entry(ADDITION, 3),
	                Map.entry(SUBTRACTION, 3), Map.entry(BITWISE_LEFT_SHIFT, 4), Map.entry(BITWISE_RIGHT_SHIFT, 4),
	                Map.entry(BITWISE_UNSIGNED_RIGHT_SHIFT, 4), Map.entry(LESS_THAN, 5), Map.entry(LESS_THAN_EQUAL, 5),
	                Map.entry(GREATER_THAN, 5), Map.entry(GREATER_THAN_EQUAL, 5), Map.entry(EQUAL, 6),
	                Map.entry(NOT_EQUAL, 6), Map.entry(BITWISE_AND, 7), Map.entry(BITWISE_XOR, 8),
	                Map.entry(BITWISE_OR, 9), Map.entry(AND, 10), Map.entry(OR, 11),
	                Map.entry(NULLISH_COALESCING_OPERATOR, 11), Map.entry(CONDITIONAL_TERNARY_OPERATOR, 12))));

	public static final Set<String> OPERATORS = Collections.unmodifiableSet(Stream
	        .of(ARITHMETIC_OPERATORS.stream(), LOGICAL_OPERATORS.stream(), BITWISE_OPERATORS.stream(),
	                Stream.of(ARRAY_OPERATOR, OBJECT_OPERATOR,ARRAY_RANGE_INDEX_OPERATOR), CONDITIONAL_OPERATORS.stream())
	        .flatMap(Function.identity())
	        .map(Operation::getOperator)
	        .collect(Collectors.toSet()));

	public static final Set<String> OPERATORS_WITHOUT_SPACE_WRAP = Collections.unmodifiableSet(Stream
	        .of(ARITHMETIC_OPERATORS.stream(), LOGICAL_OPERATORS.stream(), BITWISE_OPERATORS.stream(),
	                Stream.of(ARRAY_OPERATOR, OBJECT_OPERATOR, ARRAY_RANGE_INDEX_OPERATOR), CONDITIONAL_OPERATORS.stream())
	        .flatMap(Function.identity())
	        .filter(Predicate.not(Operation::shouldBeWrappedInSpace))
	        .map(Operation::getOperator)
	        .collect(Collectors.toSet()));

	public static final Map<String, Operation> OPERATION_VALUE_OF = Collections
	        .unmodifiableMap(Stream.of(Operation.values())
	                .collect(Collectors.toMap(Operation::getOperator, Function.identity())));

	public static final Map<Operation, Operation> UNARY_MAP = Collections.unmodifiableMap(
	        new EnumMap<>(Map.of(ADDITION, UNARY_PLUS, SUBTRACTION, UNARY_MINUS, NOT, UNARY_LOGICAL_NOT,
	                BITWISE_COMPLEMENT, UNARY_BITWISE_COMPLEMENT, UNARY_PLUS, UNARY_PLUS, UNARY_MINUS, UNARY_MINUS,
	                UNARY_LOGICAL_NOT, UNARY_LOGICAL_NOT, UNARY_BITWISE_COMPLEMENT, UNARY_BITWISE_COMPLEMENT)));

	public static final int BIGGEST_OPERATOR_SIZE = Stream.of(Operation.values())
	        .map(Operation::getOperator)
	        .filter(e -> !e.startsWith("UN: "))
	        .mapToInt(String::length)
	        .max()
	        .getAsInt() + 1;

	private String operator;

	private String operatorName;

	private boolean shouldBeWrappedInSpace = false;

	private Operation(String operator) {
		this.operator = operator;
		this.operatorName = operator;
	}

	private Operation(String operator, String operatorName) {
		this.operator = operator;
		this.operatorName = operatorName;
	}

	Operation(String operator, boolean shouldBeWrappedInSpace) {
		this.operator = operator;
		this.operatorName = operator;
		this.shouldBeWrappedInSpace = shouldBeWrappedInSpace;
	}

	public String getOperator() {
		return this.operator;
	}

	public String getOperatorName() {
		return this.operatorName;
	}

	public boolean shouldBeWrappedInSpace() {
		return this.shouldBeWrappedInSpace;
	}
}

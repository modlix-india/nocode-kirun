package com.fincity.nocode.kirun.engine.runtime.expression;

public enum Operations {
	
	MULTIPLICATION ("*"),
	DIVISION ("/"),
	MOD ("%"),
	ADDITION ("+"),
	SUBTRACTION ("-"),
	
	NOT ("not"),
	AND ("and"),
	OR ("or"),
	LESS_THAN ("<"),
	LESS_THAN_EQUAL ("<="),
	GREATER_THAN (">"),
	GREATER_THAN_EQUAL (">="),
	EQUAL ("="),
	NOT_EQUAL ("!="),
	
	BITWISE_AND ("&"),
	BITWISE_OR ("|"),
	BITWISE_XOR ("^"),
	BITWISE_COMPLEMENTARY ("~"),
	BITWISE_LEFT_SHIFT ("<<"),
	BITWISE_RIGHT_SHIFT (">>"),
	BITWISE_UNSIGNED_RIGHT_SHIFT (">>>"),
	
	UNARY_PLUS ("UN: +"),
	UNARY_MINUS ("UN: -"),
	UNARY_LOGICAL_NOT ("UN: not"),
	UNARY_BITWISE_COMPLEMENTARY ("UN: ~"),
	;
	
	private String operator;
	
	private Operations(String operator) {
		this.operator = operator;
	}
	
	public String getOperator() {
		return this.operator;
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Arithmetic as kirun-js does it (JavaScript numbers): whole numbers never wrap, a whole division
 * with a remainder is a double division, and decimals are doubles. The same cases run in
 * kirun-js, kirun-rust and kirun-py.
 */
class ExpressionArithmeticParityTest {

	private static final Map<String, TokenValueExtractor> VALUES;

	static {
		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(Map.of("a", new JsonPrimitive(40000),
				"b", new JsonPrimitive(12), "c", new JsonPrimitive(10000), "p", new JsonPrimitive(480000), "q",
				new JsonPrimitive(6000000), "n", new JsonPrimitive(-40000), "f", new JsonPrimitive(1.5), "s",
				new JsonPrimitive("ab")));
		VALUES = Map.of(atv.getPrefix(), atv);
	}

	private static JsonElement eval(String expression) {
		return new ExpressionEvaluator(expression).evaluate(VALUES);
	}

	private static void number(String expression, double expected) {
		JsonElement v = eval(expression);
		assertEquals(expected, v.getAsDouble(), 0.0, expression);
	}

	private static void whole(String expression, long expected) {
		JsonElement v = eval(expression);
		assertEquals(new JsonPrimitive(expected), v, expression);
		assertEquals(expected, v.getAsLong(), expression);
	}

	@Test
	void bigProductsDoNotWrap() {
		whole("40000 * 12 * 10000", 4800000000L);
		whole("Arguments.a * Arguments.b * Arguments.c", 4800000000L);
		whole("-40000 * 12 * 10000", -4800000000L);
		whole("Arguments.n * Arguments.b * Arguments.c", -4800000000L);
		whole("65536 * 65536", 4294967296L);
		whole("100000 * 100000", 10000000000L);
		whole("2147483647 + 1", 2147483648L);
		whole("-2147483648 - 1", -2147483649L);
		whole("-(-2147483648)", 2147483648L);
		whole("2147483647 * 2147483647", 4611686014132420609L);
		// Past the long range: a double, as in JS.
		number("9223372036854775807 + 1", 9223372036854775807d + 1);
		number("9223372036854775807 * 2", 9223372036854775807d * 2);
	}

	@Test
	void divisionIsTrueDivision() {
		number("480000 / 6000000", 0.08);
		number("Arguments.p / Arguments.q", 0.08);
		number("7 / 2", 3.5);
		number("-7 / 2", -3.5);
		number("1 / 3", 1d / 3);
		number("4800000000 / 7", 4800000000d / 7);
		whole("6 / 3", 2);
		whole("4800000000 / 1000", 4800000);
		assertEquals(new JsonPrimitive(2), eval("6 / 3"));
		assertThrows(ArithmeticException.class, () -> eval("1 / 0"));
	}

	@Test
	void integerDivisionAndModulus() {
		whole("7 // 2", 3);
		whole("-7 // 2", -4);
		whole("7 % 3", 1);
		whole("-7 % 3", -1);
		whole("7 % -3", 1);
		number("7.5 % 2", 1.5);
		number("-7.5 % 2", -1.5);
	}

	@Test
	void mixedAndComparisons() {
		number("1.5 * 4", 6);
		number("Arguments.f * Arguments.a", 60000);
		number("7 * 0.5", 3.5);
		number("0.1 + 0.2", 0.1 + 0.2);
		number("10 - 2.5", 7.5);
		number("Arguments.a * Arguments.b * Arguments.c * 10000.0 / Arguments.q", 8000000);
		assertEquals(new JsonPrimitive(true), eval("40000 * 12 * 10000 > 4000000000"));
		assertEquals(new JsonPrimitive(true), eval("Arguments.a * Arguments.b * Arguments.c = 4800000000"));
		assertEquals(new JsonPrimitive(true), eval("480000 / 6000000 = 0.08"));
		assertEquals(new JsonPrimitive(true), eval("480000 / 6000000 < 0.1"));
		assertEquals(new JsonPrimitive(true), eval("6 / 3 = 2"));
	}

	@Test
	void stringConcatenationUnaffected() {
		assertEquals(new JsonPrimitive("ab4800000000"), eval("'ab' + 40000 * 12 * 10000"));
		assertEquals(new JsonPrimitive("ab1"), eval("Arguments.s + 1"));
		assertEquals(new JsonPrimitive("x3.5"), eval("'x' + 7 / 2"));
		assertEquals(new JsonPrimitive("3a"), eval("1 + 2 + 'a'"));
		assertEquals(new JsonPrimitive("a12"), eval("'a' + 1 + 2"));
		assertEquals(new JsonPrimitive("abab"), eval("Arguments.s * 2"));
	}
}

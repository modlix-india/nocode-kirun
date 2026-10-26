package com.fincity.nocode.kirun.engine.util.primitive;

import com.google.gson.JsonPrimitive;

/**
 * Whole number arithmetic as kirun-js does it (JavaScript numbers never wrap): an int result
 * that does not fit widens to a long, a long result past the long range becomes a double, and
 * a whole division with a remainder is a double division (7 / 2 is 3.5, 6 / 3 stays 2).
 */
public final class WholeNumberArithmetic {

	private WholeNumberArithmetic() {
	}

	/** The smallest whole Java type that holds the value: an Integer when it fits, else a Long. */
	public static Number whole(long value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
			return (int) value;
		return value;
	}

	public static Number addNumber(Number a, Number b) {
		try {
			return whole(Math.addExact(a.longValue(), b.longValue()));
		} catch (ArithmeticException ex) {
			return a.doubleValue() + b.doubleValue();
		}
	}

	public static JsonPrimitive add(Number a, Number b) {
		return new JsonPrimitive(addNumber(a, b));
	}

	public static JsonPrimitive subtract(Number a, Number b) {
		try {
			return new JsonPrimitive(whole(Math.subtractExact(a.longValue(), b.longValue())));
		} catch (ArithmeticException ex) {
			return new JsonPrimitive(a.doubleValue() - b.doubleValue());
		}
	}

	public static JsonPrimitive multiply(Number a, Number b) {
		try {
			return new JsonPrimitive(whole(Math.multiplyExact(a.longValue(), b.longValue())));
		} catch (ArithmeticException ex) {
			return new JsonPrimitive(a.doubleValue() * b.doubleValue());
		}
	}

	/** A whole zero divisor still fails with "/ by zero". */
	public static JsonPrimitive divide(Number a, Number b) {
		long x = a.longValue();
		long y = b.longValue();
		if (y == 0L)
			throw new ArithmeticException("/ by zero");
		if (x % y != 0L || (x == Long.MIN_VALUE && y == -1L))
			return new JsonPrimitive(a.doubleValue() / b.doubleValue());
		return new JsonPrimitive(whole(x / y));
	}

	public static JsonPrimitive negate(Number a) {
		long x = a.longValue();
		if (x == Long.MIN_VALUE)
			return new JsonPrimitive(-a.doubleValue());
		return new JsonPrimitive(whole(-x));
	}
}

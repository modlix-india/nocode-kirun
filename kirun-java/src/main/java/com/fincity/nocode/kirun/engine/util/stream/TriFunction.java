package com.fincity.nocode.kirun.engine.util.stream;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

	public R apply(T a, U b, V c);

	default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
		Objects.requireNonNull(after);
		return (T t, U u, V v) -> after.apply(apply(t, u, v));
	}
}

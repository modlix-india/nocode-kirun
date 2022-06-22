package com.fincity.nocode.kirun.engine.util.stream;

import java.util.stream.Stream;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class StreamUtil {

	public static <T, R> R sequentialReduce(final Stream<T> stream, final R identity,
	        final TriFunction<T, R, Integer, R> reducer) {

		if (stream.isParallel())
			throw new KIRuntimeException("Cannot sequential reduce a parallel stream.");

		R output = identity;

		int i = 0;
		for (T e : stream.toList()) {
			output = reducer.apply(e, output, i);
			i++;
		}

		return output;
	}

	private StreamUtil() {

	}
}

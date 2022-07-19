package com.fincity.nocode.kirun.engine.function.math;

import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class MathFunctionRepository implements Repository<Function> {

	private static final Map<String, Function> repoMap = Map.ofEntries(
			AbstractUnaryMathFunction.ofEntryAnyType("Absolute",
					Map.of(Integer.class, n -> Math.abs(n.intValue()), Float.class, n -> Math.abs(n.floatValue()),
							Double.class, n -> Math.abs(n.doubleValue()), Long.class, n -> Math.abs(n.longValue()))),
			AbstractUnaryMathFunction.ofEntryDouble("ACosine", Math::acos),
			AbstractUnaryMathFunction.ofEntryDouble("ASine", Math::asin),
			AbstractUnaryMathFunction.ofEntryDouble("ATangent", Math::atan),
			AbstractUnaryMathFunction.ofEntryDouble("Ceiling", Math::ceil),
			AbstractUnaryMathFunction.ofEntryDouble("Cosine", Math::cos),
			AbstractUnaryMathFunction.ofEntryDouble("CosineH", Math::cosh),
			AbstractUnaryMathFunction.ofEntryDouble("CubeRoot", Math::cbrt),
			AbstractUnaryMathFunction.ofEntryDouble("Exponential", Math::exp),
			AbstractUnaryMathFunction.ofEntryDouble("Expm1", Math::expm1),
			AbstractUnaryMathFunction.ofEntryDouble("Floor", Math::floor),
			AbstractUnaryMathFunction.ofEntryDouble("Log", Math::log),
			AbstractUnaryMathFunction.ofEntryDouble("Log10", Math::log10),
			AbstractUnaryMathFunction.ofEntryDouble("Rint", Math::rint),
			AbstractUnaryMathFunction.ofEntryAnyType("Round",
					Map.of(Integer.class, n -> Math.round(n.floatValue()), Float.class, n -> Math.round(n.floatValue()),
							Double.class, n -> Math.round(n.doubleValue()), Long.class,
							n -> Math.round(n.doubleValue()))),
			AbstractUnaryMathFunction.ofEntryDouble("Sine", Math::sin),
			AbstractUnaryMathFunction.ofEntryDouble("SineH", Math::sinh),
			AbstractUnaryMathFunction.ofEntryDouble("Tangent", Math::tan),
			AbstractUnaryMathFunction.ofEntryDouble("TangentH", Math::tanh),
			AbstractUnaryMathFunction.ofEntryDouble("ToDegrees", Math::toDegrees),
			AbstractUnaryMathFunction.ofEntryDouble("ToRadians", Math::toRadians),
			AbstractUnaryMathFunction.ofEntryDouble("SquareRoot", Math::sqrt),
			AbstractBinaryMathFunction.ofEntryDouble("Hypotenuse", Math::hypot),
			AbstractBinaryMathFunction.ofEntryDouble("ArcTangent", Math::atan2),
			AbstractBinaryMathFunction.ofEntryDouble("Power", Math::pow));

	@Override
	public Function find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return null;
		return repoMap.get(name);
	}
}
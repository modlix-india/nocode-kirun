package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class MathFunctionRepository implements Repository<Function> {

	private static final Map<String, Function> repoMap = Map.ofEntries(
			AbstractUnaryMathFunction.ofEntryAnyType("Absolute",
					Map.of(Integer.class, n -> Math.abs(n.intValue()), Float.class, n -> Math.abs(n.floatValue()),
							Double.class, n -> Math.abs(n.doubleValue()), Long.class, n -> Math.abs(n.longValue()))),
			AbstractUnaryMathFunction.ofEntryDouble("ArcCosine", Math::acos),
			AbstractUnaryMathFunction.ofEntryDouble("ArcSine", Math::asin),
			AbstractUnaryMathFunction.ofEntryDouble("ArcTangent", Math::atan),
			AbstractUnaryMathFunction.ofEntryDouble("Ceiling", Math::ceil),
			AbstractUnaryMathFunction.ofEntryDouble("Cosine", Math::cos),
			AbstractUnaryMathFunction.ofEntryDouble("HyperbolicCosine", Math::cosh),
			AbstractUnaryMathFunction.ofEntryDouble("CubeRoot", Math::cbrt),
			AbstractUnaryMathFunction.ofEntryDouble("Exponential", Math::exp),
			AbstractUnaryMathFunction.ofEntryDouble("ExponentialMinus1", Math::expm1),
			AbstractUnaryMathFunction.ofEntryDouble("Floor", Math::floor),
			AbstractUnaryMathFunction.ofEntryDouble("LogNatural", Math::log),
			AbstractUnaryMathFunction.ofEntryDouble("Log10", Math::log10),
			AbstractUnaryMathFunction.ofEntryAnyType("Round",
					Map.of(Integer.class, n -> Math.round(n.floatValue()), Float.class, n -> Math.round(n.floatValue()),
							Double.class, n -> Math.round(n.doubleValue()), Long.class,
							n -> Math.round(n.doubleValue()))),
			AbstractUnaryMathFunction.ofEntryDouble("Sine", Math::sin),
			AbstractUnaryMathFunction.ofEntryDouble("HyperbolicSine", Math::sinh),
			AbstractUnaryMathFunction.ofEntryDouble("Tangent", Math::tan),
			AbstractUnaryMathFunction.ofEntryDouble("HyperbolicTangent", Math::tanh),
			AbstractUnaryMathFunction.ofEntryDouble("ToDegrees", Math::toDegrees),
			AbstractUnaryMathFunction.ofEntryDouble("ToRadians", Math::toRadians),
			AbstractUnaryMathFunction.ofEntryDouble("SquareRoot", Math::sqrt),
			AbstractBinaryMathFunction.ofEntryDouble("Hypotenuse", Math::hypot),
			AbstractBinaryMathFunction.ofEntryDouble("ArcTangent2", Math::atan2),
			AbstractBinaryMathFunction.ofEntryDouble("Power", Math::pow));

	@Override
	public Function find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return null;
		return repoMap.get(name);
	}
}
package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.AbstractUnaryFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class MathFunctionRepository implements Repository<Function> {

	private static final Map<String, Function> REPO_MAP = Map.ofEntries(
	        AbstractUnaryFunction.ofEntryAnyType("Absolute",
	                Map.of(Integer.class, n -> Math.abs(n.intValue()), Float.class, n -> Math.abs(n.floatValue()),
	                        Double.class, n -> Math.abs(n.doubleValue()), Long.class, n -> Math.abs(n.longValue())),
	                SchemaType.INTEGER, SchemaType.FLOAT, SchemaType.DOUBLE, SchemaType.LONG),
	        AbstractUnaryFunction.ofEntryDouble("ArcCosine", Math::acos),
	        AbstractUnaryFunction.ofEntryDouble("ArcSine", Math::asin),
	        AbstractUnaryFunction.ofEntryDouble("ArcTangent", Math::atan),
	        AbstractUnaryFunction.ofEntryDouble("Ceiling", Math::ceil),
	        AbstractUnaryFunction.ofEntryDouble("Cosine", Math::cos),
	        AbstractUnaryFunction.ofEntryDouble("HyperbolicCosine", Math::cosh),
	        AbstractUnaryFunction.ofEntryDouble("CubeRoot", Math::cbrt),
	        AbstractUnaryFunction.ofEntryDouble("Exponential", Math::exp),
	        AbstractUnaryFunction.ofEntryDouble("ExponentialMinus1", Math::expm1),
	        AbstractUnaryFunction.ofEntryDouble("Floor", Math::floor),
	        AbstractUnaryFunction.ofEntryDouble("LogNatural", Math::log),
	        AbstractUnaryFunction.ofEntryDouble("Log10", Math::log10),
	        AbstractUnaryFunction.ofEntryAnyType("Round",
	                Map.of(Integer.class, n -> Math.round(n.floatValue()), Float.class, n -> Math.round(n.floatValue()),
	                        Double.class, n -> Math.round(n.doubleValue()), Long.class,
	                        n -> Math.round(n.doubleValue())),
	                SchemaType.INTEGER, SchemaType.FLOAT, SchemaType.DOUBLE, SchemaType.LONG),
	        AbstractUnaryFunction.ofEntryDouble("Sine", Math::sin),
	        AbstractUnaryFunction.ofEntryDouble("HyperbolicSine", Math::sinh),
	        AbstractUnaryFunction.ofEntryDouble("Tangent", Math::tan),
	        AbstractUnaryFunction.ofEntryDouble("HyperbolicTangent", Math::tanh),
	        AbstractUnaryFunction.ofEntryDouble("ToDegrees", Math::toDegrees),
	        AbstractUnaryFunction.ofEntryDouble("ToRadians", Math::toRadians),
	        AbstractUnaryFunction.ofEntryDouble("SquareRoot", Math::sqrt, SchemaType.DOUBLE),
	        AbstractBinaryMathFunction.ofEntryDouble("ArcTangent2", Math::atan2),
	        AbstractBinaryMathFunction.ofEntryDouble("Power", Math::pow));

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
	        .stream()
	        .map(Function::getSignature)
	        .map(FunctionSignature::getFullName)
	        .toList();

	@Override
	public Function find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return null;
		return REPO_MAP.get(name);
	}

	@Override
	public List<String> filter(String name) {
		return FILTERABLE_NAMES.stream()
		        .filter(e -> e.toLowerCase()
		                .indexOf(name.toLowerCase()) != -1)
		        .toList();
	}
}
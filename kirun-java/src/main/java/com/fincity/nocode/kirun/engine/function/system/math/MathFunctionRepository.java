package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.function.system.AbstractUnaryFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MathFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(
			AbstractUnaryFunction.ofEntryAnyType("Absolute",
					Map.of(Integer.class, n -> Math.abs(n.intValue()), Float.class, n -> Math.abs(n.floatValue()),
							Double.class, n -> Math.abs(n.doubleValue()), Long.class, n -> Math.abs(n.longValue())),
					SchemaType.INTEGER, SchemaType.FLOAT, SchemaType.DOUBLE, SchemaType.LONG),
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
			AbstractUnaryFunction.ofEntryAnyType("Round",
					Map.of(Integer.class, n -> Math.round(n.floatValue()), Float.class, n -> Math.round(n.floatValue()),
							Double.class, n -> Math.round(n.doubleValue()), Long.class,
							n -> Math.round(n.doubleValue())),
					SchemaType.INTEGER, SchemaType.FLOAT, SchemaType.DOUBLE, SchemaType.LONG),
			AbstractUnaryMathFunction.ofEntryDouble("Sine", Math::sin),
			AbstractUnaryMathFunction.ofEntryDouble("HyperbolicSine", Math::sinh),
			AbstractUnaryMathFunction.ofEntryDouble("Tangent", Math::tan),
			AbstractUnaryMathFunction.ofEntryDouble("HyperbolicTangent", Math::tanh),
			AbstractUnaryMathFunction.ofEntryDouble("ToDegrees", Math::toDegrees),
			AbstractUnaryMathFunction.ofEntryDouble("ToRadians", Math::toRadians),
			AbstractUnaryMathFunction.ofEntryDouble("SquareRoot", Math::sqrt),
			AbstractBinaryMathFunction.ofEntryDouble("ArcTangent2", Math::atan2),
			AbstractBinaryMathFunction.ofEntryDouble("Power", Math::pow));

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
			.stream()
			.map(ReactiveFunction::getSignature)
			.map(FunctionSignature::getFullName)
			.toList();

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return Mono.empty();
		return Mono.justOrEmpty(REPO_MAP.get(name));
	}

	@Override
	public Flux<String> filter(String name) {
		return Flux.fromIterable(FILTERABLE_NAMES)
				.filter(e -> e.toLowerCase()
						.indexOf(name.toLowerCase()) != -1);
	}
}
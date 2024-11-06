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
			AbstractUnaryFunction.ofEntryNumber("Absolute",
					Map.of(Integer.class.getName(), n -> Math.abs(n.intValue()), Float.class.getName(),
							n -> Math.abs(n.floatValue()),
							Double.class.getName(), n -> Math.abs(n.doubleValue()), Long.class.getName(),
							n -> Math.abs(n.longValue())),
					SchemaType.INTEGER, SchemaType.FLOAT, SchemaType.DOUBLE, SchemaType.LONG),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ArcCosine", Math::acos),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ArcSine", Math::asin),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ArcTangent", Math::atan),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Ceiling", Math::ceil),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Cosine", Math::cos),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("HyperbolicCosine", Math::cosh),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("CubeRoot", Math::cbrt),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Exponential", Math::exp),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ExponentialMinus1", Math::expm1),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Floor", Math::floor),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("LogNatural", Math::log),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Log10", Math::log10),
			AbstractUnaryFunction.ofEntryNumber("Round",
					Map.of(Integer.class.getName(), n -> Math.round(n.floatValue()), Float.class.getName(),
							n -> Math.round(n.floatValue()),
							Double.class.getName(), n -> Math.round(n.doubleValue()), Long.class.getName(),
							n -> Math.round(n.doubleValue())),
					SchemaType.LONG, SchemaType.INTEGER),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Sine", Math::sin),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("HyperbolicSine", Math::sinh),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("Tangent", Math::tan),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("HyperbolicTangent", Math::tanh),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ToDegrees", Math::toDegrees),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("ToRadians", Math::toRadians),
			AbstractUnaryFunction.ofEntryAnyNumberDoubleOutput("SquareRoot", Math::sqrt),
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
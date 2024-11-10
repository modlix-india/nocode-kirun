package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StringFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(
			AbstractUnaryStringFunction.ofEntryString("Trim", String::trim),
			AbstractUnaryStringFunction.ofEntryString("TrimStart", String::stripLeading),
			AbstractUnaryStringFunction.ofEntryString("TrimEnd", String::stripTrailing),
			AbstractUnaryStringFunction.ofEntryStringAndIntegerOutput("Length", String::length),

			AbstractUnaryStringFunction.ofEntryString("LowerCase", String::toLowerCase),
			AbstractUnaryStringFunction.ofEntryString("UpperCase", String::toUpperCase),
			AbstractUnaryStringFunction.ofEntryStringAndBooleanOutput("IsBlank", String::isBlank),
			AbstractUnaryStringFunction.ofEntryStringAndBooleanOutput("IsEmpty", String::isEmpty),

			AbstractBinaryStringFunction.ofEntryStringStringAndBooleanOutput("Contains", String::contains),
			AbstractBinaryStringFunction.ofEntryStringStringAndBooleanOutput("EndsWith", String::endsWith),
			AbstractBinaryStringFunction.ofEntryStringStringAndBooleanOutput("StartsWith", String::startsWith),
			AbstractBinaryStringFunction.ofEntryStringStringAndBooleanOutput("EqualsIgnoreCase",
					String::equalsIgnoreCase),
			AbstractBinaryStringFunction.ofEntryStringStringAndBooleanOutput("Matches", String::matches),
			AbstractBinaryStringFunction.ofEntryStringStringAndIntegerOutput("IndexOf", String::indexOf),
			AbstractBinaryStringFunction.ofEntryStringStringAndIntegerOutput("LastIndexOf", String::lastIndexOf),
			AbstractBinaryStringFunction.ofEntryStringIntegerAndStringOutput("Repeat", String::repeat),

			AbstractTertiaryStringFunction.ofEntryStringStringIntegerAndIntegerOutput("IndexOfWithStartPoint",
					String::indexOf),
			AbstractTertiaryStringFunction.ofEntryStringStringIntegerAndIntegerOutput("LastIndexOfWithStartPoint",
					String::lastIndexOf),
			AbstractTertiaryStringFunction.ofEntryStringStringStringAndStringOutput("Replace", String::replace),
			AbstractTertiaryStringFunction.ofEntryStringStringStringAndStringOutput("ReplaceFirst",
					String::replaceFirst),
			AbstractTertiaryStringFunction.ofEntryStringIntegerIntegerAndStringOutput("SubString", String::substring));

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
			.stream()
			.map(ReactiveFunction::getSignature)
			.map(FunctionSignature::getFullName)
			.toList();

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {
		if (!namespace.equals(Namespaces.STRING)) {
			return Mono.empty();
		}
		return Mono.just(REPO_MAP.get(name));
	}

	@Override
	public Flux<String> filter(String name) {
		return Flux.fromIterable(FILTERABLE_NAMES)
				.filter(e -> e.toLowerCase()
						.indexOf(name.toLowerCase()) != -1);
	}
}

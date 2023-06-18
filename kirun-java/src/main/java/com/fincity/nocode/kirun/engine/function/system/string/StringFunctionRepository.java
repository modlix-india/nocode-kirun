package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.function.system.AbstractUnaryFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StringFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	@SuppressWarnings("null")
	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(
	        AbstractUnaryFunction.ofEntryString("Trim", String::trim),
	        AbstractUnaryFunction.ofEntryString("LowerCase", String::toLowerCase),
	        AbstractUnaryFunction.ofEntryString("UpperCase", String::toUpperCase),
	        AbstractUnaryFunction.ofEntryStringBooleanOutput("IsBlank", String::isBlank),
	        AbstractUnaryFunction.ofEntryStringBooleanOutput("IsEmpty", String::isEmpty),

	        AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("Contains", String::contains),
	        AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("EndsWith", String::endsWith),
	        AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("EqualsIgnoreCase", String::equalsIgnoreCase),
	        AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("Matches", String::matches),
	        AbstractBinaryStringFunction.ofEntryAsStringIntegerOutput("IndexOf", String::indexOf),
	        AbstractBinaryStringFunction.ofEntryAsStringIntegerOutput("LastIndexOf", String::lastIndexOf),
	        AbstractBinaryStringFunction.ofEntryAsStringAndIntegerStringOutput("Repeat", String::repeat),

	        AbstractTertiaryStringFunction.ofEntryAsStringStringIntegerIntegerOutput("IndexOfWithStartPoint",
	                String::indexOf),
	        AbstractTertiaryStringFunction.ofEntryAsStringStringIntegerIntegerOutput("LastIndexOfWithStartPoint",
	                String::lastIndexOf),
	        AbstractTertiaryStringFunction.ofEntryAsStringStringStringStringOutput("Replace", String::replace),
	        AbstractTertiaryStringFunction.ofEntryAsStringStringStringStringOutput("ReplaceFirst",
	                String::replaceFirst),
	        AbstractTertiaryStringFunction.ofEntryAsStringIntegerIntegerStringOutput("SubString", String::substring));

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

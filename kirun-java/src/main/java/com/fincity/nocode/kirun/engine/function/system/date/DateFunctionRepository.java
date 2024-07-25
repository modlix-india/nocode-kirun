package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

	        AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetDate", "date", inputDate ->
			{

		        ZonedDateTime localDateTime = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern(inputDate))
		                .withZoneSameInstant(ZoneId.systemDefault());

		        return localDateTime.getDayOfMonth();

	        }, SchemaType.INTEGER)

	);

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
	        .stream()
	        .map(ReactiveFunction::getSignature)
	        .map(FunctionSignature::getFullName)
	        .toList();

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {

		if (!namespace.equals(Namespaces.DATE))
			return Mono.empty();

		return Mono.just(REPO_MAP.get(name));
	}

	@Override
	public Flux<String> filter(String name) {

		return Flux.fromIterable(FILTERABLE_NAMES)
		        .filter(e -> e.toLowerCase()
		                .indexOf(name.toLowerCase()) != -1);
	}

}

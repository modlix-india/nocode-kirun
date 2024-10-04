package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetCurrentTimestamp extends AbstractReactiveFunction {

	private static final String OUTPUT = "time";

	@Override
	public FunctionSignature getSignature() {

		return new FunctionSignature().setName("GetCurrentTimestamp")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.of())
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Instant.now()
		        .toEpochMilli()), ZoneId.of("UTC"));

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		return Mono.just(
		        new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(dtf.format(zdt)))))));

	}

}

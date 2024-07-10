package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class EpochToDate extends AbstractReactiveFunction {

	private static final String EPOCH = "epoch";

	private static final String OUTPUT = "date";

	private static final String ERROR_MSG = "Please provide a valid value for epoch.";

	@Override
	public FunctionSignature getSignature() {

		return new FunctionSignature().setNamespace(Namespaces.DATE)
		        .setName("EpochToDate")
		        .setParameters(Map.of(EPOCH, Parameter.of(EPOCH, new Schema()
		                .setAnyOf(List.of(Schema.ofInteger(EPOCH), Schema.ofLong(EPOCH), Schema.ofString(EPOCH))))));

	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var epochIp = context.getArguments()
		        .get(EPOCH);

		if (!epochIp.isJsonPrimitive())
			throw new KIRuntimeException(ERROR_MSG);

		JsonPrimitive epochPrimitive = epochIp.getAsJsonPrimitive();

		if (epochPrimitive.isBoolean())
			throw new KIRuntimeException(ERROR_MSG);

		Long longDate = epochPrimitive.isNumber() ? epochPrimitive.getAsLong()
		        : Long.parseLong(epochPrimitive.getAsString());

		Long updatedValue = longDate > 999999999999L ? longDate : longDate * 1000;

		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(updatedValue), ZoneId.of("UTC"));

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		return Mono.just(
		        new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(dtf.format(zdt)))))));
	}

}

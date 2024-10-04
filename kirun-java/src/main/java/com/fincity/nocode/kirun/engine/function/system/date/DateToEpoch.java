package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class DateToEpoch extends AbstractReactiveFunction {

	private static final String VALUE = "isoDate";
	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("DateToEpoch")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var date = context.getArguments()
		        .get(VALUE);

		if (date == null || date.isJsonNull() || !ValidDateTimeUtil.validate(date.getAsString()))
			throw new KIRuntimeException("Please provide the valid iso date.");

		String inputDate = date.getAsString();

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern(inputDate));

		return Mono
		        .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(zdt.toInstant()
		                .toEpochMilli()))))));

	}

}

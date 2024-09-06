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
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetTimeAsArray extends AbstractReactiveFunction {

	private static final String VALUE = "isoDate";

	private static final String OUTPUT = "output";

	@Override
	public FunctionSignature getSignature() {

		return new FunctionSignature().setName("GetTimeAsArray")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofArray(OUTPUT))))); // write array schema type

	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputDate = context.getArguments()
		        .get(VALUE)
		        .getAsString();

		if (!ValidDateTimeUtil.validate(inputDate))

			throw new KIRuntimeException("Please provide valid ISO date.");

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern(inputDate));

		JsonArray dateArray = new JsonArray();
		dateArray.add(new JsonPrimitive(zdt.getYear()));
		dateArray.add(new JsonPrimitive(zdt.getMonthValue()));
		dateArray.add(new JsonPrimitive(zdt.getDayOfMonth()));
		dateArray.add(new JsonPrimitive(zdt.getHour()));
		dateArray.add(new JsonPrimitive(zdt.getMinute()));
		dateArray.add(new JsonPrimitive(zdt.getSecond()));
		dateArray.add(new JsonPrimitive(zdt.getNano() / 1000000));

		return Mono.just(new FunctionOutput(List.of(EventResult.of(OUTPUT,
		        Map.of(OUTPUT, dateArray)))));
	}

}

package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.DATE;

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
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class DateToEpoch extends AbstractReactiveFunction {

	private static final String ISO_DATE = "isoDate";

	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("DateToEpoch")
		        .setNamespace(DATE)
		        .setParameters(Map.of(ISO_DATE, new Parameter().setParameterName(ISO_DATE)
		                .setSchema(Schema.ofRef(DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputDate = context.getArguments()
		        .get(ISO_DATE)
		        .getAsString();

		if (!ValidDateTimeUtil.validate(inputDate))
			throw new KIRuntimeException("Please provide the valid iso date.");

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern(inputDate));

		FunctionOutput fo = new FunctionOutput(
		        List.of(EventResult.of(OUTPUT, Map.of(OUTPUT, new JsonPrimitive(zdt.toEpochSecond())))));

		System.out.println(fo.allResults());

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.of(OUTPUT, Map.of(OUTPUT, new JsonPrimitive(zdt.toEpochSecond()))))));
	}

}

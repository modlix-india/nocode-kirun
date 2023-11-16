package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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
import com.fincity.nocode.kirun.engine.util.date.AdjustTimeStampUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetEndOfTimeStamp extends AbstractReactiveFunction {

	private static final String DATE = "isodate";

	private static final String TIME_UNIT = "unit";

	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("GetEndOfTimeStamp").setNamespace(Namespaces.DATE)
				.setParameters(Map.ofEntries(Parameter.ofEntry(DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
						Parameter.ofEntry(TIME_UNIT, Schema.ofString(TIME_UNIT)
								.setEnums(List.of(new JsonPrimitive("year"), new JsonPrimitive("month"),
										new JsonPrimitive("quarter"), new JsonPrimitive("week"),
										new JsonPrimitive("day"), new JsonPrimitive("date"), new JsonPrimitive("hour"),
										new JsonPrimitive("minute"), new JsonPrimitive("second")

								)))))
				.setEvents(Map.ofEntries(
						Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputDate = context.getArguments().get(DATE).getAsString();

		Matcher matcher = dateTimePattern.matcher(inputDate);

		if (!matcher.find())
			throw new KIRuntimeException("Please provide the valid iso format");

		String givenUnit = context.getArguments().get(TIME_UNIT).getAsString();

		ZonedDateTime zdt = AdjustTimeStampUtil
				.getEndWithGivenField(Instant.ofEpochMilli(GetTimeInMillisUtil.getEpochTime(inputDate)), givenUnit);

		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

		return Mono.just(
				new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(zdt.format(df)))))));
	}

}

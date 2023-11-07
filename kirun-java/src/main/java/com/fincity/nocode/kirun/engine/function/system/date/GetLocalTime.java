package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetLocalTime extends AbstractReactiveFunction {

	private static final String DATE = "isodate";
	private static final String OUTPUT = "localtime";

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("GetLocalTime").setNamespace(Namespaces.DATE)
				.setParameters(Map.of(DATE,
						new Parameter().setParameterName(DATE).setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));

	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		String inputDate = context.getArguments().get(DATE).getAsString();

		if (!IsValidISODateUtil.checkValidity(inputDate))
			throw new KIRuntimeException("Please provide a valid ISO date");

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(zdt.toInstant().toEpochMilli()),
				ZoneId.systemDefault());

		OffsetDateTime odt = OffsetDateTime.now(ZoneId.systemDefault());
		ZoneOffset tz = odt.getOffset();
		String output = ldt.toString() + tz;
	
		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(output))))));

	}
}
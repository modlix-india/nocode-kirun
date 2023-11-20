package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.DurationUtil;
import reactor.core.publisher.Mono;

public class FromNow extends AbstractReactiveFunction {

	private static final String ISO_DATES = "isodates";
	private static final String KEY = "key";
	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {
		List<JsonElement> keyEnums = Arrays
		        .asList("N", "A", "I", "EN", "EA", "EI", "EN2", "EA2", "EI2", "EN3", "EN4", "EN5", "EA3", "EA4", "EA5",
		                "EI3", "EI4", "EI5", "ENA", "EAA", "EIA", "EY", "EM", "EW", "ED", "EH", "ES")
		        .stream()
		        .map(JsonPrimitive::new)
		        .collect(Collectors.toList());
		return new FunctionSignature().setName("FromNow")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.ofEntries(Parameter.ofEntry(ISO_DATES, Schema.ofAny(Namespaces.DATE + ".timeStamp")),
		                Parameter.ofEntry(KEY, Schema.ofString(KEY)
		                        .setEnums(keyEnums))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		JsonArray dates = context.getArguments()
		        .get(ISO_DATES)
		        .getAsJsonArray();
		String extractedKey = context.getArguments()
		        .get(KEY)
		        .getAsString();

		List<String> dateList = new ArrayList<>();

		for (JsonElement dateElement : dates) {
			dateList.add(dateElement.getAsString());
		}

		int size = dateList.size();

		if (size == 0) {
			throw new KIRuntimeException("Please provide at least one timestamp for comparing");
		}

		if (size == 2) {
			return processTwoDates(dateList.get(0), dateList.get(1), extractedKey);
		} else {
			return processOneDate(dateList.get(0), extractedKey);
		}
	}

	private Mono<FunctionOutput> processTwoDates(String firstDate, String secondDate, String extractedKey) {
		DateTimeFormatter dtf = DateTimePatternUtil.getPattern(firstDate);
		DateTimeFormatter dtf2 = DateTimePatternUtil.getPattern(secondDate);
		ZonedDateTime date1 = ZonedDateTime.parse(firstDate, dtf);
		ZonedDateTime date2 = ZonedDateTime.parse(secondDate, dtf2);
		LocalDateTime ldt1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(date1.toInstant()
		        .toEpochMilli()), ZoneId.systemDefault());

		LocalDateTime ldt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(date2.toInstant()
		        .toEpochMilli()), ZoneId.systemDefault());

		String output = DurationUtil.getDuration(ldt1, ldt2, extractedKey);
		Map<String, JsonElement> resultMap = new HashMap<>();
		resultMap.put(OUTPUT, new JsonPrimitive(output));
		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(output))))));
	}

	private Mono<FunctionOutput> processOneDate(String firstDate, String extractedKey) {
		Date currentDate = new Date();
		DateTimeFormatter dtf = DateTimePatternUtil.getPattern(firstDate);
		ZonedDateTime date1 = ZonedDateTime.parse(firstDate, dtf);
		LocalDateTime ldt1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(date1.toInstant()
		        .toEpochMilli()), ZoneId.systemDefault());
		LocalDateTime ldt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentDate.toInstant()
		        .toEpochMilli()), ZoneId.systemDefault());

		String output = DurationUtil.getDuration(ldt1, ldt2, extractedKey);
		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(output))))));
	}
}

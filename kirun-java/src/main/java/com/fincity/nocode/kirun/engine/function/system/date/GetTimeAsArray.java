package com.fincity.nocode.kirun.engine.function.system.date;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetTimeAsArray extends AbstractReactiveFunction {

	private static final String VALUE = "isoDate";

	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {

		ArraySchemaType arraySchemaType = ArraySchemaType.of(Schema.ofInteger("element"));

		return new FunctionSignature().setName("GetTimeAsArray")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofArray(OUTPUT).setItems(arraySchemaType))))); // write array schema type

	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputDate = context.getArguments()
		        .get(VALUE)
		        .getAsString();

		if (!ValidDateTimeUtil.validate(inputDate))

			throw new KIRuntimeException("Please provide valid ISO date.");


		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(new Date(GetTimeInMillisUtil.getEpochTime(inputDate)));

		JsonArray dateArray = new JsonArray();

		dateArray.add(new JsonPrimitive(calendar.get(Calendar.YEAR)));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.MONTH) + 1));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.DAY_OF_MONTH)));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.HOUR_OF_DAY)));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.MINUTE)));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.SECOND)));
		dateArray.add(new JsonPrimitive(calendar.get(Calendar.MILLISECOND)));

		
		return Mono.just(new FunctionOutput(List.of(EventResult.of(OUTPUT,
		        Map.of(OUTPUT, dateArray)))));
	}

}

package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil.validate;

import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.core.publisher.Mono;

public class GetTimeZoneOffset extends AbstractReactiveFunction {

	private static final String DATE = "isodate";

	private static final String OUTPUT = "result";

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("GetTimeZoneOffset")
		        .setNamespace(Namespaces.DATE)
		        .setParameters(Map.of(DATE, new Parameter().setParameterName(DATE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofInteger(OUTPUT)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputDate = context.getArguments()
		        .get(DATE)
		        .getAsString();

		if (!validate(inputDate))
			throw new KIRuntimeException("Please provide valid ISO date");

		return null;
	}

}

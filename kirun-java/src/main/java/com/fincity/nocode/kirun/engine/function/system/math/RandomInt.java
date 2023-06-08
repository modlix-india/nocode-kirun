package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class RandomInt extends AbstractReactiveFunction {

	private static final String MIN_VALUE = "minValue";

	private static final String MAX_VALUE = "maxValue";

	private static final String VALUE = "value";

	private Random rand = new Random();

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("RandomInt")
		        .setNamespace(MATH)
		        .setParameters(Map.of(MIN_VALUE, new Parameter().setParameterName(MIN_VALUE)
		                .setSchema(Schema.ofInteger(MIN_VALUE)
		                        .setDefaultValue(new JsonPrimitive(0))),
		                MAX_VALUE, new Parameter().setParameterName(MAX_VALUE)
		                        .setSchema(Schema.ofInteger(MAX_VALUE)
		                                .setDefaultValue(new JsonPrimitive(Integer.MAX_VALUE)))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofInteger(VALUE)))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		int minValue = context.getArguments()
		        .get(MIN_VALUE)
		        .getAsInt();

		int maxValue = context.getArguments()
		        .get(MAX_VALUE)
		        .getAsInt();

		int randValue = rand.nextInt(minValue, maxValue == Integer.MAX_VALUE ? maxValue : maxValue + 1);

		return Mono
		        .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(randValue))))));
	}

}

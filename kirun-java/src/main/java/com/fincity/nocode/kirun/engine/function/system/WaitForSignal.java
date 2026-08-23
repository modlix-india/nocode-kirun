package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.suspend.WakeCondition;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

/**
 * Stops the execution until a named signal arrives, to be resumed by the host.
 *
 * This is the gate for anything the workflow cannot decide by itself - an approval, a reply, a
 * webhook, a "link was clicked". The host decides what a signal is and how it is delivered; the
 * runtime only records which one this execution is waiting for.
 *
 * Two ways out, so a definition can branch on them:
 * <ul>
 * <li>{@code output} - the signal arrived, and its payload is this step's output.</li>
 * <li>{@code timeout} - {@code timeoutMillis} elapsed first and the host resumed down this path
 * instead.</li>
 * </ul>
 */
public class WaitForSignal extends AbstractReactiveFunction {

	static final String SIGNAL_NAME = "signalName";

	static final String TIMEOUT_MILLIS = "timeoutMillis";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("WaitForSignal")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(SIGNAL_NAME, Schema.ofString(SIGNAL_NAME)
	                .setMinLength(1)),
	                Parameter.ofEntry(TIMEOUT_MILLIS,
	                        Schema.of(TIMEOUT_MILLIS, SchemaType.LONG, SchemaType.INTEGER, SchemaType.NULL)
	                                .setMinimum(0))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of()),
	                Event.eventMapEntry(Event.TIMEOUT, Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement signalName = context.getArguments()
		        .get(SIGNAL_NAME);

		if (signalName == null || signalName.isJsonNull() || signalName.getAsString()
		        .isBlank())
			throw new KIRuntimeException("System.WaitForSignal needs a \"" + SIGNAL_NAME + "\" to wait for");

		JsonElement timeout = context.getArguments()
		        .get(TIMEOUT_MILLIS);

		Instant timeoutAt = null;

		if (timeout != null && !timeout.isJsonNull() && timeout.getAsLong() > 0)
			timeoutAt = Instant.now()
			        .plusMillis(timeout.getAsLong());

		context.suspend(new WakeCondition.SignalWake(signalName.getAsString()
		        .trim(), timeoutAt));

		return Mono.just(new FunctionOutput(List.of(EventResult.of(Event.SUSPENDED, Map.of()))));
	}
}

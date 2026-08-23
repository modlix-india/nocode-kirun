package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.time.Instant;
import java.time.format.DateTimeParseException;
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
 * Stops the execution until a point in time, to be resumed by the host.
 *
 * This is the primitive a long-running workflow is built out of - "wait three days, then send the
 * follow-up". It does not hold a subscription open, so the wait can outlive the request, the
 * process and any execution timeout the host imposes; contrast {@link Wait}, which merely delays a
 * live reactive chain and is only suitable for very short pauses.
 *
 * On resume the host's payload becomes this step's output, so a definition can read whatever the
 * wake-up carried through {@code Steps.<thisStep>.output}.
 */
public class WaitUntil extends AbstractReactiveFunction {

	static final String UNTIL = "until";

	static final String DURATION_MILLIS = "durationMillis";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("WaitUntil")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(
	                Parameter.ofEntry(UNTIL, Schema.of(UNTIL, SchemaType.STRING, SchemaType.NULL)),
	                Parameter.ofEntry(DURATION_MILLIS,
	                        Schema.of(DURATION_MILLIS, SchemaType.LONG, SchemaType.INTEGER, SchemaType.NULL)
	                                .setMinimum(0))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement until = context.getArguments()
		        .get(UNTIL);
		JsonElement duration = context.getArguments()
		        .get(DURATION_MILLIS);

		Instant wakeAt = resolveWakeAt(until, duration);

		context.suspend(new WakeCondition.TimerWake(wakeAt));

		return Mono.just(new FunctionOutput(List.of(EventResult.of(Event.SUSPENDED, Map.of()))));
	}

	private Instant resolveWakeAt(JsonElement until, JsonElement duration) {

		if (until != null && !until.isJsonNull() && !until.getAsString()
		        .isBlank()) {

			String value = until.getAsString()
			        .trim();

			try {
				return Instant.parse(value);
			} catch (DateTimeParseException e) {
				throw new KIRuntimeException(
				        "System.WaitUntil could not read \"" + value + "\" as an ISO-8601 instant, e.g. "
				                + "2026-08-24T09:30:00Z",
				        e);
			}
		}

		if (duration != null && !duration.isJsonNull()) {

			long millis = duration.getAsLong();

			if (millis > 0)
				return Instant.now()
				        .plusMillis(millis);
		}

		throw new KIRuntimeException(
		        "System.WaitUntil needs either \"" + UNTIL + "\", an ISO-8601 instant, or a positive \""
		                + DURATION_MILLIS + "\".");
	}
}

package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.time.Instant;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Why a suspended execution should be woken up again.
 *
 * The runtime only describes the condition - it neither waits nor schedules. A host reads this off
 * the {@link SuspendedExecution} and arranges the wake-up with whatever scheduler it owns (see
 * {@link com.fincity.nocode.kirun.engine.runtime.suspend.store.ResumeScheduler}).
 */
public sealed interface WakeCondition permits WakeCondition.TimerWake, WakeCondition.SignalWake {

	String KIND = "kind";
	String TIMER = "timer";
	String SIGNAL = "signal";

	String WAKE_AT = "wakeAt";
	String SIGNAL_NAME = "signalName";
	String TIMEOUT_AT = "timeoutAt";

	/** Wake once the clock passes {@code wakeAt}. */
	record TimerWake(Instant wakeAt) implements WakeCondition {

		public TimerWake {
			if (wakeAt == null)
				throw new KIRuntimeException("A timer wake condition needs a wakeAt instant");
		}
	}

	/**
	 * Wake when a named signal is delivered, or - if {@code timeoutAt} is set and passes first -
	 * wake with the timeout flag so the definition can take its timeout branch.
	 */
	record SignalWake(String signalName, Instant timeoutAt) implements WakeCondition {

		public SignalWake {
			if (signalName == null || signalName.isBlank())
				throw new KIRuntimeException("A signal wake condition needs a signalName");
		}
	}

	default JsonObject toJson() {

		JsonObject jo = new JsonObject();

		if (this instanceof TimerWake timer) {
			jo.add(KIND, new JsonPrimitive(TIMER));
			jo.add(WAKE_AT, new JsonPrimitive(timer.wakeAt()
			        .toString()));
		} else if (this instanceof SignalWake signal) {
			jo.add(KIND, new JsonPrimitive(SIGNAL));
			jo.add(SIGNAL_NAME, new JsonPrimitive(signal.signalName()));
			if (signal.timeoutAt() != null)
				jo.add(TIMEOUT_AT, new JsonPrimitive(signal.timeoutAt()
				        .toString()));
		}

		return jo;
	}

	static WakeCondition fromJson(JsonObject jo) {

		if (jo == null || !jo.has(KIND))
			throw new KIRuntimeException("Wake condition is missing its \"" + KIND + "\"");

		String kind = jo.get(KIND)
		        .getAsString();

		return switch (kind) {

		case TIMER -> new TimerWake(Instant.parse(jo.get(WAKE_AT)
		        .getAsString()));

		case SIGNAL -> new SignalWake(jo.get(SIGNAL_NAME)
		        .getAsString(),
		        jo.has(TIMEOUT_AT) && !jo.get(TIMEOUT_AT)
		                .isJsonNull() ? Instant.parse(jo.get(TIMEOUT_AT)
		                        .getAsString()) : null);

		default -> throw new KIRuntimeException("Unknown wake condition kind : " + kind);
		};
	}
}

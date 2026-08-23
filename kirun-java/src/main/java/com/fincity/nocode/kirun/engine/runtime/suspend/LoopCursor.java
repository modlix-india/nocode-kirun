package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.util.concurrent.atomic.AtomicReference;

import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

/**
 * A loop statement's iteration position, kept in the execution context so it can be serialised.
 *
 * Loop functions used to hold their position in closure state - an {@code AtomicInteger} captured by
 * the {@link com.fincity.nocode.kirun.engine.model.FunctionOutput} generator - which meant it could
 * not be snapshotted, so a suspension inside a loop body could not be resumed. Holding it in
 * {@code executionContext} instead puts it inside the snapshot for free, and makes re-executing a
 * loop step on resume pick up where it left off instead of restarting.
 *
 * The key is deliberately prefixed:
 * {@link com.fincity.nocode.kirun.engine.function.system.loop.Break} stores its break flag under the
 * bare statement name in the same map, so an unprefixed cursor key would collide with it.
 */
public class LoopCursor {

	private static final String PREFIX = "__kirun.loop.cursor.";

	private LoopCursor() {
	}

	private static String key(String statementName) {
		return PREFIX + statementName;
	}

	/**
	 * A cursor for one loop activation.
	 *
	 * When the loop is running as a named step the position lives in the execution context and is
	 * therefore snapshottable. A loop invoked directly, with no statement behind it, has nowhere
	 * durable to put it and no way to be resumed either, so it falls back to holding the position
	 * in the instance - which is exactly the old behaviour.
	 */
	public static Cursor of(ReactiveFunctionExecutionParameters context, String statementName) {
		return new Cursor(context, statementName);
	}

	public static class Cursor {

		private final ReactiveFunctionExecutionParameters context;
		private final String statementName;
		private final AtomicReference<Double> local;

		private Cursor(ReactiveFunctionExecutionParameters context, String statementName) {

			this.context = context;
			this.statementName = statementName;
			this.local = statementName == null ? new AtomicReference<>(null) : null;
		}

		/**
		 * Whether a position has been recorded yet. Loops that count from zero cannot tell "not
		 * started" from "at zero" without this; a range loop starting at a non-zero {@code from}
		 * needs it to know whether to use {@code from} or the stored value.
		 */
		public boolean isPresent() {

			if (this.local != null)
				return this.local.get() != null;

			var value = this.context.getExecutionContext()
			        .get(key(this.statementName));

			return value != null && !value.isJsonNull();
		}

		public double get() {
			return this.getOr(0d);
		}

		/** The recorded position, or {@code defaultValue} when the loop has not started. */
		public double getOr(double defaultValue) {

			if (this.local != null) {
				Double value = this.local.get();
				return value == null ? defaultValue : value;
			}

			var value = this.context.getExecutionContext()
			        .get(key(this.statementName));

			return value == null || value.isJsonNull() ? defaultValue : value.getAsDouble();
		}

		public int getAsInt() {
			return (int) this.get();
		}

		public void set(double position) {

			if (this.local != null) {
				this.local.set(position);
				return;
			}

			this.context.getExecutionContext()
			        .put(key(this.statementName), new JsonPrimitive(position));
		}

		/**
		 * Drops the position once the loop is finished, so a later re-entry of the same statement -
		 * a loop nested inside an outer loop, say - starts from the beginning again.
		 */
		public void clear() {

			if (this.local != null) {
				this.local.set(null);
				return;
			}

			this.context.getExecutionContext()
			        .remove(key(this.statementName));
		}
	}
}

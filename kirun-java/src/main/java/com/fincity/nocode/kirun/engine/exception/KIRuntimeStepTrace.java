package com.fincity.nocode.kirun.engine.exception;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Where in a KIRun function an error was raised: the function, the step, the function the step
 * called, and the arguments the step received.
 *
 * <p>
 * It is added to the error as a suppressed exception, never wrapped around it, so the error keeps
 * its own type, message and cause, and whatever a caller does with it (an HTTP status, a message
 * to show) is unchanged. An error passing out of nested functions collects one per function,
 * innermost first, which reads as the call path.
 *
 * <p>
 * The arguments are written as JSON at the moment of the failure, bounded in depth, size and
 * length, with the values of keys that look like secrets masked: the trace ends up in logs and run
 * records that more people can read than can read the data.
 */
public class KIRuntimeStepTrace extends RuntimeException {

	private static final long serialVersionUID = 4617208843025710348L;

	/** Most traces one error collects; a reused exception instance cannot grow without bound. */
	static final int MAX_TRACES = 20;

	static final int MAX_ARGUMENTS_LENGTH = 2000;
	static final int MAX_STRING_LENGTH = 200;
	static final int MAX_ITEMS = 10;
	static final int MAX_DEPTH = 4;

	private static final Pattern SECRET_KEY = Pattern
			.compile("password|passwd|secret|token|api[-_]?key|authorization|cookie|credential|private[-_]?key");

	private final String functionName;
	private final String statementName;
	private final String stepFunction;
	private final String arguments;

	private KIRuntimeStepTrace(String functionName, String statementName, String stepFunction, String arguments) {
		super(message(functionName, statementName, stepFunction, arguments), null, false, false);
		this.functionName = functionName;
		this.statementName = statementName;
		this.stepFunction = stepFunction;
		this.arguments = arguments;
	}

	/**
	 * Adds a trace to {@code error}. {@code arguments} is null when the step failed before its
	 * arguments were worked out, an expression that could not be evaluated say.
	 */
	public static void attach(Throwable error, String functionName, String statementName, String stepFunction,
			Map<String, JsonElement> arguments) {

		if (error == null || error instanceof KIRuntimeStepTrace)
			return;

		int existing = 0;
		for (Throwable s : error.getSuppressed())
			if (s instanceof KIRuntimeStepTrace)
				existing++;
		if (existing >= MAX_TRACES)
			return;

		error.addSuppressed(
				new KIRuntimeStepTrace(functionName, statementName, stepFunction, argumentsText(arguments)));
	}

	/** The function definition the step is in, namespace.name. */
	public String getFunctionName() {
		return this.functionName;
	}

	public String getStatementName() {
		return this.statementName;
	}

	/** The function the step called, namespace.name. */
	public String getStepFunction() {
		return this.stepFunction;
	}

	/** The step's arguments as bounded, masked JSON; null when they were never worked out. */
	public String getArguments() {
		return this.arguments;
	}

	/**
	 * Two lines: where, then the arguments. The first line stands alone as a summary, and callers
	 * that cannot depend on this class read it from the message.
	 */
	private static String message(String functionName, String statementName, String stepFunction,
			String arguments) {
		return "at step " + statementName + " (" + stepFunction + ") of " + functionName + "\n"
				+ (arguments == null ? "before its arguments were evaluated" : "arguments " + arguments);
	}

	static String argumentsText(Map<String, JsonElement> arguments) {

		if (arguments == null)
			return null;

		StringBuilder sb = new StringBuilder();
		try {
			sb.append('{');
			int n = 0;
			for (Entry<String, JsonElement> e : arguments.entrySet()) {
				if (n++ > 0)
					sb.append(", ");
				appendKeyValue(sb, e.getKey(), e.getValue(), 1);
				if (sb.length() > MAX_ARGUMENTS_LENGTH)
					break;
			}
			sb.append('}');
		} catch (RuntimeException ex) {
			// The values can be changing under us; that may well be why the step failed.
			sb.append("… could not be read: ").append(ex.getClass().getSimpleName());
		}

		if (sb.length() <= MAX_ARGUMENTS_LENGTH)
			return sb.toString();
		return sb.substring(0, MAX_ARGUMENTS_LENGTH - 1) + "…";
	}

	private static void appendKeyValue(StringBuilder sb, String key, JsonElement value, int depth) {
		sb.append(new JsonPrimitive(String.valueOf(key))).append(": ");
		if (key != null && SECRET_KEY.matcher(key.toLowerCase(Locale.ROOT)).find() && value != null
				&& !value.isJsonNull())
			sb.append("\"***\"");
		else
			appendValue(sb, value, depth);
	}

	private static void appendValue(StringBuilder sb, JsonElement value, int depth) {

		if (value == null || value.isJsonNull()) {
			sb.append("null");
		} else if (value.isJsonPrimitive()) {
			JsonPrimitive p = value.getAsJsonPrimitive();
			if (p.isString() && p.getAsString().length() > MAX_STRING_LENGTH)
				sb.append(new JsonPrimitive(p.getAsString().substring(0, MAX_STRING_LENGTH) + "… ("
						+ p.getAsString().length() + " chars)"));
			else
				sb.append(p);
		} else if (value.isJsonArray()) {
			appendArray(sb, value.getAsJsonArray(), depth);
		} else {
			appendObject(sb, value.getAsJsonObject(), depth);
		}
	}

	private static void appendArray(StringBuilder sb, JsonArray array, int depth) {

		if (depth >= MAX_DEPTH) {
			sb.append("[… ").append(array.size()).append(" items]");
			return;
		}

		sb.append('[');
		int n = 0;
		for (JsonElement item : array) {
			if (n == MAX_ITEMS) {
				sb.append(", … ").append(array.size() - n).append(" more");
				break;
			}
			if (n++ > 0)
				sb.append(", ");
			appendValue(sb, item, depth + 1);
		}
		sb.append(']');
	}

	private static void appendObject(StringBuilder sb, JsonObject object, int depth) {

		if (depth >= MAX_DEPTH) {
			sb.append("{… ").append(object.size()).append(" keys}");
			return;
		}

		sb.append('{');
		int n = 0;
		for (Entry<String, JsonElement> e : object.entrySet()) {
			if (n == MAX_ITEMS * 2) {
				sb.append(", … ").append(object.size() - n).append(" more keys");
				break;
			}
			if (n++ > 0)
				sb.append(", ");
			appendKeyValue(sb, e.getKey(), e.getValue(), depth + 1);
		}
		sb.append('}');
	}
}

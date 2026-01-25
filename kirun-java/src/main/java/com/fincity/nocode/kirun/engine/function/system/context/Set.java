package com.fincity.nocode.kirun.engine.function.system.context;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_CTX;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.expression.Expression;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class Set extends AbstractReactiveFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Set")
			.setNamespace(SYSTEM_CTX)
			.setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
					.setType(Type.of(SchemaType.STRING))
					.setMinLength(1)), Parameter.ofEntry(VALUE, Schema.ofAny(VALUE))))
			.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		String key = context.getArguments()
				.get(NAME)
				.getAsString();

		if (key.isBlank()) {
			throw new KIRuntimeException("Empty string is not a valid name for the context element");
		}

		JsonElement value = context.getArguments()
				.get(VALUE);

		// Use TokenValueExtractor.splitPath for consistent path parsing
		String[] parts = TokenValueExtractor.splitPath(key);
		
		if (parts.length < 1 || !parts[0].equals("Context")) {
			throw new ExecutionException(
					StringFormatter.format("The context path $ is not a valid path in context", key));
		}

		// Evaluate any dynamic expressions in the path (e.g., Context.a[Steps.loop.index])
		String[] evaluatedParts = evaluateDynamicParts(parts, context);

		return modifyContextWithParts(context, key, value, evaluatedParts);
	}
	
	/**
	 * Evaluate any dynamic expressions in path parts
	 * E.g., "Context.a[Steps.loop.index]" where the index is dynamic
	 */
	private String[] evaluateDynamicParts(String[] parts, ReactiveFunctionExecutionParameters context) {
		String[] result = new String[parts.length];
		
		for (int i = 0; i < parts.length; i++) {
			// Check if this part contains dynamic bracket expressions
			result[i] = evaluateBracketExpressions(parts[i], context);
		}
		
		return result;
	}
	
	/**
	 * Evaluate bracket expressions in a path part
	 * E.g., "arr[Steps.loop.index]" -> "arr[0]" if Steps.loop.index evaluates to 0
	 */
	private String evaluateBracketExpressions(String part, ReactiveFunctionExecutionParameters context) {
		// Find bracket expressions that need evaluation
		StringBuilder result = new StringBuilder();
		int i = 0;
		
		while (i < part.length()) {
			if (part.charAt(i) == '[') {
				result.append('[');
				i++;
				
				// Find the matching ]
				StringBuilder bracketContent = new StringBuilder();
				int depth = 1;
				boolean inQuote = false;
				char quoteChar = 0;
				
				while (i < part.length() && depth > 0) {
					char ch = part.charAt(i);
					
					if (inQuote) {
						if (ch == quoteChar && (i == 0 || part.charAt(i - 1) != '\\')) {
							inQuote = false;
						}
						bracketContent.append(ch);
					} else {
						if (ch == '"' || ch == '\'') {
							inQuote = true;
							quoteChar = ch;
							bracketContent.append(ch);
						} else if (ch == '[') {
							depth++;
							bracketContent.append(ch);
						} else if (ch == ']') {
							depth--;
							if (depth > 0) bracketContent.append(ch);
						} else {
							bracketContent.append(ch);
						}
					}
					i++;
				}
				
				String content = bracketContent.toString();
				// Check if bracket content is a static value (number or quoted string)
				if (content.matches("^-?\\d+$") || 
				    (content.startsWith("\"") && content.endsWith("\"")) ||
				    (content.startsWith("'") && content.endsWith("'"))) {
					result.append(content).append(']');
				} else {
					// Dynamic expression - evaluate it
					try {
						ExpressionEvaluator evaluator = new ExpressionEvaluator(content);
						JsonElement evaluatedValue = evaluator.evaluate(context.getValuesMap());
						result.append(evaluatedValue.getAsString()).append(']');
					} catch (Exception err) {
						// If evaluation fails, keep original
						result.append(content).append(']');
					}
				}
			} else {
				result.append(part.charAt(i));
				i++;
			}
		}
		
		return result.toString();
	}

	private Mono<FunctionOutput> modifyContextWithParts(
			ReactiveFunctionExecutionParameters context,
			String key,
			JsonElement value,
			String[] parts) {
		// parts[0] is "Context", parts[1] is the context element name
		if (parts.length < 2) {
			throw new KIRuntimeException(
					StringFormatter.format("Context path '$' is too short", key));
		}

		// Get the first segment after "Context" - this should be a context element key
		// The segment may contain bracket notation like "a[0]" which we need to parse
		String firstSegment = parts[1];
		String[] firstSegmentParts = parseBracketSegments(firstSegment);
		String contextKey = firstSegmentParts[0];
		
		ContextElement ce = context.getContext().get(contextKey);

		if (ce == null) {
			throw new KIRuntimeException(
					StringFormatter.format("Context doesn't have any element with name '$' ", contextKey));
		}

		// If we just have "Context.a" with no further path
		if (parts.length == 2 && firstSegmentParts.length == 1) {
			ce.setElement(value);
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
		}

		JsonElement el = ce.getElement();
		
		// Initialize element if null
		if (el == null || el.isJsonNull()) {
			// Determine if first access is array or object
			boolean nextIsArray = firstSegmentParts.length > 1 
				? isArrayIndex(firstSegmentParts[1])
				: (parts.length > 2 ? isArrayAccess(parts[2]) : false);
			el = nextIsArray ? new JsonArray() : new JsonObject();
			ce.setElement(el);
		}

		// Collect all path segments (including bracket notation within segments)
		List<SegmentInfo> allSegments = new ArrayList<>();
		
		// Process remaining parts of the first segment (after context key)
		for (int j = 1; j < firstSegmentParts.length; j++) {
			allSegments.add(new SegmentInfo(
				stripQuotes(firstSegmentParts[j]),
				isArrayIndex(firstSegmentParts[j])
			));
		}
		
		// Process remaining parts (parts[2], parts[3], etc.)
		for (int i = 2; i < parts.length; i++) {
			String[] segmentParts = parseBracketSegments(parts[i]);
			for (String seg : segmentParts) {
				allSegments.add(new SegmentInfo(
					stripQuotes(seg),
					isArrayIndex(seg)
				));
			}
		}
		
		// Handle case where allSegments is empty (shouldn't happen, but be safe)
		if (allSegments.isEmpty()) {
			// This means we're setting the context element directly
			ce.setElement(value);
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
		}
		
		// Navigate to the parent of the final element
		for (int i = 0; i < allSegments.size() - 1; i++) {
			SegmentInfo segment = allSegments.get(i);
			SegmentInfo nextSegment = allSegments.get(i + 1);
			
			if (segment.isArray) {
				el = getDataFromArray(el, segment.value, nextSegment.isArray);
			} else {
				el = getDataFromObject(el, segment.value, nextSegment.isArray);
			}
		}
		
		// Set the final value
		SegmentInfo lastSegment = allSegments.get(allSegments.size() - 1);
		if (lastSegment.isArray) {
			putDataInArray(el, lastSegment.value, value);
		} else {
			putDataInObject(el, lastSegment.value, value);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
	}
	
	/**
	 * Parse bracket segments from a path part
	 * E.g., "arr[0]" -> ["arr", "0"], "obj" -> ["obj"]
	 */
	private String[] parseBracketSegments(String part) {
		List<String> segments = new ArrayList<>();
		int start = 0;
		int i = 0;
		
		while (i < part.length()) {
			if (part.charAt(i) == '[') {
				if (i > start) {
					segments.add(part.substring(start, i));
				}
				// Find matching ]
				int end = i + 1;
				boolean inQuote = false;
				char quoteChar = 0;
				while (end < part.length()) {
					if (inQuote) {
						if (part.charAt(end) == quoteChar && (end == 0 || part.charAt(end - 1) != '\\')) {
							inQuote = false;
						}
					} else {
						if (part.charAt(end) == '"' || part.charAt(end) == '\'') {
							inQuote = true;
							quoteChar = part.charAt(end);
						} else if (part.charAt(end) == ']') {
							break;
						}
					}
					end++;
				}
				segments.add(part.substring(i + 1, end));
				start = end + 1;
				i = start;
			} else {
				i++;
			}
		}
		
		if (start < part.length()) {
			segments.add(part.substring(start));
		}
		
		return segments.isEmpty() ? new String[]{part} : segments.toArray(new String[0]);
	}
	
	private boolean isArrayIndex(String segment) {
		return segment.matches("^-?\\d+$");
	}
	
	private boolean isArrayAccess(String part) {
		// Check if the part starts with bracket notation or is a pure number
		return part.startsWith("[") || isArrayIndex(part);
	}
	
	private String stripQuotes(String segment) {
		if ((segment.startsWith("\"") && segment.endsWith("\"")) ||
		    (segment.startsWith("'") && segment.endsWith("'"))) {
			return segment.substring(1, segment.length() - 1);
		}
		return segment;
	}
	
	private static class SegmentInfo {
		final String value;
		final boolean isArray;
		
		SegmentInfo(String value, boolean isArray) {
			this.value = value;
			this.isArray = isArray;
		}
	}

	private JsonElement getDataFromArray(JsonElement el, String mem, boolean nextIsArray) {
		if (!el.isJsonArray())
			throw new KIRuntimeException(StringFormatter.format("Expected an array but found $", el));

		int index = Integer.parseInt(mem);
		if (index < 0)
			throw new KIRuntimeException(StringFormatter.format("Array index is out of bound - $", mem));

		JsonArray ja = el.getAsJsonArray();
		while (index >= ja.size())
			ja.add(JsonNull.INSTANCE);

		JsonElement je = ja.get(index);
		if (je == null || je.isJsonNull()) {
			je = nextIsArray ? new JsonArray() : new JsonObject();
			ja.set(index, je);
		}
		return je;
	}

	private JsonElement getDataFromObject(JsonElement el, String mem, boolean nextIsArray) {
		if (el.isJsonArray() || !el.isJsonObject())
			throw new KIRuntimeException(StringFormatter.format("Expected an object but found $", el));

		JsonObject jo = el.getAsJsonObject();
		JsonElement je = jo.get(mem);

		if (je == null || je.isJsonNull()) {
			je = nextIsArray ? new JsonArray() : new JsonObject();
			jo.add(mem, je);
		}
		return je;
	}

	private void putDataInArray(JsonElement el, String mem, JsonElement value) {

		if (!el.isJsonArray())
			throw new KIRuntimeException(StringFormatter.format("Expected an array but found $", el));

		try {
			int index = Integer.parseInt(mem);

			if (index < 0)
				throw new KIRuntimeException(StringFormatter.format("Array index is out of bound - $", mem));

			JsonArray ja = el.getAsJsonArray();
			while (index >= ja.size())
				ja.add(JsonNull.INSTANCE);

			ja.set(index, value);
		} catch (Exception ex) {
			throw new KIRuntimeException(StringFormatter.format("Expected an array index but found $", mem));
		}
	}

	private void putDataInObject(JsonElement el, String mem, JsonElement value) {

		if (!el.isJsonObject())
			throw new KIRuntimeException(StringFormatter.format("Expected an object but found $", el));

		el.getAsJsonObject()
				.add(mem, value);
	}
}

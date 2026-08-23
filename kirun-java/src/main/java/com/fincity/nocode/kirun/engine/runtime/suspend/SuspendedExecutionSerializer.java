package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution.BranchRef;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution.GraphFrame;
import com.fincity.nocode.kirun.engine.util.json.KIRunGson;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Converts a {@link SuspendedExecution} to and from JSON.
 *
 * Written out by hand rather than left to Gson's reflection: the snapshot holds a sealed interface
 * ({@link WakeCondition}), {@link java.time.Instant}s, and a {@link Schema} that needs its own
 * adapters, and reflective handling of those is either wrong or silently lossy. Doing it explicitly
 * also means the persisted shape is reviewable in one place, which matters for something a host
 * stores for months.
 */
public class SuspendedExecutionSerializer {

	private static final String VERSION = "version";
	private static final String EXECUTION_ID = "executionId";
	private static final String NAMESPACE = "namespace";
	private static final String NAME = "name";
	private static final String WAKE_CONDITION = "wakeCondition";
	private static final String SUSPENDED_STEP_NAME = "suspendedStepName";
	private static final String ARGUMENTS = "arguments";
	private static final String CONTEXT = "context";
	private static final String STEPS = "steps";
	private static final String EVENTS = "events";
	private static final String EXECUTION_CONTEXT = "executionContext";
	private static final String COUNT = "count";
	private static final String GRAPH_FRAMES = "graphFrames";
	private static final String CHILD = "child";

	private static final String OWNER_STATEMENT_NAME = "ownerStatementName";
	private static final String EVENT_NAME = "eventName";
	private static final String OWNER_CONSUMED_EVENTS = "ownerConsumedEvents";
	private static final String EXECUTION_QUEUE = "executionQueue";
	private static final String BRANCH_QUEUE = "branchQueue";
	private static final String STATEMENT_NAME = "statementName";
	private static final String CONSUMED_EVENTS = "consumedEvents";

	private static final String SCHEMA = "schema";
	private static final String ELEMENT = "element";

	private SuspendedExecutionSerializer() {
	}

	public static String serialize(SuspendedExecution state) {
		return toJson(state).toString();
	}

	public static SuspendedExecution deserialize(String json) {

		if (json == null || json.isBlank())
			throw new KIRuntimeException("Cannot deserialize a blank suspended execution");

		JsonElement parsed = JsonParser.parseString(json);

		if (!parsed.isJsonObject())
			throw new KIRuntimeException("A suspended execution must be a JSON object");

		return fromJson(parsed.getAsJsonObject());
	}

	public static JsonObject toJson(SuspendedExecution state) {

		if (state == null)
			return null;

		Gson gson = KIRunGson.get();
		JsonObject jo = new JsonObject();

		jo.add(VERSION, new JsonPrimitive(state.getVersion()));
		addIfPresent(jo, EXECUTION_ID, state.getExecutionId());
		addIfPresent(jo, NAMESPACE, state.getNamespace());
		addIfPresent(jo, NAME, state.getName());
		addIfPresent(jo, SUSPENDED_STEP_NAME, state.getSuspendedStepName());
		jo.add(COUNT, new JsonPrimitive(state.getCount()));

		if (state.getWakeCondition() != null)
			jo.add(WAKE_CONDITION, state.getWakeCondition()
			        .toJson());

		jo.add(ARGUMENTS, mapToJson(state.getArguments()));
		jo.add(EXECUTION_CONTEXT, mapToJson(state.getExecutionContext()));

		JsonObject context = new JsonObject();
		if (state.getContext() != null)
			state.getContext()
			        .forEach((key, element) -> {

				        if (element == null)
					        return;

				        JsonObject ce = new JsonObject();
				        if (element.getSchema() != null)
					        ce.add(SCHEMA, gson.toJsonTree(element.getSchema()));
				        if (element.getElement() != null)
					        ce.add(ELEMENT, element.getElement());
				        context.add(key, ce);
			        });
		jo.add(CONTEXT, context);

		JsonObject steps = new JsonObject();
		if (state.getSteps() != null)
			state.getSteps()
			        .forEach((stepName, byEvent) -> {

				        JsonObject eventMap = new JsonObject();
				        if (byEvent != null)
					        byEvent.forEach((eventName, result) -> eventMap.add(eventName, mapToJson(result)));
				        steps.add(stepName, eventMap);
			        });
		jo.add(STEPS, steps);

		JsonObject events = new JsonObject();
		if (state.getEvents() != null)
			state.getEvents()
			        .forEach((eventName, results) -> {

				        JsonArray arr = new JsonArray();
				        if (results != null)
					        results.forEach(result -> arr.add(mapToJson(result)));
				        events.add(eventName, arr);
			        });
		jo.add(EVENTS, events);

		JsonArray frames = new JsonArray();
		if (state.getGraphFrames() != null)
			state.getGraphFrames()
			        .forEach(frame -> frames.add(frameToJson(frame)));
		jo.add(GRAPH_FRAMES, frames);

		if (state.getChild() != null)
			jo.add(CHILD, toJson(state.getChild()));

		return jo;
	}

	public static SuspendedExecution fromJson(JsonObject jo) {

		if (jo == null)
			return null;

		int version = jo.has(VERSION) ? jo.get(VERSION)
		        .getAsInt() : 0;

		if (version > SuspendedExecution.VERSION)
			throw new KIRuntimeException("This snapshot is at version " + version
			        + " and this runtime only understands up to version " + SuspendedExecution.VERSION
			        + ". Upgrade the runtime before resuming it.");

		Gson gson = KIRunGson.get();
		SuspendedExecution state = new SuspendedExecution().setVersion(version)
		        .setExecutionId(stringOrNull(jo, EXECUTION_ID))
		        .setNamespace(stringOrNull(jo, NAMESPACE))
		        .setName(stringOrNull(jo, NAME))
		        .setSuspendedStepName(stringOrNull(jo, SUSPENDED_STEP_NAME))
		        .setCount(jo.has(COUNT) ? jo.get(COUNT)
		                .getAsInt() : 0);

		if (jo.has(WAKE_CONDITION) && jo.get(WAKE_CONDITION)
		        .isJsonObject())
			state.setWakeCondition(WakeCondition.fromJson(jo.getAsJsonObject(WAKE_CONDITION)));

		state.setArguments(jsonToMap(objectOrNull(jo, ARGUMENTS)));
		state.setExecutionContext(jsonToMap(objectOrNull(jo, EXECUTION_CONTEXT)));

		Map<String, ContextElement> context = new LinkedHashMap<>();
		JsonObject contextJson = objectOrNull(jo, CONTEXT);
		if (contextJson != null)
			contextJson.entrySet()
			        .forEach(e -> {

				        JsonObject ce = e.getValue()
				                .getAsJsonObject();

				        context.put(e.getKey(), new ContextElement(
				                ce.has(SCHEMA) ? gson.fromJson(ce.get(SCHEMA), Schema.class) : null,
				                ce.get(ELEMENT)));
			        });
		state.setContext(context);

		Map<String, Map<String, Map<String, JsonElement>>> steps = new LinkedHashMap<>();
		JsonObject stepsJson = objectOrNull(jo, STEPS);
		if (stepsJson != null)
			stepsJson.entrySet()
			        .forEach(step -> {

				        Map<String, Map<String, JsonElement>> byEvent = new LinkedHashMap<>();
				        step.getValue()
				                .getAsJsonObject()
				                .entrySet()
				                .forEach(event -> byEvent.put(event.getKey(), jsonToMap(event.getValue()
				                        .getAsJsonObject())));
				        steps.put(step.getKey(), byEvent);
			        });
		state.setSteps(steps);

		Map<String, List<Map<String, JsonElement>>> events = new LinkedHashMap<>();
		JsonObject eventsJson = objectOrNull(jo, EVENTS);
		if (eventsJson != null)
			eventsJson.entrySet()
			        .forEach(event -> {

				        List<Map<String, JsonElement>> results = new ArrayList<>();
				        event.getValue()
				                .getAsJsonArray()
				                .forEach(result -> results.add(jsonToMap(result.getAsJsonObject())));
				        events.put(event.getKey(), results);
			        });
		state.setEvents(events);

		List<GraphFrame> frames = new ArrayList<>();
		if (jo.has(GRAPH_FRAMES))
			jo.getAsJsonArray(GRAPH_FRAMES)
			        .forEach(frame -> frames.add(frameFromJson(frame.getAsJsonObject())));
		state.setGraphFrames(frames);

		if (jo.has(CHILD) && jo.get(CHILD)
		        .isJsonObject())
			state.setChild(fromJson(jo.getAsJsonObject(CHILD)));

		return state;
	}

	private static JsonObject frameToJson(GraphFrame frame) {

		JsonObject jo = new JsonObject();
		addIfPresent(jo, OWNER_STATEMENT_NAME, frame.getOwnerStatementName());
		addIfPresent(jo, EVENT_NAME, frame.getEventName());
		jo.add(OWNER_CONSUMED_EVENTS, new JsonPrimitive(frame.getOwnerConsumedEvents()));

		JsonArray queue = new JsonArray();
		if (frame.getExecutionQueue() != null)
			frame.getExecutionQueue()
			        .forEach(queue::add);
		jo.add(EXECUTION_QUEUE, queue);

		JsonArray branches = new JsonArray();
		if (frame.getBranchQueue() != null)
			frame.getBranchQueue()
			        .forEach(branch -> {

				        JsonObject bo = new JsonObject();
				        addIfPresent(bo, STATEMENT_NAME, branch.getStatementName());
				        addIfPresent(bo, EVENT_NAME, branch.getEventName());
				        bo.add(CONSUMED_EVENTS, new JsonPrimitive(branch.getConsumedEvents()));
				        branches.add(bo);
			        });
		jo.add(BRANCH_QUEUE, branches);

		return jo;
	}

	private static GraphFrame frameFromJson(JsonObject jo) {

		GraphFrame frame = new GraphFrame().setOwnerStatementName(stringOrNull(jo, OWNER_STATEMENT_NAME))
		        .setEventName(stringOrNull(jo, EVENT_NAME))
		        .setOwnerConsumedEvents(jo.has(OWNER_CONSUMED_EVENTS) ? jo.get(OWNER_CONSUMED_EVENTS)
		                .getAsInt() : 0);

		List<String> queue = new ArrayList<>();
		if (jo.has(EXECUTION_QUEUE))
			jo.getAsJsonArray(EXECUTION_QUEUE)
			        .forEach(e -> queue.add(e.getAsString()));
		frame.setExecutionQueue(queue);

		List<BranchRef> branches = new ArrayList<>();
		if (jo.has(BRANCH_QUEUE))
			jo.getAsJsonArray(BRANCH_QUEUE)
			        .forEach(e -> {

				        JsonObject bo = e.getAsJsonObject();
				        branches.add(new BranchRef().setStatementName(stringOrNull(bo, STATEMENT_NAME))
				                .setEventName(stringOrNull(bo, EVENT_NAME))
				                .setConsumedEvents(bo.has(CONSUMED_EVENTS) ? bo.get(CONSUMED_EVENTS)
				                        .getAsInt() : 0));
			        });
		frame.setBranchQueue(branches);

		return frame;
	}

	private static JsonObject mapToJson(Map<String, JsonElement> map) {

		JsonObject jo = new JsonObject();
		if (map != null)
			map.forEach(jo::add);
		return jo;
	}

	private static Map<String, JsonElement> jsonToMap(JsonObject jo) {

		Map<String, JsonElement> map = new LinkedHashMap<>();
		if (jo != null)
			jo.entrySet()
			        .forEach(e -> map.put(e.getKey(), e.getValue()));
		return map;
	}

	private static void addIfPresent(JsonObject jo, String key, String value) {
		if (value != null)
			jo.add(key, new JsonPrimitive(value));
	}

	private static String stringOrNull(JsonObject jo, String key) {
		return jo.has(key) && !jo.get(key)
		        .isJsonNull() ? jo.get(key)
		                .getAsString() : null;
	}

	private static JsonObject objectOrNull(JsonObject jo, String key) {
		return jo.has(key) && jo.get(key)
		        .isJsonObject() ? jo.getAsJsonObject(key) : null;
	}
}

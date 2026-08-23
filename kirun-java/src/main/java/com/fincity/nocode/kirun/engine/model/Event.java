package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Event implements Serializable {

	private static final long serialVersionUID = -3042360312867594104L;

	public static final String OUTPUT = "output";

	public static final String ERROR = "error";

	public static final String ITERATION = "iteration";

	public static final String TRUE = "true";

	public static final String FALSE = "false";

	/**
	 * Raised when a step stops the execution to be resumed later - see
	 * {@link com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution}. Like
	 * {@link #OUTPUT} it terminates the enclosing graph, but unlike every other non-output event
	 * it does not open a branch. Its result carries only the execution id and the wake condition;
	 * the state snapshot is handed to the host on the execution parameters.
	 */
	public static final String SUSPENDED = "suspended";

	/**
	 * Raised by a signal wait whose deadline passed before the signal arrived, so a definition can
	 * branch on "approved" versus "expired".
	 */
	public static final String TIMEOUT = "timeout";

	public static final String SCHEMA_NAME = "Event";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("name", Schema.ofString("name"), "parameters", Schema.ofObject("parameter")
	                .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.SCHEMA))));

	private String name;
	private Map<String, Schema> parameters;

	public Event(Event event) {
		this.name = event.name;
		this.parameters = event.parameters == null ? null
		        : event.parameters.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, e -> new Schema(e.getValue())));
	}

	public static Map.Entry<String, Event> outputEventMapEntry(Map<String, Schema> parameters) {
		return eventMapEntry(OUTPUT, parameters);
	}

	public static Map.Entry<String, Event> eventMapEntry(String eventName, Map<String, Schema> parameters) {
		return Map.entry(eventName, new Event().setName(eventName)
		        .setParameters(parameters));
	}
}

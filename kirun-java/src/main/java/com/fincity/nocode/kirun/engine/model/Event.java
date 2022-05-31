package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Event implements Serializable {

	private static final long serialVersionUID = -3042360312867594104L;

	public static final String OUTPUT = "output";

	public static final String ERROR = "error";
	
	public static final String ITERATION = "iteration";
	
	public static final String TRUE = "true";
	
	public static final String FALSE = "false";

	public static final String SCHEMA_NAME = "Event";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle(SCHEMA_NAME)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("name", Schema.STRING, "parameters", Schema.ofObject("parameter")
	                .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.SCHEMA))));

	private String name;
	private Map<String, Schema> parameters;

	public static Map.Entry<String, Event> outputEventMapEntry(Map<String, Schema> parameters) {
		return eventMapEntry(OUTPUT, parameters);
	}

	public static Map.Entry<String, Event> eventMapEntry(String eventName, Map<String, Schema> parameters) {
		return Map.entry(eventName, new Event().setName(eventName)
		        .setParameters(parameters));
	}
}

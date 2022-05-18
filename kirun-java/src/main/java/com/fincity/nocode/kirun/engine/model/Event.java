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
	
	public static final String EVENT_OUTPUT = "output";
	
	public static final String EVENT_ERROR = "error";

	public static final String SCHEMA_NAME = "Event";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle(SCHEMA_NAME)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("name", Schema.STRING, "parameters", Schema.ofObject("parameter")
	                .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.SCHEMA))));

	private String name;
	private Map<String, Schema> parameters;
	
	
	public static Event outputEvent(Map<String, Schema> parameters) {
		return new Event().setName(EVENT_OUTPUT).setParameters(parameters);
	}
}

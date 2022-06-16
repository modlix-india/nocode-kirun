package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionSignature implements Serializable {

	private static final long serialVersionUID = 1989008489249395287L;

	private static final String SCHEMA_NAME = "FunctionSignature";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.STRING, "namespace", Schema.STRING, "parameters",
	                Schema.ofObject("parameters")
	                        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Parameter.SCHEMA)),
	                "events", Schema.ofObject("events")
	                        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Event.SCHEMA))));

	private String namespace;
	private String name;
	private Map<String, Parameter> parameters = Map.of();
	private Map<String, Event> events = Map.of();
	
	public String getFullName() {
		
		if (this.namespace == null || this.namespace.isBlank())
			return this.name;
		
		return this.namespace + "." + this.name;
	}
}

package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FunctionSignature extends BasicFunctionSignature {

	private static final long serialVersionUID = 1989008489249395287L;

	private static final String SCHEMA_NAME = "Part";
	
	public static final String FUNCTION_PART_ITERATION = "iterationPart";
	
	public static final String FUNCTION_PART_RETURN = "returnPart";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.STRING, "namespace", Schema.STRING, "parameters",
	                Schema.ofObject("parameters")
	                        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Parameter.SCHEMA)),
	                "events", Schema.ofObject("events")
	                        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Event.SCHEMA)),
	                "parts", Schema.ofObject("parts")
	                        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(BasicFunctionSignature.SCHEMA))));
	
	private Map<String, BasicFunctionSignature> parts;
	
	@Override
	public FunctionSignature setNamespace(String namespace) {
		return (FunctionSignature) super.setNamespace(namespace);
	}
	
	@Override
	public FunctionSignature setName(String name) {
		return (FunctionSignature) super.setName(name);
	}
	
	@Override
	public FunctionSignature setParameters(Map<String, Parameter> parameters) {
		return (FunctionSignature) super.setParameters(parameters);
	}
	
	@Override
	public FunctionSignature setEvents(Map<String, Event> events) {
		return (FunctionSignature) super.setEvents(events);
	}
}

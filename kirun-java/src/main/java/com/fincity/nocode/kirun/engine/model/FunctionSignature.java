package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class FunctionSignature implements Serializable {

	private static final long serialVersionUID = 1989008489249395287L;

	private static final String SCHEMA_NAME = "FunctionSignature";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.ofString("name"), "namespace", Schema.ofString("namespace"),
	                "parameters", Schema.ofObject("parameters")
	                        .setAdditionalProperties(new AdditionalType().setSchemaValue(Parameter.SCHEMA)),
	                "events", Schema.ofObject("events")
	                        .setAdditionalProperties(new AdditionalType().setSchemaValue(Event.SCHEMA))));

	private String namespace;
	private String name;
	private Map<String, Parameter> parameters = Map.of();
	private Map<String, Event> events = Map.of();

	public FunctionSignature(FunctionSignature obj) {

		this.name = obj.name;
		this.namespace = obj.namespace;
		
		this.parameters = obj.parameters == null ? null
		        : obj.parameters.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, e -> new Parameter(e.getValue())));
		
		this.events = obj.events == null ? null
		        : obj.events.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, e -> new Event(e.getValue())));
	}
	
	public String getFullName() {
		return this.namespace + "." + this.name;
	}
}

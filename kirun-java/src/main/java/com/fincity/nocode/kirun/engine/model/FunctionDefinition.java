package com.fincity.nocode.kirun.engine.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class FunctionDefinition extends FunctionSignature {

	private static final long serialVersionUID = 4891316472479078149L;

	private static final String SCHEMA_NAME = "FunctionDefinition";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.ofString("name"), "namespace", Schema.ofString("namespace"),
	                "parameters", Schema.ofArray("parameters", Parameter.SCHEMA), "events", Schema.ofObject("events")
	                        .setAdditionalProperties(new AdditionalType().setSchemaValue(Event.SCHEMA)),
	                "parts", Schema.ofObject("parts")
	                        .setAdditionalProperties(
	                                new AdditionalType().setSchemaValue(FunctionSignature.SCHEMA)),
	                "steps", Schema.ofObject("steps")
	                        .setAdditionalProperties(new AdditionalType().setSchemaValue(Statement.SCHEMA))));

	private int version = 1;
	private Map<String, Statement> steps;
	private Map<String, StatementGroup> stepGroups;

	public FunctionDefinition(FunctionDefinition fd) {
		super(fd);

		this.version = fd.version;
		
		this.steps = fd.steps == null ? null
		        : fd.steps.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, v -> new Statement(v.getValue())));
		
		this.stepGroups = fd.stepGroups == null ? null
		        : fd.stepGroups.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, v -> new StatementGroup(v.getValue())));
	}
}

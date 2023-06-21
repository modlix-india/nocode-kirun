package com.fincity.nocode.kirun.engine.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "parameterMap", "dependentStatements" })
public class Statement extends AbstractStatement {

	private static final long serialVersionUID = 8126173238268421930L;

	public static final String SCHEMA_NAME = "Statement";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(
	                Map.of("statementName", Schema.ofString("statementName"), "comment", Schema.ofString("comment"),
	                        "description", Schema.ofString("description"), "namespace", Schema.ofString("namespace"),
	                        "name", Schema.ofString("name"),

	                        "dependentStatements", Schema.ofObject("dependentstatement")
	                                .setAdditionalProperties(
	                                        new AdditionalType().setSchemaValue(Schema.ofBoolean("exits")))
	                                .setDefaultValue(new JsonObject()),

	                        "executeIftrue", Schema.ofObject("executeIftrue")
	                                .setAdditionalProperties(
	                                        new AdditionalType().setSchemaValue(Schema.ofBoolean("exits")))
	                                .setDefaultValue(new JsonObject()),

	                        "parameterMap", new Schema().setName("parameterMap")
	                                .setAdditionalProperties(
	                                        new AdditionalType().setSchemaValue(Schema.ofObject("parameterReference")
	                                                .setAdditionalProperties(new AdditionalType()
	                                                        .setSchemaValue(ParameterReference.SCHEMA)))),
	                        "position", Position.SCHEMA));

	private String statementName;
	private String namespace;
	private String name;
	private Map<String, Map<String, ParameterReference>> parameterMap;
	private Map<String, Boolean> dependentStatements;
	private Map<String, Boolean> executeIftrue;

	public Statement(Statement statement) {

		super(statement);

		this.statementName = statement.statementName;
		this.name = statement.name;
		this.namespace = statement.namespace;
		this.dependentStatements = statement.dependentStatements == null ? null
		        : statement.dependentStatements.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		this.executeIftrue = statement.executeIftrue == null ? null
		        : statement.executeIftrue.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		this.parameterMap = statement.parameterMap == null ? null
		        : statement.parameterMap.entrySet()
		                .stream()
		                .filter(e -> Objects.nonNull(e.getValue()))
		                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
		                        .entrySet()
		                        .stream()
		                        .collect(Collectors.toMap(Entry::getKey, k -> new ParameterReference(k.getValue())))));
		
	}

	public Statement(String statementName) {
		this.statementName = statementName;
	}

	public Map<String, Map<String, ParameterReference>> getParameterMap() {
		if (parameterMap == null) {
			this.parameterMap = Map.of();
		}
		return parameterMap;
	}

	public static Map.Entry<String, Statement> ofEntry(Statement statement) {
		return Map.entry(statement.statementName, statement);
	}
}

package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

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
	                                        new AdditionalPropertiesType().setSchemaValue(Schema.ofBoolean("exits"))),

	                        "parameterMap", new Schema().setName("parameterMap")
	                                .setAdditionalProperties(new AdditionalPropertiesType()
	                                        .setSchemaValue(Schema.ofObject("parameterReference")
	                                                .setAdditionalProperties(new AdditionalPropertiesType()
	                                                        .setSchemaValue(ParameterReference.SCHEMA)))),
	                        "position", Position.SCHEMA));

	public Statement(String statementName) {
		this.statementName = statementName;
	}

	private String statementName;
	private String namespace;
	private String name;
	private Map<String, Map<String, ParameterReference>> parameterMap;
	private Map<String, Boolean> dependentStatements;

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

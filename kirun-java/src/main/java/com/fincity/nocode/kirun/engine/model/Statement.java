package com.fincity.nocode.kirun.engine.model;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.util.graph.GraphVertexType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Statement extends AbstractStatement implements GraphVertexType<String> {

	private static final long serialVersionUID = 8126173238268421930L;

	public static final String SCHEMA_NAME = "Statement";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("statementName", Schema.STRING, "comment", Schema.STRING, "description",
	                Schema.STRING, "namespace", Schema.STRING, "name", Schema.STRING, "dependentStatementName",
	                Schema.STRING, "parameterMap", new Schema().setName("parameterMap")
	                        .setAdditionalProperties(new AdditionalPropertiesType()
	                                .setSchemaValue(Schema.ofArray("parameterReference", ParameterReference.SCHEMA))),
	                "position", Position.SCHEMA));

	public Statement(String statementName) {
		this.statementName = statementName;
	}

	private String statementName;
	private String namespace;
	private String name;
	private Map<String, List<ParameterReference>> parameterMap;
	private String dependentStatementName;

	public static Map.Entry<String, Statement> ofEntry(Statement statement) {
		return Map.entry(statement.statementName, statement);
	}

	@Override
	public String getUniqueKey() {
		return this.statementName;
	}
}

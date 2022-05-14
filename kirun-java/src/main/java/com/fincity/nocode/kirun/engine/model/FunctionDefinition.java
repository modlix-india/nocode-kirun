package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FunctionDefinition extends FunctionSignature{

	private static final long serialVersionUID = 4891316472479078149L;
	
	private static final String SCHEMA_NAME = "FunctionDefinition";
	
	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM).setName(SCHEMA_NAME)
						.setTitle(SCHEMA_NAME).setProperties(Map.of(
								"name", Schema.of("name",  SchemaType.STRING),
								"namespace", Schema.of("namespace",  SchemaType.STRING),
								"returns", Returns.SCHEMA,
								"parameters", Schema.ofArray("parameters", Parameter.SCHEMA),
								"alias", Schema.of("alias",  SchemaType.STRING),
								"steps", Schema.of("steps", SchemaType.OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Statement.SCHEMA)),
								"flow", Schema.of("flow", SchemaType.OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(StatementFlow.SCHEMA))
								));

	private Map<String, Statement> steps;
	private Map<String, StatementFlow> flow;
}

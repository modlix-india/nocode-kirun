package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatementFlow implements Serializable {

	private static final long serialVersionUID = -3179284164405372725L;
	
	private static final String SCHEMA_NAME = "StatementFlow";
	
	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM).setName(SCHEMA_NAME)
						.setTitle(SCHEMA_NAME).setProperties(Map.of(
								"next", Schema.of("next",  SchemaType.STRING),
								"branches", Schema.of("branches",  SchemaType.OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.of("branchProperty", SchemaType.STRING)))
								));
	
	private String next;
	private Map<String, String> branches;
}

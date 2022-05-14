package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Parameter implements Serializable{

	private static final long serialVersionUID = 8040181175269846469L;
	
	private static final String SCHEMA_NAME = "Parameter";
	
	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM).setName(SCHEMA_NAME)
						.setTitle(SCHEMA_NAME).setProperties(Map.of(
								"schema", Schema.SCHEMA,
								"variableArgument", Schema.of("variableArgument", SchemaType.BOOLEAN).setDefaultValue(new JsonPrimitive(Boolean.FALSE))
								));

	private Schema schema; // NOSONAR - this is really getting on my nerves, I have a use case for same name.
	private boolean variableArgument = false;
}

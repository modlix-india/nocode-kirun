package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Statement implements Serializable {

	private static final long serialVersionUID = -6342165835845448711L;

	private static final String SCHEMA_NAME = "Statement";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.of("name", SchemaType.STRING), "expression",
	                Schema.of("expression", SchemaType.STRING), "type", Schema.of("type", SchemaType.STRING)
	                        .setEnums(List.of(new JsonPrimitive("ASSERT"), new JsonPrimitive("BREAK"),
	                                new JsonPrimitive("CATCH"), new JsonPrimitive("CASE"),
	                                new JsonPrimitive("CONTINUE"), new JsonPrimitive("END"), new JsonPrimitive("ELSE"),
	                                new JsonPrimitive("ELSEIF"), new JsonPrimitive("EXPRESSION"),
	                                new JsonPrimitive("FINALLY"), new JsonPrimitive("FUNCTION"),
	                                new JsonPrimitive("IF"), new JsonPrimitive("LOOP"), new JsonPrimitive("START"),
	                                new JsonPrimitive("SWITCH"), new JsonPrimitive("THEN"), new JsonPrimitive("THROW"),
	                                new JsonPrimitive("TRY"), new JsonPrimitive("PARALLELIZE"),
	                                new JsonPrimitive("MERGE"))),
	                "properties", Schema.of("properties", SchemaType.OBJECT)
	                        .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(true))));

	private String name;
	private String expression;
	private StatementType type;
	private Map<String, JsonElement> properties; // NOSONAR - Because statement properties may contain anything.
}

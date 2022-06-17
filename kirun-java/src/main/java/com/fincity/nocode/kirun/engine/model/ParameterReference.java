package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParameterReference implements Serializable {

	private static final long serialVersionUID = 9001249267359224945L;

	private static final String SCHEMA_NAME = "ParameterReference";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("references", Schema.STRING, "value", Schema.ANY, "expression", Schema.STRING));

	private ParameterReferenceType type;
	private JsonElement value; // NOSONAR - JSON element is not serialised for some reason.
	private String expression;

	public enum ParameterReferenceType {
		VALUE, EXPRESSION;
	}

	public static ParameterReference of(String expression) {
		return new ParameterReference().setType(ParameterReferenceType.EXPRESSION)
		        .setExpression(expression);
	}

	public static ParameterReference of(JsonElement value) {
		return new ParameterReference().setType(ParameterReferenceType.VALUE)
		        .setValue(value);
	}
}

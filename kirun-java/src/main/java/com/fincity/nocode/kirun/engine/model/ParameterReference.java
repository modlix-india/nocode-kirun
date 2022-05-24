package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonElement;

import lombok.Data;

@Data
public class ParameterReference implements Serializable {

	private static final long serialVersionUID = 9001249267359224945L;

	private static final String SCHEMA_NAME = "ParameterReference";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("references", Schema.ofArray("reference", Schema.STRING), "value", Schema.ANY));

	private List<String> references;
	private JsonElement value; // NOSONAR - JSON element is not serialised for some reason.
}

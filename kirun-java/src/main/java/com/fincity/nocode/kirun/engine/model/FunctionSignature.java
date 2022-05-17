package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionSignature implements Serializable {

	private static final long serialVersionUID = 3414813295233452308L;

	private static final String SCHEMA_NAME = "FunctionSignature";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setProperties(Map.of("name", Schema.of("name", SchemaType.STRING), "namespace",
	                Schema.of("namespace", SchemaType.STRING), "returns", Returns.SCHEMA, "parameters",
	                Schema.ofArray("parameters", Parameter.SCHEMA), "alias", Schema.of("alias", SchemaType.STRING)));

	private String namespace;
	private String name;
	private List<Parameter> parameters;
	private String alias;
	private Map<String, Event> events;
}

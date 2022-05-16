package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Returns implements Serializable {

	private static final long serialVersionUID = -8017898005324871935L;

	private static final String SCHEMA_NAME = "Parameter";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setTitle(SCHEMA_NAME)
	        .setProperties(Map.of("schema", Schema.ofArray("schema", Schema.SCHEMA)));

	private List<Schema> schema; // NOSONAR - again it is because it is needed.
}

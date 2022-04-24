package com.fincity.nocode.kirun.engine.json.schema.type;

import java.io.Serializable;
import java.util.Set;

public interface Type extends Serializable{

	public Set<SchemaType> getAllowedSchemaTypes();
}

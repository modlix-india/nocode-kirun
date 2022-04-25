package com.fincity.nocode.kirun.engine.json.schema.type;

import java.io.Serializable;
import java.util.Set;

public interface Type extends Serializable{

	public Set<SchemaType> getAllowedSchemaTypes();
	
	public static Type of(SchemaType ...types) {
		
		if (types.length == 1)
			return new SingleType(types[0]);
		
		return new MultipleType().setType(Set.of(types));
	}
}

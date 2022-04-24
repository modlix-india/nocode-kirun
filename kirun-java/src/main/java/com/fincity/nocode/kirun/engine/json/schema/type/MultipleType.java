package com.fincity.nocode.kirun.engine.json.schema.type;

import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MultipleType implements Type {

	private static final long serialVersionUID = -8138857609871683543L;

	private Set<SchemaType> type;

	@Override
	public Set<SchemaType> getAllowedSchemaTypes() {
		return type;
	}
}

package com.fincity.nocode.kirun.engine.json.schema.type;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MultipleType extends Type {

	private static final long serialVersionUID = -8138857609871683543L;

	private Set<SchemaType> type;

	@Override
	public Set<SchemaType> getAllowedSchemaTypes() {
		return type;
	}
}

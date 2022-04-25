package com.fincity.nocode.kirun.engine.json.schema.type;

import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SingleType implements Type {

	private static final long serialVersionUID = -6709929624465529827L;

	private SchemaType type;

	public SingleType(SchemaType type) {
		this.type = type;
	}

	public Set<SchemaType> getAllowedSchemaTypes() {
		return Set.of(type);
	}
}

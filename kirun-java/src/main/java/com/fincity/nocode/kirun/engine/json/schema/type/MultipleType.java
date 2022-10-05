package com.fincity.nocode.kirun.engine.json.schema.type;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MultipleType extends Type {

	private static final long serialVersionUID = -8138857609871683543L;

	private Set<SchemaType> type;
	
	public MultipleType(MultipleType mtype) {
		
		if (mtype.type == null) return;
		
		this.type = mtype.type.stream().collect(Collectors.toSet());  
	}

	@Override
	public Set<SchemaType> getAllowedSchemaTypes() {
		return type;
	}
}

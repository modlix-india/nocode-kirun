package com.fincity.nocode.kirun.engine.json.schema.string;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StringSchema extends Schema {

	private static final long serialVersionUID = 5665601576555771876L;

	@Override
	public Type getType() {
		return new SingleType(SchemaType.STRING);
	}
}

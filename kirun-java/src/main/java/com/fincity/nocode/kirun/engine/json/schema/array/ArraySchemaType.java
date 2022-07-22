package com.fincity.nocode.kirun.engine.json.schema.array;

import java.io.Serializable;
import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ArraySchemaType implements Serializable {

	private static final long serialVersionUID = -3312223652146445370L;

	private Schema singleSchema;
	private List<Schema> tupleSchema;

	public static ArraySchemaType of(Schema... schemas) {

		if (schemas.length == 1)
			return new ArraySchemaType().setSingleSchema(schemas[0]);

		return new ArraySchemaType().setTupleSchema(List.of(schemas));
	}

	public boolean isSingleType() {
		return singleSchema != null;
	}
}

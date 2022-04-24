package com.fincity.nocode.kirun.engine.model;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Returns {

	private Schema schema;
}

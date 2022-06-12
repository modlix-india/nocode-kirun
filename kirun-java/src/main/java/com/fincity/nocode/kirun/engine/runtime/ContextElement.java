package com.fincity.nocode.kirun.engine.runtime;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.google.gson.JsonElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContextElement {

	private Schema schema;
	private JsonElement element;
}

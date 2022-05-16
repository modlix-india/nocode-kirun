package com.fincity.nocode.kirun.engine.json.schema.object;

import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdditionalPropertiesType implements Serializable {

	private static final long serialVersionUID = -3710026689972221380L;

	private Boolean booleanValue;
	private Schema schemaValue;
}

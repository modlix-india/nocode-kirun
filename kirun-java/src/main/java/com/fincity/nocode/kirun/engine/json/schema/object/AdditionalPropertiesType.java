package com.fincity.nocode.kirun.engine.json.schema.object;

import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AdditionalPropertiesType implements Serializable {

	private static final long serialVersionUID = -3710026689972221380L;

	private Boolean booleanValue;
	private Schema schemaValue;

	public AdditionalPropertiesType(AdditionalPropertiesType props) {

		this.booleanValue = props.booleanValue;
		this.schemaValue = new Schema(props.schemaValue);
	}
}

package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Parameter implements Serializable{

	private static final long serialVersionUID = 8040181175269846469L;
	
	private String name;
	private Schema schema;
	private boolean variableArgument = false;
}

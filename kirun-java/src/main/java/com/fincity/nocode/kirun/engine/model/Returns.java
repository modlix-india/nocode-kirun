package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Returns implements Serializable {

	private static final long serialVersionUID = -8017898005324871935L;
	
	private List<Schema> schema;
}

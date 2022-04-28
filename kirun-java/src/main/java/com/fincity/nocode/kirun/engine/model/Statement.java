package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Statement implements Serializable {

	private static final long serialVersionUID = -6342165835845448711L;

	private String name;
	private String expression;
	private StatementType type;
	private Map<String, JsonElement> properties; // NOSONAR - Because statement properties may contain anything.
}

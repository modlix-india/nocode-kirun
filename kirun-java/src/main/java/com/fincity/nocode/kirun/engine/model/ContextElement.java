package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

@Data
@Accessors(chain = true)
public class ContextElement implements Serializable {

	private static final long serialVersionUID = -8318421228015460990L;
	
	private Schema schema;
	private Mono<JsonElement> element; //NOSONAR - json element is not serializable
}

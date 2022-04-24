package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatementFlow {

	private String next;
	private Map<String, String> branches;
}

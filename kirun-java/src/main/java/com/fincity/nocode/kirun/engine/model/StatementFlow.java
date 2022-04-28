package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatementFlow implements Serializable {

	private static final long serialVersionUID = -3179284164405372725L;
	
	private String next;
	private Map<String, String> branches;
}

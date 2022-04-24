package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionSignature implements Serializable{

	private static final long serialVersionUID = 3414813295233452308L;

	private String name;
	private List<Parameter> parameters;
	private Returns returns;
	private String nameSpace;
	private String alias;
}

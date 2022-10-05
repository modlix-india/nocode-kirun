package com.fincity.nocode.kirun.engine.model;

import java.io.Serializable;

import lombok.Data;

@Data
public abstract class AbstractStatement implements Serializable {

	private static final long serialVersionUID = 1593473740405656933L;

	private String comment;
	private String description;
	private Position position;
	private boolean override = false;

	protected AbstractStatement() {

	}

	protected AbstractStatement(AbstractStatement abs) {
		this.comment = abs.comment;
		this.description = abs.description;
		this.override = abs.override;
		this.position = abs.position == null ? null : new Position(abs.position);
	}
}
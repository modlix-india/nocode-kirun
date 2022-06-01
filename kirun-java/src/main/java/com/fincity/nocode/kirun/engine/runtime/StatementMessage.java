package com.fincity.nocode.kirun.engine.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementMessage {

	private StatementMessageType messageType;
	private String message;
}

package com.fincity.nocode.kirun.engine.runtime;

import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.util.graph.GraphVertexType;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatementExecution  implements GraphVertexType<String>{
	
	private Statement statement;

	@Override
	public String getUniqueKey() {
		return statement.getStatementName();
	}
}
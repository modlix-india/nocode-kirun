package com.fincity.nocode.kirun.engine.runtime;

import java.util.ArrayList;
import java.util.List;

import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.util.graph.GraphVertexType;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatementExecution implements GraphVertexType<String> {

	private final Statement statement;

	private List<StatementMessage> messages = new ArrayList<>(5);
	
	private List<String> dependencies = new ArrayList<>();

	public StatementExecution(Statement statement) {

		this.statement = statement;
	}

	@Override
	public String getUniqueKey() {
		return statement.getStatementName();
	}

	public void addMessage(StatementMessageType type, String message) {
		this.messages.add(new StatementMessage(type, message));
	}
	
	public void addDependency(String dependency) {
		this.dependencies.add(dependency);
	}
}
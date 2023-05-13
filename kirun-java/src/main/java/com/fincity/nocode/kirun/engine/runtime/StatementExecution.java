package com.fincity.nocode.kirun.engine.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.graph.GraphVertexType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = { "messages", "dependencies" })
public class StatementExecution implements GraphVertexType<String> {

	private final Statement statement;

	private List<StatementMessage> messages = new ArrayList<>(5);

	private Set<String> dependencies = new HashSet<>();

	public StatementExecution(Statement statement) {

		this.statement = statement;
	}

	@Override
	public String getUniqueKey() {
		return statement.getStatementName();
	}

	public StatementExecution addMessage(StatementMessageType type, String message) {
		this.messages.add(new StatementMessage(type, message));
		return this;
	}

	public StatementExecution addDependency(String dependency) {
		this.dependencies.add(dependency);
		return this;
	}

	@Override
	public Set<String> getDepenedencies() {
		return this.dependencies;
	}
}
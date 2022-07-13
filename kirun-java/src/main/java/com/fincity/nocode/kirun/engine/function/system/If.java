package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

public class If extends AbstractFunction {

	static final String CONDITION = "condition";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("If")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(CONDITION, Schema.of(CONDITION, SchemaType.BOOLEAN))))
	        .setEvents(Map.ofEntries(Event.eventMapEntry(Event.TRUE, Map.of()),
	                Event.eventMapEntry(Event.FALSE, Map.of()), Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		var condition = context.getArguments()
		        .get(CONDITION);

		return new FunctionOutput(List.of(EventResult.of(condition.getAsBoolean() ? Event.TRUE : Event.FALSE, Map.of()),
		        EventResult.outputOf(Map.of())));
	}
}

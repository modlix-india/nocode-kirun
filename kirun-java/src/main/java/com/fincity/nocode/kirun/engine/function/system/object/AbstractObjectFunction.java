package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public abstract class AbstractObjectFunction extends AbstractReactiveFunction {

	private static final String SOURCE = "source";

	private static final String VALUE = "value";

	private FunctionSignature signature;

	protected AbstractObjectFunction(String functionName, Schema schema) {

		signature = new FunctionSignature().setName(functionName)
		        .setNamespace(Namespaces.SYSTEM_OBJECT)
		        .setParameters(Map.of(SOURCE, new Parameter().setParameterName(SOURCE)
		                .setSchema(Schema.ofAny(SOURCE))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, schema))));
	}

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}
}

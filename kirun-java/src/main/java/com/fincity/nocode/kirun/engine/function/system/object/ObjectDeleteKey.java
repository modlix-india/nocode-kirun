package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class ObjectDeleteKey extends AbstractFunction {

	private static final String VALUE = "value";
	private static final String SOURCE = "source";
	private static final String KEY = "key";

	private FunctionSignature signature;

	public ObjectDeleteKey() {

        this.signature = new FunctionSignature()
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setName("ObjectDeleteKey")
            .setParameters(Map.ofEntries(Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE))
            		,Parameter.ofEntry(KEY, Schema.ofString(KEY))))
            .setEvents(
                Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE))))
            );
    }

	public FunctionSignature getSignature() {
        return this.signature;
    }

	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
		
        JsonElement source = context.getArguments().get(SOURCE);
        String key = context.getArguments().get(KEY).getAsString();

        if (source == null || source.isJsonNull())
        	return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, JsonNull.INSTANCE))));

        source = source.deepCopy();
        source.getAsJsonObject().remove(key);

        return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, source))));
    }
}

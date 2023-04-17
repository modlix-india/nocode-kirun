package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public abstract class AbstractObjectFunction extends AbstractFunction {

    private static final String SOURCE = "source";

    private static final String EVENT_RESULT = "value";

    private FunctionSignature signature;

    protected AbstractObjectFunction(String functionName) {

        Event event = new Event().setName(Event.OUTPUT)
                .setParameters(Map.of(EVENT_RESULT, Schema.ofAny(EVENT_RESULT)));

        signature = new FunctionSignature().setName(functionName).setNamespace(Namespaces.SYSTEM)
                .setParameters(
                        Map.of(SOURCE, new Parameter().setParameterName(SOURCE).setSchema(Schema.ofAny(SOURCE))))
                .setEvents(Map.of(event.getName(), event));
    }

    @Override
    public FunctionSignature getSignature() {
        return signature;
    }
}

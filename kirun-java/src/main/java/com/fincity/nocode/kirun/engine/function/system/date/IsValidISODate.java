package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IsValidISODate extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isoDate";

    private static final String OUTPUT = "output";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("IsValidISODate").setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.of(ISO_DATE,
                                new Parameter().setParameterName(ISO_DATE)
                                        .setSchema(Schema.ofRef(Namespaces.DATE
                                                + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String date = context.getArguments().get(ISO_DATE).getAsString();

        return Mono.just(new FunctionOutput(
                List.of(EventResult.of(OUTPUT, Map.of(OUTPUT,
                        new JsonPrimitive(ValidDateTimeUtil.validate(date)))))));
    }

}

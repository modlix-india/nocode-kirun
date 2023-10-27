package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetCurrentTimeStamp extends AbstractReactiveFunction {

    private static final String OUTPUT = "time";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("GetCurrentTimeStamp")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of())
                .setEvents(Map.ofEntries(
                        Event.eventMapEntry(
                                OUTPUT, Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));

    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        
        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(cal.getTimeInMillis()))))));
    }

}

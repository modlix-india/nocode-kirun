package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.SimpleDateFormat;
import java.util.Date;
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

public class GetCurrentTime extends AbstractReactiveFunction {

    private static final String OUTPUT = "time";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("GetCurrentTime")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of())
                .setEvents(Map.ofEntries(
                        Event.eventMapEntry(
                                OUTPUT, Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(sdf.format(currentDate)))))));
    }

}

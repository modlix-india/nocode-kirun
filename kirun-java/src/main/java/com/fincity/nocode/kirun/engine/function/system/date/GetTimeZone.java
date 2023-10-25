package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetTimeZone extends AbstractReactiveFunction {

    private static final String DATE = "isodate";

    private static final String OUTPUT = "timeZone";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("GetTimeZone")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DATE, new Parameter().setParameterName(DATE)
                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide valid ISO date");

        ZoneId presentZone = ZonedDateTime.parse(inputDate).getZone();

        return Mono
                .just(new FunctionOutput(List.of(EventResult
                        .outputOf(Map.of(OUTPUT, new JsonPrimitive(TimeZone.getTimeZone(presentZone).getID()))))));

    }

}

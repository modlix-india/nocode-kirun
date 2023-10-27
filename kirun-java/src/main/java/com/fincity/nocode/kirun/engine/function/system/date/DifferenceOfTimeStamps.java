package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

public class DifferenceOfTimeStamps extends AbstractReactiveFunction {

    private static final String ISO_DATE1 = "dateone";

    private static final String ISO_DATE2 = "datetwo";

    private static final String OUTPUT = "difference";

    private static final Schema dateSchema = Schema.ofRef(Namespaces.DATE + ".timeStamp");

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("DifferenceOfTimeStamps")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(
                        Parameter.ofEntry(ISO_DATE1, dateSchema),
                        Parameter.ofEntry(ISO_DATE2, dateSchema)))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String firstDate = context.getArguments().get(ISO_DATE1).getAsString();

        String secondDate = context.getArguments().get(ISO_DATE2).getAsString();

        if (!IsValidIsoDateTime.checkValidity(firstDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE1);

        if (!IsValidIsoDateTime.checkValidity(secondDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE2);

        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[xx][XX]");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxx")
                .withZone(ZoneOffset.UTC);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[xx][XX]");

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(
                        Map.of(OUTPUT, new JsonPrimitive((ZonedDateTime.parse(firstDate, dtf).toEpochSecond()
                                - ZonedDateTime.parse(secondDate, dtf).toEpochSecond()) * 1000))))));
    }

}

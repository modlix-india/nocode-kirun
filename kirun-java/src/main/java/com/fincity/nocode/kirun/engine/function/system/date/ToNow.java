package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
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
import com.fincity.nocode.kirun.engine.util.date.DurationUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ToNow extends AbstractReactiveFunction {

    private static final String DATE = "isodate";

    private static final String SUFFIX_REQ = "suffixReq";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("ToNow")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DATE, new Parameter().setParameterName(DATE)
                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                        SUFFIX_REQ,
                        new Parameter().setParameterName(SUFFIX_REQ)
                                .setSchema(Schema.ofBoolean(SUFFIX_REQ).setDefaultValue(new JsonPrimitive(false)))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide the valid ISO date.");

        boolean suffix = context.getArguments().get(SUFFIX_REQ).getAsBoolean();

        Long inputInMillis = GetTimeInMillisUtil.getEpochTime(inputDate);

        Long currentInMillis = Calendar.getInstance().getTimeInMillis();

        Duration dur = Duration.between(Instant.ofEpochMilli(inputInMillis), Instant.ofEpochMilli(currentInMillis));

        String output = DurationUtil.getDuration(dur.toDays(), dur.toHours(), dur.toMinutes(), dur.toSeconds());

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(suffix ? output : "In " + output))))));

    }

}

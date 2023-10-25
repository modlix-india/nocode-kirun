package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

public class AddTime extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isodate";

    private static final String ADD = "add";

    private static final String TIME_UNIT = "timeunit";

    private static final String OUTPUT = "date";

    private static final String DAYS = "DAYS";

    private static final String HOURS = "HOURS";

    private static final String MINUTES = "MINUTES";

    private static final String SECONDS = "SECONDS";

    private static final String MILLISECONDS = "MILLISECONDS";

    private static final String MICROSECONDS = "MICROSECONDS";

    private static final String NANOSECONDS = "NANOSECONDS";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("AddTime")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(
                                Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                                Parameter.ofEntry(ADD, Schema.ofLong(ADD)),
                                Parameter.ofEntry(TIME_UNIT, Schema.ofString(TIME_UNIT)
                                        .setEnums(List.of(new JsonPrimitive(DAYS),
                                                new JsonPrimitive(HOURS),
                                                new JsonPrimitive(MINUTES),
                                                new JsonPrimitive(SECONDS),
                                                new JsonPrimitive(MILLISECONDS),
                                                new JsonPrimitive(MICROSECONDS),
                                                new JsonPrimitive(NANOSECONDS))))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(
                                Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide the valid ISO date");

        long span = context.getArguments().get(ADD).getAsLong();

        String unit = context.getArguments().get(TIME_UNIT).getAsString();

        // call add function if exists

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            Instant instant = sdf.parse(inputDate).toInstant();

            Instant updatedInstant = instant.plus(span, ChronoUnit.valueOf(unit));

            DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;

            if (inputDate.contains("+")) {
                
                sdf.applyPattern(unit);

                dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                System.out.println(dtf.format(updatedInstant));

                return Mono.just(new FunctionOutput(
                        List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(dtf.format(updatedInstant)))))));
            }

            sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            String updatedDate = sdf.format(new Date(updatedInstant.toEpochMilli()));

            System.out.println(updatedDate);

            return Mono.just(new FunctionOutput(
                    List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(updatedDate))))));

        } catch (ParseException e) {

            throw new KIRuntimeException("Please provide the valid ISO date");
        }

    }

}

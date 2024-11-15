package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class Difference extends AbstractDateFunction {

    public Difference() {
        super("Difference",
                new Event().setName(Event.OUTPUT)
                        .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofRef(Namespaces.DATE + ".Duration"))),
                PARAMETER_TIMESTAMP_ONE,
                PARAMETER_TIMESTAMP_TWO,
                PARAMETER_VARIABLE_UNIT);
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        final String timeStampOneString = context.getArguments().get(PARAMETER_TIMESTAMP_NAME_ONE).getAsString();
        final String timeStampTwoString = context.getArguments().get(PARAMETER_TIMESTAMP_NAME_TWO).getAsString();

        final ZonedDateTime timeStampOne = DateUtil.getDateTime(timeStampOneString);
        final ZonedDateTime timeStampTwo = DateUtil.getDateTime(timeStampTwoString);

        List<DateUnit> units = context.getArguments().get(PARAMETER_UNIT_NAME).getAsJsonArray().asList()
                .stream()
                .map(e -> DateUnit.valueOf(e.getAsString().toUpperCase()))
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());

        JsonObject durationObject = new JsonObject();

        if (units.isEmpty()) {
            units = List.of(DateUnit.MILLISECONDS);
        }

        Duration duration = Duration.between(timeStampTwo, timeStampOne);
        long millis = duration.toMillis();
        int factor = millis < 0 ? -1 : 1;
        millis = millis * factor;

        for (int i = 0; i < units.size(); i++) {
            DateUnit unit = units.get(i);
            if (millis < unit.getDuration().toMillis())
                continue;

            double value = ((double) millis) / unit.getDuration().toMillis();
            if (i + 1 != units.size()) {
                value = Math.floor(value);
                durationObject.addProperty(unit.name().toLowerCase(), ((long) value) * factor);
                millis = millis - ((long) value) * unit.getDuration().toMillis();
            } else {
                durationObject.addProperty(unit.name().toLowerCase(), value * factor);
                millis -= value;
            }
        }

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, durationObject)))));
    }
}

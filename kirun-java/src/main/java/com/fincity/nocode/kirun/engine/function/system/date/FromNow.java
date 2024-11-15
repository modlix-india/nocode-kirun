package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class FromNow extends AbstractDateFunction {

    public static final String PARAMETER_BASE_NAME = "base";
    public static final Parameter PARAMETER_BASE = new Parameter()
            .setParameterName(PARAMETER_BASE_NAME)
            .setSchema(Schema.ofRef(Namespaces.DATE + ".Timestamp").setDefaultValue(new JsonPrimitive("")));

    public static final String PARAMETER_LOCALE_NAME = "locale";
    public static final Parameter PARAMETER_LOCALE = new Parameter()
            .setParameterName(PARAMETER_LOCALE_NAME)
            .setSchema(Schema.ofString(PARAMETER_LOCALE_NAME).setDefaultValue(new JsonPrimitive("system")));

    public static final String PARAMETER_FORMAT_NAME = "format";
    public static final Parameter PARAMETER_FORMAT = new Parameter()
            .setParameterName(PARAMETER_FORMAT_NAME)
            .setSchema(Schema.ofString(PARAMETER_FORMAT_NAME)
                    .setEnums(
                            List.of(new JsonPrimitive("LONG"), new JsonPrimitive("SHORT"),
                                    new JsonPrimitive("NARROW")))
                    .setDefaultValue(new JsonPrimitive("LONG")));

    public static final String PARAMETER_ROUND_NAME = "round";
    public static final Parameter PARAMETER_ROUND = new Parameter()
            .setParameterName(PARAMETER_ROUND_NAME)
            .setSchema(Schema.ofBoolean(PARAMETER_ROUND_NAME).setDefaultValue(new JsonPrimitive(true)));

    public FromNow() {
        super(
                "FromNow",
                EVENT_STRING,
                PARAMETER_TIMESTAMP,
                PARAMETER_FORMAT,
                PARAMETER_BASE,
                PARAMETER_VARIABLE_UNIT,
                PARAMETER_ROUND,
                PARAMETER_LOCALE);
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        final JsonElement base = context.getArguments().get(PARAMETER_BASE_NAME);
        final ZonedDateTime baseDate = (base.isJsonPrimitive() && (base.getAsString().isEmpty()))
                ? ZonedDateTime.now()
                : ZonedDateTime.from(DateUtil.getDateTime(base.getAsString()));
        final JsonElement given = context.getArguments().get(PARAMETER_TIMESTAMP_NAME);
        final ZonedDateTime givenDate = DateUtil.getDateTime(given.getAsString());

        final List<ChronoUnit> units = context.getArguments().get(PARAMETER_UNIT_NAME).getAsJsonArray().asList()
                .stream()
                .map(e -> ChronoUnit.valueOf(e.getAsString())).toList();
        final String format = context.getArguments().get(PARAMETER_FORMAT_NAME).toString().toLowerCase();
        final boolean round = context.getArguments().get(PARAMETER_ROUND_NAME).getAsBoolean();

        return Mono.just(new FunctionOutput(List.of(EventResult
                .outputOf(Map.of(EVENT_RESULT_NAME,
                        new JsonPrimitive(DateUtil.toRelative(baseDate, givenDate, units, round,
                                format)))))));
    }
}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetNames extends AbstractDateFunction {
    public static final String EVENT_NAMES_NAME = "names";
    public static final String PARAMETER_LOCALE_NAME = "locale";

    public GetNames() {
        super(
                "GetNames",
                new Event()
                        .setName(EVENT_NAMES_NAME)
                        .setParameters(Map.of(
                                EVENT_NAMES_NAME,
                                Schema.ofArray(
                                        EVENT_NAMES_NAME,
                                        Schema.ofString(EVENT_NAMES_NAME)))),
                new Parameter()
                        .setParameterName(PARAMETER_UNIT_NAME)
                        .setSchema(Schema.ofString(PARAMETER_UNIT_NAME)
                                .setEnums(List.of(
                                        new JsonPrimitive("TIMEZONES"),
                                        new JsonPrimitive("MONTHS"),
                                        new JsonPrimitive("WEEKDAYS")))),
                new Parameter()
                        .setParameterName(PARAMETER_LOCALE_NAME)
                        .setSchema(Schema.ofString(PARAMETER_LOCALE_NAME)
                                .setDefaultValue(new JsonPrimitive("system"))));
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String unit = context.getArguments().get(PARAMETER_UNIT_NAME).getAsString();
        String locale = context.getArguments().get(PARAMETER_LOCALE_NAME).getAsString();

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_NAMES_NAME,
                this.getNames(unit, locale))))));
    }

    private JsonElement getNames(String unit, String locale) {

        JsonElement names = new JsonArray();

        switch (unit) {
            case "TIMEZONES" -> ZoneId.getAvailableZoneIds().stream().map(ZoneId::of)
                    .map(e -> e.getDisplayName(TextStyle.FULL, Locale.forLanguageTag(locale)))
                    .forEach(names.getAsJsonArray()::add);
            case "MONTHS" -> List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).stream()
                    .map(e -> ZonedDateTime.now().withMonth(e).format(DateTimeFormatter.ofPattern("MMMM")))
                    .forEach(names.getAsJsonArray()::add);
            case "WEEKDAYS" -> List.of(1, 2, 3, 4, 5, 6, 7).stream()
                    .map(e -> ZonedDateTime.now().with(ChronoField.DAY_OF_WEEK, e)
                            .format(DateTimeFormatter.ofPattern("EEEE")))
                    .forEach(names.getAsJsonArray()::add);
            default -> {
                names = JsonNull.INSTANCE;
            }
        }

        return names;
    }
}

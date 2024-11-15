package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;

import static com.fincity.nocode.kirun.engine.function.system.date.DateUtil.*;

import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            Map.entry("EpochSecondsToTimestamp", new EpochToTimestamp("EpochSecondsToTimestamp", true)),
            Map.entry("EpochMillisecondsToTimestamp", new EpochToTimestamp("EpochMillisecondsToTimestamp", false)),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetDay",
                    isoTimestamp -> getDateTime(isoTimestamp).getDayOfMonth()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetDayOfWeek",
                    isoTimestamp -> getDateTime(isoTimestamp).getDayOfWeek().getValue()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetMonth",
                    isoTimestamp -> getDateTime(isoTimestamp).getMonthValue()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetYear",
                    isoTimestamp -> getDateTime(isoTimestamp).getYear()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetHours",
                    isoTimestamp -> getDateTime(isoTimestamp).getHour()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetMinutes",
                    isoTimestamp -> getDateTime(isoTimestamp).getMinute()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetSeconds",
                    isoTimestamp -> getDateTime(isoTimestamp).getSecond()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetMilliseconds",
                    isoTimestamp -> getDateTime(isoTimestamp).get(ChronoField.MILLI_OF_SECOND)),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetDaysInMonth",
                    isoTimestamp -> (int) getDateTime(isoTimestamp).range(ChronoField.DAY_OF_MONTH).getMaximum()),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetDaysInYear",
                    isoTimestamp -> (int) getDateTime(isoTimestamp).range(ChronoField.DAY_OF_YEAR).getMaximum()),
            Map.entry("TimestampToEpochSeconds", new TimestampToEpoch("TimestampToEpochSeconds", true)),
            Map.entry("TimestampToEpochMilliseconds", new TimestampToEpoch("TimestampToEpochMilliseconds", false)),
            AbstractDateFunction.ofEntryTimestampAndStringOutput(
                    "GetTimeZoneName",
                    isoTimestamp -> getDateTime(isoTimestamp).getZone().getDisplayName(TextStyle.FULL,
                            Locale.getDefault())),
            AbstractDateFunction.ofEntryTimestampAndStringOutput(
                    "GetTimeZoneOffsetLong",
                    isoTimestamp -> getDateTime(isoTimestamp).getZone().getDisplayName(TextStyle.FULL_STANDALONE,
                            Locale.getDefault())),
            AbstractDateFunction.ofEntryTimestampAndStringOutput(
                    "GetTimeZoneOffsetShort",
                    isoTimestamp -> getDateTime(isoTimestamp).getZone().getDisplayName(TextStyle.SHORT_STANDALONE,
                            Locale.getDefault())),
            AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
                    "GetTimeZoneOffset",
                    isoTimestamp -> getDateTime(isoTimestamp).getOffset().get(ChronoField.OFFSET_SECONDS) / 60),
            Map.entry("AddTime", new AddSubtractTime(true)),
            Map.entry("SubtractTime", new AddSubtractTime(false)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetDay",
                    (isoTimestamp, day) -> getDateTime(isoTimestamp)
                            .with(ChronoField.DAY_OF_MONTH, day)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetMonth",
                    (isoTimestamp, month) -> getDateTime(isoTimestamp)
                            .with(ChronoField.MONTH_OF_YEAR, month)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetYear",
                    (isoTimestamp, year) -> getDateTime(isoTimestamp)
                            .with(ChronoField.YEAR, year)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetHours",
                    (isoTimestamp, hour) -> getDateTime(isoTimestamp)
                            .with(ChronoField.HOUR_OF_DAY, hour)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetMinutes",
                    (isoTimestamp, minute) -> getDateTime(isoTimestamp)
                            .with(ChronoField.MINUTE_OF_HOUR, minute)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetSeconds",
                    (isoTimestamp, second) -> getDateTime(isoTimestamp)
                            .with(ChronoField.SECOND_OF_MINUTE, second)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
                    "SetMilliseconds",
                    (isoTimestamp, millisecond) -> getDateTime(isoTimestamp)
                            .with(ChronoField.MILLI_OF_SECOND, millisecond)
                            .format(DateUtil.ISO_DATE_TIME_FORMATTER)),

            AbstractDateFunction.<JsonPrimitive>ofEntryTimestampTimestampAndTOutput(
                    "IsBefore",
                    new Event()
                            .setName(Event.OUTPUT)
                            .setParameters(Map.of(
                                    AbstractDateFunction.EVENT_RESULT_NAME,
                                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME))),
                    (t1, t2, params) -> new JsonPrimitive(getDateTime(t1).isBefore(getDateTime(t2)))),

            AbstractDateFunction.<JsonPrimitive>ofEntryTimestampTimestampAndTOutput(
                    "IsAfter",
                    new Event()
                            .setName(Event.OUTPUT)
                            .setParameters(Map.of(
                                    AbstractDateFunction.EVENT_RESULT_NAME,
                                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME))),
                    (t1, t2, params) -> new JsonPrimitive(getDateTime(t1).isAfter(getDateTime(t2)))),

            AbstractDateFunction.<JsonPrimitive>ofEntryTimestampTimestampAndTOutput(
                    "IsSame",
                    new Event()
                            .setName(Event.OUTPUT)
                            .setParameters(Map.of(

                                    AbstractDateFunction.EVENT_RESULT_NAME,
                                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME))),
                    (t1, t2, params) -> new JsonPrimitive(getDateTime(t1).isEqual(getDateTime(t2)))),

            AbstractDateFunction.<JsonPrimitive>ofEntryTimestampTimestampAndTOutput(
                    "IsSameOrBefore",
                    new Event()
                            .setName(Event.OUTPUT)
                            .setParameters(Map.of(
                                    AbstractDateFunction.EVENT_RESULT_NAME,
                                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME))),
                    (t1, t2, params) -> {
                        ZonedDateTime d1 = getDateTime(t1);
                        ZonedDateTime d2 = getDateTime(t2);
                        return new JsonPrimitive(d1.isBefore(d2) || d1.isEqual(d2));
                    }),

            AbstractDateFunction.<JsonPrimitive>ofEntryTimestampTimestampAndTOutput(
                    "IsSameOrAfter",
                    new Event()
                            .setName(Event.OUTPUT)
                            .setParameters(Map.of(
                                    AbstractDateFunction.EVENT_RESULT_NAME,
                                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME))),
                    (t1, t2, params) -> {
                        ZonedDateTime d1 = getDateTime(t1);
                        ZonedDateTime d2 = getDateTime(t2);
                        return new JsonPrimitive(d1.isAfter(d2) || d1.isEqual(d2));
                    }),

            AbstractDateFunction.ofEntryTimestampAndBooleanOutput(
                    "IsInLeapYear",
                    isoTimestamp -> getDateTime(isoTimestamp).getYear() % 4 == 0
                            && (getDateTime(isoTimestamp).getYear() % 100 != 0
                                    || getDateTime(isoTimestamp).getYear() % 400 == 0)),

            AbstractDateFunction.ofEntryTimestampAndBooleanOutput(
                    "IsInDST",
                    isoTimestamp -> getDateTime(isoTimestamp).getZone().getRules()
                            .isDaylightSavings(getDateTime(isoTimestamp).toInstant())),

            Map.entry("LastOf", new LastFirstOf(true)),
            Map.entry("FirstOf", new LastFirstOf(false)),
            Map.entry("StartOf", new StartEndOf(true)),
            Map.entry("EndOf", new StartEndOf(false)),
            Map.entry("TimeAsObject", new TimeAs(false)),
            Map.entry("TimeAsArray", new TimeAs(true))

    );

    private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
            .stream()
            .map(ReactiveFunction::getSignature)
            .map(FunctionSignature::getFullName)
            .toList();

    @Override
    public Mono<ReactiveFunction> find(String namespace, String name) {
        if (!namespace.equals(Namespaces.DATE))
            return Mono.empty();
        return Mono.justOrEmpty(REPO_MAP.get(name));
    }

    @Override
    public Flux<String> filter(String name) {
        return Flux.fromIterable(FILTERABLE_NAMES)
                .filter(e -> e.toLowerCase()
                        .contains(name.toLowerCase()));
    }
}

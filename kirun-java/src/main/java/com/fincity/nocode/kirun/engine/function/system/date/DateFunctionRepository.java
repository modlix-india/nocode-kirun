package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.DAY;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.HOUR;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MILLIS;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MINUTE;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MONTH;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.SECOND;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.YEAR;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetDate",
                    "date",

                    (inputDate) -> {

                        DateTimeFormatter dtf = DateTimePatternUtil.getPattern();
                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, dtf);

                        System.out.println(zdt);
                        return zdt.getDayOfMonth();

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithLongOutput("GetTime", "isoDate",

                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).toInstant()
                            .toEpochMilli()

            ),

            AbstractDateFunction.ofEntryDateAndLongAndUnitAndDateOutput("AddTime", "isoDate", "add", "unit",

                    (inputDate, amount, unit) -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

                        zdt = addUnit(zdt, unit, amount);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]");

                        return zdt.format(formatter);
                    }

            ),
            AbstractDateFunction.ofEntryDateAndLongAndUnitAndDateOutput("SubtractTime", "isoDate", "subtract", "unit",

                    (inputDate, amount, unit) -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

                        zdt = subtractUnit(zdt, unit, amount);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]");

                        return zdt.format(formatter);
                    }

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetFullYear", "isoDate", "yearValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, YEAR, amount)

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetDate", "isoDate", "dateValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, DAY, amount)

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetMonth", "isoDate", "monthValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, MONTH, amount)),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetHours", "isoDate", "hourValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, HOUR, amount)

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetMinutes", "isoDate", "minuteValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, MINUTE, amount)

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetSeconds", "isoDate", "secondValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, SECOND, amount)),

            AbstractDateFunction.ofEntryDateAndIntegerAndIntegerOutput("SetMilliSeconds", "isoDate", "milliValue",

                    (inputDate, amount) -> setMethodAndGetValue(inputDate, MILLIS, amount)

            ),

            AbstractDateFunction.ofEntryDateWithOutputBoolean("IsLeapYear", "isoDate",
                    inputDate -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());
                        int year = zdt.getYear();

                        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;

                    }

            ),

            AbstractDateFunction.ofEntryDateAndUnitAndDateOutput("GetStartOfTimeStamp", "isoDate", "unit",

                    (inputDate, unit) -> {

                        ZonedDateTime zdt = startOfTimeStamp(inputDate, unit);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]");

                        System.out.println(zdt.format(formatter));
                        return zdt.format(formatter);
                    }

            ),

            AbstractDateFunction.ofEntryTwoDateAndBooleanOutput("IsBefore", "isoDate1", "isoDate2", "unit",

                    (firstDate, secondDate, units) -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(firstDate, DateTimePatternUtil.getPattern());

                        System.out.println(ZonedDateTime.parse(firstDate, DateTimePatternUtil.getPattern()));
                        System.err.println(ZonedDateTime.parse(secondDate, DateTimePatternUtil.getPattern()));

                        boolean result = zdt
                                .isBefore(ZonedDateTime.parse(secondDate, DateTimePatternUtil.getPattern()));

                        System.out.println(result);

                        return true;
                    }

            )

    );

    private static final int setMethodAndGetValue(String inputDate, String unit, int amount) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        switch (unit) {

            case YEAR:
                return zdt.withYear(amount).getYear();

            case MONTH:
                return zdt.withMonth(amount).getMonthValue();

            case DAY:
                return zdt.withDayOfMonth(amount).getDayOfMonth();

            case HOUR:
                return zdt.withHour(amount).getHour();

            case MINUTE:
                return zdt.withMinute(amount).getMinute();

            case SECOND:
                return zdt.withSecond(amount).getSecond();

            case MILLIS:
                return zdt.with(ChronoField.MILLI_OF_SECOND, amount).get(ChronoField.MILLI_OF_SECOND);

            default:
                return -1;

        }

    }

    private static final ZonedDateTime addUnit(ZonedDateTime zdt, String unit, Long amount) {

        switch (unit) {

            case YEAR:
                return zdt.plusYears(amount);

            case MONTH:
                return zdt.plusMonths(amount);

            case DAY:
                return zdt.plusDays(amount);

            case HOUR:
                return zdt.plusHours(amount);

            case MINUTE:
                return zdt.plusMinutes(amount);

            case SECOND:
                return zdt.plusSeconds(amount);

            case MILLIS:
                return zdt.plus(amount, ChronoUnit.MILLIS);

            default:
                return zdt;
        }

    }

    private static final ZonedDateTime subtractUnit(ZonedDateTime zdt, String unit, Long amount) {

        switch (unit) {

            case YEAR:
                return zdt.minusYears(amount);

            case MONTH:
                return zdt.minusMonths(amount);

            case DAY:
                return zdt.minusDays(amount);

            case HOUR:
                return zdt.minusHours(amount);

            case MINUTE:
                return zdt.minusMinutes(amount);

            case SECOND:
                return zdt.minusSeconds(amount);

            case MILLIS:
                return zdt.minus(amount, ChronoUnit.MILLIS);

            default:
                return zdt;
        }

    }

    private static final ZonedDateTime startOfTimeStamp(String inputDate, String unit) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        switch (unit) {

            case YEAR:

                return zdt.withDayOfYear(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case MONTH:
                return zdt.withDayOfMonth(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case DAY:
                return zdt.truncatedTo(ChronoUnit.DAYS);

            case HOUR:
                return zdt.truncatedTo(ChronoUnit.HOURS);

            case MINUTE:
                return zdt.truncatedTo(ChronoUnit.MINUTES);

            case SECOND:
                return zdt.truncatedTo(ChronoUnit.SECONDS);

            case MILLIS:
                return zdt.truncatedTo(ChronoUnit.MILLIS);

            default:
                return zdt;
        }

    }

    private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
            .stream()
            .map(ReactiveFunction::getSignature)
            .map(FunctionSignature::getFullName)
            .toList();

    @Override
    public Mono<ReactiveFunction> find(String namespace, String name) {

        if (!namespace.equals(Namespaces.DATE))
            return Mono.empty();

        return Mono.just(REPO_MAP.get(name));
    }

    @Override
    public Flux<String> filter(String name) {

        return Flux.fromIterable(FILTERABLE_NAMES)
                .filter(e -> e.toLowerCase()
                        .indexOf(name.toLowerCase()) != -1);
    }

}

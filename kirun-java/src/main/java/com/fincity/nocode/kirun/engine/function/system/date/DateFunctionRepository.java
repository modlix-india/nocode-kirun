package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.DAY;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.HOUR;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MILLIS;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MINUTE;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MONTH;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.SECOND;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.YEAR;
import static com.fincity.nocode.kirun.engine.util.date.AdjustTimeStampUtil.endOfGivenField;
import static com.fincity.nocode.kirun.engine.util.date.AdjustTimeStampUtil.startOfTimeStamp;
import static com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil.validate;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.DateCompareUtil;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.SetDateFunctionsUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final String UNIT = "unit";

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetDay", AbstractDateFunction.ISO_DATE,

                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getDayOfWeek()
                            .getValue() % 7 // Sunday - 0 to Saturday - 6 to match with javascript

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetFullYear", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getYear()

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetMonth", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getMonthValue() - 1

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetDate", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getDayOfMonth()

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetHours", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getHour()

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetMinutes", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getMinute()

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetSeconds", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getSecond()

            ),

            AbstractDateFunction.ofEntryDateWithIntegerOutput("GetMilliSeconds", AbstractDateFunction.ISO_DATE,
                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern())
                            .get(ChronoField.MILLI_OF_SECOND)

            ),

            AbstractDateFunction.ofEntryDateWithLongOutput("GetTime", AbstractDateFunction.ISO_DATE,

                    inputDate -> ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).toInstant()
                            .toEpochMilli()

            ),

            AbstractDateFunction.ofEntryDateAndLongAndUnitAndDateOutput("AddTime", AbstractDateFunction.ISO_DATE, "add",
                    UNIT,

                    (inputDate, amount, unit) -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

                        zdt = addUnit(zdt, unit, amount);

                        return zdt.format(DateTimePatternUtil.getPattern());
                    }

            ),
            AbstractDateFunction.ofEntryDateAndLongAndUnitAndDateOutput("SubtractTime", AbstractDateFunction.ISO_DATE,
                    "subtract", UNIT,

                    (inputDate, amount, unit) -> {

                        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

                        zdt = subtractUnit(zdt, unit, amount);

                        return zdt.format(DateTimePatternUtil.getPattern());
                    }

            ),

            AbstractDateFunction.ofEntryDateAndIntegerAndStringOutput("SetFullYear", AbstractDateFunction.ISO_DATE,
                    "yearValue",

                    (inputDate, year) -> {

                        String ou = SetDateFunctionsUtil.setFullYear(inputDate, year);
                        System.out.println(ou);
                        return ou;
                    }

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

            AbstractDateFunction.ofEntryDateWithOutputBoolean("IsLeapYear", AbstractDateFunction.ISO_DATE,
                    inputDate -> {

                        int year = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern()).getYear();
                        return ValidDateTimeUtil.isLeapYear(year);
                    }

            ), // check leap year

            AbstractDateFunction.ofEntryDateAndUnitAndDateOutput("GetStartOfTimeStamp", "isoDate", UNIT,

                    (inputDate, unit) -> {

                        ZonedDateTime zdt = startOfTimeStamp(inputDate, unit);

                        return zdt.format(DateTimePatternUtil.getPattern());
                    }

            ),

            AbstractDateFunction.ofEntryDateAndUnitAndDateOutput("GetEndOfTimeStamp", "isoDate", UNIT,

                    (inputDate, unit) -> {

                        ZonedDateTime zdt = endOfGivenField(inputDate, unit);

                        return zdt.format(DateTimePatternUtil.getPattern());
                    }

            ),

            AbstractDateFunction.ofEntryTwoDateAndBooleanOutput("IsAfter", AbstractDateFunction.ISO_DATE1,
                    AbstractDateFunction.ISO_DATE2, UNIT,

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "after", fields)

            ),

            AbstractDateFunction.ofEntryTwoDateAndBooleanOutput("IsBefore", AbstractDateFunction.ISO_DATE1,
                    AbstractDateFunction.ISO_DATE2, UNIT,

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "before", fields)

            ),

            AbstractDateFunction.ofEntryTwoDateAndBooleanOutput("IsSame", AbstractDateFunction.ISO_DATE1,
                    AbstractDateFunction.ISO_DATE2, UNIT,

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "same", fields)

            ),

            AbstractDateFunction.ofEntryThreeDateAndBooleanOutput("InBetween", AbstractDateFunction.ISO_DATE1,
                    AbstractDateFunction.ISO_DATE2, "betweenDate", UNIT, DateCompareUtil::inBetween)

    );

    private static final int setMethodAndGetValue(String inputDate, String unit, int amount) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        switch (unit) {

            case YEAR:

                try {

                    zdt = zdt.withYear(amount);

                    if (validate(zdt.format(DateTimePatternUtil.getPattern())))

                        return zdt.getYear();

                    throw new KIRuntimeException("Please provide valid value for year.");

                } catch (DateTimeException ex) {

                    throw new KIRuntimeException("Please provide valid value for year.");
                }

            case MONTH:

                try {
                    zdt = zdt.withMonth(amount);

                    if (validate(zdt.format(DateTimePatternUtil.getPattern(inputDate))))

                        return zdt.getMonthValue();

                    throw new KIRuntimeException("Please provide a valid month number");

                } catch (DateTimeException ex) {

                    throw new KIRuntimeException("Please provide a valid month number");
                }

            case DAY:

                try {
                    zdt = zdt.withDayOfMonth(amount);

                    if (validate(zdt.format(DateTimePatternUtil.getPattern(inputDate))))

                        return zdt.getDayOfMonth();

                    throw new KIRuntimeException("Please provide a valid month number");

                } catch (DateTimeException ex) {

                    throw new KIRuntimeException("Please provide a valid month number");
                }

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

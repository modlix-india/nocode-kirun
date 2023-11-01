package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;
import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.DateCompareUtil;
import com.fincity.nocode.kirun.engine.util.date.DurationUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetDate",
                    "date",
                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.DATE);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetDay", "day",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.DAY_OF_WEEK) - 1; // 0 - Sunday to 6- Saturday

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetFullYear", "year",

                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.YEAR);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetMonth", "month",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.MONTH); // 0- January to 11- December

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetHours", "hours",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.HOUR_OF_DAY);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetMinutes", "minutes",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.MINUTE);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetSeconds", "seconds",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.SECOND);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetMilliSeconds", "milliSeconds",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getRequiredField(inputDate, Calendar.MILLISECOND);

                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetTime", "time",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return getEpochTime(inputDate);

                    }, SchemaType.LONG),

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetTimeZoneOffset", "timeZoneOffset",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        sdf.format(new Date());

                        return 0;

                    }, SchemaType.LONG),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputDate("SetTime", "timeValue", "date",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                        System.out
                                .println(dtf.format(Instant.ofEpochMilli(getEpochTime(inputDate) + value.longValue())));

                        return dtf.format(Instant.ofEpochMilli(getEpochTime(inputDate) + value.longValue()));

                    }, SchemaType.LONG, SchemaType.STRING),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetDate", "dateValue", "date",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.DATE, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetFullYear", "yearValue", "year",

                    (inputDate, value) ->

                    {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.YEAR, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetMonth", "monthValue", "month",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return setAndFetchCalendarField(inputDate, Calendar.MONTH, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetHours", "hourValue", "hour",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.HOUR_OF_DAY, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetMinutes", "minuteValue", "minute",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.MINUTE, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetSeconds", "secondValue", "second",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.SECOND, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndIntegerWithOutputInteger("SetMilliSeconds", "milliSecondValue",
                    "milliSecond",

                    (inputDate, value) -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return setAndFetchCalendarField(inputDate, Calendar.MILLISECOND, value.intValue());

                    },

                    SchemaType.INTEGER, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateAndBooleanWithOutputName("IsDST", "dst",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        return false;

                    }, SchemaType.BOOLEAN),

            AbstractDateFunction.ofEntryDateAndBooleanWithOutputName("IsLeapYear", "leap",

                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        int year = getRequiredField(inputDate, Calendar.YEAR);

                        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;

                    }, SchemaType.BOOLEAN),

            AbstractDateFunction.ofEntryDateAndBooleanSuffixWithStringOutput("FromNow", "suffix",

                    (inputDate, suffix) -> fetchDuration(inputDate, suffix, false)

            ),

            AbstractDateFunction.ofEntryDateAndBooleanSuffixWithStringOutput("ToNow", "suffix",

                    (inputDate, suffix) -> fetchDuration(inputDate, suffix, true)

            ),

            AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput("IsSame",

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "same", fields)

            ),

            AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput("IsBefore",

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "before", fields)

            ),

            AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput("IsAfter",

                    (firstDate, secondDate, fields) -> DateCompareUtil.compare(firstDate, secondDate, "after", fields)

            ),

            AbstractCompareDateFunction.ofEntryThreeDateAndBooleanOutput("InBetween", "betweenDate",
                    DateCompareUtil::inBetween)

    );

    private static int setAndFetchCalendarField(String inputDate, int field, int value) {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date(getEpochTime(inputDate)));
        cal.set(field, value);

        return cal.get(field);
    }

    private static int getRequiredField(String inputDate, int field) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(getEpochTime(inputDate)));
        return cal.get(field);
    }

    private static String fetchDuration(String date, boolean suffix, boolean toNow) {

        Duration dur = Duration.between(
                Instant.ofEpochMilli(GetTimeInMillisUtil.getEpochTime(date)),
                Instant.ofEpochMilli(Calendar.getInstance().getTimeInMillis()));

        String output = DurationUtil.getDuration(Math.abs(dur.toDays()), Math.abs(dur.toHours()),
                Math.abs(dur.toMinutes()),
                Math.abs(dur.toSeconds()));

        if (suffix)
            return output;

        if (toNow)
            return dur.isNegative() ? output + " ago" : "In " + output;

        return dur.isNegative() ? "In " + output : output + " ago";
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

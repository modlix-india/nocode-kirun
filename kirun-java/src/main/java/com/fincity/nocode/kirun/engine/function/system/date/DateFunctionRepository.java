package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;
import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            AbstractDateFunction.ofEntryDateAndStringWithOutputName("GetDate",
                    "date",
                    inputDate -> {

                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();

                        Date updatedDate = new Date(getEpochTime(inputDate));

                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                        cal.setTime(updatedDate);

                        return cal.get(Calendar.DATE);

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

                        Calendar cal = Calendar.getInstance();

                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        sdf.format(new Date());

                        return 0;

                    }, SchemaType.LONG),

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

                        return false;

                    }, SchemaType.BOOLEAN));

    private static int getRequiredField(String inputDate, int field) {

        Date updatedDate = new Date(getEpochTime(inputDate));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(updatedDate);
        return cal.get(field);

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

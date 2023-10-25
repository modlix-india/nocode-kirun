package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

    private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

            AbstractDateFunction.ofEntryDateWithOutputName("GetDate",
                    "date",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(3));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetDay", "day", inputDate -> {
                Matcher matcher = dateTimePattern.matcher(inputDate);
                matcher.matches();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                try {
                    Date dt = sdf.parse(inputDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    cal.setTime(dt);
                    System.out.println(cal.get(Calendar.DAY_OF_WEEK));
                    return cal.get(Calendar.DAY_OF_WEEK) - 1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                throw new KIRuntimeException("Please provide the valid iso date.");

            }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetFullYear", "year",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(1));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetHours", "hours",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(4));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetMinutes", "minutes",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(5));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetSeconds", "seconds",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(6));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetMilliSeconds", "milliSeconds",
                    inputDate -> {
                        Matcher matcher = dateTimePattern.matcher(inputDate);
                        matcher.matches();
                        return Integer.parseInt(matcher.group(7).substring(1));
                    }, SchemaType.INTEGER),

            AbstractDateFunction.ofEntryDateWithOutputName("GetTime", "time",
                    inputDate -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        try {
                            Date dt = sdf.parse(inputDate);
                            return Long.valueOf(dt.getTime());

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        throw new KIRuntimeException("Please provide the valid iso date.");

                    }, SchemaType.LONG)

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

        return Mono.just(REPO_MAP.get(name));
    }

    @Override
    public Flux<String> filter(String name) {

        return Flux.fromIterable(FILTERABLE_NAMES)
                .filter(e -> e.toLowerCase()
                        .indexOf(name.toLowerCase()) != -1);
    }

}

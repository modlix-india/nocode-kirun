package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

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

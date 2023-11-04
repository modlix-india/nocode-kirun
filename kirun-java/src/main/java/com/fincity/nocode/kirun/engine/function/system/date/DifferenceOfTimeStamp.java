package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil.checkValidity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class DifferenceOfTimeStamp extends AbstractReactiveFunction {

    private static final String ISO_DATE_1 = "isoDate1";
    private static final String ISO_DATE_2 = "isoDate2";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("DifferenceOfTimeStamp")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(Parameter.ofEntry(ISO_DATE_1, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                                Parameter.ofEntry(ISO_DATE_2, Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofLong(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String firstDate = context.getArguments().get(ISO_DATE_1).getAsString();

        String secondDate = context.getArguments().get(ISO_DATE_2).getAsString();

        if (!checkValidity(firstDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE_1);

        if (!checkValidity(secondDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE_2);

        ZonedDateTime zdt1 = ZonedDateTime.parse(firstDate, DateTimePatternUtil.getPattern());

        ZonedDateTime zdt2 = ZonedDateTime.parse(secondDate, DateTimePatternUtil.getPattern());

        long diff = zdt1.toInstant().toEpochMilli() - zdt2.toInstant().toEpochMilli();

        return Mono.just(
                new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(diff))))));
    }

}

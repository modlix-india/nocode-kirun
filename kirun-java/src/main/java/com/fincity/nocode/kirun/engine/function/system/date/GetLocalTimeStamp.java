package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Map;
import java.util.regex.Matcher;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;

import reactor.core.publisher.Mono;

public class GetLocalTimeStamp extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isodate";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("GetLocalTimeStamp")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(
                                Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        Matcher matcher = dateTimePattern.matcher(inputDate);

        if (!matcher.find())
            throw new KIRuntimeException("Please provide the valid iso format");

        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(GetTimeInMillisUtil.getEpochTime(inputDate)),
                ZoneId.systemDefault());

        System.out.println(ldt.toLocalDate());

        System.out.println(ldt.toString());

        return null;
    }

}

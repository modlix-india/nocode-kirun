package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class FromDateString extends AbstractReactiveFunction {

    private static final String DATE = "date";

    private static final String ISO_DATE = "isodate";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("FromDateString")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DATE, new Parameter()
                        .setParameterName(DATE).setSchema(Schema.ofString(DATE))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Given String is not convertable to ISO date format");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {

            df.parse(inputDate);

            return Mono
                    .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(ISO_DATE, new JsonPrimitive(""))))));
        } catch (ParseException e) {
            throw new KIRuntimeException("Given String is not convertable to ISO date format");
        }
    }

}

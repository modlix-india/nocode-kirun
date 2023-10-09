package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

//check wheher is it possible to improve program

public class EpochToDate extends AbstractReactiveFunction {

    private static final String EPOCH = "epoch";

    private static final String OUTPUT = "date";

    private static final String ERROR_MSG = "Please provide a valid value for epoch.";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("EpochToDate")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(

                        EPOCH,
                        Parameter.of(EPOCH,

                                new Schema()
                                        .setOneOf(List.of(
                                                Schema.ofLong(EPOCH),
                                                Schema.ofString(EPOCH))))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + "timestamp")))));

    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var epochIp = context.getArguments().get(EPOCH);

        try {
            if (epochIp.isJsonPrimitive()) {

                JsonPrimitive epochPrimitive = epochIp.getAsJsonPrimitive();

                if (epochPrimitive.isBoolean())
                    throw new KIRuntimeException(ERROR_MSG);

                Long longDate = epochPrimitive.isNumber() ? epochPrimitive.getAsLong()
                        : Long.parseLong(epochPrimitive.getAsString());

                Date dt = longDate > 999999999999L ? new Date(longDate) : new Date(longDate * 1000);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                TimeZone tz = TimeZone.getTimeZone("UTC");
                df.setTimeZone(tz);

                return Mono.just(
                        new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(df.format(dt)))))));

            }

            throw new KIRuntimeException(ERROR_MSG);

        } catch (NumberFormatException nfe) {

            throw new KIRuntimeException(ERROR_MSG);

        }

    }

}

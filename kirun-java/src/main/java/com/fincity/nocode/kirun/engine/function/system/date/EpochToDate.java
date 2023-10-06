package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class EpochToDate extends AbstractReactiveFunction {

    private static final String EPOCH = "epoch";

    private static final String OUTPUT = "date";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("EpochToDate")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(

                        EPOCH,
                        Parameter.of(EPOCH,

                                new Schema()
                                        .setOneOf(List.of(

                                                new Schema().setAnyOf(
                                                        List.of(Schema.ofInteger(EPOCH),
                                                                Schema.ofLong(EPOCH))),

                                                Schema.ofString(EPOCH))))

                ))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + "timestamp")))));

    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var epochIp = context.getArguments().get(EPOCH);

        try {
            if (epochIp.isJsonPrimitive()) {
                JsonPrimitive epochPrimitive = epochIp.getAsJsonPrimitive();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'");

                if (epochPrimitive.isNumber()) {

                    Long longDate = epochPrimitive.getAsLong();

                    Date dt = new Date(longDate * 1000); // multipling with ms

                    return Mono.just(
                            new FunctionOutput(
                                    List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(sdf.format(dt)))))));

                } else if (epochPrimitive.isString()) {

                    Long longDate = Long.parseLong(epochPrimitive.getAsString());
                    Date dt = new Date(longDate * 1000); // multipling with ms

                    return Mono.just(
                            new FunctionOutput(
                                    List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(sdf.format(dt)))))));

                }
            }

        } catch (NumberFormatException nfe) {

            throw new KIRuntimeException("Please provide a valid ");

        }

        throw new KIRuntimeException("Please provide a valid ");

    }

}

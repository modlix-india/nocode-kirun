package com.fincity.nocode.kirun.engine.function.system.date;

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
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonPrimitive;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

import reactor.core.publisher.Mono;

public class DifferenceOfTimestamps extends AbstractReactiveFunction {

    public static final String DateOne = "isoDateOne";
    public static final String DateTwo = "isoDateTwo";
    public static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature()
                .setName("DifferenceOfTimestamps")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DateOne, new Parameter().setParameterName(DateOne)
                        .setSchema(Schema.ofString(DateOne).setRef(Namespaces.DATE + ".timeStamp")),
                        DateTwo, new Parameter().setParameterName(DateTwo)
                        .setSchema(Schema.ofString(DateTwo).setRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofInteger(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
       
        String firstDate = context.getArguments().get(DateOne).getAsString();
        String secondDate = context.getArguments().get(DateTwo).getAsString();

        if(!ValidDateTimeUtil.validate(firstDate) || !ValidDateTimeUtil.validate(secondDate)) 
            throw new KIRuntimeException("Please provide valid ISO date for both the given dates.");

        return Mono.just(new FunctionOutput(List.of(
            EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive((getEpochTime(secondDate) - getEpochTime(firstDate))/60000))))));
    }
}

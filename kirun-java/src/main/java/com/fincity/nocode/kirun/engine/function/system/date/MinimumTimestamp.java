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
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import static com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil.validate;
import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;


import reactor.core.publisher.Mono;

public class MinimumTimestamp extends AbstractReactiveFunction {

    private static final String VALUE = "isoDates";

	private static final String OUTPUT = "result";

    private static final String ERROR_MESSAGE = "Please provide a valid date";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("MinimumTimestamp")
            .setNamespace(Namespaces.DATE)
            .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
            .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp")).setVariableArgument(true)))
            .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT).setRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {


        JsonArray dates = context.getArguments().get(VALUE).getAsJsonArray();

        int size = dates.size();

        if (size == 0)
            throw new KIRuntimeException("Please provide atleast one timestamp for comparing");

        else if (size == 1) {

            String firstDate = dates.get(0).getAsString();

            if (!validate(firstDate))

                throw new KIRuntimeException(ERROR_MESSAGE);

            return Mono.just(
                    new FunctionOutput(List
                            .of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(firstDate))))));
        }

        int minIndex = 0;
        long min = getEpochTime(dates.get(0).getAsString());

        for(int i=1;i<size;i++){

            String date = dates.get(i).getAsString();

            if(!validate(date))
                throw new KIRuntimeException(ERROR_MESSAGE);
            
            long current = getEpochTime(date);

            if(current < min){
                min = current;
                minIndex = i;
            }
        }

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf( Map.of( OUTPUT , new JsonPrimitive(dates.get(minIndex).getAsString()) ) ) )));

    }
    
}

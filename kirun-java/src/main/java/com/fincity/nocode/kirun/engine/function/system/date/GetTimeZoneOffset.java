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
import com.fincity.nocode.kirun.engine.util.date.GetTimeZoneOffsetUtil;
import com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetTimeZoneOffset extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isoDate";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("GetTimeZoneOffset")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofInteger(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        if (!IsValidISODateUtil.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide valid ISO date");


        int offset = GetTimeZoneOffsetUtil.getOffset(inputDate);
  
            return Mono
                    .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(offset))))));

        }

    }



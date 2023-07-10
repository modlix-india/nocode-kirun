package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

public class Matches extends AbstractReactiveFunction {

    protected static final String PARAMETER_REGEX_NAME = "regex";

    protected static final String PARAMETER_STRING_NAME = "string";

    protected static final String EVENT_RESULT_NAME = "result";

    private static final FunctionSignature signature = new FunctionSignature().setName("Matches")
            .setNamespace(Namespaces.STRING)
            .setParameters(
                    Map.ofEntries(
                            Parameter.ofEntry(PARAMETER_REGEX_NAME, Schema.ofString(PARAMETER_REGEX_NAME)),
                            Parameter.ofEntry(PARAMETER_STRING_NAME, Schema.ofString(PARAMETER_STRING_NAME))))
            .setEvents(Map.ofEntries(
                    Event.outputEventMapEntry(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)))));

    @Override
    public FunctionSignature getSignature() {
        return signature;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String regexPat = context.getArguments()
                .get(PARAMETER_REGEX_NAME)
                .getAsJsonPrimitive()
                .getAsString();

        String inputString = context.getArguments()
                .get(PARAMETER_STRING_NAME)
                .getAsJsonPrimitive()
                .getAsString();

        Pattern pattern = Pattern.compile(regexPat, Pattern.MULTILINE);

        return Mono.just(new FunctionOutput(List.of(
                EventResult
                        .outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(pattern.matcher(inputString).find()))))));
    }

}

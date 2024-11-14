package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

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

public class IsValidISODate extends AbstractReactiveFunction {

    private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("IsValidISODate")
            .setNamespace(Namespaces.DATE)
            .setParameters(
                    Map.ofEntries(
                            Parameter.ofEntry(
                                    AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                                    Schema.ofString(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME))))
            .setEvents(
                    Map.ofEntries(
                            Event.outputEventMapEntry(
                                    Map.of(
                                            AbstractDateFunction.EVENT_RESULT_NAME,
                                            Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME)))));

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        String timestamp = context
                .getArguments()
                .get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
                .getAsString();

        ZonedDateTime dt = null;
        try {
            dt = DateUtil.getDateTime(timestamp);
        } catch (Exception e) {
            // ignore
        }

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_RESULT_NAME, new JsonPrimitive(dt != null))))));
    }

    @Override
    public FunctionSignature getSignature() {
        return IsValidISODate.SIGNATURE;
    }
}

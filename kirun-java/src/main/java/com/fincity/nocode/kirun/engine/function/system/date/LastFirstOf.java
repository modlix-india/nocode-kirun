package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class LastFirstOf extends AbstractReactiveFunction {

    private final FunctionSignature signature;
    private final boolean isLast;

    public LastFirstOf(boolean isLast) {

        this.isLast = isLast;
        this.signature = new FunctionSignature().setName(isLast ? "LastOf" : "FirstOf")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(
                                Parameter.ofEntry(
                                        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                                        Schema.ofRef(Namespaces.DATE + ".Timestamp"),
                                        true)))
                .setEvents(Map.of(
                        AbstractDateFunction.EVENT_TIMESTAMP.getName(),
                        AbstractDateFunction.EVENT_TIMESTAMP));
    }

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        List<String> timestamps = context
                .getArguments()
                .get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
                .getAsJsonArray()
                .asList()
                .stream()
                .map(e -> e.getAsString())
                .collect(Collectors.toList());

        if (timestamps.isEmpty()) {
            throw new KIRuntimeException("No timestamps provided");
        }

        List<ZonedDateTime> dateTimes = timestamps.stream()
                .map(DateUtil::getDateTime)
                .collect(Collectors.toList());

        dateTimes.sort(ZonedDateTime::compareTo);

        return Mono.just(new FunctionOutput(List.of(
                EventResult.outputOf(
                        Map.of(
                                AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                                new JsonPrimitive(dateTimes.get(this.isLast ? dateTimes.size() - 1 : 0)
                                        .format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }
}

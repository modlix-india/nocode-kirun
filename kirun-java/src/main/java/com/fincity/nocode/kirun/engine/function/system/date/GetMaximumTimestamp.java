package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetMaximumTimestamp extends AbstractReactiveFunction {

    private static final String ISO_DATES = "isodates";

    private static final String ERROR_MSG = "Please provide valid iso datetime format";

    private static final Schema dateSchema = Schema.ofRef(Namespaces.DATE + ".timeStamp");

    private String functionName;

    private String outputName;

    public GetMaximumTimestamp() {
        this.functionName = "GetMaximumTimestamp";
        this.outputName = "maximum";
    }

    protected GetMaximumTimestamp(String name, String outputName) {
        this.functionName = name;
        this.outputName = outputName;
    }

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName(functionName).setNamespace(Namespaces.DATE)
                .setParameters(Map.of(ISO_DATES, new Parameter().setParameterName(ISO_DATES)
                        .setSchema(dateSchema).setVariableArgument(true)))
                .setEvents(Map.ofEntries(Event
                        .outputEventMapEntry(Map.of(outputName, dateSchema))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        JsonArray dates = context.getArguments().get(ISO_DATES).getAsJsonArray();

        int size = dates.size();

        if (size == 0)
            throw new KIRuntimeException("Please provide atleast one timestamp for comparing");

        else if (size == 1) {

            String firstDate = dates.get(0).getAsString();

            if (!IsValidIsoDateTime.checkValidity(firstDate))

                throw new KIRuntimeException(ERROR_MSG);

            return Mono.just(
                    new FunctionOutput(List
                            .of(EventResult.outputOf(Map.of(outputName, new JsonPrimitive(firstDate))))));
        }

        String max = dates.get(0).getAsString();

        if (!IsValidIsoDateTime.checkValidity(max))

            throw new KIRuntimeException(ERROR_MSG);

        for (int i = 1; i < size; i++) {

            String currentDate = dates.get(i).getAsString();

            if (!IsValidIsoDateTime.checkValidity(currentDate))

                throw new KIRuntimeException(ERROR_MSG);

            max = compare(max, currentDate);

        }

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(outputName, new JsonPrimitive(max))))));
    }

    public String compare(String maxDate, String currentDate) {
        return ZonedDateTime.parse(maxDate).toEpochSecond() >= ZonedDateTime.parse(currentDate).toEpochSecond()
                ? maxDate
                : currentDate;
    }

}

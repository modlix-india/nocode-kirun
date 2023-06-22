package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class RemoveDuplicates extends AbstractArrayFunction {

    protected RemoveDuplicates() {

        super("RemoveDuplicates", List.of(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM, AbstractArrayFunction.PARAMETER_INT_LENGTH),
                AbstractArrayFunction.EVENT_RESULT_ARRAY);
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        JsonArray source = context.getArguments().get(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName())
                .getAsJsonArray();

        int srcFrom = context.getArguments().get(AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.getParameterName())
                .getAsJsonPrimitive().getAsInt();

        int length = context.getArguments().get(AbstractArrayFunction.PARAMETER_INT_LENGTH.getParameterName())
                .getAsJsonPrimitive().getAsInt();

        if (length == -1)
            length = source.size() - srcFrom;

        if (srcFrom + length > source.size())
            throw new KIRuntimeException(
                    StringFormatter.format(
                            "Array has no elements from $ to $ as the array size is $",
                            srcFrom,
                            srcFrom + length,
                            source.size()));

        JsonArray ja = source.deepCopy();
        int to = srcFrom + length;

        for (int i = to - 1; i >= srcFrom; i--) {
            for (int j = i - 1; j >= srcFrom; j--) {
                if (ja.get(i).equals(ja.get(j))) {
                    ja.remove(j);
                    break;
                }
            }
        }

        return Mono.just(
                new FunctionOutput(List.of(EventResult.outputOf(Map.of(AbstractArrayFunction.EVENT_RESULT_NAME, ja)))));
    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.core.publisher.Mono;

public class GetStartOfTimeStamp extends AbstractReactiveFunction {

    private static final String DATE = "isodate";

    private static final String TIME_UNIT = "unit";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("GetStartOfTimeStamp")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(
                        Parameter.ofEntry(DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                        Parameter.ofEntry(TIME_UNIT, Schema.ofString(TIME_UNIT).setEnums(
                                List.of()))))
                .setEvents(Map.of());
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        return null;
    }

}

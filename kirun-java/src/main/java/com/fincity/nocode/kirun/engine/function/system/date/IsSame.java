package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IsSame extends AbstractReactiveFunction {

    private static final String ISO_DATE1 = "dateone";

    private static final String ISO_DATE2 = "datetwo";

    private static final String OUTPUT = "result";

    private static final String TIME_UNIT = "unit";

    private static final String YEAR = "year";

    private static final String MONTH = "month";

    private static final String DAY = "day";

    private static final String HOUR = "hour";

    private static final String MINUTE = "minute";

    private static final String SECOND = "second";

    private static final Schema dateSchema = Schema.ofRef(Namespaces.DATE + ".timeStamp");

    private static final TreeMap<Integer, String> priority_map = new TreeMap(
            Map.of(1, YEAR, 2, MONTH, 3, DAY, 4, HOUR, 5, MINUTE, 6, SECOND));

    private static final List<String> priority_arr = List.of(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND);

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("IsSame")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(

                        Parameter.ofEntry(ISO_DATE1, dateSchema),
                        Parameter.ofEntry(ISO_DATE2, dateSchema),

                        Parameter.ofEntry(TIME_UNIT, Schema.ofString(TIME_UNIT)
                                .setEnums(List.of(
                                        new JsonPrimitive(YEAR),
                                        new JsonPrimitive(MONTH),
                                        new JsonPrimitive(DAY),
                                        new JsonPrimitive(HOUR),
                                        new JsonPrimitive(MINUTE),
                                        new JsonPrimitive(SECOND))),
                                true)))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String firstDate = context.getArguments().get(ISO_DATE1).getAsString();

        String secondDate = context.getArguments().get(ISO_DATE2).getAsString();

        if (!IsValidIsoDateTime.checkValidity(firstDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE1);

        if (!IsValidIsoDateTime.checkValidity(secondDate))
            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE2);

        JsonArray arr = context.getArguments().get(TIME_UNIT).getAsJsonArray();

        int size = arr.size();

        if (size == 0) // check here
            throw new KIRuntimeException("Please provide a unit for checking");
        
        boolean same = false;

        

        return null;
    }

    private static int getGivenField(String inputDate, int field) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(getEpochTime(inputDate)));
        return cal.get(field);
    }
}

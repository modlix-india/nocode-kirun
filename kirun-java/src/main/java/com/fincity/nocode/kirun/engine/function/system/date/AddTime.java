package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class AddTime extends AbstractReactiveFunction {

    private String functionName;

    private String methodName;

    private static final String ISO_DATE = "isodate";

    private static final String TIME_UNIT = "unit";

    private static final String OUTPUT = "dateTime";

    private static final String YEARS = "YEARS";

    private static final String MONTHS = "MONTHS";

    private static final String DAYS = "DAYS";

    private static final String HOURS = "HOURS";

    private static final String MINUTES = "MINUTES";

    private static final String SECONDS = "SECONDS";

    private static final String MILLIS = "MILLIS";

    public AddTime() {
        this.functionName = "AddTime";
        this.methodName = "add";
    }

    public AddTime(String functionName, String methodName) {
        this.functionName = functionName;
        this.methodName = methodName;
    }

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName(functionName)
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(
                                Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                                Parameter.ofEntry(methodName, Schema.ofInteger(methodName)),
                                Parameter.ofEntry(TIME_UNIT, Schema.ofString(TIME_UNIT)
                                        .setEnums(List.of(
                                                new JsonPrimitive(YEARS),
                                                new JsonPrimitive(MONTHS),
                                                new JsonPrimitive(DAYS),
                                                new JsonPrimitive(HOURS),
                                                new JsonPrimitive(MINUTES),
                                                new JsonPrimitive(SECONDS),
                                                new JsonPrimitive(MILLIS))))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(
                                Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide the valid ISO date");

        int span = context.getArguments().get(methodName).getAsInt();

        String unit = context.getArguments().get(TIME_UNIT).getAsString();

        switch (unit) {

            case YEARS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.YEAR, span));

            case MONTHS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.MONTH, span));

            case DAYS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.DAY_OF_MONTH, span));

            case HOURS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.HOUR_OF_DAY, span));

            case MINUTES:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.MINUTE, span));

            case SECONDS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.SECOND, span));

            case MILLIS:
                return Mono.just(setAndFetchCalendarField(inputDate, Calendar.MILLISECOND, span));

            default:
                throw new KIRuntimeException("Please select valid unit");
        }

    }

    private FunctionOutput setAndFetchCalendarField(String inputDate, int field, int value) {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date(getEpochTime(inputDate)));

        cal.set(field, setFunction(cal.get(field), value));

        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(cal.getTimeInMillis()), ZoneId.of("UTC"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        return new FunctionOutput(
                List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(dtf.format(zdt))))));

    }

    public int setFunction(int actual, int addValue) {
        return actual + addValue;
    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
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

public class FromNow extends AbstractReactiveFunction {

    private static final String DATE = "isodate";

    private static final String OUTPUT = "difference";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("FromNow")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DATE, new Parameter().setParameterName(DATE)
                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(DATE).getAsString();

        if (!IsValidIsoDateTime.checkValidity(inputDate))
            throw new KIRuntimeException("Given String is not convertable to ISO date format");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            LocalDateTime pastTime = LocalDateTime.ofInstant(df.parse(inputDate).toInstant(),
                    TimeZone.getTimeZone("UTC").toZoneId());

            LocalDateTime currentTime = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());

            Period period = Period.between(pastTime.toLocalDate(), currentTime.toLocalDate());

            Duration duration = Duration.between(pastTime, currentTime);

            String difference = "Several";

            if (period.getYears() != 0)
                difference += " years ago";

            else if (period.getMonths() != 0)
                difference += " months ago";

            else if (period.getDays() != 0)
                difference += " days ago";

            else if (duration.toHours() != 0)
                difference += " hours ago";

            else if (duration.toMinutes() != 0)
                difference += " minutes ago";

            else if (duration.toSeconds() != 0)
                difference += " seconds ago";
            else
                difference = "Moments ago";

            return Mono
                    .just(new FunctionOutput(
                            List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(difference))))));

        } catch (ParseException e) {

            throw new KIRuntimeException("Please provide valid ISO date format");
        }

    }

}

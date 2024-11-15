package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class EpochToTimestamp extends AbstractDateFunction {

    private final boolean isSeconds;
    private final String paramName;

    private static final String EPOCH = "epoch";
    private static final String SECONDS = "Seconds";
    private static final String MILLISECONDS = "Milliseconds";

    public EpochToTimestamp(String name, boolean isSeconds) {

        super(
                name,
                EVENT_TIMESTAMP,
                Parameter.of(EPOCH + (isSeconds ? SECONDS : MILLISECONDS), new Schema()
                        .setName(EPOCH + (isSeconds ? SECONDS : MILLISECONDS))
                        .setType(
                                Type.of(
                                        SchemaType.LONG,
                                        SchemaType.INTEGER,
                                        SchemaType.STRING))));

        this.isSeconds = isSeconds;
        this.paramName = EPOCH + (isSeconds ? SECONDS : MILLISECONDS);
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        Long epoch = context.getArguments().get(this.paramName).getAsLong();

        ZonedDateTime dateTime = ZonedDateTime.ofInstant(
                this.isSeconds ? Instant.ofEpochSecond(epoch) : Instant.ofEpochMilli(epoch), ZoneId.systemDefault());

        return Mono.just(new FunctionOutput(List.of(EventResult
                .outputOf(Map.of(EVENT_TIMESTAMP_NAME,
                        new JsonPrimitive(dateTime.format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }

}

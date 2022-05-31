package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class RangeLoop extends AbstractFunction {

	static final String FROM = "from";

	static final String TO = "to";

	static final String STEP = "step";

	static final String VALUE = "value";

	static final String INDEX = "index";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("RangeLoop")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(
	                Parameter.ofEntry(FROM,
	                        Schema.of(FROM, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(0))),
	                Parameter.ofEntry(TO,
	                        Schema.of(TO, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(1))),
	                Parameter.ofEntry(STEP,
	                        Schema.of(STEP, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(1))
	                                .setNot(new Schema().setConstant(new JsonPrimitive(0))))))
	        .setEvents(Map.ofEntries(
	                Event.eventMapEntry(Event.ITERATION,
	                        Map.of(INDEX,
	                                Schema.of(INDEX, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT,
	                                        SchemaType.DOUBLE))),
	                Event.outputEventMapEntry(Map.of(VALUE, Schema.of(VALUE, SchemaType.INTEGER, SchemaType.LONG,
	                        SchemaType.FLOAT, SchemaType.DOUBLE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		var from = args.get(FROM);
		var to = args.get(TO);
		var step = args.get(STEP);

		Mono<Tuple3<JsonElement, JsonElement, JsonElement>> tuple = from
		        .flatMap(f -> to.flatMap(t -> step.map(s -> Tuples.of(f, t, s))));

		Flux<JsonPrimitive> fluxrange = tuple.flatMapMany(tup ->
			{
				final Number f = tup.getT1()
				        .getAsNumber();
				final Number t = tup.getT2()
				        .getAsNumber();
				final Number s = tup.getT3()
				        .getAsNumber();

				final boolean forward = s.doubleValue() > 0.0d;

				if (f instanceof Double || t instanceof Double || s instanceof Double) {
					return doubleSeries(f.doubleValue(), t.doubleValue(), s.doubleValue(), forward);
				} else if (f instanceof Float || t instanceof Float || s instanceof Float) {
					return floatSeries(f.floatValue(), t.floatValue(), s.floatValue(), forward);
				} else if (f instanceof Long || t instanceof Long || s instanceof Long) {
					return longSeries(f.longValue(), t.longValue(), s.longValue(), forward);
				}

				return integerSeries(f.intValue(), t.intValue(), s.intValue(), forward);
			});

		return Flux.merge(fluxrange.map(e -> EventResult.of(Event.ITERATION, Map.of(INDEX, e))),
		        Flux.from(to.map(v -> EventResult.outputOf(Map.of(VALUE, v)))));
	}

	private Flux<JsonPrimitive> integerSeries(final Integer f, final Integer t, final Integer s,
	        final boolean forward) {
		return Flux.generate(() -> f, (state, sink) ->
			{
				int v = state;

				if ((forward && v >= t) || (!forward && v <= t))
					sink.complete();
				else
					sink.next(Integer.valueOf(v));

				return v + s;
			})
		        .map(e -> new JsonPrimitive((Number) e));
	}

	private Flux<JsonPrimitive> longSeries(final Long f, final Long t, final Long s, final boolean forward) {
		return Flux.generate(() -> f, (state, sink) ->
			{
				long v = state;

				if ((forward && v >= t) || (!forward && v <= t))
					sink.complete();
				else
					sink.next(Long.valueOf(v));

				return v + s;
			})
		        .map(e -> new JsonPrimitive((Number) e));
	}

	private Flux<JsonPrimitive> floatSeries(final Float f, final Float t, final Float s, final boolean forward) {
		return Flux.generate(() -> f, (state, sink) ->
			{
				float v = state;

				if ((forward && v >= t) || (!forward && v <= t))
					sink.complete();
				else
					sink.next(Float.valueOf(v));

				return v + s;
			})
		        .map(e -> new JsonPrimitive((Number) e));
	}

	private Flux<JsonPrimitive> doubleSeries(final Double f, final Double t, final Double s, final boolean forward) {
		return Flux.generate(() -> f, (state, sink) ->
			{
				double v = state;

				if ((forward && v >= t) || (!forward && v <= t))
					sink.complete();
				else
					sink.next(Double.valueOf(v));

				return v + s;
			})
		        .map(e -> new JsonPrimitive((Number) e));
	}
}

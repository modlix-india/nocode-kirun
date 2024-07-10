package com.fincity.nocode.kirun.engine.function.system.loop;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_LOOP;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionOutputGenerator;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class RangeLoop extends AbstractReactiveFunction {

	static final String FROM = "from";

	static final String TO = "to";

	static final String STEP = "step";

	static final String VALUE = "value";

	static final String INDEX = "index";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("RangeLoop")
			.setNamespace(SYSTEM_LOOP)
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
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var from = context.getArguments()
				.get(FROM);
		var toElement = context.getArguments()
				.get(TO);
		var step = context.getArguments()
				.get(STEP);

		AtomicReference<Double> current = new AtomicReference<>(from.getAsNumber()
				.doubleValue());

		String statementName = context.getStatementExecution() == null ? null
				: context.getStatementExecution()
						.getStatement()
						.getStatementName();

		Number f = from.getAsNumber();
		Number t = toElement.getAsNumber();
		Number s = step.getAsNumber();

		Method valueOf;
		try {
			valueOf = findValueMethod(f, t, s);
		} catch (Exception e) {
			return Mono.error(() -> new KIRuntimeException("Unable to find value Method : ", e));
		}

		FunctionOutputGenerator generator = makeGenerator(context, current, statementName, step, t, valueOf);

		return Mono.just(generator)
				.map(FunctionOutput::new);
	}

	private FunctionOutputGenerator makeGenerator(ReactiveFunctionExecutionParameters context,
			AtomicReference<Double> current, String statementName, JsonElement step, Number t, Method valueOf) {

		Number s = step.getAsNumber();

		final boolean forward = step.getAsDouble() > 0d;
		final double to = t.doubleValue();

		AtomicBoolean done = new AtomicBoolean(false);

		return () -> {

			if (done.get())
				return null;

			if ((forward && current.get()
					.doubleValue() >= to) || (!forward
							&& current.get()
									.doubleValue() <= to)
					|| (statementName != null && context.getExecutionContext()
							.getOrDefault(statementName, new JsonPrimitive(false))
							.getAsBoolean())) {

				done.set(true);
				if (statementName != null)
					context.getExecutionContext()
							.remove(statementName);
				return EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(current.get())));
			}

			EventResult er;
			try {
				er = EventResult.of(Event.ITERATION,
						Map.of(INDEX, new JsonPrimitive((Number) valueOf.invoke(current.get()))));
			} catch (Exception e) {
				throw new KIRuntimeException("Error in invoking method valueOf with value : " + current.get(), e);
			}

			current.getAndUpdate(num -> num.doubleValue() + s.doubleValue());

			return er;
		};
	}

	private Method findValueMethod(Number f, Number t, Number s) throws NoSuchMethodException, SecurityException {

		if (f instanceof Double || t instanceof Double || s instanceof Double) {
			return Double.class.getMethod("doubleValue");
		} else if (f instanceof Float || t instanceof Float || s instanceof Float) {
			return Double.class.getMethod("floatValue");
		} else if (f instanceof Long || t instanceof Long || s instanceof Long) {
			return Double.class.getMethod("longValue");
		} else if (f instanceof BigInteger || t instanceof BigInteger || s instanceof BigInteger) {
			return BigInteger.class.getMethod("longValue");
		} else if (f instanceof BigDecimal || t instanceof BigDecimal || s instanceof BigDecimal) {
			return BigDecimal.class.getMethod("doubleValue");
		}
		return Double.class.getMethod("intValue");
	}
}

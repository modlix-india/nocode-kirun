package com.fincity.nocode.kirun.engine.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.SchemaUtil;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.ParameterReference.ParameterReferenceType;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.util.graph.DiGraph;
import com.fincity.nocode.kirun.engine.runtime.util.string.StringFormatter;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KIRuntime extends AbstractFunction {

	private final FunctionDefinition fd;

	private final Repository<Function> fRepo;

	private final Repository<Schema> sRepo;

	private static final int VERSION = 1;

	public KIRuntime(FunctionDefinition fd, Repository<Function> functionRepository,
	        Repository<Schema> schemaRepository) {

		this.fd = fd;
		if (this.fd.getVersion() > VERSION) {
			throw new KIRuntimeException("Runtime is at a lower version " + VERSION
			        + " and trying to run code from version " + this.fd.getVersion() + ".");
		}

		this.fRepo = functionRepository;
		this.sRepo = schemaRepository;
	}

	@Override
	public FunctionSignature getSignature() {

		return this.fd;
	}

	private Mono<DiGraph<String, StatementExecution>> getExecutionPlan(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		return Flux.fromIterable(this.fd.getSteps()
		        .values())
		        .map(s -> this.prepareStatementExecution(context, s))
		        .collect(DiGraph<String, StatementExecution>::new, DiGraph::addVertex);
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		Mono<DiGraph<String, StatementExecution>> eGraph = this.getExecutionPlan(context, args);

		if (context == null)
			context = new ConcurrentHashMap<>();

		return Flux.empty();
	}

	private StatementExecution prepareStatementExecution(Map<String, ContextElement> context, Statement s) {

		StatementExecution se = new StatementExecution(s);

		Function fun = this.fRepo.find(s.getNamespace() + "." + s.getName());

		HashMap<String, Parameter> paramSet = new HashMap<>(fun.getSignature()
		        .getParameters());

		for (Entry<String, List<ParameterReference>> param : s.getParameterMap()
		        .entrySet()) {

			Parameter p = paramSet.get(param.getKey());

			List<ParameterReference> refList = param.getValue();

			if (refList == null || refList.isEmpty()) {

				if (SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format("Parameter \"$\" need a value", p.getParameterName()));
				continue;
			}

			if (p.isVariableArgument()) {

			} else {

				ParameterReference ref = refList.get(0);
				if (ref == null) {
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format("Parameter \"$\" need a value", p.getParameterName()));
				} else if (ref.getType() == ParameterReferenceType.VALUE) {
					if (ref.getValue() == null)
						se.addMessage(StatementMessageType.ERROR,
						        StringFormatter.format("Parameter \"$\" need a value", p.getParameterName()));
				} else if (ref.getType() == ParameterReferenceType.EXPRESSION) {
					if (ref.getExpression() == null) {
						se.addMessage(StatementMessageType.ERROR,
						        StringFormatter.format("Parameter \"$\" need a value", p.getParameterName()));
					}else {
					
					}
				}
			}

			paramSet.remove(p.getParameterName());
		}

		if (!paramSet.isEmpty()) {
			for (Parameter param : paramSet.values()) {
				if (SchemaUtil.getDefaultValue(param.getSchema(), this.sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format("Parameter \"$\" need a value", param.getParameterName()));
			}
		}

		return se;
	}
}

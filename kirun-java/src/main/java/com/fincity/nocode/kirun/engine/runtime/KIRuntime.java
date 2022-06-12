package com.fincity.nocode.kirun.engine.runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.SchemaUtil;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.ParameterReference.ParameterReferenceType;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.expression.Expression;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionToken;
import com.fincity.nocode.kirun.engine.runtime.graph.ExecutionGraph;
import com.fincity.nocode.kirun.engine.runtime.graph.GraphVertex;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class KIRuntime extends AbstractFunction {

	private static final String PARAMETER_NEEDS_A_VALUE = "Parameter \"$\" needs a value";

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

	private ExecutionGraph<String, StatementExecution> getExecutionPlan(Map<String, ContextElement> context) {

		ExecutionGraph<String, StatementExecution> g = new ExecutionGraph<>();
		for (Statement s : this.fd.getSteps()
		        .values())
			g.addVertex(this.prepareStatementExecution(context, s));
		return g.makeEdges();
	}

	@Override
	protected Flux<EventResult> internalExecute(final Map<String, ContextElement> context,
	        final Map<String, JsonElement> args) {

		Mono.fromCallable(() ->
			{
				ExecutionGraph<String, StatementExecution> eGraph = this.getExecutionPlan(context);

				boolean hasError = eGraph.getVerticesDataFlux()
				        .flatMap(e -> Flux.fromIterable(e.getMessages()))
				        .map(StatementMessage::getMessageType)
				        .filter(e -> e == StatementMessageType.ERROR)
				        .take(1)
				        .blockFirst() == null;

				if (hasError) {
					throw new KIRuntimeException("Please fix the errors before execution");
				}

				Map<String, ContextElement> newContext = context;

				if (newContext == null)
					newContext = new ConcurrentHashMap<>();

//			StepName, EventName, result
				Map<String, Map<String, EventResult>> results = new ConcurrentHashMap<>();
				LinkedList<GraphVertex<String, StatementExecution>> executionQue = new LinkedList<>(
				        eGraph.getVerticesWithNoIncomingEdges());

				while (!executionQue.isEmpty()) {

					LinkedList<GraphVertex<String, StatementExecution>> nextQue = new LinkedList<>();
					for (GraphVertex<String, StatementExecution> exList : executionQue) {
						Statement s = exList.getData()
						        .getStatement();

						Function fun = this.fRepo.find(s.getNamespace() + "." + s.getName());

						Map<String, Parameter> paramSet = fun.getSignature()
						        .getParameters();

						Map<String, JsonElement> arguments = getArgumentsFromParametersMap(context, results, s,
						        paramSet);

						Flux<EventResult> output = fun.execute(context, arguments);
						
						
					}

					executionQue = nextQue;
				}

				return "";
			})
		        .subscribeOn(Schedulers.boundedElastic());

		return Flux.empty();
	}

	private Map<String, JsonElement> getArgumentsFromParametersMap(final Map<String, ContextElement> context,
	        Map<String, Map<String, EventResult>> results, Statement s, Map<String, Parameter> paramSet) {

		return s.getParameterMap()
		        .entrySet()
		        .stream()
		        .map(e ->
			        {
				        List<ParameterReference> prList = e.getValue();

				        JsonElement ret = null;

				        if (prList == null || prList.isEmpty())
					        return Tuples.of(e.getKey(), ret);

				        Parameter pDef = paramSet.get(e.getKey());

				        if (pDef.isVariableArgument()) {

					        ret = new JsonArray();

					        prList.stream()
					                .map(r -> this.parameterReferenceEvaluation(context, results, r))
					                .flatMap(r -> r.isJsonArray() ? StreamSupport.stream(r.getAsJsonArray()
					                        .spliterator(), false) : Stream.of(r))
					                .forEachOrdered(((JsonArray) ret)::add);

				        } else {

					        ret = parameterReferenceEvaluation(context, results, prList.get(0));
				        }

				        return Tuples.of(e.getKey(), ret);
			        })
		        .filter(e -> !(e.getT2() == null || e.getT2()
		                .isJsonNull()))
		        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private JsonElement parameterReferenceEvaluation(final Map<String, ContextElement> context,
	        Map<String, Map<String, EventResult>> results, ParameterReference ref) {

		JsonElement ret = null;

		if (ref.getType() == ParameterReferenceType.VALUE) {
			ret = ref.getValue();
		} else if (ref.getType() == ParameterReferenceType.EXPRESSION && ref.getExpression() != null
		        && !ref.getExpression()
		                .isBlank()) {
			ExpressionEvaluator exp = new ExpressionEvaluator(ref.getExpression());
			ret = exp.evaluate(context, results);
		}
		return ret;
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
					        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
				continue;
			}

			if (p.isVariableArgument()) {

				for (ParameterReference ref : refList)
					parameterReferenceValidation(context, se, p, ref);
			} else {

				ParameterReference ref = refList.get(0);
				parameterReferenceValidation(context, se, p, ref);
			}

			paramSet.remove(p.getParameterName());
		}

		if (!paramSet.isEmpty()) {
			for (Parameter param : paramSet.values()) {
				if (SchemaUtil.getDefaultValue(param.getSchema(), this.sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, param.getParameterName()));
			}
		}

		return se;
	}

	private void parameterReferenceValidation(Map<String, ContextElement> context, StatementExecution se, Parameter p,
	        ParameterReference ref) {

		if (ref == null) {
			if (SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo) == null)
				se.addMessage(StatementMessageType.ERROR,
				        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
		} else if (ref.getType() == ParameterReferenceType.VALUE) {
			if (ref.getValue() == null && SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo) == null)
				se.addMessage(StatementMessageType.ERROR,
				        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
		} else if (ref.getType() == ParameterReferenceType.EXPRESSION) {
			if (ref.getExpression() == null || ref.getExpression()
			        .isBlank()) {
				if (SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
			} else {
				try {
					Expression exp = new Expression(ref.getExpression());
					this.typeCheckExpression(context, p, exp);
					this.addDependencies(se, exp);
				} catch (KIRuntimeException ex) {
					se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format("Error evaluating $ : ", ref.getExpression(), ex.getMessage()));
				}
			}
		}
	}

	private void addDependencies(StatementExecution se, Expression exp) {

		LinkedList<Expression> que = new LinkedList<>();
		que.add(exp);

		while (!que.isEmpty()) {
			for (ExpressionToken token : que.getFirst()
			        .getTokens()) {
				if (token instanceof Expression e) {
					que.push(e);
				} else if (token.getExpression()
				        .startsWith("Steps.")) {
					se.addDependency(token.getExpression());
				}
			}
		}

		if (se.getStatement()
		        .getDependentStatements() == null)
			return;

		for (String statement : se.getStatement()
		        .getDependentStatements())
			se.addDependency(statement);
	}

	private void typeCheckExpression(Map<String, ContextElement> context, Parameter p, Expression exp) {

		// TODO: we need to check the type of the parameters based on the input they
		// get.
	}
}

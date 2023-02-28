package com.fincity.nocode.kirun.engine.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.JsonExpression;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.SchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.ParameterReferenceType;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.graph.ExecutionGraph;
import com.fincity.nocode.kirun.engine.runtime.graph.GraphVertex;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.OutputMapTokenValueExtractor;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public class KIRuntime extends AbstractFunction {

	private static final String DEBUG_STEP = "Step : ";

	private static final String PARAMETER_NEEDS_A_VALUE = "Parameter \"$\" needs a value";

	private static final Pattern STEP_REGEX_PATTERN = Pattern
			.compile("Steps\\.([a-zA-Z0-9\\\\-]+)\\.([a-zA-Z0-9\\\\-]+)");

	private static final int VERSION = 1;

	private static final int MAX_EXECUTION_ITERATIONS = 10000000;

	private static final Logger logger = LoggerFactory.getLogger(KIRuntime.class);

	private final FunctionDefinition fd;

	private final boolean debugMode;

	private final StringBuilder sb = new StringBuilder();

	public KIRuntime(FunctionDefinition fd) {
		this(fd, false);
	}

	public KIRuntime(FunctionDefinition fd, boolean debugMode) {

		this.fd = fd;
		this.debugMode = debugMode;
		if (this.fd.getVersion() > VERSION) {
			throw new KIRuntimeException("Runtime is at a lower version " + VERSION
					+ " and trying to run code from version " + this.fd.getVersion() + ".");
		}
	}

	@Override
	public FunctionSignature getSignature() {

		return this.fd;
	}

	public Tuple2<List<Tuple2<String, String>>, ExecutionGraph<String, StatementExecution>> getExecutionPlan(
			Repository<Function> fRepo, Repository<Schema> sRepo) {

		ExecutionGraph<String, StatementExecution> g = new ExecutionGraph<>();
		for (Statement s : this.fd.getSteps().values())
			g.addVertex(this.prepareStatementExecution(s, fRepo, sRepo));

		return Tuples.of(this.makeEdges(g), g);
	}

	@Override
	protected FunctionOutput internalExecute(final FunctionExecutionParameters inContext) {

		if (inContext.getContext() == null)
			inContext.setContext(new ConcurrentHashMap<>());

		if (inContext.getEvents() == null)
			inContext.setEvents(new ConcurrentHashMap<>());

		if (inContext.getSteps() == null)
			inContext.setSteps(new ConcurrentHashMap<>());

		inContext.addTokenValueExtractor(new ArgumentsTokenValueExtractor(
				inContext.getArguments() == null ? Map.of() : inContext.getArguments()));

		if (this.debugMode) {

			this.sb.append("Executing: ").append(this.fd.getNamespace()).append('.').append(this.fd.getName())
					.append("\n").append("Parameters: ").append(inContext).append("\n");

		}

		var eGraph = this.getExecutionPlan(inContext.getFunctionRepository(), inContext.getSchemaRepository());

		if (this.debugMode) {
			this.sb.append(eGraph.getT2().toString()).append("\n");

			if (!eGraph.getT1().isEmpty()) {
				this.sb.append("Unresolved Dependencies: ");
				this.sb.append(
						eGraph.getT1().stream().map(e -> StringFormatter.format("Steps.$.$", e.getT1(), e.getT2())))
						.append("\n");
			}
		}

		if (!eGraph.getT1().isEmpty()) {
			throw new KIRuntimeException(StringFormatter.format("Found these unresolved dependencies : $ ",
					eGraph.getT1().stream().map(e -> StringFormatter.format("Steps.$.$", e.getT1(), e.getT2()))));
		}

		if (logger.isDebugEnabled()) {
			logger.debug(StringFormatter.format("Executing : $.$", this.fd.getNamespace(), this.fd.getName()));
			logger.debug(eGraph.toString());
		}

		List<StatementMessage> messages = eGraph.getT2().getVerticesDataFlux()
				.flatMap(e -> Flux.fromIterable(e.getMessages())).collectList().block();

		if (messages != null && !messages.isEmpty()) {
			throw new KIRuntimeException(
					"Please fix the errors in the function definition before execution : \n" + messages);
		}

		return executeGraph(eGraph.getT2(), inContext);
	}

	private FunctionOutput executeGraph(ExecutionGraph<String, StatementExecution> eGraph,
			FunctionExecutionParameters inContext) {

		LinkedList<GraphVertex<String, StatementExecution>> executionQue = new LinkedList<>();
		executionQue.addAll(eGraph.getVerticesWithNoIncomingEdges());

		LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue = new LinkedList<>();

		while ((!executionQue.isEmpty() || !branchQue.isEmpty()) && !inContext.getEvents().containsKey(Event.OUTPUT)) {

			processBranchQue(inContext, executionQue, branchQue);
			processExecutionQue(inContext, executionQue, branchQue);

			inContext.setCount(inContext.getCount() + 1);

			if (inContext.getCount() == MAX_EXECUTION_ITERATIONS)
				throw new KIRuntimeException("Execution locked in an infinite loop");
		}

		if (!eGraph.isSubGraph() && inContext.getEvents().isEmpty()) {

			var eventMap = this.getSignature().getEvents();
			if (!eventMap.isEmpty() && eventMap.get(Event.OUTPUT).getParameters() != null
					&& !eventMap.get(Event.OUTPUT).getParameters().isEmpty())
				throw new KIRuntimeException("No events raised");
		}

		return new FunctionOutput(inContext.getEvents().entrySet().stream()
				.flatMap(e -> e.getValue().stream().map(v -> EventResult.of(e.getKey(), v))).toList());
	}

	private void processExecutionQue(FunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue) {

		if (!executionQue.isEmpty()) {

			var vertex = executionQue.pop();

			if (!allDependenciesResolved(vertex, inContext.getSteps()))
				executionQue.add(vertex);
			else
				executeVertex(vertex, inContext, branchQue, executionQue);
		}
	}

	private void processBranchQue(FunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue) {
		if (!branchQue.isEmpty()) {

			var branch = branchQue.pop();

			if (!allDependenciesResolved(branch.getT2(), inContext.getSteps()))
				branchQue.add(branch);
			else
				executeBranch(inContext, executionQue, branch);
		}
	}

	private void executeBranch(FunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>> branch) {

		var vertex = branch.getT4();
		EventResult nextOutput = null;

		do {
			this.executeGraph(branch.getT1(), inContext);
			nextOutput = branch.getT3().next();

			if (nextOutput != null) {
				inContext.getSteps()
						.computeIfAbsent(vertex.getData().getStatement().getStatementName(),
								k -> new ConcurrentHashMap<>())
						.put(nextOutput.getName(), resolveInternalExpressions(nextOutput.getResult(), inContext));

				if (this.debugMode) {
					var s = vertex.getData().getStatement();
					this.sb.append(DEBUG_STEP).append(s.getStatementName()).append(" => ").append(s.getNamespace())
							.append('.').append(s.getName()).append("\n");
					this.sb.append("Event : ").append(nextOutput.getName()).append("\n")
							.append(inContext.getSteps().get(s.getStatementName()).get(nextOutput.getName()))
							.append("\n");

				}
			}
		} while (nextOutput != null && !nextOutput.getName().equals(Event.OUTPUT));

		if (nextOutput != null && nextOutput.getName().equals(Event.OUTPUT)
				&& vertex.getOutVertices().containsKey(Event.OUTPUT)) {

			vertex.getOutVertices().get(Event.OUTPUT).stream()
					.filter(e -> this.allDependenciesResolved(e, inContext.getSteps())).forEach(executionQue::add);
		}
	}

	private void executeVertex(GraphVertex<String, StatementExecution> vertex, FunctionExecutionParameters inContext,
			LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue) {

		Statement s = vertex.getData().getStatement();

		Function fun = inContext.getFunctionRepository().find(s.getNamespace(), s.getName());

		Map<String, Parameter> paramSet = fun.getSignature().getParameters();

		Map<String, JsonElement> arguments = getArgumentsFromParametersMap(inContext, s, paramSet);

		if (this.debugMode) {

			this.sb.append(DEBUG_STEP).append(s.getStatementName()).append(" => ").append(s.getNamespace()).append('.')
					.append(s.getName()).append("\n");
			this.sb.append("Arguments : \n").append(arguments).append("\n");
		}

		Map<String, ContextElement> context = inContext.getContext();

		FunctionExecutionParameters fep;

		if (fun instanceof KIRuntime) {
			fep = new FunctionExecutionParameters(inContext.getFunctionRepository(), inContext.getSchemaRepository(),
					inContext.getExecutionId() + "_" + s.getStatementName())
					.setArguments(arguments)
					.setValuesMap(inContext.getValuesMap().values().stream()
							.filter(e -> !e.getPrefix().equals(ArgumentsTokenValueExtractor.PREFIX)
									&& !e.getPrefix().equals(OutputMapTokenValueExtractor.PREFIX)
									&& !e.getPrefix().equals(ContextTokenValueExtractor.PREFIX))
							.collect(Collectors.toMap(TokenValueExtractor::getPrefix,
									java.util.function.Function.identity())));
		} else {
			fep = new FunctionExecutionParameters(inContext.getFunctionRepository(), inContext.getSchemaRepository(),
					inContext.getExecutionId()).setValuesMap(inContext.getValuesMap()).setContext(context)
					.setArguments(arguments).setEvents(inContext.getEvents()).setSteps(inContext.getSteps())
					.setStatementExecution(vertex.getData()).setCount(inContext.getCount());
		}

		FunctionOutput result = fun.execute(fep);

		EventResult er = result.next();

		if (er == null)
			throw new KIRuntimeException(
					StringFormatter.format("Executing $ returned no events", s.getStatementName()));

		boolean isOutput = er.getName().equals(Event.OUTPUT);

		inContext.getSteps().computeIfAbsent(s.getStatementName(), k -> new ConcurrentHashMap<>()).put(er.getName(),
				resolveInternalExpressions(er.getResult(), inContext));

		if (this.debugMode) {

			this.sb.append(DEBUG_STEP).append(s.getStatementName()).append(" => ").append(s.getNamespace()).append('.')
					.append(s.getName()).append("\n");

			this.sb.append("Event :").append(er.getName()).append("\n")
					.append(inContext.getSteps().get(s.getStatementName()).get(er.getName())).append("\n");
		}

		if (!isOutput) {

			var subGraph = vertex.getSubGraphOfType(er.getName());
			List<Tuple2<String, String>> unResolvedDependencies = this.makeEdges(subGraph);
			branchQue.add(Tuples.of(subGraph, unResolvedDependencies, result, vertex));
		} else {

			Set<GraphVertex<String, StatementExecution>> out = vertex.getOutVertices().get(Event.OUTPUT);
			if (out != null)
				out.stream().filter(e -> this.allDependenciesResolved(e, inContext.getSteps()))
						.forEach(executionQue::add);
		}
	}

	private Map<String, JsonElement> resolveInternalExpressions(Map<String, JsonElement> result,
			FunctionExecutionParameters inContext) {

		if (result == null)
			return result;

		return result.entrySet().stream()
				.map(e -> Tuples.of(e.getKey(), resolveInternalExpression(e.getValue(), inContext)))
				.collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private JsonElement resolveInternalExpression(JsonElement value, FunctionExecutionParameters inContext) {

		if (value == null || value.isJsonNull() || value.isJsonPrimitive())
			return value;

		if (value instanceof JsonExpression valueExpression) {

			ExpressionEvaluator exp = new ExpressionEvaluator(valueExpression.getExpression());
			return exp.evaluate(inContext.getValueExtractors());
		}

		if (value instanceof JsonObject valueObject) {

			JsonObject retObject = new JsonObject();

			for (Entry<String, JsonElement> entry : valueObject.entrySet()) {
				retObject.add(entry.getKey(), resolveInternalExpression(entry.getValue(), inContext));
			}

			return retObject;
		}

		if (value instanceof JsonArray valueArray) {

			JsonArray retArray = new JsonArray();

			for (JsonElement obj : valueArray) {
				retArray.add(resolveInternalExpression(obj, inContext));
			}

			return retArray;
		}

		return null;
	}

	private boolean allDependenciesResolved(List<Tuple2<String, String>> unResolvedDependencies,
			Map<String, Map<String, Map<String, JsonElement>>> output) {

		return unResolvedDependencies.stream()
				.takeWhile(e -> output.containsKey(e.getT1()) && output.get(e.getT1()).containsKey(e.getT2()))
				.count() == unResolvedDependencies.size();
	}

	private boolean allDependenciesResolved(GraphVertex<String, StatementExecution> vertex,
			Map<String, Map<String, Map<String, JsonElement>>> output) {

		if (vertex.getInVertices().isEmpty())
			return true;

		return vertex.getInVertices().stream().filter(e -> {
			String stepName = e.getT1().getData().getStatement().getStatementName();
			String type = e.getT2();

			return !(output.containsKey(stepName) && output.get(stepName).containsKey(type));
		}).count() == 0;
	}

	private Map<String, JsonElement> getArgumentsFromParametersMap(final FunctionExecutionParameters inContext,
			Statement s, Map<String, Parameter> paramSet) {

		return s.getParameterMap().entrySet().stream().map(e -> {
			List<ParameterReference> prList = e.getValue() == null ? List.of() : new ArrayList<>(e.getValue().values());

			JsonElement ret = null;

			if (prList == null || prList.isEmpty())
				return Tuples.of(e.getKey(), ret);

			Parameter pDef = paramSet.get(e.getKey());

			if (pDef.isVariableArgument()) {

				ret = new JsonArray();

				prList.stream().sorted((a, b) -> a.getOrder() - b.getOrder())
						.map(r -> this.parameterReferenceEvaluation(inContext, r))
						.flatMap(r -> r.isJsonArray() ? StreamSupport.stream(r.getAsJsonArray().spliterator(), false)
								: Stream.of(r))
						.forEachOrdered(((JsonArray) ret)::add);

			} else {

				ret = this.parameterReferenceEvaluation(inContext, prList.get(0));
			}

			return Tuples.of(e.getKey(), ret);
		}).filter(e -> !(e.getT2() == null || e.getT2().isJsonNull()))
				.collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private JsonElement parameterReferenceEvaluation(final FunctionExecutionParameters inContext,
			ParameterReference ref) {

		JsonElement ret = null;

		if (ref.getType() == ParameterReferenceType.VALUE) {
			ret = this.resolveInternalExpression(ref.getValue(), inContext);
		} else if (ref.getType() == ParameterReferenceType.EXPRESSION && ref.getExpression() != null
				&& !ref.getExpression().isBlank()) {
			ExpressionEvaluator exp = new ExpressionEvaluator(ref.getExpression());
			ret = exp.evaluate(inContext.getValueExtractors());
		}
		return ret;
	}

	private StatementExecution prepareStatementExecution(Statement s, Repository<Function> fRepo,
			Repository<Schema> sRepo) { // NOSONAR
		// Breaking this execution doesn't make sense.

		StatementExecution se = new StatementExecution(s);

		Function fun = fRepo.find(s.getNamespace(), s.getName());

		HashMap<String, Parameter> paramSet = new HashMap<>(fun.getSignature().getParameters());

		for (Entry<String, Map<String, ParameterReference>> param : s.getParameterMap().entrySet()) {

			Parameter p = paramSet.get(param.getKey());

			List<ParameterReference> refList = param.getValue() == null ? List.of()
					: new ArrayList<>(param.getValue().values());

			if ((refList == null || refList.isEmpty()) && !p.isVariableArgument()) {

				if (SchemaUtil.getDefaultValue(p.getSchema(), sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
							StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
				continue;
			}

			if (p.isVariableArgument()) {

				if (refList != null)
					refList.sort((a, b) -> a.getOrder() - b.getOrder());

				for (ParameterReference ref : refList)
					parameterReferenceValidation(se, p, ref, sRepo);
			} else if (refList != null && !refList.isEmpty()) {

				ParameterReference ref = refList.get(0);
				parameterReferenceValidation(se, p, ref, sRepo);
			}

			paramSet.remove(p.getParameterName());
		}

		if (se.getStatement().getDependentStatements() != null)
			for (Entry<String, Boolean> statement : s.getDependentStatements().entrySet())
				if (statement.getValue().booleanValue())
					se.addDependency(statement.getKey());

		if (!paramSet.isEmpty()) {
			for (Parameter param : paramSet.values()) {
				if (param.isVariableArgument())
					continue;
				if (SchemaUtil.getDefaultValue(param.getSchema(), sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
							StringFormatter.format(PARAMETER_NEEDS_A_VALUE, param.getParameterName()));
			}
		}

		return se;
	}

	private void parameterReferenceValidation(StatementExecution se, Parameter p, // NOSONAR
			ParameterReference ref, Repository<Schema> sRepo) {
		// Breaking this execution doesn't make sense.

		if (ref == null) {
			if (SchemaUtil.getDefaultValue(p.getSchema(), sRepo) == null)
				se.addMessage(StatementMessageType.ERROR,
						StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
		} else if (ref.getType() == ParameterReferenceType.VALUE) {
			if (ref.getValue() == null && SchemaUtil.getDefaultValue(p.getSchema(), sRepo) == null)
				se.addMessage(StatementMessageType.ERROR,
						StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
			LinkedList<Tuple2<Schema, JsonElement>> paramElements = new LinkedList<>();
			paramElements.push(Tuples.of(p.getSchema(), ref.getValue()));

			while (!paramElements.isEmpty()) {
				Tuple2<Schema, JsonElement> e = paramElements.pop();

				if (e.getT2() instanceof JsonExpression jexp) {
					this.addDependencies(se, jexp.getExpression());
				} else {

					if (e.getT1() == null || e.getT1().getType() == null)
						continue;

					if (e.getT1().getType().contains(SchemaType.ARRAY) && e.getT2().isJsonArray()) {
						ArraySchemaType ast = e.getT1().getItems();
						if (ast == null) {
							continue;
						}
						if (ast.isSingleType()) {
							for (JsonElement je : e.getT2().getAsJsonArray())
								paramElements.push(Tuples.of(ast.getSingleSchema(), je));
						} else {
							JsonArray array = e.getT2().getAsJsonArray();
							for (int i = 0; i < array.size(); i++) {
								paramElements.push(Tuples.of(ast.getTupleSchema().get(i), array.get(i)));
							}
						}
					} else if (e.getT1().getType().contains(SchemaType.OBJECT) && e.getT2().isJsonObject()) {

						Schema sch = e.getT1();

						if (sch.getName().equals(Parameter.EXPRESSION.getName())
								&& sch.getNamespace().equals(Parameter.EXPRESSION.getNamespace())) {
							JsonObject obj = e.getT2().getAsJsonObject();
							boolean isExpression = obj.get("isExpression").getAsBoolean();
							if (isExpression) {
								this.addDependencies(se, obj.get("value").getAsString());
							}
						} else {

							for (Entry<String, JsonElement> entry : e.getT2().getAsJsonObject().entrySet()) {
								paramElements
										.push(Tuples.of(sch.getProperties().get(entry.getKey()), entry.getValue()));
							}
						}
					}
				}
			}

		} else if (ref.getType() == ParameterReferenceType.EXPRESSION) {
			if (ref.getExpression() == null || ref.getExpression().isBlank()) {
				if (SchemaUtil.getDefaultValue(p.getSchema(), sRepo) == null)
					se.addMessage(StatementMessageType.ERROR,
							StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
			} else {
				try {
					// TODO: Type check for the resulting expression has to be done here...
					this.addDependencies(se, ref.getExpression());
				} catch (KIRuntimeException ex) {
					se.addMessage(StatementMessageType.ERROR,
							StringFormatter.format("Error evaluating $ : ", ref.getExpression(), ex.getMessage()));
				}
			}
		}
	}

	private void addDependencies(StatementExecution se, String expression) {

		Matcher m = STEP_REGEX_PATTERN.matcher(expression);

		while (m.find()) {

			if (m.groupCount() != 2)
				continue;
			se.addDependency(m.group(0));
		}
	}

	public List<Tuple2<String, String>> makeEdges(ExecutionGraph<String, StatementExecution> graph) {

		return graph.getNodeMap().values().stream().filter(e -> e.getData().getDepenedencies() != null)
				.flatMap(e -> e.getData().getDepenedencies().stream().map(d -> {
					int secondDot = d.indexOf('.', 6);
					String step = d.substring(6, secondDot);
					int eventDot = d.indexOf('.', secondDot + 1);
					String event = eventDot == -1 ? d.substring(secondDot + 1) : d.substring(secondDot + 1, eventDot);

					if (!graph.getNodeMap().containsKey(step))
						return Tuples.of(step, event);

					e.addInEdgeTo(graph.getNodeMap().get(step), event);
					return null;
				}).filter(Objects::nonNull)).toList();

	}

	public String getDebugString() {
		return this.sb.toString();
	}
}

package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.JsonExpression;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.reactive.ReactiveSchemaUtil;
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
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.runtime.StatementMessageType;
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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public class ReactiveKIRuntime extends AbstractReactiveFunction {

	private static final String DEBUG_STEP = "Step : ";

	private static final String PARAMETER_NEEDS_A_VALUE = "Parameter \"$\" needs a value";

	private static final Pattern STEP_REGEX_PATTERN = Pattern
	        .compile("Steps\\.([a-zA-Z0-9\\\\-]+)\\.([a-zA-Z0-9\\\\-]+)");

	private static final int VERSION = 1;

	private static final int MAX_EXECUTION_ITERATIONS = 10000000;

	private static final Logger logger = LoggerFactory.getLogger(ReactiveKIRuntime.class);

	private final FunctionDefinition fd;

	private final boolean debugMode;

	private final StringBuilder sb = new StringBuilder();

	public ReactiveKIRuntime(FunctionDefinition fd) {
		this(fd, false);
	}

	public ReactiveKIRuntime(FunctionDefinition fd, boolean debugMode) {

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

	public Mono<ExecutionGraph<String, StatementExecution>> getExecutionPlan(ReactiveRepository<ReactiveFunction> fRepo,
	        ReactiveRepository<Schema> sRepo) {

		return Flux.fromIterable(this.fd.getSteps()
		        .values())
		        .flatMap(e -> this.prepareStatementExecution(e, fRepo, sRepo))
		        .collectList()
		        .map(e ->
				{
			        ExecutionGraph<String, StatementExecution> g = new ExecutionGraph<>();
			        for (var x : e)
				        g.addVertex(x);

			        this.makeEdges(g)
			                .getT2()
			                .forEach((key, value) ->
							{
				                StatementExecution ex = g.getNodeMap()
				                        .get(key)
				                        .getData();
				                if (ex == null)
					                return;
				                ex.addMessage(StatementMessageType.ERROR, value);

			                });

			        return g;
		        })
		        .defaultIfEmpty(new ExecutionGraph<>());
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(final ReactiveFunctionExecutionParameters inContext) {

		if (inContext.getContext() == null)
			inContext.setContext(new ConcurrentHashMap<>());

		if (inContext.getEvents() == null)
			inContext.setEvents(new ConcurrentHashMap<>());

		if (inContext.getSteps() == null)
			inContext.setSteps(new ConcurrentHashMap<>());

		inContext.addTokenValueExtractor(new ArgumentsTokenValueExtractor(
		        inContext.getArguments() == null ? Map.of() : inContext.getArguments()));

		if (this.debugMode) {

			this.sb.append("Executing: ")
			        .append(this.fd.getNamespace())
			        .append('.')
			        .append(this.fd.getName())
			        .append("\n")
			        .append("Parameters: ")
			        .append(inContext)
			        .append("\n");

		}

		Mono<ExecutionGraph<String, StatementExecution>> eGraph = this
		        .getExecutionPlan(inContext.getFunctionRepository(), inContext.getSchemaRepository());

		return eGraph.flatMap(g -> g.getVerticesDataFlux()
		        .flatMap(e -> Flux.fromIterable(e.getMessages()))
		        .collectList()
		        .flatMap(msgs ->
				{
			        if (this.debugMode) {
				        this.sb.append(g.toString())
				                .append("\n");
			        }
			        if (logger.isDebugEnabled()) {
				        logger.debug(
				                StringFormatter.format("Executing : $.$", this.fd.getNamespace(), this.fd.getName()));
				        logger.debug(eGraph.toString());
			        }

			        if (!msgs.isEmpty())
				        return Mono.error(new KIRuntimeException(
				                "Please fix the errors in the function definition before execution : \n" + msgs));

			        return this.executeGraph(g, inContext);
		        }));
	}

	private Mono<FunctionOutput> executeGraph(ExecutionGraph<String, StatementExecution> eGraph,
	        ReactiveFunctionExecutionParameters inContext) {
		LinkedList<GraphVertex<String, StatementExecution>> eq = new LinkedList<>();
		eq.addAll(eGraph.getVerticesWithNoIncomingEdges());

		LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> bq = new LinkedList<>();

		return Mono.just(Tuples.of(eq, bq))
		        .expandDeep(tup ->
				{
			        if ((tup.getT1()
			                .isEmpty()
			                && tup.getT2()
			                        .isEmpty())
			                || (inContext.getEvents()
			                        .containsKey(Event.OUTPUT)))
				        return Mono.empty();

			        return this.processBranchQue(inContext, tup.getT1(), tup.getT2())
			                .flatMap(e -> this.processExecutionQue(inContext, tup.getT1(), tup.getT2()))
			                .flatMap(e ->
							{
				                inContext.setCount(inContext.getCount() + 1);

				                if (inContext.getCount() == MAX_EXECUTION_ITERATIONS)
					                return Mono.error(new KIRuntimeException("Execution locked in an infinite loop"));

				                return Mono.just(Tuples.of(tup.getT1(), tup.getT2()));
			                });
		        })
		        .collectList()
		        .flatMap(tups ->
				{

			        if (!eGraph.isSubGraph() && inContext.getEvents()
			                .isEmpty()) {

				        var eventMap = this.getSignature()
				                .getEvents();
				        if (!eventMap.isEmpty() && eventMap.get(Event.OUTPUT)
				                .getParameters() != null && !eventMap.get(Event.OUTPUT)
				                        .getParameters()
				                        .isEmpty())
					        return Mono.error(new KIRuntimeException("No events raised"));
			        }
			        
			        List<EventResult> list = inContext.getEvents()
	                .entrySet()
	                .stream()
	                .flatMap(e -> e.getValue()
	                        .stream()
	                        .map(v -> EventResult.of(e.getKey(), v)))
	                .toList();
			        
			        if (!eGraph.isSubGraph() && list.isEmpty()) {
			        	list = List.of(EventResult.outputOf(Map.of()));
			        }

			        return Mono.just(new FunctionOutput(list));

		        });
	}

	private Mono<Boolean> processExecutionQue(ReactiveFunctionExecutionParameters inContext,
	        LinkedList<GraphVertex<String, StatementExecution>> executionQue,
	        LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue) {

		if (executionQue.isEmpty())
			return Mono.just(false);

		var vertex = executionQue.pop();

		if (!allDependenciesResolved(vertex, inContext.getSteps())) {
			executionQue.add(vertex);
			return Mono.just(false);
		}

		return executeVertex(vertex, inContext, branchQue, executionQue);
	}

	private Mono<Boolean> processBranchQue(ReactiveFunctionExecutionParameters inContext,
	        LinkedList<GraphVertex<String, StatementExecution>> executionQue,
	        LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue) {
		if (branchQue.isEmpty())
			return Mono.just(false);

		var branch = branchQue.pop();

		if (!allDependenciesResolved(branch.getT2(), inContext.getSteps())) {
			branchQue.add(branch);
			return Mono.just(false);
		}

		return executeBranch(inContext, executionQue, branch);
	}

	private Mono<Boolean> executeBranch(ReactiveFunctionExecutionParameters inContext,
	        LinkedList<GraphVertex<String, StatementExecution>> executionQue,
	        Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>> branch) {

		return Mono.just(Tuples.of(branch.getT1(), inContext, Optional.<EventResult>empty()))
		        .expandDeep(e ->
				{
			        if (e.getT3()
			                .isPresent()
			                && e.getT3()
			                        .get()
			                        .getName()
			                        .equals(Event.OUTPUT)) {
				        return Mono.empty();
			        }

			        return this.executeGraph(e.getT1(), e.getT2())
			                .flatMap(funcOut ->
							{
				                GraphVertex<String, StatementExecution> vertex = branch.getT4();

				                EventResult nextOutput = branch.getT3()
				                        .next();

				                if (nextOutput != null) {
					                inContext.getSteps()
					                        .computeIfAbsent(vertex.getData()
					                                .getStatement()
					                                .getStatementName(), k -> new ConcurrentHashMap<>())
					                        .put(nextOutput.getName(),
					                                resolveInternalExpressions(nextOutput.getResult(), inContext));

					                if (this.debugMode) {
						                var s = vertex.getData()
						                        .getStatement();
						                this.sb.append(DEBUG_STEP)
						                        .append(s.getStatementName())
						                        .append(" => ")
						                        .append(s.getNamespace())
						                        .append('.')
						                        .append(s.getName())
						                        .append("\n");
						                this.sb.append("Event : ")
						                        .append(nextOutput.getName())
						                        .append("\n")
						                        .append(inContext.getSteps()
						                                .get(s.getStatementName())
						                                .get(nextOutput.getName()))
						                        .append("\n");

					                }
				                }

				                return Mono.just(Tuples.of(branch.getT1(), inContext, Optional.of(nextOutput)));

			                });
		        })
		        .collectList()
		        .map(e ->
				{

			        GraphVertex<String, StatementExecution> vertex = branch.getT4();
			        EventResult nextOutput = e.isEmpty() ? null
			                : e.get(e.size() - 1)
			                        .getT3()
			                        .orElse(null);

			        if (nextOutput != null && nextOutput.getName()
			                .equals(Event.OUTPUT) && vertex.getOutVertices()
			                        .containsKey(Event.OUTPUT)) {

				        vertex.getOutVertices()
				                .get(Event.OUTPUT)
				                .stream()
				                .filter(x -> this.allDependenciesResolved(x, inContext.getSteps()))
				                .forEach(executionQue::add);
			        }

			        return true;
		        });
	}

	private Mono<Boolean> executeVertex(GraphVertex<String, StatementExecution> vertex,
	        ReactiveFunctionExecutionParameters inContext,
	        LinkedList<Tuple4<ExecutionGraph<String, StatementExecution>, List<Tuple2<String, String>>, FunctionOutput, GraphVertex<String, StatementExecution>>> branchQue,
	        LinkedList<GraphVertex<String, StatementExecution>> executionQue) {

		Statement s = vertex.getData()
		        .getStatement();

		if (s.getExecuteIftrue() != null && !s.getExecuteIftrue()
		        .isEmpty()) {

			boolean allTrue = s.getExecuteIftrue()
			        .entrySet()
			        .stream()
			        .filter(Entry::getValue)
			        .map(e -> new ExpressionEvaluator(e.getKey()).evaluate(inContext.getValuesMap()))
			        .allMatch(e ->
					{
				        if (e == null || JsonNull.INSTANCE.equals(e))
					        return false;
				        if (!e.isJsonPrimitive())
					        return true;

				        JsonPrimitive jp = e.getAsJsonPrimitive();
				        return !jp.isBoolean() || jp.getAsBoolean();
			        });

			if (!allTrue)
				return Mono.just(true);
		}

		Mono<ReactiveFunction> monoFunction = inContext.getFunctionRepository()
		        .find(s.getNamespace(), s.getName());

		return monoFunction.flatMap(fun -> {

			Map<String, Parameter> paramSet = fun.getSignature()
			        .getParameters();

			Map<String, JsonElement> arguments = getArgumentsFromParametersMap(inContext, s, paramSet);

			if (this.debugMode) {

				this.sb.append(DEBUG_STEP)
				        .append(s.getStatementName())
				        .append(" => ")
				        .append(s.getNamespace())
				        .append('.')
				        .append(s.getName())
				        .append("\n");
				this.sb.append("Arguments : \n")
				        .append(arguments)
				        .append("\n");
			}

			Map<String, ContextElement> context = inContext.getContext();

			ReactiveFunctionExecutionParameters fep;

			if (fun instanceof ReactiveKIRuntime) {
				fep = new ReactiveFunctionExecutionParameters(inContext.getFunctionRepository(),
				        inContext.getSchemaRepository(), inContext.getExecutionId() + "_" + s.getStatementName())
				        .setArguments(arguments)
				        .setValuesMap(inContext.getValuesMap()
				                .values()
				                .stream()
				                .filter(e -> !e.getPrefix()
				                        .equals(ArgumentsTokenValueExtractor.PREFIX)
				                        && !e.getPrefix()
				                                .equals(OutputMapTokenValueExtractor.PREFIX)
				                        && !e.getPrefix()
				                                .equals(ContextTokenValueExtractor.PREFIX))
				                .collect(Collectors.toMap(TokenValueExtractor::getPrefix,
				                        java.util.function.Function.identity())));
			} else {
				fep = new ReactiveFunctionExecutionParameters(inContext.getFunctionRepository(),
				        inContext.getSchemaRepository(), inContext.getExecutionId())
				        .setValuesMap(inContext.getValuesMap())
				        .setContext(context)
				        .setArguments(arguments)
				        .setEvents(inContext.getEvents())
				        .setSteps(inContext.getSteps())
				        .setStatementExecution(vertex.getData())
				        .setCount(inContext.getCount())
				        .setExecutionContext(inContext.getExecutionContext());
			}

			return fun.execute(fep);
		})
		        .flatMap(result ->
				{

			        EventResult er = result.next();

			        if (er == null)
				        return Mono.error(new KIRuntimeException(
				                StringFormatter.format("Executing $ returned no events", s.getStatementName())));

			        boolean isOutput = er.getName()
			                .equals(Event.OUTPUT);

			        inContext.getSteps()
			                .computeIfAbsent(s.getStatementName(), k -> new ConcurrentHashMap<>())
			                .put(er.getName(), resolveInternalExpressions(er.getResult(), inContext));

			        if (this.debugMode) {

				        this.sb.append(DEBUG_STEP)
				                .append(s.getStatementName())
				                .append(" => ")
				                .append(s.getNamespace())
				                .append('.')
				                .append(s.getName())
				                .append("\n");

				        this.sb.append("Event :")
				                .append(er.getName())
				                .append("\n")
				                .append(inContext.getSteps()
				                        .get(s.getStatementName())
				                        .get(er.getName()))
				                .append("\n");
			        }

			        if (!isOutput) {

				        var subGraph = vertex.getSubGraphOfType(er.getName());
				        List<Tuple2<String, String>> unResolvedDependencies = this.makeEdges(subGraph)
				                .getT1();
				        branchQue.add(Tuples.of(subGraph, unResolvedDependencies, result, vertex));
			        } else {

				        Set<GraphVertex<String, StatementExecution>> out = vertex.getOutVertices()
				                .get(Event.OUTPUT);
				        if (out != null)
					        out.stream()
					                .filter(e -> this.allDependenciesResolved(e, inContext.getSteps()))
					                .forEach(executionQue::add);
			        }

			        return Mono.just(true);

		        });
	}

	private Map<String, JsonElement> resolveInternalExpressions(Map<String, JsonElement> result,
	        ReactiveFunctionExecutionParameters inContext) {

		if (result == null)
			return result;

		return result.entrySet()
		        .stream()
		        .map(e -> Tuples.of(e.getKey(), resolveInternalExpression(e.getValue(), inContext)))
		        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private JsonElement resolveInternalExpression(JsonElement value, ReactiveFunctionExecutionParameters inContext) {

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
		        .takeWhile(e -> output.containsKey(e.getT1()) && output.get(e.getT1())
		                .containsKey(e.getT2()))
		        .count() == unResolvedDependencies.size();
	}

	private boolean allDependenciesResolved(GraphVertex<String, StatementExecution> vertex,
	        Map<String, Map<String, Map<String, JsonElement>>> output) {

		if (vertex.getInVertices()
		        .isEmpty())
			return true;

		return vertex.getInVertices()
		        .stream()
		        .filter(e ->
				{
			        String stepName = e.getT1()
			                .getData()
			                .getStatement()
			                .getStatementName();
			        String type = e.getT2();

			        return !(output.containsKey(stepName) && output.get(stepName)
			                .containsKey(type));
		        })
		        .count() == 0;
	}

	private Map<String, JsonElement> getArgumentsFromParametersMap(final ReactiveFunctionExecutionParameters inContext,
	        Statement s, Map<String, Parameter> paramSet) {

		return s.getParameterMap()
		        .entrySet()
		        .stream()
		        .map(e ->
				{
			        List<ParameterReference> prList = e.getValue() == null ? List.of()
			                : new ArrayList<>(e.getValue()
			                        .values());

			        JsonElement ret = JsonNull.INSTANCE;

			        if (prList == null || prList.isEmpty())
				        return Tuples.of(e.getKey(), ret);

			        Parameter pDef = paramSet.get(e.getKey());

			        if (pDef.isVariableArgument()) {

				        ret = new JsonArray();

				        prList.stream()
				                .sorted((a, b) -> a.getOrder() - b.getOrder())
				                .map(r -> this.parameterReferenceEvaluation(inContext, r))
				                .filter(r -> r != null && !r.isJsonNull())
				                .flatMap(r -> r.isJsonArray() ? StreamSupport.stream(r.getAsJsonArray()
				                        .spliterator(), false) : Stream.of(r))
				                .forEachOrdered(((JsonArray) ret)::add);

			        } else {

				        ret = this.parameterReferenceEvaluation(inContext, prList.get(0));
			        }

			        return Tuples.of(e.getKey(), ret);
		        })
		        .filter(e -> !(e.getT2() == null || e.getT2()
		                .isJsonNull()))
		        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private JsonElement parameterReferenceEvaluation(final ReactiveFunctionExecutionParameters inContext,
	        ParameterReference ref) {

		JsonElement ret = null;

		if (ref.getType() == ParameterReferenceType.VALUE) {
			ret = this.resolveInternalExpression(ref.getValue(), inContext);
		} else if (ref.getType() == ParameterReferenceType.EXPRESSION && ref.getExpression() != null
		        && !ref.getExpression()
		                .isBlank()) {
			ExpressionEvaluator exp = new ExpressionEvaluator(ref.getExpression());
			ret = exp.evaluate(inContext.getValueExtractors());
		}
		return ret;
	}

	private Mono<StatementExecution> prepareStatementExecution(Statement s, ReactiveRepository<ReactiveFunction> fRepo, // NOSONAR
	        ReactiveRepository<Schema> sRepo) {
		// Breaking this execution doesn't make sense.

		return fRepo.find(s.getNamespace(), s.getName())
		        .map(ReactiveFunction::getSignature)
		        .map(FunctionSignature::getParameters)
		        .flatMap(paramSet ->
				{

			        if (s.getParameterMap() == null)
				        return Mono.just(new StatementExecution(s));

			        StatementExecution se = new StatementExecution(s);

			        return Flux.fromIterable(s.getParameterMap()
			                .entrySet())
			                .flatMap(param ->
							{
				                Parameter p = paramSet.get(param.getKey());
				                List<ParameterReference> refList = param.getValue() == null ? List.of()
				                        : new ArrayList<>(param.getValue()
				                                .values());

				                if ((refList == null || refList.isEmpty()) && !p.isVariableArgument()) {

					                return ReactiveSchemaUtil.hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo)
					                        .flatMap(hasDefault ->
											{
						                        if (!hasDefault.booleanValue())
							                        se.addMessage(StatementMessageType.ERROR, StringFormatter
							                                .format(PARAMETER_NEEDS_A_VALUE, p.getParameterName()));
						                        return Mono.just(Tuples.of(param.getKey(), se));
					                        });

				                } else if (p.isVariableArgument()) {

					                if (refList != null) {

						                return Flux.fromIterable(refList)
						                        .sort((a, b) -> a.getOrder() - b.getOrder())
						                        .flatMap(ref -> parameterReferenceValidation(se, p, ref, sRepo))
						                        .collectList()
						                        .map(e -> Tuples.of(param.getKey(), se));
					                }

				                } else if (refList != null && !refList.isEmpty()) {
					                ParameterReference ref = refList.get(0);
					                return parameterReferenceValidation(se, p, ref, sRepo)
					                        .map(e -> Tuples.of(param.getKey(), e));
				                }

				                return Mono.just(Tuples.of(param.getKey(), se));
			                })
			                .collectList()
			                .map(lst ->
							{

				                Set<String> leftOver = new HashSet<>(paramSet.keySet());
				                lst.stream()
				                        .map(Tuple2::getT1)
				                        .forEach(leftOver::remove);
				                if (se.getStatement()
				                        .getDependentStatements() != null)
					                for (Entry<String, Boolean> statement : s.getDependentStatements()
					                        .entrySet())
						                if (statement.getValue()
						                        .booleanValue())
							                se.addDependency(statement.getKey());

				                return leftOver;
			                })
			                .flatMap(remaining ->
							{

				                return Flux.fromIterable(remaining)
				                        .map(paramSet::get)
				                        .filter(Predicate.not(Parameter::isVariableArgument))
				                        .flatMap(p -> ReactiveSchemaUtil
				                                .hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo)
				                                .map(hasDefaultValue -> hasDefaultValue.booleanValue() ? se
				                                        : se.addMessage(StatementMessageType.ERROR,
				                                                StringFormatter.format(PARAMETER_NEEDS_A_VALUE,
				                                                        p.getParameterName()))))
				                        .collectList()
				                        .map(e -> se);
			                });
		        })
		        .defaultIfEmpty((new StatementExecution(s)).addMessage(StatementMessageType.ERROR,
		                StringFormatter.format("$.$ is not available", s.getNamespace(), s.getName())));

	}

	private Mono<StatementExecution> parameterReferenceValidation(StatementExecution se, Parameter p, // NOSONAR
	        ParameterReference ref, ReactiveRepository<Schema> sRepo) {

		if (ref == null) {

			return ReactiveSchemaUtil.getDefaultValue(p.getSchema(), sRepo)
			        .map(e -> se)
			        .switchIfEmpty(Mono.defer(() -> Mono.just(se.addMessage(StatementMessageType.ERROR,
			                StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName())))));
		} else if (ref.getType() == ParameterReferenceType.VALUE) {

			if (ref.getValue() == null || JsonNull.INSTANCE.equals(ref.getValue())) {

				return ReactiveSchemaUtil.hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo)
				        .map(hasDefault -> hasDefault.booleanValue() ?

				                se :

				                se.addMessage(StatementMessageType.ERROR,
				                        StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName())));
			}

			LinkedList<Tuple2<Schema, JsonElement>> paramElements = new LinkedList<>();
			paramElements.push(Tuples.of(p.getSchema(), ref.getValue()));

			while (!paramElements.isEmpty()) { // NOSONAR
				// Breaking this loop doesn't make sense
				Tuple2<Schema, JsonElement> e = paramElements.pop();

				if (e.getT2() instanceof JsonExpression jexp) {
					this.addDependencies(se, jexp.getExpression());
				} else {

					if (e.getT1() == null || e.getT1()
					        .getType() == null)
						continue;

					if (e.getT1()
					        .getType()
					        .contains(SchemaType.ARRAY)
					        && e.getT2()
					                .isJsonArray()) {
						ArraySchemaType ast = e.getT1()
						        .getItems();
						if (ast == null) {
							continue;
						}
						if (ast.isSingleType()) {
							for (JsonElement je : e.getT2()
							        .getAsJsonArray())
								paramElements.push(Tuples.of(ast.getSingleSchema(), je));
						} else {
							JsonArray array = e.getT2()
							        .getAsJsonArray();
							for (int i = 0; i < array.size(); i++) {
								paramElements.push(Tuples.of(ast.getTupleSchema()
								        .get(i), array.get(i)));
							}
						}
					} else if (e.getT1()
					        .getType()
					        .contains(SchemaType.OBJECT)
					        && e.getT2()
					                .isJsonObject()) {

						Schema sch = e.getT1();

						if (sch.getName()
						        .equals(Parameter.EXPRESSION.getName())
						        && sch.getNamespace()
						                .equals(Parameter.EXPRESSION.getNamespace())) {
							JsonObject obj = e.getT2()
							        .getAsJsonObject();
							boolean isExpression = obj.get("isExpression")
							        .getAsBoolean();
							if (isExpression) {
								this.addDependencies(se, obj.get("value")
								        .getAsString());
							}
						} else {

							for (Entry<String, JsonElement> entry : e.getT2()
							        .getAsJsonObject()
							        .entrySet()) {
								paramElements.push(Tuples.of(sch.getProperties()
								        .get(entry.getKey()), entry.getValue()));
							}
						}
					}
				}
			}

			return Mono.just(se);

		} else if (ref.getType() == ParameterReferenceType.EXPRESSION) {

			if (ref.getExpression() == null || ref.getExpression()
			        .isBlank()) {
				return ReactiveSchemaUtil.getDefaultValue(p.getSchema(), sRepo)
				        .map(e -> se)
				        .switchIfEmpty(Mono.defer(() -> Mono.just(se.addMessage(StatementMessageType.ERROR,
				                StringFormatter.format(PARAMETER_NEEDS_A_VALUE, p.getParameterName())))));
			} else {
				try {
					// TODO: Type check for the resulting expression has to be done here...
					this.addDependencies(se, ref.getExpression());
				} catch (KIRuntimeException ex) {
					return Mono.just(se.addMessage(StatementMessageType.ERROR,
					        StringFormatter.format("Error evaluating $ : ", ref.getExpression(), ex.getMessage())));
				}
			}
		}
		return Mono.just(se);
	}

	private void addDependencies(StatementExecution se, String expression) {

		Matcher m = STEP_REGEX_PATTERN.matcher(expression);

		while (m.find()) {

			if (m.groupCount() != 2)
				continue;
			se.addDependency(m.group(0));
		}
	}

	public Tuple2<List<Tuple2<String, String>>, Map<String, String>> makeEdges(
	        ExecutionGraph<String, StatementExecution> graph) {

		List<Tuple2<String, String>> retValue = new ArrayList<>();
		Map<String, String> retMap = new HashMap<>();

		for (GraphVertex<String, StatementExecution> e : graph.getNodeMap()
		        .values()) {

			if (e.getData()
			        .getDependencies() == null)
				continue;

			for (String d : e.getData()
			        .getDependencies()) {

				int secondDot = d.indexOf('.', 6);
				String step = d.substring(6, secondDot);
				int eventDot = d.indexOf('.', secondDot + 1);
				String event = eventDot == -1 ? d.substring(secondDot + 1) : d.substring(secondDot + 1, eventDot);

				if (!graph.getNodeMap()
				        .containsKey(step)) {
					retValue.add(Tuples.of(step, event));
					retMap.put(e.getData()
					        .getStatement()
					        .getStatementName(), StringFormatter.format("Unable to find the step with name $", step));

				} else

					e.addInEdgeTo(graph.getNodeMap()
					        .get(step), event);

			}
		}

		return Tuples.of(retValue, retMap);

	}

	public String getDebugString() {
		return this.sb.toString();
	}
}

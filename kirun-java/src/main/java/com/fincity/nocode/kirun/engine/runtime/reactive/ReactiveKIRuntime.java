package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import com.fincity.nocode.kirun.engine.function.reactive.IDefinitionBasedFunction;
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
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.runtime.StatementMessageType;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.graph.ExecutionGraph;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution.BranchRef;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution.GraphFrame;
import com.fincity.nocode.kirun.engine.runtime.suspend.WakeCondition;
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
import reactor.util.function.Tuples;

public class ReactiveKIRuntime extends AbstractReactiveFunction implements IDefinitionBasedFunction {

	/**
	 * A branch waiting to run: the sub-graph opened by a step's non-output event, together with the
	 * live output that step is producing events from.
	 *
	 * Replaces what used to be a five-type-argument Tuple4. Naming the parts matters here because
	 * the resume path has to rebuild these from a snapshot, and {@link #eventName} - which the
	 * tuple never carried - is what identifies which sub-graph of the owning vertex this is.
	 */
	private record BranchEntry(ExecutionGraph<String, StatementExecution> subGraph,
			List<Tuple2<String, String>> unresolvedDependencies, FunctionOutput output,
			GraphVertex<String, StatementExecution> vertex, String eventName) {

		String statementName() {
			return this.vertex.getData()
					.getStatement()
					.getStatementName();
		}
	}

	private static final String PARAMETER_NEEDS_A_VALUE = "Parameter \"$\" needs a value";

	private static final Pattern STEP_REGEX_PATTERN = Pattern
			.compile("Steps\\.([a-zA-Z0-9\\\\-]+)\\.([a-zA-Z0-9\\\\-]+)");

	private static final int VERSION = 1;

	// Counts statement iterations, not loop passes: a ForEachLoop over N rows with S statements
	// in its body consumes roughly N * (S + 1). 1_000_000 still leaves ~10x headroom over a
	// 100k-row loop while tripping a slow runaway 10x sooner than the previous 10_000_000.
	// Overridable so a genuine batch workload can raise it without a rebuild.
	private static final int MAX_EXECUTION_ITERATIONS = Integer.getInteger(
			"kirun.execution.maxIterations", 1000000);

	private static final Logger logger = LoggerFactory.getLogger(ReactiveKIRuntime.class);

	private final FunctionDefinition fd;

	private final boolean debugMode;

	private com.fincity.nocode.kirun.engine.runtime.debug.DebugCollector debugCollector;

	// Cache for resolved functions to avoid repeated lookups
	private final Map<String, ReactiveFunction> functionCache = new ConcurrentHashMap<>();

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
	
	/**
	 * Get a function from cache or repository. Caches the result for future lookups.
	 */
	private Mono<ReactiveFunction> getCachedFunction(ReactiveRepository<ReactiveFunction> fRepo, String namespace, String name) {
		String key = namespace + "." + name;
		ReactiveFunction cached = functionCache.get(key);
		if (cached != null) {
			return Mono.just(cached);
		}
		return fRepo.find(namespace, name)
				.doOnNext(fun -> functionCache.put(key, fun));
	}

	public Mono<ExecutionGraph<String, StatementExecution>> getExecutionPlan(ReactiveRepository<ReactiveFunction> fRepo,
			ReactiveRepository<Schema> sRepo) {

		return Flux.fromIterable(this.fd.getSteps()
				.values())
				.flatMap(e -> this.prepareStatementExecution(e, fRepo, sRepo))
				.collectList()
				.map(e -> {
					ExecutionGraph<String, StatementExecution> g = new ExecutionGraph<>();
					for (var x : e)
						g.addVertex(x);

					this.makeEdges(g)
							.getT2()
							.forEach((key, value) -> {
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

		// Stop and go: a resuming activation starts from the snapshot's state rather than from
		// empty maps, so the defaults below find everything already populated.
		if (inContext.isResuming())
			this.restoreState(inContext);

		if (inContext.getContext() == null)
			inContext.setContext(Collections.synchronizedMap(new LinkedHashMap<>()));

		if (inContext.getEvents() == null)
			inContext.setEvents(Collections.synchronizedMap(new LinkedHashMap<>()));

		if (inContext.getSteps() == null)
			inContext.setSteps(Collections.synchronizedMap(new LinkedHashMap<>()));

		inContext.addTokenValueExtractor(new ArgumentsTokenValueExtractor(
				inContext.getArguments() == null ? Map.of() : inContext.getArguments()));

		// Create debug collector if debug mode enabled
		if (this.debugMode) {
			// Check if a collector already exists (from parent call)
			if (inContext.getDebugCollector() == null) {
				// Create new collector for top-level execution
				String functionName = this.fd.getNamespace() != null
						? this.fd.getNamespace() + "." + this.fd.getName()
						: this.fd.getName();
				this.debugCollector = new com.fincity.nocode.kirun.engine.runtime.debug.DebugCollector(
						inContext.getExecutionId(),
						functionName);
				inContext.setDebugCollector(this.debugCollector);
			} else {
				// Reuse the existing collector from parent
				this.debugCollector = inContext.getDebugCollector();
			}
		}

		Mono<ExecutionGraph<String, StatementExecution>> eGraph = this
				.getExecutionPlan(inContext.getFunctionRepository(), inContext.getSchemaRepository());

		return eGraph.flatMap(g -> g.getVerticesDataFlux()
				.flatMap(e -> Flux.fromIterable(e.getMessages()))
				.collectList()
				.flatMap(msgs -> {
					if (logger.isDebugEnabled()) {
						logger.debug(
								StringFormatter.format("Executing : $.$", this.fd.getNamespace(), this.fd.getName()));
						logger.debug(eGraph.toString());
					}

					if (!msgs.isEmpty()) {
						// Mark execution as errored before throwing
						if (this.debugCollector != null) {
							this.debugCollector.getExecutionLog().markErrored();
						}
						return Mono.error(new KIRuntimeException(
								"Please fix the errors in the function definition before execution : \n" + msgs));
					}

					return this.executeGraph(g, inContext)
							.flatMap(output -> this.assembleSuspension(inContext, output));
				}))
				.doOnError(error -> {
					// Mark execution as errored on any error
					if (this.debugCollector != null) {
						this.debugCollector.getExecutionLog().markErrored();
					}
				})
				.doFinally(signal -> {
					// Mark execution as complete
					if (this.debugCollector != null) {
						this.debugCollector.endExecution();
					}
				});
	}

	private Mono<FunctionOutput> executeGraph(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext) {
		return this.executeGraph(eGraph, inContext, null, null);
	}

	/**
	 * Runs one graph level.
	 *
	 * The owner arguments identify which level this is - null/null for the function's own graph, or
	 * the statement and event that opened this sub-graph. They are only needed for stop and go: a
	 * snapshot has one frame per level, and a frame has to be matched back to the level it came
	 * from.
	 */
	private Mono<FunctionOutput> executeGraph(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext, String ownerStatementName, String eventName) {

		LinkedList<GraphVertex<String, StatementExecution>> eq = new LinkedList<>();
		LinkedList<BranchEntry> bq = new LinkedList<>();

		GraphFrame frame = inContext.takeResumeFrame(ownerStatementName, eventName);

		if (frame == null) {

			eq.addAll(eGraph.getVerticesWithNoIncomingEdges());

			// Clearing a sub-graph's previous outputs is what makes a loop body start each pass
			// fresh. It must not happen when restoring a frame, though: on resume the completed
			// statements of the interrupted pass are exactly what we are trying to keep.
			if (eGraph.isSubGraph()) {
				for (StatementExecution x : eGraph
						.getVerticesData()) {
					inContext.getSteps().remove(x.getStatement().getStatementName());
				}
			}

			return this.runGraph(eGraph, inContext, ownerStatementName, eventName, eq, bq);
		}

		return this.restoreFrame(eGraph, inContext, frame, eq, bq)
				.then(Mono.defer(() -> this.runGraph(eGraph, inContext, ownerStatementName, eventName, eq, bq)));
	}

	private Mono<FunctionOutput> runGraph(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext, String ownerStatementName, String eventName,
			LinkedList<GraphVertex<String, StatementExecution>> eq, LinkedList<BranchEntry> bq) {

		return Mono.just(Tuples.of(eq, bq))
				.expandDeep(tup -> {
					if ((tup.getT1()
							.isEmpty()
							&& tup.getT2()
									.isEmpty())
							|| (inContext.getEvents()
									.containsKey(Event.OUTPUT))
							|| inContext.isSuspended())
						return Mono.empty();

					return this.processBranchQue(inContext, tup.getT1(), tup.getT2())
							.flatMap(e -> this.processExecutionQue(inContext, tup.getT1(), tup.getT2()))
							.flatMap(e -> {
								inContext.setCount(inContext.getCount() + 1);

								// Must be >=, not ==. The counter is a plain int bumped with a
								// read-modify-write, and nested executeGraph calls share the same
								// context, so the exact boundary value can be skipped and an ==
								// check would then never trip at all.
								if (inContext.getCount() >= MAX_EXECUTION_ITERATIONS)
									return Mono.error(new KIRuntimeException(StringFormatter.format(
											"Execution locked in an infinite loop : $.$ exceeded $ statement iterations",
											this.fd.getNamespace(), this.fd.getName(), MAX_EXECUTION_ITERATIONS)));

								return Mono.just(Tuples.of(tup.getT1(), tup.getT2()));
							});
				})
				.collectList()
				.flatMap(tups -> {

					// Stop and go: record what this level still had to do, then let the suspension
					// travel up. Nothing else about this level is inspected - it did not finish.
					if (inContext.isSuspended()) {
						inContext.addCapturedFrame(this.captureFrame(ownerStatementName, eventName, eq, bq));
						return Mono.just(new FunctionOutput(this.raisedEvents(inContext)));
					}

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

					List<EventResult> list = this.raisedEvents(inContext);

					if (!eGraph.isSubGraph() && list.isEmpty()) {
						list = List.of(EventResult.outputOf(Map.of()));
					}

					return Mono.just(new FunctionOutput(list));

				});
	}

	private List<EventResult> raisedEvents(ReactiveFunctionExecutionParameters inContext) {

		return inContext.getEvents()
				.entrySet()
				.stream()
				.flatMap(e -> e.getValue()
						.stream()
						.map(v -> EventResult.of(e.getKey(), v)))
				.toList();
	}

	private GraphFrame captureFrame(String ownerStatementName, String eventName,
			LinkedList<GraphVertex<String, StatementExecution>> eq, LinkedList<BranchEntry> bq) {

		GraphFrame frame = new GraphFrame().setOwnerStatementName(ownerStatementName)
				.setEventName(eventName);

		frame.setExecutionQueue(eq.stream()
				.map(GraphVertex::getKey)
				.collect(Collectors.toCollection(ArrayList::new)));

		frame.setBranchQueue(bq.stream()
				.map(b -> new BranchRef().setStatementName(b.statementName())
						.setEventName(b.eventName())
						.setConsumedEvents(b.output()
								.getConsumedCount()))
				.collect(Collectors.toCollection(ArrayList::new)));

		return frame;
	}

	/**
	 * Rebuilds one level's pending work from a snapshot.
	 *
	 * Vertices come back by statement name from the rebuilt graph, and branches by re-executing the
	 * step that owns them - see {@link #rebuildBranch}. The frame below this one, if any, describes
	 * the branch that was actually mid-flight when the execution stopped; it was popped off the
	 * queue before it ran, so it has to be put back at the head to be picked up first.
	 */
	private Mono<Boolean> restoreFrame(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext, GraphFrame frame,
			LinkedList<GraphVertex<String, StatementExecution>> eq, LinkedList<BranchEntry> bq) {

		for (String statementName : frame.getExecutionQueue()) {

			GraphVertex<String, StatementExecution> vertex = eGraph.getVertex(statementName);

			if (vertex == null)
				return Mono.error(new KIRuntimeException(StringFormatter.format(
						"Cannot resume : the definition of $.$ no longer has a step named $",
						this.fd.getNamespace(), this.fd.getName(), statementName)));

			eq.add(vertex);
		}

		List<BranchRef> refs = new ArrayList<>(frame.getBranchQueue());

		GraphFrame next = inContext.peekResumeFrame();

		if (next != null)
			// The branch that was actually mid-flight had already been popped off the queue when
			// the execution stopped, so put it back at the head to be picked up first.
			refs.add(0, new BranchRef().setStatementName(next.getOwnerStatementName())
					.setEventName(next.getEventName())
					.setConsumedEvents(next.getOwnerConsumedEvents()));
		else
			// No deeper frame means this is the level the step stopped on.
			this.seedResumedStep(eGraph, inContext, eq);

		return Flux.fromIterable(refs)
				.concatMap(ref -> this.rebuildBranch(eGraph, inContext, ref))
				.doOnNext(bq::add)
				.then(Mono.just(true));
	}

	/**
	 * Recreates a queued branch.
	 *
	 * The sub-graph and its unresolved dependencies are derived the same way {@link #executeVertex}
	 * derives them, so they come back identical. The live {@link FunctionOutput} cannot be stored,
	 * so the owning step is executed again to produce a fresh one and then wound forward. That is
	 * safe for the steps that can own a branch - the loops and If - because a loop's position now
	 * lives in the execution context rather than in the generator's closure.
	 */
	private Mono<BranchEntry> rebuildBranch(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext, BranchRef ref) {

		GraphVertex<String, StatementExecution> vertex = eGraph.getVertex(ref.getStatementName());

		if (vertex == null)
			return Mono.error(new KIRuntimeException(StringFormatter.format(
					"Cannot resume : the definition of $.$ no longer has a step named $", this.fd.getNamespace(),
					this.fd.getName(), ref.getStatementName())));

		Statement s = vertex.getData()
				.getStatement();

		return this.getCachedFunction(inContext.getFunctionRepository(), s.getNamespace(), s.getName())
				.flatMap(fun -> {

					Map<String, JsonElement> arguments = getArgumentsFromParametersMap(inContext, s,
							fun.getSignature()
									.getParameters());

					ReactiveFunctionExecutionParameters fep = this.childParameters(inContext, fun, vertex, arguments);

					return fun.execute(fep)
							.map(output -> {

								output.restoreTo(ref.getConsumedEvents());

								ExecutionGraph<String, StatementExecution> subGraph = vertex
										.getSubGraphOfType(ref.getEventName());

								return new BranchEntry(subGraph, this.makeEdges(subGraph)
										.getT1(), output, vertex, ref.getEventName());
							});
				});
	}

	private Mono<Boolean> processExecutionQue(ReactiveFunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			LinkedList<BranchEntry> branchQue) {

		if (executionQue.isEmpty())
			return Mono.just(false);

		// Collect all vertices from the queue
		List<GraphVertex<String, StatementExecution>> allVertices = new ArrayList<>();
		while (!executionQue.isEmpty()) {
			allVertices.add(executionQue.pop());
		}

		// Separate ready and not-ready vertices
		List<GraphVertex<String, StatementExecution>> readyVertices = new ArrayList<>();
		List<GraphVertex<String, StatementExecution>> notReadyVertices = new ArrayList<>();

		for (GraphVertex<String, StatementExecution> vertex : allVertices) {
			if (allDependenciesResolved(vertex, inContext.getSteps())) {
				readyVertices.add(vertex);
			} else {
				notReadyVertices.add(vertex);
			}
		}

		// Re-add not-ready vertices back to the queue
		executionQue.addAll(notReadyVertices);

		// Execute all ready vertices in parallel
		if (readyVertices.isEmpty()) {
			return Mono.just(false);
		}

		return Flux.fromIterable(readyVertices)
				.flatMap(vertex -> executeVertex(vertex, inContext, branchQue, executionQue))
				.then(Mono.just(true));
	}

	private Mono<Boolean> processBranchQue(ReactiveFunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			LinkedList<BranchEntry> branchQue) {
		if (branchQue.isEmpty())
			return Mono.just(false);

		var branch = branchQue.pop();

		if (!allDependenciesResolved(branch.unresolvedDependencies(), inContext.getSteps())) {
			branchQue.add(branch);
			return Mono.just(false);
		}

		return executeBranch(inContext, executionQue, branch);
	}

	private Mono<Boolean> executeBranch(ReactiveFunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> executionQue,
			BranchEntry branch) {

		return Mono.just(Tuples.of(branch.subGraph(), inContext, Optional.<EventResult>empty()))
				.expandDeep(e -> {
					if (e.getT3()
							.isPresent()
							&& e.getT3()
									.get()
									.getName()
									.equals(Event.OUTPUT)) {
						return Mono.empty();
					}

					return this.executeGraph(e.getT1(), e.getT2(), branch.statementName(), branch.eventName())
							.flatMap(funcOut -> {

								// Stop and go: the body stopped part-way through this pass. Pulling
								// the next event here would advance the loop past the very
								// iteration we are meant to resume inside of. Record how far this
								// branch had got and unwind instead.
								if (inContext.isSuspended()) {
									inContext.setLastFrameOwnerConsumed(branch.output()
											.getConsumedCount());
									return Mono.empty();
								}

								EventResult nextOutput = branch.output()
										.next();

								if (nextOutput != null) {
									inContext.getSteps()
											.computeIfAbsent(branch.statementName(), k -> new ConcurrentHashMap<>())
											.put(nextOutput.getName(),
													resolveInternalExpressions(nextOutput.getResult(), inContext));
								}

								// next() returns null once the branch's event stream is exhausted.
								// Optional.of(null) would throw NPE here, turning a normal
								// end-of-branch into an opaque failure. Stop expanding instead.
								if (nextOutput == null)
									return Mono.empty();

								return Mono.just(Tuples.of(branch.subGraph(), inContext, Optional.of(nextOutput)));

							});
				})
				.collectList()
				.map(e -> {

					if (inContext.isSuspended())
						return true;

					GraphVertex<String, StatementExecution> vertex = branch.vertex();
					EventResult nextOutput = e.isEmpty() ? null
							: e.get(e.size() - 1)
									.getT3()
									.orElse(null);

					if (nextOutput != null && nextOutput.getName()
							.equals(Event.OUTPUT) && vertex.getOutVertices()
									.containsKey(Event.OUTPUT)) {

						// Synchronize access to executionQue to avoid race conditions in parallel execution
						synchronized (executionQue) {
							vertex.getOutVertices()
									.get(Event.OUTPUT)
									.stream()
									.filter(x -> this.allDependenciesResolved(x, inContext.getSteps()))
									.forEach(executionQue::add);
						}
					}

					return true;
				});
	}

	/**
	 * Builds the parameters a step is executed with.
	 *
	 * A definition-based function is a separate activation: it gets a fresh, isolated set of
	 * steps/events/context. Everything else runs inside this activation and shares those maps by
	 * reference, which is how sibling steps read each other's output.
	 *
	 * Shared by normal dispatch and by the resume path, which has to reproduce a step's parameters
	 * exactly in order to rebuild the branch it owned.
	 */
	private ReactiveFunctionExecutionParameters childParameters(ReactiveFunctionExecutionParameters inContext,
			ReactiveFunction fun, GraphVertex<String, StatementExecution> vertex, Map<String, JsonElement> arguments) {

		Statement s = vertex.getData()
				.getStatement();

		if (fun instanceof IDefinitionBasedFunction) {

			ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
					inContext.getFunctionRepository(), inContext.getSchemaRepository(),
					inContext.getExecutionId() + "_" + s.getStatementName())
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

			// Reuse debug collector for nested KIRuntime calls
			com.fincity.nocode.kirun.engine.runtime.debug.DebugCollector collector = inContext.getDebugCollector();
			if (collector != null) {
				fep.setDebugCollector(collector);
			}

			// Stop and go: hand a nested activation the snapshot it should resume from, so the
			// child picks up where it stopped instead of starting over.
			SuspendedExecution childResume = inContext.takePendingResume(s.getStatementName());
			if (childResume != null)
				fep.setResumeState(childResume)
						.setResumePayload(inContext.getResumePayload())
						.setResumeTimedOut(inContext.isResumeTimedOut());

			return fep;
		}

		return new ReactiveFunctionExecutionParameters(inContext.getFunctionRepository(),
				inContext.getSchemaRepository(), inContext.getExecutionId())
				.setValuesMap(inContext.getValuesMap())
				.setContext(inContext.getContext())
				.setArguments(arguments)
				.setEvents(inContext.getEvents())
				.setSteps(inContext.getSteps())
				.setStatementExecution(vertex.getData())
				.setCount(inContext.getCount())
				.setExecutionContext(inContext.getExecutionContext());
	}

	private Mono<Boolean> executeVertex(GraphVertex<String, StatementExecution> vertex,
			ReactiveFunctionExecutionParameters inContext,
			LinkedList<BranchEntry> branchQue,
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
					.allMatch(e -> {
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

		Mono<ReactiveFunction> monoFunction = getCachedFunction(inContext.getFunctionRepository(),
				s.getNamespace(), s.getName());

		return monoFunction.flatMap(fun -> {

			Map<String, Parameter> paramSet = fun.getSignature()
					.getParameters();

			Map<String, JsonElement> arguments = getArgumentsFromParametersMap(inContext, s, paramSet);

			// Start debug step tracking
			String stepId = null;
			if (inContext.getDebugCollector() != null) {
				String kirunFunctionName = this.fd.getNamespace() != null
						? this.fd.getNamespace() + "." + this.fd.getName()
						: this.fd.getName();
				stepId = inContext.getDebugCollector().startStep(
						s.getStatementName(),
						s.getNamespace() + "." + s.getName(),
						arguments,
						kirunFunctionName);
			}

			final String finalStepId = stepId; // For lambda capture

			ReactiveFunctionExecutionParameters fep = this.childParameters(inContext, fun, vertex, arguments);

			return fun.execute(fep)
					.doOnError(error -> {
						// End debug step with error
						if (inContext.getDebugCollector() != null && finalStepId != null) {
							inContext.getDebugCollector().endStep(
									finalStepId,
									"error",
									null,
									error.getMessage());
						}
					})
					.flatMap(result -> {

						// Stop and go: this step asked to stop, or a nested function below it did.
						// Its output is deliberately not recorded and no branch is opened - on
						// resume the step is not re-executed, its output is seeded instead.
						if (fep.getPendingWake() != null || fep.getSuspension() != null)
							return this.recordSuspension(s, fep, inContext, finalStepId);

						EventResult er = result.next();

						if (er == null)
							return Mono.error(new KIRuntimeException(
									StringFormatter.format("Executing $ returned no events", s.getStatementName())));

						boolean isOutput = er.getName()
								.equals(Event.OUTPUT);

						inContext.getSteps()
								.computeIfAbsent(s.getStatementName(), k -> new ConcurrentHashMap<>())
								.put(er.getName(), resolveInternalExpressions(er.getResult(), inContext));

						// End debug step with success
						if (inContext.getDebugCollector() != null && finalStepId != null) {
							inContext.getDebugCollector().endStep(
									finalStepId,
									er.getName(),
									inContext.getSteps().get(s.getStatementName()).get(er.getName()),
									null);
						}

						if (!isOutput) {

							var subGraph = vertex.getSubGraphOfType(er.getName());
							List<Tuple2<String, String>> unResolvedDependencies = this.makeEdges(subGraph)
									.getT1();
							branchQue.add(new BranchEntry(subGraph, unResolvedDependencies, result, vertex,
									er.getName()));
						} else {

							Set<GraphVertex<String, StatementExecution>> out = vertex.getOutVertices()
									.get(Event.OUTPUT);
							if (out != null) {
								// Synchronize access to executionQue to avoid race conditions in parallel execution
								synchronized (executionQue) {
									out.stream()
											.filter(e -> this.allDependenciesResolved(e, inContext.getSteps()))
											.forEach(executionQue::add);
								}
							}
						}

						return Mono.just(true);

					});
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
				.filter(e -> {
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

		record ParameterReferenceValue(String name, JsonElement value) {
		}

		return s.getParameterMap()
				.entrySet()
				.stream()
				.map(e -> {
					List<ParameterReference> prList = e.getValue() == null ? List.of()
							: new ArrayList<>(e.getValue()
									.values());

					JsonElement ret = JsonNull.INSTANCE;

					if (prList == null || prList.isEmpty())
						return new ParameterReferenceValue(e.getKey(), ret);

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

					return new ParameterReferenceValue(e.getKey(), ret);
				})
				.filter(e -> !(e.value() == null || e.value()
						.isJsonNull()))
				.collect(Collectors.toMap(ParameterReferenceValue::name, ParameterReferenceValue::value));
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

		return getCachedFunction(fRepo, s.getNamespace(), s.getName())
				.map(ReactiveFunction::getSignature)
				.map(FunctionSignature::getParameters)
				.flatMap(paramSet -> {

					if (s.getParameterMap() == null)
						return Mono.just(new StatementExecution(s));

					StatementExecution se = new StatementExecution(s);

					return Flux.fromIterable(s.getParameterMap()
							.entrySet())
							.flatMap(param -> {
								Parameter p = paramSet.get(param.getKey());
								List<ParameterReference> refList = param.getValue() == null ? List.of()
										: new ArrayList<>(param.getValue()
												.values());

								if ((refList == null || refList.isEmpty()) && !p.isVariableArgument()) {

									return ReactiveSchemaUtil.hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo)
											.flatMap(hasDefault -> {
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
							.map(lst -> {

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

								if (se.getStatement()
										.getExecuteIftrue() != null)
									for (Entry<String, Boolean> statement : s.getExecuteIftrue()
											.entrySet())
										if (statement.getValue()
												.booleanValue())
											this.addDependencies(se, statement.getKey());

								return leftOver;
							})
							.flatMap(remaining -> {

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
								if (sch.getProperties() == null || !sch.getProperties()
										.containsKey(entry.getKey()) || entry.getValue() == null)
									continue;
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

	// -------------------------------------------------------------------------------------------
	// Stop and go
	// -------------------------------------------------------------------------------------------

	/**
	 * Resumes a stopped execution.
	 *
	 * The graph is rebuilt from the definition, the snapshot's state is put back, and the step that
	 * stopped has its result seeded rather than being run again - so a wait cannot immediately stop
	 * the execution a second time.
	 *
	 * The host must supply anything the snapshot deliberately does not carry: the repositories, and
	 * any token value extractors of its own (security or application context, say) that the
	 * definition's expressions refer to.
	 *
	 * @param state         the snapshot, as handed out when the execution stopped
	 * @param resumePayload becomes the stopped step's output; may be null
	 * @param timedOut      true to resume a signal wait down its timeout path instead
	 */
	public Mono<FunctionOutput> resume(SuspendedExecution state, Map<String, JsonElement> resumePayload,
			boolean timedOut, ReactiveRepository<ReactiveFunction> fRepo, ReactiveRepository<Schema> sRepo,
			Map<String, TokenValueExtractor> hostExtractors) {

		if (state == null)
			return Mono.error(new KIRuntimeException("Cannot resume without a suspended execution"));

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo,
				state.getExecutionId())
				.setResumeState(state)
				.setResumePayload(resumePayload)
				.setResumeTimedOut(timedOut);

		// The arguments have to be back before execute() validates them against the signature,
		// which happens before internalExecute restores the rest of the state. A function with a
		// required parameter would otherwise fail validation on the way back in.
		if (state.getArguments() != null)
			params.setArguments(new LinkedHashMap<>(state.getArguments()));

		if (hostExtractors != null && !hostExtractors.isEmpty())
			params.setValuesMap(hostExtractors);

		return this.execute(params);
	}

	/** Puts a snapshot's state back onto a fresh set of execution parameters. */
	private void restoreState(ReactiveFunctionExecutionParameters inContext) {

		SuspendedExecution state = inContext.getResumeState();

		inContext.setArguments(new LinkedHashMap<>(state.getArguments()));
		inContext.setContext(Collections.synchronizedMap(new LinkedHashMap<>(state.getContext())));
		inContext.setExecutionContext(new HashMap<>(state.getExecutionContext()));

		// Events raised before the stop are kept, so a definition that reports progress as it goes
		// still has that history in its final output. The reserved suspended event is dropped: it
		// described the stop we are now coming back from.
		Map<String, List<Map<String, JsonElement>>> events = Collections.synchronizedMap(new LinkedHashMap<>());
		state.getEvents()
				.forEach((eventName, results) -> {
					if (!Event.SUSPENDED.equals(eventName))
						events.put(eventName, Collections.synchronizedList(new ArrayList<>(results)));
				});
		inContext.setEvents(events);

		Map<String, Map<String, Map<String, JsonElement>>> steps = Collections
				.synchronizedMap(new LinkedHashMap<>());
		state.getSteps()
				.forEach((stepName, byEvent) -> steps.put(stepName, new ConcurrentHashMap<>(byEvent)));
		inContext.setSteps(steps);

		// The iteration guard exists to catch a runaway within one activation. Carrying the count
		// across resumes would make a long-lived journey trip it purely for having been resumed
		// often, so each resumption starts the count again.
		inContext.setCount(0);

		inContext.getResumeFrames()
				.addAll(state.getGraphFrames());

		// A nested function's snapshot is handed to the step that owns it, which re-enters that
		// function rather than seeding a result.
		if (state.getChild() != null)
			inContext.addPendingResume(state.getSuspendedStepName(), state.getChild());
	}

	/**
	 * Puts the stopped step back into play at the level it stopped on.
	 *
	 * A nested function call has to run again so it can resume its own inner activation. A wait is
	 * not re-run at all: its result is seeded and the steps that were waiting on it are queued, so
	 * the wait cannot stop the execution again the instant it comes back.
	 */
	private void seedResumedStep(ExecutionGraph<String, StatementExecution> eGraph,
			ReactiveFunctionExecutionParameters inContext,
			LinkedList<GraphVertex<String, StatementExecution>> eq) {

		SuspendedExecution state = inContext.getResumeState();
		String statementName = state.getSuspendedStepName();

		if (statementName == null)
			return;

		GraphVertex<String, StatementExecution> vertex = eGraph.getVertex(statementName);

		if (vertex == null)
			throw new KIRuntimeException(StringFormatter.format(
					"Cannot resume : the definition of $.$ no longer has a step named $", this.fd.getNamespace(),
					this.fd.getName(), statementName));

		if (state.getChild() != null) {
			eq.add(vertex);
			return;
		}

		String eventName = inContext.isResumeTimedOut() ? Event.TIMEOUT : Event.OUTPUT;

		Map<String, JsonElement> payload = inContext.getResumePayload() == null ? Map.of()
				: inContext.getResumePayload();

		inContext.getSteps()
				.computeIfAbsent(statementName, k -> new ConcurrentHashMap<>())
				.put(eventName, new LinkedHashMap<>(payload));

		Set<GraphVertex<String, StatementExecution>> out = vertex.getOutVertices()
				.get(eventName);

		if (out != null)
			out.stream()
					.filter(e -> this.allDependenciesResolved(e, inContext.getSteps()))
					.forEach(eq::add);
	}

	/**
	 * Records that a step stopped this activation, and raises the reserved event so the stop is
	 * visible in the output.
	 *
	 * The event carries only the execution id and the wake condition; the state snapshot is far too
	 * big to push through an event result and is handed to the host on the execution parameters
	 * instead.
	 */
	private Mono<Boolean> recordSuspension(Statement s, ReactiveFunctionExecutionParameters fep,
			ReactiveFunctionExecutionParameters inContext, String debugStepId) {

		String statementName = s.getStatementName();

		synchronized (inContext) {

			// Ready steps are dispatched concurrently, so two waits with no dependency between them
			// can both come back asking to stop. There is no single point to resume such an
			// execution from, so refuse it rather than silently keep one and lose the other.
			if (inContext.isSuspended() && !statementName.equals(inContext.getSuspendedStepName()))
				return Mono.error(new KIRuntimeException(StringFormatter.format(
						"Steps $ and $ of $.$ both stopped the execution in the same pass. Only one step can stop an execution at a time - make one wait depend on the other so they run in sequence.",
						inContext.getSuspendedStepName(), statementName, this.fd.getNamespace(), this.fd.getName())));

			inContext.setSuspendedStepName(statementName);

			// A nested activation carries its own wake condition inside its snapshot. Copying it up
			// as well would store the same condition once per level of nesting, and leave two
			// places to keep in step when resuming.
			if (fep.getSuspension() != null)
				inContext.setChildSuspension(fep.getSuspension());
			else
				inContext.setPendingWake(fep.getPendingWake());
		}

		WakeCondition condition = fep.getPendingWake() != null ? fep.getPendingWake()
				: fep.getSuspension()
						.effectiveWakeCondition();

		Map<String, JsonElement> result = new LinkedHashMap<>();
		result.put("executionId", new JsonPrimitive(inContext.getExecutionId()));
		result.put("stepName", new JsonPrimitive(statementName));
		if (condition != null)
			result.put("wakeCondition", condition.toJson());

		inContext.getEvents()
				.computeIfAbsent(Event.SUSPENDED, k -> Collections.synchronizedList(new ArrayList<>()))
				.add(result);

		if (inContext.getDebugCollector() != null && debugStepId != null)
			inContext.getDebugCollector()
					.endStep(debugStepId, Event.SUSPENDED, result, null);

		return Mono.just(true);
	}

	/**
	 * Builds this activation's snapshot, once its graph has unwound.
	 *
	 * Left on the execution parameters rather than returned: the caller of a nested function keeps
	 * the parameters object it created, so that is where it looks for the child's snapshot, and a
	 * host driving the outermost call reads it from the object it passed in.
	 */
	private Mono<FunctionOutput> assembleSuspension(ReactiveFunctionExecutionParameters inContext,
			FunctionOutput output) {

		if (!inContext.isSuspended())
			return Mono.just(output);

		List<GraphFrame> frames = new ArrayList<>(inContext.getCapturedFrames());
		Collections.reverse(frames);

		SuspendedExecution state = new SuspendedExecution().setExecutionId(inContext.getExecutionId())
				.setNamespace(this.fd.getNamespace())
				.setName(this.fd.getName())
				.setWakeCondition(inContext.getPendingWake())
				.setSuspendedStepName(inContext.getSuspendedStepName())
				.setArguments(new LinkedHashMap<>(inContext.getArguments()))
				.setContext(new LinkedHashMap<>(inContext.getContext()))
				.setEvents(new LinkedHashMap<>(inContext.getEvents()))
				.setExecutionContext(new LinkedHashMap<>(inContext.getExecutionContext()))
				.setCount(inContext.getCount())
				.setGraphFrames(frames)
				.setChild(inContext.getChildSuspension());

		Map<String, Map<String, Map<String, JsonElement>>> steps = new LinkedHashMap<>();
		inContext.getSteps()
				.forEach((stepName, byEvent) -> steps.put(stepName, new LinkedHashMap<>(byEvent)));
		state.setSteps(steps);

		inContext.setSuspension(state);

		return Mono.just(output);
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

	/**
	 * Get the execution log after execution completes.
	 * Only available if debugMode was enabled in constructor.
	 *
	 * @return ExecutionLog with all step details, or null if debug mode is off
	 */
	public com.fincity.nocode.kirun.engine.runtime.debug.ExecutionLog getExecutionLog() {
		return this.debugCollector != null ? this.debugCollector.getExecutionLog() : null;
	}
}

package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution.GraphFrame;
import com.fincity.nocode.kirun.engine.runtime.suspend.WakeCondition;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.OutputMapTokenValueExtractor;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@ToString
public class ReactiveFunctionExecutionParameters {

	private Map<String, ContextElement> context;
	private Map<String, JsonElement> arguments;
	private Map<String, List<Map<String, JsonElement>>> events;
	private StatementExecution statementExecution;
	private Map<String, Map<String, Map<String, JsonElement>>> steps;
	private int count;
	private final ReactiveRepository<ReactiveFunction> functionRepository;
	private final ReactiveRepository<Schema> schemaRepository;
	private final String executionId;
	private Map<String, JsonElement> executionContext = new HashMap<>();

	private HashMap<String, TokenValueExtractor> valueExtractors = new HashMap<>();

	private com.fincity.nocode.kirun.engine.runtime.debug.DebugCollector debugCollector;

	// ---------------------------------------------------------------------------------------
	// Stop and go.
	//
	// These fields are the channel between a step and the runtime that dispatched it. They
	// cannot travel through the shared maps: a native step is handed a fresh parameters object
	// that shares only context/steps/events/executionContext by reference, so anything scalar
	// has to be read back off the object the caller still holds.
	// ---------------------------------------------------------------------------------------

	/** Set by a suspending built-in on its own parameters to ask the runtime to stop. */
	private WakeCondition pendingWake;

	/** The snapshot this activation produced when it stopped. Read by the calling activation. */
	private SuspendedExecution suspension;

	/** A nested function step's snapshot, harvested by the activation that called it. */
	private SuspendedExecution childSuspension;

	/** The statement that stopped this activation. */
	private String suspendedStepName;

	/**
	 * Pending work captured as executeGraph unwinds, innermost level first. Reversed into
	 * outermost-first order when the snapshot is assembled.
	 */
	private List<GraphFrame> capturedFrames = new ArrayList<>();

	/** Non-null when this activation is resuming rather than starting cold. */
	private SuspendedExecution resumeState;

	/** What the host resumed with. Becomes the suspended step's output. */
	private Map<String, JsonElement> resumePayload;

	/** True when the host resumed a signal wait because its deadline passed. */
	private boolean resumeTimedOut;

	/**
	 * Frames still to be restored, outermost first. Each graph level takes the head if it is the
	 * level that frame describes.
	 */
	private LinkedList<GraphFrame> resumeFrames = new LinkedList<>();

	public ReactiveFunctionExecutionParameters(ReactiveRepository<ReactiveFunction> functionRepository,
	        ReactiveRepository<Schema> schemaRepository) {
		this(functionRepository, schemaRepository, UUID.randomUUID()
		        .toString());
	}

	public ReactiveFunctionExecutionParameters setContext(Map<String, ContextElement> context) {

		this.context = context;
		var x = new ContextTokenValueExtractor(context);
		valueExtractors.put(x.getPrefix(), x);

		return this;
	}

	public ReactiveFunctionExecutionParameters setSteps(Map<String, Map<String, Map<String, JsonElement>>> steps) {

		this.steps = steps;
		var x = new OutputMapTokenValueExtractor(steps);
		valueExtractors.put(x.getPrefix(), x);

		return this;
	}

	public ReactiveFunctionExecutionParameters setArguments(Map<String, JsonElement> arguments) {

		this.arguments = arguments;
		return this;
	}

	public Map<String, TokenValueExtractor> getValuesMap() {
		return this.valueExtractors;
	}

	public ReactiveFunctionExecutionParameters addTokenValueExtractor(TokenValueExtractor... extractors) {

		for (TokenValueExtractor tve : extractors)
			this.valueExtractors.put(tve.getPrefix(), tve);
		return this;
	}

	public ReactiveFunctionExecutionParameters setValuesMap(Map<String, TokenValueExtractor> valuesMap) {
		this.valueExtractors.putAll(valuesMap);
		return this;
	}

	public Map<String, JsonElement> getArguments() {
		if (this.arguments == null)
			return Map.of();
		return this.arguments;
	}

	/**
	 * Asks the runtime to stop this execution and resume it later on {@code condition}. Called by a
	 * suspending built-in, which then returns an {@link
	 * com.fincity.nocode.kirun.engine.model.Event#SUSPENDED} event result.
	 */
	public ReactiveFunctionExecutionParameters suspend(WakeCondition condition) {

		this.pendingWake = condition;
		return this;
	}

	/** Whether something below this activation has asked to stop. */
	public boolean isSuspended() {
		return this.pendingWake != null || this.childSuspension != null;
	}

	/** Whether this activation is being resumed from a snapshot. */
	public boolean isResuming() {
		return this.resumeState != null;
	}

	public ReactiveFunctionExecutionParameters addCapturedFrame(GraphFrame frame) {

		this.capturedFrames.add(frame);
		return this;
	}

	/**
	 * Records how far the branch-owning step's output had been consumed, on the frame just
	 * captured. Only the code driving the branch knows this, and it runs immediately after the
	 * frame for that branch's sub-graph was appended.
	 */
	public ReactiveFunctionExecutionParameters setLastFrameOwnerConsumed(int consumed) {

		if (!this.capturedFrames.isEmpty())
			this.capturedFrames.get(this.capturedFrames.size() - 1)
			        .setOwnerConsumedEvents(consumed);

		return this;
	}

	/**
	 * Takes the pending frame if it describes this graph level, otherwise leaves it alone and
	 * returns null so the level starts normally.
	 */
	public GraphFrame takeResumeFrame(String ownerStatementName, String eventName) {

		if (this.resumeFrames.isEmpty())
			return null;

		GraphFrame head = this.resumeFrames.peek();

		if (!Objects.equals(head.getOwnerStatementName(), ownerStatementName)
		        || !Objects.equals(head.getEventName(), eventName))
			return null;

		return this.resumeFrames.poll();
	}

	/** The frame below the one being restored, i.e. the branch that was mid-flight. */
	public GraphFrame peekResumeFrame() {
		return this.resumeFrames.peek();
	}

	/**
	 * Snapshots handed to nested function steps, keyed by statement name. Set up when this
	 * activation starts resuming; consumed when the step in question is dispatched.
	 */
	private Map<String, SuspendedExecution> pendingResumes = new HashMap<>();

	public ReactiveFunctionExecutionParameters addPendingResume(String statementName, SuspendedExecution state) {

		if (statementName != null && state != null)
			this.pendingResumes.put(statementName, state);

		return this;
	}

	/** The snapshot a nested step should resume from, removed so it is only used once. */
	public SuspendedExecution takePendingResume(String statementName) {
		return statementName == null ? null : this.pendingResumes.remove(statementName);
	}
}

package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * A serialisable snapshot of one function activation that stopped part-way through, plus the
 * condition on which it should be resumed.
 *
 * Everything here is either a primitive or a {@link JsonElement}/{@link ContextElement}, so the
 * whole object round-trips through
 * {@link com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecutionSerializer} and can be
 * parked in a database for days and resumed in a different JVM.
 *
 * The execution graph is deliberately <em>not</em> stored. It is rebuilt from the function
 * definition on resume and vertices are found again by statement name, so a snapshot stays valid
 * even though graph objects are not serialisable.
 *
 * One instance describes one function activation. When the suspension happened inside a nested
 * KIRun function call, that inner activation hangs off {@link #child} - the chain of children is
 * the call stack at the moment of suspension.
 */
@Data
@Accessors(chain = true)
public class SuspendedExecution {

	/**
	 * Snapshot format version. Bumped whenever the persisted shape changes so a host can detect a
	 * snapshot it is too old to understand instead of misreading it.
	 */
	public static final int VERSION = 1;

	private int version = VERSION;

	private String executionId;
	private String namespace;
	private String name;

	/** Why to wake up. Only set on the innermost activation - the one that actually stopped. */
	private WakeCondition wakeCondition;

	/** The statement that stopped. On resume its output is seeded rather than re-executed. */
	private String suspendedStepName;

	private Map<String, JsonElement> arguments = new LinkedHashMap<>();
	private Map<String, ContextElement> context = new LinkedHashMap<>();
	private Map<String, Map<String, Map<String, JsonElement>>> steps = new LinkedHashMap<>();
	private Map<String, List<Map<String, JsonElement>>> events = new LinkedHashMap<>();
	private Map<String, JsonElement> executionContext = new LinkedHashMap<>();

	/** Statement-iteration count at the moment of suspension, kept for diagnostics only. */
	private int count;

	/**
	 * One frame per nested {@code executeGraph} level within this activation, outermost first. The
	 * last frame is where execution actually stopped; a loop or branch body adds a frame above the
	 * function's own top-level graph.
	 */
	private List<GraphFrame> graphFrames = new ArrayList<>();

	/** Set when the suspension happened inside a nested KIRun function step. */
	private SuspendedExecution child;

	/**
	 * The pending work of one graph level: which statements were waiting to run, and which
	 * branches were mid-flight.
	 */
	@Data
	@Accessors(chain = true)
	public static class GraphFrame {

		/**
		 * The statement whose event opened this sub-graph, or null for the function's own
		 * top-level graph.
		 */
		private String ownerStatementName;

		/** The event that opened this sub-graph, or null for the top-level graph. */
		private String eventName;

		/**
		 * How many events the owning step had already produced. Used to wind its output back to
		 * this position when the branch is rebuilt on resume.
		 */
		private int ownerConsumedEvents;

		/** Statement names still queued to execute at this level. */
		private List<String> executionQueue = new ArrayList<>();

		/** Branches queued at this level, in queue order. */
		private List<BranchRef> branchQueue = new ArrayList<>();
	}

	/**
	 * A queued branch, described by what can be rebuilt deterministically rather than by the live
	 * objects. The owning step's {@code FunctionOutput} is re-obtained on resume by re-executing
	 * that step, then wound forward by {@link #consumedEvents}.
	 */
	@Data
	@Accessors(chain = true)
	public static class BranchRef {

		private String statementName;
		private String eventName;

		/**
		 * How many events had already been pulled from the owning step's output. Only meaningful
		 * for list-backed outputs; generator-backed loops keep their position in
		 * {@code executionContext} via {@link LoopCursor}.
		 */
		private int consumedEvents;
	}

	/** The innermost activation in this chain - the one that actually stopped. */
	public SuspendedExecution innermost() {

		SuspendedExecution s = this;
		while (s.child != null)
			s = s.child;
		return s;
	}

	/** The wake condition of the activation that actually stopped. */
	public WakeCondition effectiveWakeCondition() {
		return this.innermost()
		        .getWakeCondition();
	}
}

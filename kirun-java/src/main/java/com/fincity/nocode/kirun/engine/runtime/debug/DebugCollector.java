package com.fincity.nocode.kirun.engine.runtime.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import lombok.Getter;

/**
 * Collector for gathering debug information during a single execution.
 * Thread-safe for parallel step execution within one execution.
 * Owned by ReactiveKIRuntime instance.
 */
public class DebugCollector {

	private static final Logger logger = LoggerFactory.getLogger(DebugCollector.class);

	@Getter
	private final ExecutionLog executionLog;

	/**
	 * Track pending logs (step started but not ended).
	 * Thread-safe for concurrent step tracking.
	 */
	private final Map<String, LogEntry.LogEntryBuilder> pendingLogs = new ConcurrentHashMap<>();

	/**
	 * Create a new debug collector for an execution.
	 *
	 * @param executionId  Unique execution ID
	 * @param functionName Full function name (namespace.name)
	 */
	public DebugCollector(String executionId, String functionName) {

		this.executionLog = new ExecutionLog(executionId, System.currentTimeMillis());
	}

	/**
	 * Start tracking a step.
	 * Thread-safe for concurrent calls from parallel step execution.
	 *
	 * @param statementName     The statement name
	 * @param functionName      The function name (namespace.name)
	 * @param arguments         Optional function arguments
	 * @param kirunFunctionName The KIRunFunction that is calling this
	 * @return Step ID for tracking
	 */
	public String startStep(String statementName, String functionName, Map<String, JsonElement> arguments,
	        String kirunFunctionName) {

		// Generate unique step ID using timestamp + thread ID + random
		String stepId = System.currentTimeMillis() + "_" + Thread.currentThread()
		        .threadId() + "_" + Math.random();

		LogEntry.LogEntryBuilder builder = LogEntry.builder()
		        .timestamp(System.currentTimeMillis())
		        .functionName(functionName)
		        .kirunFunctionName(kirunFunctionName)
		        .statementName(statementName)
		        .arguments(arguments);

		// Store as pending (not written yet - waiting for endStep)
		this.pendingLogs.put(stepId, builder);

		return stepId;
	}

	/**
	 * End tracking a step.
	 * Thread-safe for concurrent calls from parallel step execution.
	 *
	 * @param stepId    The step ID returned from startStep
	 * @param eventName The event name (output, error, etc.)
	 * @param result    Optional event result
	 * @param error     Optional error message
	 */
	public void endStep(String stepId, String eventName, Map<String, JsonElement> result, String error) {

		LogEntry.LogEntryBuilder builder = this.pendingLogs.remove(stepId);
		if (builder == null) {
			logger.warn("DebugCollector: No step found with ID {}", stepId);
			return;
		}

		// Complete the log entry
		long endTime = System.currentTimeMillis();

		// Build a temporary log entry to get the timestamp
		LogEntry tempLog = builder.build();
		long duration = endTime - tempLog.getTimestamp();

		LogEntry log = builder.duration(duration)
		        .result(result)
		        .eventName(eventName)
		        .error(error)
		        .build();

		// Add to execution log (thread-safe)
		this.executionLog.addLog(log);

		// Mark execution as errored if this step has an error
		if (error != null) {
			this.executionLog.markErrored();
		}
	}

	/**
	 * Mark execution as completed.
	 * Call this when execution finishes.
	 */
	public void endExecution() {

		this.executionLog.setEndTime(System.currentTimeMillis());
	}
}

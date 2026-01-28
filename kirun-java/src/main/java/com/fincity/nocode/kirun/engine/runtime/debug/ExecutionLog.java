package com.fincity.nocode.kirun.engine.runtime.debug;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Data;

/**
 * Complete execution log with all steps.
 * Thread-safe for concurrent log additions during parallel step execution.
 */
@Data
public class ExecutionLog {

	/**
	 * Unique execution ID (UUID)
	 */
	private final String executionId;

	/**
	 * When execution started (System.currentTimeMillis())
	 */
	private final long startTime;

	/**
	 * When execution completed (System.currentTimeMillis())
	 * Null until execution completes.
	 */
	private Long endTime;

	/**
	 * Whether execution had errors
	 */
	private volatile boolean errored;

	/**
	 * Flat chronological array of all logs.
	 * Thread-safe for concurrent additions.
	 */
	private final List<LogEntry> logs;

	/**
	 * Create a new execution log.
	 *
	 * @param executionId Unique execution ID
	 * @param startTime   Start timestamp
	 */
	public ExecutionLog(String executionId, long startTime) {

		this.executionId = executionId;
		this.startTime = startTime;
		this.errored = false;
		this.logs = new CopyOnWriteArrayList<>();
	}

	/**
	 * Add a log entry.
	 * Thread-safe.
	 *
	 * @param log Log entry to add
	 */
	public void addLog(LogEntry log) {

		this.logs.add(log);
	}

	/**
	 * Mark execution as errored.
	 * Thread-safe.
	 */
	public void markErrored() {

		this.errored = true;
	}
}

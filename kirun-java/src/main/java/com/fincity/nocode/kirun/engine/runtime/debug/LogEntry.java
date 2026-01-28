package com.fincity.nocode.kirun.engine.runtime.debug;

import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Builder;
import lombok.Data;

/**
 * Log entry for a single step execution.
 * Immutable once built - thread-safe for reading.
 */
@Data
@Builder
public class LogEntry {

	/**
	 * When this step happened (System.currentTimeMillis())
	 */
	private final long timestamp;

	/**
	 * The function being executed (e.g., "System.GenerateEvent")
	 */
	private final String functionName;

	/**
	 * The KIRunFunction that called this (e.g., "App.onLoad")
	 */
	private final String kirunFunctionName;

	/**
	 * The statement name (e.g., "loadStorages")
	 */
	private final String statementName;

	/**
	 * Execution time in milliseconds (set when step completes)
	 */
	private final Long duration;

	/**
	 * Function arguments
	 */
	private final Map<String, JsonElement> arguments;

	/**
	 * Event result
	 */
	private final Map<String, JsonElement> result;

	/**
	 * Event name ("output", "error", etc.)
	 */
	private final String eventName;

	/**
	 * Error message if failed
	 */
	private final String error;
}

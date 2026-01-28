package com.fincity.nocode.kirun.engine.runtime.debug;

import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Data;

/**
 * Log entry for a single step execution.
 * Immutable once built - thread-safe for reading.
 */
@Data
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

	/**
	 * Private constructor - use builder() to create instances
	 */
	private LogEntry(long timestamp, String functionName, String kirunFunctionName,
			String statementName, Long duration, Map<String, JsonElement> arguments,
			Map<String, JsonElement> result, String eventName, String error) {
		this.timestamp = timestamp;
		this.functionName = functionName;
		this.kirunFunctionName = kirunFunctionName;
		this.statementName = statementName;
		this.duration = duration;
		this.arguments = arguments;
		this.result = result;
		this.eventName = eventName;
		this.error = error;
	}

	/**
	 * Create a new builder
	 */
	public static LogEntryBuilder builder() {
		return new LogEntryBuilder();
	}

	/**
	 * Builder for LogEntry
	 */
	public static class LogEntryBuilder {
		private long timestamp;
		private String functionName;
		private String kirunFunctionName;
		private String statementName;
		private Long duration;
		private Map<String, JsonElement> arguments;
		private Map<String, JsonElement> result;
		private String eventName;
		private String error;

		public LogEntryBuilder timestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public LogEntryBuilder functionName(String functionName) {
			this.functionName = functionName;
			return this;
		}

		public LogEntryBuilder kirunFunctionName(String kirunFunctionName) {
			this.kirunFunctionName = kirunFunctionName;
			return this;
		}

		public LogEntryBuilder statementName(String statementName) {
			this.statementName = statementName;
			return this;
		}

		public LogEntryBuilder duration(Long duration) {
			this.duration = duration;
			return this;
		}

		public LogEntryBuilder arguments(Map<String, JsonElement> arguments) {
			this.arguments = arguments;
			return this;
		}

		public LogEntryBuilder result(Map<String, JsonElement> result) {
			this.result = result;
			return this;
		}

		public LogEntryBuilder eventName(String eventName) {
			this.eventName = eventName;
			return this;
		}

		public LogEntryBuilder error(String error) {
			this.error = error;
			return this;
		}

		public LogEntry build() {
			return new LogEntry(timestamp, functionName, kirunFunctionName, statementName,
					duration, arguments, result, eventName, error);
		}
	}
}

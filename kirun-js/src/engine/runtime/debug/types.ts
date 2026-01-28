/**
 * Log entry for a single step execution
 */
export interface LogEntry {
    timestamp: number; // When this step happened
    functionName: string; // e.g., "System.loadStorages" or "App.onLoad"
    kirunFunctionName?: string; // The KIRunFunction that called this (e.g., "App.onLoad")
    statementName?: string; // e.g., "loadStorages" (optional for nested calls)
    duration?: number; // Execution time in ms (set when step completes)
    arguments?: any; // Function arguments
    result?: any; // Event result
    eventName?: string; // output, error, etc.
    error?: string; // Error message if failed
}

/**
 * Complete execution log with all steps
 */
export interface ExecutionLog {
    executionId: string; // e.g., "key_abc123"
    startTime: number; // When execution started
    endTime?: number; // When execution completed
    errored: boolean; // Whether execution had errors
    logs: LogEntry[]; // Flat chronological array of all logs
}

/**
 * Interface for debug collector callbacks
 * Used to avoid circular dependency between DebugCollector and GlobalDebugCollector
 */
export interface IDebugCollectorCallbacks {
    addLog(executionId: string, log: LogEntry): void;
    markExecutionErrored(executionId: string): void;
}

/**
 * Debug event types
 */
export type DebugEventType = 'executionStart' | 'executionEnd' | 'logAdded' | 'executionErrored';

/**
 * Debug event data
 */
export interface DebugEvent {
    type: DebugEventType;
    executionId: string;
    timestamp: number;
    data?: any;
}

/**
 * Execution start event
 */
export interface ExecutionStartEvent extends DebugEvent {
    type: 'executionStart';
    data: {
        functionName: string;
    };
}

/**
 * Execution end event
 */
export interface ExecutionEndEvent extends DebugEvent {
    type: 'executionEnd';
    data: {
        duration: number;
        errored: boolean;
    };
}

/**
 * Log added event
 */
export interface LogAddedEvent extends DebugEvent {
    type: 'logAdded';
    data: {
        log: LogEntry;
    };
}

/**
 * Execution errored event
 */
export interface ExecutionErroredEvent extends DebugEvent {
    type: 'executionErrored';
}

/**
 * Debug event listener callback
 */
export type DebugEventListener = (event: DebugEvent) => void;

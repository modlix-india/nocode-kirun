/**
 * Log entry for a single step execution
 */
export interface LogEntry {
    stepId: string;
    timestamp: number;
    functionName: string;
    statementName?: string;
    kirunFunctionName?: string;
    duration?: number;
    arguments?: any;
    result?: any;
    eventName?: string;
    error?: string;
    children: LogEntry[];
}

/**
 * Complete execution log
 */
export interface ExecutionLog {
    executionId: string;
    startTime: number;
    endTime?: number;
    errored: boolean;
    logs: LogEntry[];
    definitions: Map<string, any>;
}

/**
 * Debug event types
 */
export type DebugEventType =
    | 'executionStart'
    | 'executionEnd'
    | 'stepStart'
    | 'stepEnd'
    | 'executionErrored';

/**
 * Debug event listener callback
 */
export type DebugEventListener = (event: { type: DebugEventType; executionId: string; data?: any }) => void;

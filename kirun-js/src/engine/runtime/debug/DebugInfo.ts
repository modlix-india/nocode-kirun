import type { LogEntry, IDebugCollectorCallbacks } from './types';

export type { LogEntry, ExecutionLog } from './types';

/**
 * Collector class for gathering debug information during execution
 * Writes logs directly to GlobalDebugCollector as they happen
 */
export class DebugCollector {
    private readonly globalCollector: IDebugCollectorCallbacks;
    private readonly executionId: string;
    private readonly functionName: string;
    private readonly pendingLogs: Map<string, LogEntry> = new Map();

    constructor(globalCollector: IDebugCollectorCallbacks, executionId: string, functionName: string) {
        this.globalCollector = globalCollector;
        this.executionId = executionId;
        this.functionName = functionName;
    }

    /**
     * Start tracking a step
     * @param statementName - The statement name
     * @param functionName - The function name (namespace.name)
     * @param args - Optional function arguments
     * @param kirunFunctionName - The KIRunFunction that is calling this (e.g., "App.onLoad")
     * @returns Step ID for tracking
     */
    public startStep(statementName: string, functionName: string, args?: any, kirunFunctionName?: string): string {
        const stepId = `${Date.now()}_${Math.random()}`;

        // Serialize Map arguments
        let serializedArgs = args;
        if (args instanceof Map) {
            serializedArgs = Object.fromEntries(args);
        }

        const log: LogEntry = {
            timestamp: Date.now(),
            functionName,
            kirunFunctionName,
            statementName,
            arguments: serializedArgs,
        };

        // Store as pending (not written yet - waiting for endStep)
        this.pendingLogs.set(stepId, log);

        return stepId;
    }

    /**
     * End tracking a step
     * @param stepId - The step ID returned from startStep
     * @param eventName - The event name (output, error, etc.)
     * @param result - Optional event result
     * @param error - Optional error message
     */
    public endStep(stepId: string, eventName: string, result?: any, error?: string): void {
        const log = this.pendingLogs.get(stepId);
        if (!log) {
            console.warn(`DebugCollector: No step found with ID ${stepId}`);
            return;
        }

        // Serialize Map results
        let serializedResult = result;
        if (result instanceof Map) {
            serializedResult = Object.fromEntries(result);
        }

        // Complete the log entry
        const endTime = Date.now();
        log.duration = endTime - log.timestamp;
        log.result = serializedResult;
        log.eventName = eventName;
        log.error = error;

        // Write to GlobalDebugCollector
        this.globalCollector.addLog(this.executionId, log);

        // Remove from pending
        this.pendingLogs.delete(stepId);

        // Mark execution as errored if this step has an error
        if (error) {
            this.globalCollector.markExecutionErrored(this.executionId);
        }
    }
}

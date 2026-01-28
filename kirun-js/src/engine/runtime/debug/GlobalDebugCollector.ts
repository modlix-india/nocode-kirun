import { DebugCollector } from './DebugInfo';
import type { ExecutionLog, LogEntry } from './types';

/**
 * Global debug collector singleton for tracking all function executions
 * Stores logs grouped by execution ID with automatic LRU pruning
 */
export class GlobalDebugCollector {
    private static instance: GlobalDebugCollector;
    private readonly executions: Map<string, ExecutionLog> = new Map();
    private executionOrder: string[] = [];
    private enabled: boolean = false;
    private readonly maxExecutions: number = 10;

    private constructor() {}

    /**
     * Get the singleton instance
     */
    public static getInstance(): GlobalDebugCollector {
        if (!GlobalDebugCollector.instance) {
            GlobalDebugCollector.instance = new GlobalDebugCollector();
        }
        return GlobalDebugCollector.instance;
    }

    /**
     * Enable debug collection
     */
    public enable(): void {
        this.enabled = true;
    }

    /**
     * Disable debug collection
     */
    public disable(): void {
        this.enabled = false;
    }

    /**
     * Check if debug collection is enabled
     */
    public isEnabled(): boolean {
        return this.enabled;
    }

    /**
     * Create a local debug collector (not stored globally)
     * Initializes execution log if it doesn't exist
     * @param executionId - Unique execution ID
     * @param namespace - Function namespace (can be undefined)
     * @param name - Function name
     * @returns New DebugCollector instance
     */
    public createCollector(
        executionId: string,
        namespace: string | undefined,
        name: string,
    ): DebugCollector | undefined {
        if (!this.enabled) {
            return undefined;
        }

        const functionName = namespace ? `${namespace}.${name}` : name;

        // Initialize execution log if it doesn't exist
        if (!this.executions.has(executionId)) {
            this.executions.set(executionId, {
                executionId,
                startTime: Date.now(),
                errored: false,
                logs: [],
            });
            this.executionOrder.push(executionId);

            // Auto-prune if exceeded limit
            if (this.executionOrder.length > this.maxExecutions) {
                const oldestId = this.executionOrder.shift()!;
                this.executions.delete(oldestId);
            }
        }

        return new DebugCollector(this, executionId, functionName);
    }

    /**
     * Add a log entry to an execution (called by DebugCollector)
     * @internal
     */
    public addLog(executionId: string, log: LogEntry): void {
        const execution = this.executions.get(executionId);
        if (execution) {
            execution.logs.push(log);
        }
    }

    /**
     * Mark an execution as completed
     */
    public endExecution(executionId: string): void {
        const execution = this.executions.get(executionId);
        if (execution) {
            execution.endTime = Date.now();
        }
    }

    /**
     * Mark an execution as errored
     */
    public markExecutionErrored(executionId: string): void {
        const execution = this.executions.get(executionId);
        if (execution) {
            execution.errored = true;
        }
    }

    /**
     * Get execution log by ID
     * @param executionId - Execution ID
     * @returns ExecutionLog or undefined
     */
    public getDebugInfoByExecutionId(executionId: string): ExecutionLog | undefined {
        return this.executions.get(executionId);
    }

    /**
     * Get the most recent execution
     * @returns Most recent ExecutionLog or undefined
     */
    public getLastExecution(): ExecutionLog | undefined {
        if (this.executionOrder.length === 0) return undefined;
        const lastId = this.executionOrder.at(-1)!;
        return this.executions.get(lastId);
    }

    /**
     * Get all executions that called a specific function
     * @param functionName - Full function name (e.g., "System.loadStorages")
     * @returns Map of execution ID to execution log
     */
    public getDebugInfoByFunction(functionName: string): Map<string, ExecutionLog> {
        const result = new Map<string, ExecutionLog>();

        for (const [execId, execution] of this.executions.entries()) {
            // Check if any log entry has this function name
            if (execution.logs.some((log) => log.functionName === functionName)) {
                result.set(execId, execution);
            }
        }

        return result;
    }

    /**
     * Get logs from a specific execution filtered by KIRunFunction name
     * @param executionId - Execution ID
     * @param kirunFunctionName - KIRunFunction name (e.g., "App.onLoad")
     * @returns Array of log entries matching the filter, or undefined if execution not found
     */
    public getDebugInfoByExecutionIdAndKirunFunction(
        executionId: string,
        kirunFunctionName: string,
    ): LogEntry[] | undefined {
        const execution = this.executions.get(executionId);
        if (!execution) {
            return undefined;
        }

        return execution.logs.filter((log) => log.kirunFunctionName === kirunFunctionName);
    }

    /**
     * Get all logs for a specific KIRunFunction, grouped by execution ID
     * @param kirunFunctionName - KIRunFunction name (e.g., "App.onLoad")
     * @returns Map of execution ID to array of matching log entries
     */
    public getDebugInfoByKirunFunction(kirunFunctionName: string): Map<string, LogEntry[]> {
        const result = new Map<string, LogEntry[]>();

        for (const [execId, execution] of this.executions.entries()) {
            const matchingLogs = execution.logs.filter(
                (log) => log.kirunFunctionName === kirunFunctionName,
            );
            if (matchingLogs.length > 0) {
                result.set(execId, matchingLogs);
            }
        }

        return result;
    }

    /**
     * Get all execution IDs
     * @returns Array of execution IDs in chronological order
     */
    public getAllExecutionIds(): string[] {
        return Array.from(this.executionOrder);
    }

    /**
     * Clear all debug info
     */
    public clearAll(): void {
        this.executions.clear();
        this.executionOrder = [];
    }

    /**
     * Get total number of tracked executions
     */
    public getExecutionCount(): number {
        return this.executions.size;
    }
}

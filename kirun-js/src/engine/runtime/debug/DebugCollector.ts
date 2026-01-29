import type { LogEntry, ExecutionLog, DebugEventListener } from './types';

/**
 * Simple debug collector - single class that handles everything
 */
export class DebugCollector {
    private static instance: DebugCollector;

    private executions: Map<string, ExecutionLog> = new Map();
    private executionOrder: string[] = [];
    private stepStacks: Map<string, LogEntry[]> = new Map();
    private pendingLogs: Map<string, LogEntry> = new Map();
    private listeners: Set<DebugEventListener> = new Set();

    private enabled = false;
    private maxExecutions = 10;

    private constructor() {}

    static getInstance(): DebugCollector {
        if (!DebugCollector.instance) {
            DebugCollector.instance = new DebugCollector();
        }
        return DebugCollector.instance;
    }

    enable(): void {
        this.enabled = true;
    }

    disable(): void {
        this.enabled = false;
    }

    isEnabled(): boolean {
        return this.enabled;
    }

    /**
     * Start a new execution or register a nested function call.
     * Called when a KIRuntime (definition-based) function begins execution.
     * For nested calls with the same executionId, stores the additional definition.
     */
    startExecution(executionId: string, functionName: string, definition?: any): void {
        if (!this.enabled) return;

        const execution = this.executions.get(executionId);

        if (!execution) {
            // First execution with this ID
            this.executions.set(executionId, {
                executionId,
                startTime: Date.now(),
                errored: false,
                logs: [],
                definitions: new Map(),
            });
            this.executionOrder.push(executionId);
            this.stepStacks.set(executionId, []);

            // Prune old executions
            while (this.executionOrder.length > this.maxExecutions) {
                const oldId = this.executionOrder.shift()!;
                this.executions.delete(oldId);
                this.stepStacks.delete(oldId);
            }

            this.emit('executionStart', executionId, { functionName });
        }

        // Store definition (works for both new and nested calls)
        const exec = this.executions.get(executionId)!;
        if (definition && !exec.definitions.has(functionName)) {
            exec.definitions.set(functionName, definition);
        }
    }

    /**
     * End an execution
     */
    endExecution(executionId: string): void {
        if (!this.enabled) return;

        const execution = this.executions.get(executionId);
        if (execution) {
            execution.endTime = Date.now();
            this.emit('executionEnd', executionId, {
                duration: execution.endTime - execution.startTime,
                errored: execution.errored,
            });
        }
    }

    /**
     * Start tracking a step.
     * Called for each statement execution within a function.
     */
    startStep(
        executionId: string,
        statementName: string,
        functionName: string,
        args?: any,
        kirunFunctionName?: string,
    ): string | undefined {
        if (!this.enabled) return undefined;

        const execution = this.executions.get(executionId);
        if (!execution) return undefined;

        const stepId = `${Date.now()}_${Math.random().toString(36).slice(2)}`;
        const stack = this.stepStacks.get(executionId) || [];

        const log: LogEntry = {
            stepId,
            timestamp: Date.now(),
            functionName,
            statementName,
            kirunFunctionName,
            arguments: this.serialize(args),
            children: [],
        };

        this.pendingLogs.set(stepId, log);
        stack.push(log);
        this.stepStacks.set(executionId, stack);

        this.emit('stepStart', executionId, { stepId, statementName, functionName });

        return stepId;
    }

    /**
     * End tracking a step
     */
    endStep(executionId: string, stepId: string, eventName: string, result?: any, error?: string): void {
        if (!this.enabled) return;

        const log = this.pendingLogs.get(stepId);
        if (!log) return;

        const execution = this.executions.get(executionId);
        if (!execution) return;

        // Complete the log
        log.duration = Date.now() - log.timestamp;
        log.result = this.serialize(result);
        log.eventName = eventName;
        log.error = error;

        // Remove from stack
        const stack = this.stepStacks.get(executionId) || [];
        const idx = stack.findIndex((l) => l.stepId === stepId);
        if (idx !== -1) stack.splice(idx, 1);

        // Add to parent's children or to root logs
        if (stack.length > 0) {
            stack.at(-1)!.children.push(log);
        } else {
            execution.logs.push(log);
        }

        this.pendingLogs.delete(stepId);

        if (error) {
            execution.errored = true;
            this.emit('executionErrored', executionId);
        }

        this.emit('stepEnd', executionId, { log });
    }

    /**
     * Mark execution as errored
     */
    markErrored(executionId: string): void {
        const execution = this.executions.get(executionId);
        if (execution) {
            execution.errored = true;
            this.emit('executionErrored', executionId);
        }
    }

    // Query methods

    getExecution(executionId: string): ExecutionLog | undefined {
        return this.executions.get(executionId);
    }

    getLastExecution(): ExecutionLog | undefined {
        const lastId = this.executionOrder.at(-1);
        return lastId ? this.executions.get(lastId) : undefined;
    }

    getDefinition(executionId: string, key: string): any {
        return this.executions.get(executionId)?.definitions.get(key);
    }

    getFlatLogs(executionId: string): LogEntry[] {
        const execution = this.executions.get(executionId);
        if (!execution) return [];

        const result: LogEntry[] = [];
        const flatten = (logs: LogEntry[]) => {
            for (const log of logs) {
                result.push(log);
                if (log.children.length) flatten(log.children);
            }
        };
        flatten(execution.logs);
        return result;
    }

    getAllExecutionIds(): string[] {
        return [...this.executionOrder];
    }

    clear(): void {
        this.executions.clear();
        this.executionOrder = [];
        this.stepStacks.clear();
        this.pendingLogs.clear();
    }

    // Event listeners

    addEventListener(listener: DebugEventListener): () => void {
        this.listeners.add(listener);
        return () => this.listeners.delete(listener);
    }

    private emit(type: string, executionId: string, data?: any): void {
        const event = { type: type as any, executionId, data };
        this.listeners.forEach((l) => {
            try {
                l(event);
            } catch (e) {
                console.error('Debug listener error:', e);
            }
        });
    }

    private serialize(value: any): any {
        if (value == null) return value;
        if (value instanceof Map) {
            const obj: any = {};
            for (const [k, v] of value.entries()) obj[k] = this.serialize(v);
            return obj;
        }
        if (Array.isArray(value)) return value.map((v) => this.serialize(v));
        if (typeof value === 'object') {
            const obj: any = {};
            for (const [k, v] of Object.entries(value)) obj[k] = this.serialize(v);
            return obj;
        }
        return value;
    }
}

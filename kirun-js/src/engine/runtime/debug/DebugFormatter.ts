import { ExecutionLog, LogEntry } from './types';

/**
 * Performance summary for execution
 */
export interface PerformanceSummary {
    totalDuration: number;
    stepCount: number;
    slowestSteps: LogEntry[];
    averageDuration: number;
}

/**
 * Utility class for formatting debug information
 */
export class DebugFormatter {
    /**
     * Format debug info as text
     * @param executionLog - The execution log to format
     * @param indent - Indentation level (default: 0)
     * @returns Formatted string
     */
    public static formatAsText(executionLog: ExecutionLog, indent: number = 0): string {
        const indentStr = '  '.repeat(indent);
        const lines: string[] = [];

        const duration = executionLog.endTime
            ? executionLog.endTime - executionLog.startTime
            : Date.now() - executionLog.startTime;
        const status = executionLog.errored ? '❌' : '✓';

        lines.push(
            `${indentStr}${status} Execution: ${executionLog.executionId}`,
        );

        const endTimeStr = executionLog.endTime
            ? ` - ${new Date(executionLog.endTime).toISOString()}`
            : ' - ongoing';
        lines.push(
            `${indentStr}Duration: ${duration}ms (${new Date(executionLog.startTime).toISOString()}${endTimeStr})`,
        );
        lines.push(`${indentStr}Steps: ${executionLog.logs.length}`);
        lines.push('');

        for (const log of executionLog.logs) {
            lines.push(this.formatStep(log, indent + 1));
        }

        return lines.join('\n');
    }

    /**
     * Format debug info as JSON
     * @param executionLog - The execution log to format
     * @returns JSON string
     */
    public static formatAsJSON(executionLog: ExecutionLog): string {
        return JSON.stringify(executionLog, null, 2);
    }

    /**
     * Format a single log entry
     * @param log - The log entry
     * @param indent - Indentation level (default: 0)
     * @returns Formatted string
     */
    public static formatStep(log: LogEntry, indent: number = 0): string {
        const indentStr = '  '.repeat(indent);
        const lines: string[] = [];

        const status = log.error ? '❌ ERROR' : '✓';
        const duration = log.duration ?? 0;
        const statementName = log.statementName || '(no statement)';

        lines.push(
            `${indentStr}${status} ${statementName} => ${log.functionName} (${duration}ms)`,
        );

        if (log.eventName) {
            lines.push(`${indentStr}  Event: ${log.eventName}`);
        }

        if (log.error) {
            lines.push(`${indentStr}  Error: ${log.error}`);
        }

        return lines.join('\n');
    }

    /**
     * Get execution timeline (all logs in chronological order)
     * @param executionLog - The execution log
     * @returns Array of logs sorted by timestamp
     */
    public static getExecutionTimeline(executionLog: ExecutionLog): LogEntry[] {
        // Logs are already flat and chronological, just return sorted by timestamp
        return [...executionLog.logs].sort((a, b) => a.timestamp - b.timestamp);
    }

    /**
     * Get performance summary
     * @param executionLog - The execution log
     * @returns Performance summary
     */
    public static getPerformanceSummary(executionLog: ExecutionLog): PerformanceSummary {
        const logs = executionLog.logs;
        const stepCount = logs.length;
        const totalDuration = executionLog.endTime
            ? executionLog.endTime - executionLog.startTime
            : Date.now() - executionLog.startTime;
        const averageDuration = stepCount > 0 ? totalDuration / stepCount : 0;

        // Get top 10 slowest steps
        const slowestSteps = [...logs]
            .filter(log => log.duration !== undefined)
            .sort((a, b) => (b.duration ?? 0) - (a.duration ?? 0))
            .slice(0, 10);

        return {
            totalDuration,
            stepCount,
            slowestSteps,
            averageDuration,
        };
    }

    /**
     * Format performance summary as text
     * @param summary - The performance summary
     * @returns Formatted string
     */
    public static formatPerformanceSummary(summary: PerformanceSummary): string {
        const lines: string[] = [];

        lines.push('Performance Summary:');
        lines.push(`  Total Duration: ${summary.totalDuration}ms`);
        lines.push(`  Step Count: ${summary.stepCount}`);
        lines.push(`  Average Duration: ${summary.averageDuration.toFixed(2)}ms`);
        lines.push('');
        lines.push('Slowest Steps:');

        for (let i = 0; i < summary.slowestSteps.length; i++) {
            const log = summary.slowestSteps[i];
            const statementName = log.statementName || '(no statement)';
            lines.push(
                `  ${i + 1}. ${statementName} => ${log.functionName}: ${log.duration ?? 0}ms`,
            );
        }

        return lines.join('\n');
    }
}

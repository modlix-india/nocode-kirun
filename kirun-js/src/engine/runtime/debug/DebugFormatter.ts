import type { ExecutionLog, LogEntry } from './types';

export interface PerformanceSummary {
    totalDuration: number;
    stepCount: number;
    slowestSteps: LogEntry[];
    averageDuration: number;
}

export class DebugFormatter {
    static formatAsText(execution: ExecutionLog): string {
        const lines: string[] = [];
        const duration = execution.endTime
            ? execution.endTime - execution.startTime
            : Date.now() - execution.startTime;
        const status = execution.errored ? '❌' : '✓';

        lines.push(`${status} Execution: ${execution.executionId}`);
        lines.push(`Duration: ${duration}ms`);
        lines.push(`Steps: ${this.flatten(execution.logs).length}`);
        lines.push('');

        this.formatLogs(execution.logs, lines, 0);
        return lines.join('\n');
    }

    static getTimeline(execution: ExecutionLog): LogEntry[] {
        return this.flatten(execution.logs).sort((a, b) => a.timestamp - b.timestamp);
    }

    static getPerformanceSummary(execution: ExecutionLog): PerformanceSummary {
        const logs = this.flatten(execution.logs);
        const totalDuration = execution.endTime
            ? execution.endTime - execution.startTime
            : Date.now() - execution.startTime;

        return {
            totalDuration,
            stepCount: logs.length,
            averageDuration: logs.length > 0 ? totalDuration / logs.length : 0,
            slowestSteps: [...logs]
                .filter((l) => l.duration != null)
                .sort((a, b) => (b.duration ?? 0) - (a.duration ?? 0))
                .slice(0, 10),
        };
    }

    private static formatLogs(logs: LogEntry[], lines: string[], depth: number): void {
        const indent = '  '.repeat(depth);
        for (const log of logs) {
            const status = log.error ? '❌' : '✓';
            const name = log.statementName || '(anonymous)';
            lines.push(`${indent}${status} ${name} => ${log.functionName} (${log.duration ?? 0}ms)`);
            if (log.error) lines.push(`${indent}  Error: ${log.error}`);
            if (log.children.length) this.formatLogs(log.children, lines, depth + 1);
        }
    }

    private static flatten(logs: LogEntry[]): LogEntry[] {
        const result: LogEntry[] = [];
        const visit = (entries: LogEntry[]) => {
            for (const log of entries) {
                result.push(log);
                if (log.children.length) visit(log.children);
            }
        };
        visit(logs);
        return result;
    }
}

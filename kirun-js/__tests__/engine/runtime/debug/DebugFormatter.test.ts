import {
    DebugFormatter,
    GlobalDebugCollector,
} from '../../../../src/engine/runtime/debug';

describe('DebugFormatter', () => {
    let globalCollector: GlobalDebugCollector;

    beforeEach(() => {
        globalCollector = GlobalDebugCollector.getInstance();
        globalCollector.clearAll();
        globalCollector.enable();
    });

    afterEach(() => {
        globalCollector.disable();
        globalCollector.clearAll();
    });

    test('should format debug info as text', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'System.Math.Add');
        collector!.endStep(step1, 'output', { result: 3 });

        const step2 = collector!.startStep('step2', 'System.Math.Multiply');
        collector!.endStep(step2, 'output', { result: 6 });

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const text = DebugFormatter.formatAsText(debugInfo!);

        expect(text).toContain('test-eid');
        expect(text).toContain('step1');
        expect(text).toContain('System.Math.Add');
        expect(text).toContain('step2');
        expect(text).toContain('System.Math.Multiply');
        expect(text).toMatch(/Duration: \d+ms/);
    });

    test('should format debug info as JSON', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'System.Math.Add');
        collector!.endStep(step1, 'output', { result: 3 });

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const json = DebugFormatter.formatAsJSON(debugInfo!);

        expect(() => JSON.parse(json)).not.toThrow();

        const parsed = JSON.parse(json);
        expect(parsed.executionId).toBe('test-eid');
        expect(parsed.logs).toHaveLength(1);
        expect(parsed.logs[0].statementName).toBe('step1');
    });

    test('should format single step', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const stepId = collector!.startStep('testStep', 'System.Test.TestFunction');
        collector!.endStep(stepId, 'output', { value: 42 });

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const stepText = DebugFormatter.formatStep(debugInfo!.logs[0]);

        expect(stepText).toContain('testStep');
        expect(stepText).toContain('System.Test.TestFunction');
        expect(stepText).toContain('output');
        expect(stepText).toMatch(/\d+ms/);
    });

    test('should format step with error', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const stepId = collector!.startStep('failingStep', 'System.Test.Failing');
        collector!.endStep(stepId, 'error', undefined, 'Something went wrong');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const stepText = DebugFormatter.formatStep(debugInfo!.logs[0]);

        expect(stepText).toContain('❌ ERROR');
        expect(stepText).toContain('Something went wrong');
        expect(stepText).toContain('error');
    });

    test('should get execution timeline in chronological order', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'Function1');
        collector!.endStep(step1, 'output');

        const step2 = collector!.startStep('step2', 'Function2');
        collector!.endStep(step2, 'output');

        const step3 = collector!.startStep('step3', 'Function3');
        collector!.endStep(step3, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const timeline = DebugFormatter.getExecutionTimeline(debugInfo!);

        expect(timeline).toHaveLength(3);
        expect(timeline[0].statementName).toBe('step1');
        expect(timeline[1].statementName).toBe('step2');
        expect(timeline[2].statementName).toBe('step3');

        // Verify chronological order
        for (let i = 1; i < timeline.length; i++) {
            expect(timeline[i].timestamp).toBeGreaterThanOrEqual(timeline[i - 1].timestamp);
        }
    });

    test('should get timeline including nested calls', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'Function1');

        // Add nested call in same execution
        const step2 = collector!.startStep('nestedStep', 'Nested.Internal');
        collector!.endStep(step2, 'output');

        collector!.endStep(step1, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const timeline = DebugFormatter.getExecutionTimeline(debugInfo!);

        // Should include both parent and nested steps
        expect(timeline.length).toBe(2);
        expect(timeline.some((s) => s.statementName === 'step1')).toBe(true);
        expect(timeline.some((s) => s.statementName === 'nestedStep')).toBe(true);
    });

    test('should get performance summary', async () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('fastStep', 'Function1');
        await new Promise((resolve) => setTimeout(resolve, 5));
        collector!.endStep(step1, 'output');

        const step2 = collector!.startStep('slowStep', 'Function2');
        await new Promise((resolve) => setTimeout(resolve, 50));
        collector!.endStep(step2, 'output');

        const step3 = collector!.startStep('mediumStep', 'Function3');
        await new Promise((resolve) => setTimeout(resolve, 20));
        collector!.endStep(step3, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const summary = DebugFormatter.getPerformanceSummary(debugInfo!);

        expect(summary.stepCount).toBe(3);
        expect(summary.totalDuration).toBeGreaterThan(0);
        expect(summary.averageDuration).toBeGreaterThan(0);
        expect(summary.slowestSteps).toHaveLength(3);

        // Slowest step should be first
        expect(summary.slowestSteps[0].statementName).toBe('slowStep');
        expect(summary.slowestSteps[0].duration!).toBeGreaterThan(
            summary.slowestSteps[1].duration!,
        );
    });

    test('should format performance summary as text', async () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'Function1');
        await new Promise((resolve) => setTimeout(resolve, 10));
        collector!.endStep(step1, 'output');

        const step2 = collector!.startStep('step2', 'Function2');
        await new Promise((resolve) => setTimeout(resolve, 20));
        collector!.endStep(step2, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        const summary = DebugFormatter.getPerformanceSummary(debugInfo!);
        const summaryText = DebugFormatter.formatPerformanceSummary(summary);

        expect(summaryText).toContain('Performance Summary');
        expect(summaryText).toContain('Total Duration');
        expect(summaryText).toContain('Step Count: 2');
        expect(summaryText).toContain('Average Duration');
        expect(summaryText).toContain('Slowest Steps');
        expect(summaryText).toContain('step1');
        expect(summaryText).toContain('step2');
    });
});

import { DebugCollector, DebugFormatter } from '../../../../src/engine/runtime/debug';

describe('DebugFormatter', () => {
    let collector: DebugCollector;

    beforeEach(() => {
        collector = DebugCollector.getInstance();
        collector.clear();
        collector.enable();
    });

    afterEach(() => {
        collector.disable();
        collector.clear();
    });

    test('should format execution as text', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('test-eid', 'step1', 'System.Math.Add');
        collector.endStep('test-eid', step1!, 'output', { result: 3 });

        const step2 = collector.startStep('test-eid', 'step2', 'System.Math.Multiply');
        collector.endStep('test-eid', step2!, 'output', { result: 6 });

        const execution = collector.getExecution('test-eid');
        const text = DebugFormatter.formatAsText(execution!);

        expect(text).toContain('test-eid');
        expect(text).toContain('step1');
        expect(text).toContain('System.Math.Add');
        expect(text).toContain('step2');
        expect(text).toContain('System.Math.Multiply');
        expect(text).toMatch(/Duration: \d+ms/);
    });

    test('should format execution with error status', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('test-eid', 'failingStep', 'System.Test.Failing');
        collector.endStep('test-eid', stepId!, 'error', undefined, 'Something went wrong');

        const execution = collector.getExecution('test-eid');
        const text = DebugFormatter.formatAsText(execution!);

        expect(text).toContain('❌');
        expect(text).toContain('Error: Something went wrong');
    });

    test('should get execution timeline in chronological order', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('test-eid', 'step1', 'Function1');
        collector.endStep('test-eid', step1!, 'output');

        const step2 = collector.startStep('test-eid', 'step2', 'Function2');
        collector.endStep('test-eid', step2!, 'output');

        const step3 = collector.startStep('test-eid', 'step3', 'Function3');
        collector.endStep('test-eid', step3!, 'output');

        const execution = collector.getExecution('test-eid');
        const timeline = DebugFormatter.getTimeline(execution!);

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
        collector.startExecution('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('test-eid', 'step1', 'Function1');

        // Add nested call in same execution
        const step2 = collector.startStep('test-eid', 'nestedStep', 'Nested.Internal');
        collector.endStep('test-eid', step2!, 'output');

        collector.endStep('test-eid', step1!, 'output');

        const execution = collector.getExecution('test-eid');
        const timeline = DebugFormatter.getTimeline(execution!);

        // Should include both parent and nested steps
        expect(timeline.length).toBe(2);
        expect(timeline.some((s) => s.statementName === 'step1')).toBe(true);
        expect(timeline.some((s) => s.statementName === 'nestedStep')).toBe(true);
    });

    test('should get performance summary', async () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('test-eid', 'fastStep', 'Function1');
        await new Promise((resolve) => setTimeout(resolve, 5));
        collector.endStep('test-eid', step1!, 'output');

        const step2 = collector.startStep('test-eid', 'slowStep', 'Function2');
        await new Promise((resolve) => setTimeout(resolve, 50));
        collector.endStep('test-eid', step2!, 'output');

        const step3 = collector.startStep('test-eid', 'mediumStep', 'Function3');
        await new Promise((resolve) => setTimeout(resolve, 20));
        collector.endStep('test-eid', step3!, 'output');

        const execution = collector.getExecution('test-eid');
        const summary = DebugFormatter.getPerformanceSummary(execution!);

        expect(summary.stepCount).toBe(3);
        expect(summary.totalDuration).toBeGreaterThan(0);
        expect(summary.averageDuration).toBeGreaterThan(0);
        expect(summary.slowestSteps).toHaveLength(3);

        // Slowest step should be first
        expect(summary.slowestSteps[0].statementName).toBe('slowStep');
        expect(summary.slowestSteps[0].duration!).toBeGreaterThan(summary.slowestSteps[1].duration!);
    });

    test('should format hierarchical logs correctly', () => {
        collector.startExecution('test-eid', 'Parent.Function');

        const parentStep = collector.startStep('test-eid', 'parentStep', 'Parent.Function');
        const childStep = collector.startStep('test-eid', 'childStep', 'Child.Function');
        const grandchildStep = collector.startStep('test-eid', 'grandchildStep', 'Grandchild.Function');

        collector.endStep('test-eid', grandchildStep!, 'output');
        collector.endStep('test-eid', childStep!, 'output');
        collector.endStep('test-eid', parentStep!, 'output');

        const execution = collector.getExecution('test-eid');
        const text = DebugFormatter.formatAsText(execution!);

        // Should contain all step names with proper indentation
        expect(text).toContain('parentStep');
        expect(text).toContain('childStep');
        expect(text).toContain('grandchildStep');

        // Timeline should flatten all logs
        const timeline = DebugFormatter.getTimeline(execution!);
        expect(timeline).toHaveLength(3);
    });

    test('should handle empty execution', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const execution = collector.getExecution('test-eid');
        const text = DebugFormatter.formatAsText(execution!);
        const timeline = DebugFormatter.getTimeline(execution!);
        const summary = DebugFormatter.getPerformanceSummary(execution!);

        expect(text).toContain('test-eid');
        expect(text).toContain('Steps: 0');
        expect(timeline).toHaveLength(0);
        expect(summary.stepCount).toBe(0);
        expect(summary.averageDuration).toBe(0);
    });
});

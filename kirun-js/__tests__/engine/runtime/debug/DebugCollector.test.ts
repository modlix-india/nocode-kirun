import { GlobalDebugCollector } from '../../../../src/engine/runtime/debug';

describe('DebugCollector', () => {
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

    test('should initialize with correct values', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        expect(collector).toBeDefined();

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        expect(debugInfo).toBeDefined();
        expect(debugInfo!.executionId).toBe('test-eid');
        expect(debugInfo!.logs).toHaveLength(0);
        expect(debugInfo!.startTime).toBeGreaterThan(0);
    });

    test('should track step start and end', async () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const stepId = collector!.startStep('step1', 'System.Math.Add', { a: 1, b: 2 });

        // Simulate some work
        await new Promise((resolve) => setTimeout(resolve, 10));

        collector!.endStep(stepId, 'output', { result: 3 });

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        expect(debugInfo!.logs).toHaveLength(1);
        expect(debugInfo!.logs[0].statementName).toBe('step1');
        expect(debugInfo!.logs[0].functionName).toBe('System.Math.Add');
        expect(debugInfo!.logs[0].eventName).toBe('output');
        expect(debugInfo!.logs[0].duration).toBeGreaterThan(0);
        expect(debugInfo!.logs[0].arguments).toEqual({ a: 1, b: 2 });
        expect(debugInfo!.logs[0].result).toEqual({ result: 3 });
    });

    test('should track multiple steps in order', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const step1 = collector!.startStep('step1', 'Function1');
        collector!.endStep(step1, 'output');

        const step2 = collector!.startStep('step2', 'Function2');
        collector!.endStep(step2, 'output');

        const step3 = collector!.startStep('step3', 'Function3');
        collector!.endStep(step3, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        expect(debugInfo!.logs).toHaveLength(3);
        expect(debugInfo!.logs[0].statementName).toBe('step1');
        expect(debugInfo!.logs[1].statementName).toBe('step2');
        expect(debugInfo!.logs[2].statementName).toBe('step3');
    });

    test('should record error in step', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const stepId = collector!.startStep('failingStep', 'System.Test.Failing');
        collector!.endStep(stepId, 'error', undefined, 'Test error message');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        expect(debugInfo!.logs[0].error).toBe('Test error message');
        expect(debugInfo!.logs[0].eventName).toBe('error');
        expect(debugInfo!.errored).toBe(true);
    });

    test('should track nested function calls in same execution', () => {
        const parentCollector = globalCollector.createCollector('test-eid', 'Parent', 'Function');
        const stepId = parentCollector!.startStep('nestedCall', 'Child.Function');

        // Create nested collector with same execution ID
        const childCollector = globalCollector.createCollector('test-eid', 'Child', 'Function');
        const childStep = childCollector!.startStep('childStep', 'Child.InternalFunction');
        childCollector!.endStep(childStep, 'output', { value: 42 });

        parentCollector!.endStep(stepId, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        // Both parent and child steps should be in the same execution log
        expect(debugInfo!.logs.length).toBeGreaterThanOrEqual(2);
        expect(debugInfo!.logs.some(log => log.statementName === 'nestedCall')).toBe(true);
        expect(debugInfo!.logs.some(log => log.statementName === 'childStep')).toBe(true);
    });

    test('should handle missing step ID gracefully', () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        // Try to end a step that was never started
        expect(() => {
            collector!.endStep('non-existent-step', 'output');
        }).not.toThrow();

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');
        expect(debugInfo!.logs).toHaveLength(0);
    });

    test('should calculate correct duration', async () => {
        const collector = globalCollector.createCollector('test-eid', 'System.Test', 'Function');

        const stepId = collector!.startStep('timedStep', 'System.Test.Timed');

        // Wait a known amount of time
        await new Promise((resolve) => setTimeout(resolve, 50));

        collector!.endStep(stepId, 'output');

        const debugInfo = globalCollector.getDebugInfoByExecutionId('test-eid');

        // Duration should be at least 50ms (accounting for timing variations)
        expect(debugInfo!.logs[0].duration).toBeGreaterThanOrEqual(45);
        expect(debugInfo!.logs[0].duration).toBeLessThan(100);
    });
});

import { DebugCollector } from '../../../../src/engine/runtime/debug/DebugInfo';

describe('DebugCollector', () => {
    test('should initialize with correct values', () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        const debugInfo = collector.getExecutionDebugInfo();

        expect(debugInfo.executionId).toBe('test-eid');
        expect(debugInfo.rootFunctionName).toBe('System.Test.Function');
        expect(debugInfo.steps).toHaveLength(0);
        expect(debugInfo.totalDuration).toBeGreaterThanOrEqual(0);
    });

    test('should track step start and end', async () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('step1', 'System.Math.Add', { a: 1, b: 2 });

        // Simulate some work
        await new Promise((resolve) => setTimeout(resolve, 10));

        collector.endStep(stepId, 'output', { result: 3 });

        const debugInfo = collector.getExecutionDebugInfo();

        expect(debugInfo.steps).toHaveLength(1);
        expect(debugInfo.steps[0].statementName).toBe('step1');
        expect(debugInfo.steps[0].functionName).toBe('System.Math.Add');
        expect(debugInfo.steps[0].eventName).toBe('output');
        expect(debugInfo.steps[0].duration).toBeGreaterThan(0);
        expect(debugInfo.steps[0].arguments).toEqual({ a: 1, b: 2 });
        expect(debugInfo.steps[0].result).toEqual({ result: 3 });
    });

    test('should track multiple steps in order', () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('step1', 'Function1');
        collector.endStep(step1, 'output');

        const step2 = collector.startStep('step2', 'Function2');
        collector.endStep(step2, 'output');

        const step3 = collector.startStep('step3', 'Function3');
        collector.endStep(step3, 'output');

        const debugInfo = collector.getExecutionDebugInfo();

        expect(debugInfo.steps).toHaveLength(3);
        expect(debugInfo.steps[0].statementName).toBe('step1');
        expect(debugInfo.steps[1].statementName).toBe('step2');
        expect(debugInfo.steps[2].statementName).toBe('step3');
    });

    test('should record error in step', () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('failingStep', 'System.Test.Failing');
        collector.endStep(stepId, 'error', undefined, 'Test error message');

        const debugInfo = collector.getExecutionDebugInfo();

        expect(debugInfo.steps[0].error).toBe('Test error message');
        expect(debugInfo.steps[0].eventName).toBe('error');
    });

    test('should add nested debug info', () => {
        const parentCollector = new DebugCollector('parent-eid', 'Parent.Function');
        const stepId = parentCollector.startStep('nestedCall', 'Child.Function');

        // Create nested execution debug info
        const childCollector = new DebugCollector('child-eid', 'Child.Function');
        const childStep = childCollector.startStep('childStep', 'Child.InternalFunction');
        childCollector.endStep(childStep, 'output', { value: 42 });
        const childDebugInfo = childCollector.getExecutionDebugInfo();

        // Add nested info to parent
        parentCollector.addNestedDebugInfo(stepId, childDebugInfo);
        parentCollector.endStep(stepId, 'output');

        const parentDebugInfo = parentCollector.getExecutionDebugInfo();

        expect(parentDebugInfo.steps).toHaveLength(1);
        expect(parentDebugInfo.steps[0].nestedCalls).toBeDefined();
        expect(parentDebugInfo.steps[0].nestedCalls).toHaveLength(1);
        expect(parentDebugInfo.steps[0].nestedCalls![0].statementName).toBe('childStep');
        expect(parentDebugInfo.steps[0].nestedCalls![0].result).toEqual({ value: 42 });
    });

    test('should handle missing step ID gracefully', () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        // Try to end a step that was never started
        expect(() => {
            collector.endStep('non-existent-step', 'output');
        }).not.toThrow();

        const debugInfo = collector.getExecutionDebugInfo();
        expect(debugInfo.steps).toHaveLength(0);
    });

    test('should calculate correct duration', async () => {
        const collector = new DebugCollector('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('timedStep', 'System.Test.Timed');

        // Wait a known amount of time
        await new Promise((resolve) => setTimeout(resolve, 50));

        collector.endStep(stepId, 'output');

        const debugInfo = collector.getExecutionDebugInfo();

        // Duration should be at least 50ms (accounting for timing variations)
        expect(debugInfo.steps[0].duration).toBeGreaterThanOrEqual(45);
        expect(debugInfo.steps[0].duration).toBeLessThan(100);
    });
});

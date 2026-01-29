import { DebugCollector } from '../../../../src/engine/runtime/debug';

describe('DebugCollector', () => {
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

    test('should initialize execution correctly', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const debugInfo = collector.getExecution('test-eid');

        expect(debugInfo).toBeDefined();
        expect(debugInfo!.executionId).toBe('test-eid');
        expect(debugInfo!.logs).toHaveLength(0);
        expect(debugInfo!.startTime).toBeGreaterThan(0);
    });

    test('should track step start and end', async () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('test-eid', 'step1', 'System.Math.Add', { a: 1, b: 2 });

        await new Promise((resolve) => setTimeout(resolve, 10));

        collector.endStep('test-eid', stepId!, 'output', { result: 3 });

        const debugInfo = collector.getExecution('test-eid');

        expect(debugInfo!.logs).toHaveLength(1);
        expect(debugInfo!.logs[0].statementName).toBe('step1');
        expect(debugInfo!.logs[0].functionName).toBe('System.Math.Add');
        expect(debugInfo!.logs[0].eventName).toBe('output');
        expect(debugInfo!.logs[0].duration).toBeGreaterThan(0);
        expect(debugInfo!.logs[0].arguments).toEqual({ a: 1, b: 2 });
        expect(debugInfo!.logs[0].result).toEqual({ result: 3 });
    });

    test('should track multiple steps in order', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const step1 = collector.startStep('test-eid', 'step1', 'Function1');
        collector.endStep('test-eid', step1!, 'output');

        const step2 = collector.startStep('test-eid', 'step2', 'Function2');
        collector.endStep('test-eid', step2!, 'output');

        const step3 = collector.startStep('test-eid', 'step3', 'Function3');
        collector.endStep('test-eid', step3!, 'output');

        const debugInfo = collector.getExecution('test-eid');

        expect(debugInfo!.logs).toHaveLength(3);
        expect(debugInfo!.logs[0].statementName).toBe('step1');
        expect(debugInfo!.logs[1].statementName).toBe('step2');
        expect(debugInfo!.logs[2].statementName).toBe('step3');
    });

    test('should record error in step', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('test-eid', 'failingStep', 'System.Test.Failing');
        collector.endStep('test-eid', stepId!, 'error', undefined, 'Test error message');

        const debugInfo = collector.getExecution('test-eid');

        expect(debugInfo!.logs[0].error).toBe('Test error message');
        expect(debugInfo!.logs[0].eventName).toBe('error');
        expect(debugInfo!.errored).toBe(true);
    });

    test('should track nested function calls hierarchically', () => {
        collector.startExecution('test-eid', 'Parent.Function');

        const parentStep = collector.startStep('test-eid', 'parentStep', 'Parent.Function');
        const childStep = collector.startStep('test-eid', 'childStep', 'Child.Function');
        collector.endStep('test-eid', childStep!, 'output', { value: 42 });
        collector.endStep('test-eid', parentStep!, 'output');

        const debugInfo = collector.getExecution('test-eid');

        // Parent step should be at root level
        expect(debugInfo!.logs).toHaveLength(1);
        expect(debugInfo!.logs[0].statementName).toBe('parentStep');
        // Child step should be nested in parent's children
        expect(debugInfo!.logs[0].children).toHaveLength(1);
        expect(debugInfo!.logs[0].children[0].statementName).toBe('childStep');
    });

    test('should flatten nested logs correctly', () => {
        collector.startExecution('test-eid', 'Parent.Function');

        const parentStep = collector.startStep('test-eid', 'parentStep', 'Parent.Function');
        const childStep = collector.startStep('test-eid', 'childStep', 'Child.Function');
        collector.endStep('test-eid', childStep!, 'output');
        collector.endStep('test-eid', parentStep!, 'output');

        const flatLogs = collector.getFlatLogs('test-eid');

        expect(flatLogs).toHaveLength(2);
        expect(flatLogs.some((l) => l.statementName === 'parentStep')).toBe(true);
        expect(flatLogs.some((l) => l.statementName === 'childStep')).toBe(true);
    });

    test('should handle missing step ID gracefully', () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        expect(() => {
            collector.endStep('test-eid', 'non-existent-step', 'output');
        }).not.toThrow();

        const debugInfo = collector.getExecution('test-eid');
        expect(debugInfo!.logs).toHaveLength(0);
    });

    test('should calculate correct duration', async () => {
        collector.startExecution('test-eid', 'System.Test.Function');

        const stepId = collector.startStep('test-eid', 'timedStep', 'System.Test.Timed');

        await new Promise((resolve) => setTimeout(resolve, 50));

        collector.endStep('test-eid', stepId!, 'output');

        const debugInfo = collector.getExecution('test-eid');

        expect(debugInfo!.logs[0].duration).toBeGreaterThanOrEqual(45);
        expect(debugInfo!.logs[0].duration).toBeLessThan(150);
    });

    test('should store definitions via startExecution', () => {
        const definition = { name: 'TestFunc', steps: { step1: {} } };

        // Start execution with definition
        collector.startExecution('test-eid', 'MyNamespace.TestFunc', definition);

        const step1 = collector.startStep('test-eid', 'step1', 'System.Math.Add');
        collector.endStep('test-eid', step1!, 'output');

        const debugInfo = collector.getExecution('test-eid');

        // Definition should be stored
        expect(debugInfo!.definitions.size).toBe(1);
        expect(debugInfo!.definitions.get('MyNamespace.TestFunc')).toEqual(definition);
    });

    test('should store multiple definitions for nested function calls', () => {
        const parentDef = { name: 'Parent', steps: { callChild: {} } };
        const childDef = { name: 'Child', steps: { doWork: {} } };

        // Parent function starts
        collector.startExecution('test-eid', 'App.Parent', parentDef);

        // Parent starts a step that will call child
        const parentStep = collector.startStep('test-eid', 'callChild', 'App.Child');

        // Child function starts (nested call with same executionId)
        collector.startExecution('test-eid', 'App.Child', childDef);

        const childStep = collector.startStep('test-eid', 'doWork', 'System.Math.Add');
        collector.endStep('test-eid', childStep!, 'output');

        collector.endStep('test-eid', parentStep!, 'output');

        const debugInfo = collector.getExecution('test-eid');

        // Both definitions should be stored
        expect(debugInfo!.definitions.size).toBe(2);
        expect(debugInfo!.definitions.get('App.Parent')).toEqual(parentDef);
        expect(debugInfo!.definitions.get('App.Child')).toEqual(childDef);
    });

    describe('Event Listeners', () => {
        test('should emit executionStart event', () => {
            const events: any[] = [];
            const unsubscribe = collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Test.Function');

            expect(events).toHaveLength(1);
            expect(events[0].type).toBe('executionStart');
            expect(events[0].executionId).toBe('test-eid');
            expect(events[0].data.functionName).toBe('Test.Function');

            unsubscribe();
        });

        test('should emit stepStart and stepEnd events', () => {
            const events: any[] = [];
            collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Test.Function');
            const stepId = collector.startStep('test-eid', 'step1', 'System.Math.Add', { a: 1 });
            collector.endStep('test-eid', stepId!, 'output', { result: 2 });

            // executionStart, stepStart, stepEnd
            expect(events).toHaveLength(3);

            expect(events[1].type).toBe('stepStart');
            expect(events[1].data.statementName).toBe('step1');
            expect(events[1].data.functionName).toBe('System.Math.Add');

            expect(events[2].type).toBe('stepEnd');
            expect(events[2].data.log.statementName).toBe('step1');
            expect(events[2].data.log.result).toEqual({ result: 2 });
        });

        test('should emit executionEnd event', () => {
            const events: any[] = [];
            collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Test.Function');
            collector.endExecution('test-eid');

            const endEvent = events.find((e) => e.type === 'executionEnd');
            expect(endEvent).toBeDefined();
            expect(endEvent.executionId).toBe('test-eid');
            expect(endEvent.data.duration).toBeGreaterThanOrEqual(0);
        });

        test('should emit executionErrored event on error', () => {
            const events: any[] = [];
            collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Test.Function');
            const stepId = collector.startStep('test-eid', 'step1', 'Failing.Function');
            collector.endStep('test-eid', stepId!, 'error', undefined, 'Something failed');

            const errorEvent = events.find((e) => e.type === 'executionErrored');
            expect(errorEvent).toBeDefined();
            expect(errorEvent.executionId).toBe('test-eid');
        });

        test('should allow unsubscribing from events', () => {
            const events: any[] = [];
            const unsubscribe = collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Test.Function');
            expect(events).toHaveLength(1);

            unsubscribe();

            collector.startExecution('test-eid2', 'Test.Function2');
            expect(events).toHaveLength(1); // No new events after unsubscribe
        });

        test('should emit events in correct order for nested calls', () => {
            const events: any[] = [];
            collector.addEventListener((e) => events.push(e));

            collector.startExecution('test-eid', 'Parent.Function');
            const parentStep = collector.startStep('test-eid', 'parentStep', 'Child.Function');

            // Nested function
            collector.startExecution('test-eid', 'Child.Function');
            const childStep = collector.startStep('test-eid', 'childStep', 'System.Math.Add');
            collector.endStep('test-eid', childStep!, 'output');

            collector.endStep('test-eid', parentStep!, 'output');
            collector.endExecution('test-eid');

            const eventTypes = events.map((e) => e.type);
            expect(eventTypes).toEqual([
                'executionStart', // Parent starts
                'stepStart', // parentStep starts
                'stepStart', // childStep starts
                'stepEnd', // childStep ends
                'stepEnd', // parentStep ends
                'executionEnd', // Execution ends
            ]);
        });
    });
});

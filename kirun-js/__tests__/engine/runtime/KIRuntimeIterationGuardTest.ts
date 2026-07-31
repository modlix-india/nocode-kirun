import {
    FunctionDefinition,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
} from '../../../src';

/**
 * Regression test for the runaway-execution guard.
 *
 * The counter used to be incremented only when a queue size changed, which disabled the guard
 * in exactly the case it exists for: a graph making no progress leaves the queue sizes
 * untouched, so the counter never advanced and executeGraph could spin forever. The check was
 * also `==` rather than `>=`, so a skipped boundary value would never trip it.
 */

// A CountLoop with a step in its body, so execution takes many statement iterations.
const loopingDefinition = {
    name: 'loops',
    namespace: 'TestUI',
    steps: {
        loop: {
            statementName: 'loop',
            name: 'CountLoop',
            namespace: 'System.Loop',
            parameterMap: {
                count: {
                    one: {
                        key: 'one',
                        type: 'VALUE',
                        value: 40,
                    },
                },
            },
        },
        printer: {
            statementName: 'printer',
            name: 'Print',
            namespace: 'System',
            dependentStatements: {
                'Steps.loop.iteration': true,
            },
            parameterMap: {
                values: {
                    one: {
                        key: 'one',
                        type: 'EXPRESSION',
                        expression: 'Steps.loop.iteration.index',
                    },
                },
            },
        },
    },
};

function run(): Promise<any> {
    const runtime = new KIRuntime(FunctionDefinition.from(loopingDefinition));

    return runtime.execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map()),
    );
}

describe('KIRuntime execution iteration guard', () => {
    const originalMax = KIRuntime.MAX_EXECUTION_ITERATIONS;

    afterEach(() => {
        KIRuntime.MAX_EXECUTION_ITERATIONS = originalMax;
    });

    test('default cap is 1,000,000 and is tunable', () => {
        expect(originalMax).toBe(1000000);
    });

    test('a loop that exceeds the cap is stopped', async () => {
        // Low enough that a 40-pass loop is guaranteed to cross it. If the counter were still
        // gated on a queue-size change this would run to completion instead of throwing.
        KIRuntime.MAX_EXECUTION_ITERATIONS = 5;

        await expect(run()).rejects.toThrow(/Execution locked in an infinite loop/);
    });

    test('the same loop completes when the cap is not exceeded', async () => {
        KIRuntime.MAX_EXECUTION_ITERATIONS = originalMax;

        await expect(run()).resolves.toBeDefined();
    });

    // A linear chain pops one vertex and pushes its successor, so the queue size is unchanged
    // across passes even though real work happened. Under the old "only count when a queue size
    // changed" rule the counter stayed at zero here, which is precisely why a stuck graph could
    // never trip the guard. Counting must now advance.
    test('counts passes even when the queue size never changes', async () => {
        const linearChain = {
            name: 'linear',
            namespace: 'TestUI',
            steps: {
                first: {
                    statementName: 'first',
                    name: 'Print',
                    namespace: 'System',
                    parameterMap: {
                        values: { one: { key: 'one', type: 'VALUE', value: 'a' } },
                    },
                },
                second: {
                    statementName: 'second',
                    name: 'Print',
                    namespace: 'System',
                    dependentStatements: { 'Steps.first.output': true },
                    parameterMap: {
                        values: { one: { key: 'one', type: 'VALUE', value: 'b' } },
                    },
                },
                third: {
                    statementName: 'third',
                    name: 'Print',
                    namespace: 'System',
                    dependentStatements: { 'Steps.second.output': true },
                    parameterMap: {
                        values: { one: { key: 'one', type: 'VALUE', value: 'c' } },
                    },
                },
            },
        };

        const params = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map());

        await new KIRuntime(FunctionDefinition.from(linearChain)).execute(params);

        expect(params.getCount()).toBeGreaterThanOrEqual(3);
    });
});

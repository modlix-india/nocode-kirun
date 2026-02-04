import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';

// Fibonacci function from test-data
const fibonacciJson = {
    name: 'fibonaccii',
    namespace: 'TestUI',
    parameters: {
        n: {
            parameterName: 'n',
            schema: {
                type: ['INTEGER'],
                version: 1,
            },
        },
    },
    events: {
        output: {
            name: 'output',
            parameters: {
                result: {
                    type: ['ARRAY'],
                    version: 1,
                    items: {
                        type: ['INTEGER'],
                    },
                },
            },
        },
    },
    steps: {
        create: {
            statementName: 'create',
            name: 'Create',
            namespace: 'System.Context',
            parameterMap: {
                name: {
                    '2JUURU6NHj9voaArLv0pS8': {
                        key: '2JUURU6NHj9voaArLv0pS8',
                        type: 'VALUE',
                        order: 1,
                        value: 'a',
                    },
                },
                schema: {
                    '2vxDXyOakSmhiUmH4CuDED': {
                        key: '2vxDXyOakSmhiUmH4CuDED',
                        type: 'VALUE',
                        order: 1,
                        value: {
                            type: 'ARRAY',
                            items: {
                                type: 'INTEGER',
                            },
                        },
                    },
                },
            },
        },
        rangeLoop: {
            statementName: 'rangeLoop',
            name: 'RangeLoop',
            namespace: 'System.Loop',
            parameterMap: {
                to: {
                    '6RAIEdM1OAek2AKf64ucqM': {
                        key: '6RAIEdM1OAek2AKf64ucqM',
                        type: 'EXPRESSION',
                        expression: 'Arguments.n',
                        value: 1,
                    },
                },
            },
            dependentStatements: {
                'Steps.create.output': false,
                'Steps.set2.output': true,
            },
        },
        if: {
            statementName: 'if',
            name: 'If',
            namespace: 'System',
            parameterMap: {
                condition: {
                    C2FB54BobtFXTmLKv4v6s: {
                        key: 'C2FB54BobtFXTmLKv4v6s',
                        type: 'EXPRESSION',
                        expression: 'Steps.rangeLoop.iteration.index < 2',
                        order: 1,
                    },
                },
            },
        },
        trueInsert: {
            statementName: 'trueInsert',
            name: 'InsertLast',
            namespace: 'System.Array',
            parameterMap: {
                source: {
                    '4DzS3icjwGl7nUPvk7QUST': {
                        key: '4DzS3icjwGl7nUPvk7QUST',
                        type: 'EXPRESSION',
                        expression: 'Context.a',
                        order: 1,
                    },
                },
                element: {
                    '1sWH8NKqkdKevU7vQJk9jn': {
                        key: '1sWH8NKqkdKevU7vQJk9jn',
                        type: 'EXPRESSION',
                        expression: 'Steps.rangeLoop.iteration.index',
                        order: 1,
                    },
                },
            },
            dependentStatements: {
                'Steps.if.true': true,
            },
        },
        set: {
            statementName: 'set',
            name: 'Set',
            namespace: 'System.Context',
            parameterMap: {
                name: {
                    UPMhlPSuFHCygh0DYa7Zc: {
                        key: 'UPMhlPSuFHCygh0DYa7Zc',
                        type: 'VALUE',
                        order: 1,
                        value: 'Context.a',
                    },
                },
                value: {
                    '1B8Bfavmym6BmbdsDesmNX': {
                        key: '1B8Bfavmym6BmbdsDesmNX',
                        type: 'EXPRESSION',
                        expression: 'Steps.trueInsert.output.result',
                        order: 1,
                    },
                },
            },
            dependentStatements: {
                'Steps.if.true': false,
            },
        },
        set1: {
            statementName: 'set1',
            name: 'Set',
            namespace: 'System.Context',
            parameterMap: {
                name: {
                    '6dYVAyspJbF82O8qJm3f2O': {
                        key: '6dYVAyspJbF82O8qJm3f2O',
                        type: 'VALUE',
                        order: 1,
                        value: 'Context.a',
                    },
                },
                value: {
                    fVB75EKlR6It0i7iho3oQ: {
                        key: 'fVB75EKlR6It0i7iho3oQ',
                        type: 'EXPRESSION',
                        expression: 'Steps.falseInsert.output.result',
                        order: 1,
                    },
                },
            },
            dependentStatements: {
                'Steps.if.false': false,
            },
        },
        falseInsert: {
            statementName: 'falseInsert',
            name: 'InsertLast',
            namespace: 'System.Array',
            parameterMap: {
                source: {
                    '1GsJu3FbuponBIcaqI48rF': {
                        key: '1GsJu3FbuponBIcaqI48rF',
                        type: 'EXPRESSION',
                        expression: 'Context.a',
                        order: 1,
                    },
                },
                element: {
                    '53vsp50RjvhS94xdCtWWx0': {
                        key: '53vsp50RjvhS94xdCtWWx0',
                        type: 'EXPRESSION',
                        expression:
                            'Context.a[Steps.rangeLoop.iteration.index - 1] + Context.a[Steps.rangeLoop.iteration.index - 2]',
                        order: 1,
                    },
                },
            },
            dependentStatements: {
                'Steps.if.false': true,
            },
        },
        set2: {
            statementName: 'set2',
            name: 'Set',
            namespace: 'System.Context',
            parameterMap: {
                name: {
                    '2XVsWDK0VUQhZJ0VnJllrQ': {
                        key: '2XVsWDK0VUQhZJ0VnJllrQ',
                        type: 'VALUE',
                        order: 1,
                        value: 'Context.a',
                    },
                },
                value: {
                    '1ksRfalqqgGfh4uZcLXDy9': {
                        key: '1ksRfalqqgGfh4uZcLXDy9',
                        type: 'VALUE',
                        order: 1,
                        value: [],
                    },
                },
            },
            dependentStatements: {
                'Steps.create.output': true,
            },
        },
        generateEvent: {
            statementName: 'generateEvent',
            name: 'GenerateEvent',
            namespace: 'System',
            parameterMap: {
                results: {
                    '8brcu20HciA9rYDPOFeef': {
                        key: '8brcu20HciA9rYDPOFeef',
                        type: 'VALUE',
                        order: 1,
                        value: {
                            name: 'result',
                            value: {
                                isExpression: true,
                                value: 'Context.a',
                            },
                        },
                    },
                },
            },
            dependentStatements: {
                'Steps.rangeLoop.output': true,
            },
        },
    },
};

import { JSONToTextTransformer } from '../../../src/engine/dsl/transformer/JSONToText';

describe('Execution Graph Debug', () => {
    test('should transform fibonacci to DSL with nested blocks', async () => {
        const transformer = new JSONToTextTransformer();
        const dslText = await transformer.transform(fibonacciJson);
        console.log('\n=== GENERATED DSL ===');
        console.log(dslText);

        // Verify nesting
        expect(dslText).toContain('rangeLoop:');
        expect(dslText).toContain('iteration');
        expect(dslText).toContain('if:');
        expect(dslText).toContain('true');
        expect(dslText).toContain('false');
        expect(dslText).toContain('trueInsert:');
        expect(dslText).toContain('falseInsert:');
    });

    test('should show graph edges for fibonacci', async () => {
        const funcDef = FunctionDefinition.from(fibonacciJson);
        const runtime = new KIRuntime(funcDef);
        const functionRepo = new KIRunFunctionRepository();
        const schemaRepo = new KIRunSchemaRepository();

        const graph = await runtime.getExecutionPlan(functionRepo, schemaRepo);
        console.log('Graph created successfully');
        console.log('Nodes:', Array.from(graph.getNodeMap().keys()));

        console.log('\n=== EXPECTED NESTING ===');
        console.log('if -> nested under rangeLoop.iteration (uses Steps.rangeLoop.iteration.index)');
        console.log('trueInsert -> nested under if.true (dependentStatements["Steps.if.true"])');
        console.log('set -> nested under if.true (has dep on trueInsert.output which is under if.true)');
        console.log('falseInsert -> nested under if.false (dependentStatements["Steps.if.false"])');
        console.log('set1 -> nested under if.false (has dep on falseInsert.output which is under if.false)');
        console.log('generateEvent -> nested under rangeLoop.output (dependentStatements["Steps.rangeLoop.output"])');
        console.log('set2 -> nested under create.output (dependentStatements["Steps.create.output"])');

        console.log('\n=== ACTUAL GRAPH EDGES ===');
        const validBlockNames = new Set(['true', 'false', 'iteration', 'output', 'error']);

        for (const [stepName, vertex] of graph.getNodeMap()) {
            console.log(`\nStep: ${stepName}`);
            const inVertices = vertex.getInVertices();
            console.log(`  InVertices count: ${inVertices.size}`);

            const blockDeps: { parent: string; blockName: string }[] = [];
            for (const tuple of Array.from(inVertices)) {
                const parentKey = tuple.getT1().getKey();
                const edgeType = tuple.getT2();
                console.log(`  - Parent: ${parentKey}, EdgeType: "${edgeType}"`);
                if (validBlockNames.has(edgeType)) {
                    blockDeps.push({ parent: parentKey, blockName: edgeType });
                }
            }
            if (blockDeps.length > 0) {
                console.log(`  -> Should be nested under: ${blockDeps.map((d) => `${d.parent}.${d.blockName}`).join(', ')}`);
            }
        }

        expect(graph.getNodeMap().size).toBeGreaterThan(0);
    });
});

import { DSLCompiler } from '../../../src/engine/dsl/DSLCompiler';
import { KIRunFunctionRepository } from '../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionDefinition } from '../../../src/engine/model/FunctionDefinition';
import { FunctionExecutionParameters } from '../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

/**
 * GenerateEvent returns named values. Each `results` entry is `{name, value}` and its `value` is
 * always an object, `{isExpression, value}`. `value: "odd"` used to compile to a bare string, which
 * the runtime rejected ("Expected an object but found odd"); a literal now compiles to
 * `{isExpression: false, value}` and prints back as the literal.
 */
describe('GenerateEvent results with literal values', () => {
    const SOURCE = `FUNCTION Parity
    NAMESPACE Test
    PARAMETERS
        n AS INTEGER
    EVENTS
        output
            kind AS STRING
            n AS INTEGER
            tags AS ARRAY OF STRING
            sum AS INTEGER
    LOGIC
        out: System.GenerateEvent(results = {name: "kind", value: "odd"}, results = {name: "n", value: 5}, results = {name: "tags", value: ["a", "b"]}, results = {name: "sum", value: Arguments.n + 1})`;

    const resultsOf = (json: any) =>
        Object.values<any>(json.steps.out.parameterMap.results)
            .sort((a, b) => a.order - b.order)
            .map((r) => r.value);

    test('a literal compiles to {isExpression: false, value}', () => {
        expect(resultsOf(DSLCompiler.compile(SOURCE))).toEqual([
            { name: 'kind', value: { isExpression: false, value: 'odd' } },
            { name: 'n', value: { isExpression: false, value: 5 } },
            { name: 'tags', value: { isExpression: false, value: ['a', 'b'] } },
            { name: 'sum', value: { isExpression: true, value: 'Arguments.n + 1' } },
        ]);
    });

    test('prints back as the literal and round-trips', async () => {
        const json = DSLCompiler.compile(SOURCE);
        const printed = await DSLCompiler.decompile(json);
        expect(printed).toContain('{name: "kind", value: "odd"}');
        expect(printed).toContain('{name: "n", value: 5}');
        expect(printed).toContain('{name: "tags", value: ["a", "b"]}');
        expect(resultsOf(DSLCompiler.compile(printed))).toEqual(resultsOf(json));
    });

    test('runs and returns every named value', async () => {
        const fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
            .setArguments(new Map([['n', 2]]));
        const out = await new KIRuntime(FunctionDefinition.from(DSLCompiler.compile(SOURCE))).execute(fep);
        const result = out.allResults()[0].getResult();
        expect(result.get('kind')).toBe('odd');
        expect(result.get('n')).toBe(5);
        expect(result.get('tags')).toEqual(['a', 'b']);
        expect(result.get('sum')).toBe(3);
    });
});

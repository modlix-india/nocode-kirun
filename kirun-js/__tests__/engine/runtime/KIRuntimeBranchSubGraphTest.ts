import {
    AbstractFunction,
    EventResult,
    ExecutionGraph,
    Function,
    FunctionDefinition,
    FunctionExecutionParameters,
    FunctionOutput,
    FunctionSignature,
    GraphVertex,
    HybridRepository,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
    LinkedList,
    Parameter,
    Repository,
    Schema,
} from '../../../src';

// ADZ-1423: runs real functions with If branches and loops twice, once with the old (3.17.2,
// no visited set) sub-graph walk patched in and once with the current one, and checks that the
// steps run in exactly the same order with the same results, and that the plans' sub-graphs match.

let trace: string[] = [];

class Rec extends AbstractFunction {
    private readonly signature = new FunctionSignature('Rec')
        .setNamespace('Test')
        .setParameters(
            new Map([
                Parameter.ofEntry('label', Schema.ofString('label')),
                Parameter.ofEntry('idx', Schema.ofAny('idx').setDefaultValue('')),
            ]),
        );
    getSignature(): FunctionSignature {
        return this.signature;
    }
    protected async internalExecute(ctx: FunctionExecutionParameters): Promise<FunctionOutput> {
        const label = ctx.getArguments()?.get('label') + (ctx.getArguments()?.get('idx') ?? '');
        trace.push(label);
        return new FunctionOutput([EventResult.outputOf(new Map([['label', label]]))]);
    }
}

const testRepo: Repository<Function> = {
    find: async (ns, name) => (ns === 'Test' && name === 'Rec' ? new Rec() : undefined),
    filter: async () => [],
};
const fRepo = new HybridRepository<Function>(new KIRunFunctionRepository(), testRepo);
const sRepo = new KIRunSchemaRepository();

type P = Record<string, ['V', any] | ['E', string]>;
function st(name: string, ns: string, fn: string, params: P, after: string[] = []) {
    const parameterMap: any = {};
    for (const [p, [t, v]] of Object.entries(params))
        parameterMap[p] = {
            [p + '1']:
                t === 'V'
                    ? { key: p + '1', type: 'VALUE', value: v }
                    : { key: p + '1', type: 'EXPRESSION', expression: v },
        };
    return {
        statementName: name,
        namespace: ns,
        name: fn,
        parameterMap,
        dependentStatements: Object.fromEntries(after.map((a) => [`Steps.${a}`, true])),
    };
}
const rec = (name: string, after: string[], idx?: string) =>
    st(name, 'Test', 'Rec', idx ? { label: ['V', name], idx: ['E', idx] } : { label: ['V', name] }, after);
const iff = (name: string, cond: string, after: string[] = []) =>
    st(name, 'System', 'If', { condition: ['E', cond] }, after);

function fd(steps: any[], params: string[] = []) {
    return FunctionDefinition.from({
        name: 'BranchSubGraph',
        namespace: 'Test',
        parameters: Object.fromEntries(
            params.map((p) => [
                p,
                {
                    parameterName: p,
                    schema:
                        p === 'list'
                            ? { type: ['ARRAY'], items: { type: ['INTEGER'] }, version: 1 }
                            : { type: ['BOOLEAN'], version: 1 },
                },
            ]),
        ),
        steps: Object.fromEntries(steps.map((s) => [s.statementName, s])),
    });
}

// Verbatim 3.17.2 GraphVertex.getSubGraphOfType.
function oldGetSubGraphOfType(this: any, type: string): ExecutionGraph<any, any> {
    const cached = this.subGraphCache.get(type);
    if (cached) return cached;
    let subGraph: ExecutionGraph<any, any> = new ExecutionGraph(true);
    var typeVertices = new LinkedList<any>(Array.from(this.outVertices.get(type) ?? []));
    typeVertices.map((e: any) => e.getData()).forEach((e: any) => subGraph.addVertex(e));
    while (!typeVertices.isEmpty()) {
        var vertex = typeVertices.pop();
        Array.from(vertex.outVertices.values() as Iterable<Set<any>>)
            .flatMap((e) => Array.from(e))
            .forEach((e: any) => {
                subGraph.addVertex(e.getData());
                typeVertices.add(e);
            });
    }
    this.subGraphCache.set(type, subGraph);
    return subGraph;
}

const NEW_WALK = GraphVertex.prototype.getSubGraphOfType;

async function run(def: FunctionDefinition, args: Record<string, any>, old: boolean) {
    trace = [];
    if (old) (GraphVertex.prototype as any).getSubGraphOfType = oldGetSubGraphOfType;
    try {
        const out = await new KIRuntime(def).execute(
            new FunctionExecutionParameters(fRepo, sRepo).setArguments(new Map(Object.entries(args))),
        );
        const events: any[] = [];
        for (let e = out.next(); e; e = out.next())
            events.push([e.getName(), Object.fromEntries(e.getResult())]);
        return { trace: [...trace], events };
    } finally {
        GraphVertex.prototype.getSubGraphOfType = NEW_WALK;
    }
}

async function planSubGraphs(def: FunctionDefinition, old: boolean) {
    if (old) (GraphVertex.prototype as any).getSubGraphOfType = oldGetSubGraphOfType;
    try {
        const g = await new KIRuntime(def).getExecutionPlan(fRepo, sRepo);
        const out: Record<string, string[]> = {};
        for (const v of g.getNodeMap().values())
            for (const type of v.getOutVertices().keys())
                out[`${v.getKey()}.${type}`] = Array.from(v.getSubGraphOfType(type).getNodeMap().keys());
        return out;
    } finally {
        GraphVertex.prototype.getSubGraphOfType = NEW_WALK;
    }
}

async function sameAsOld(def: FunctionDefinition, args: Record<string, any>) {
    const a = await run(def, args, true);
    const b = await run(def, args, false);
    expect(b).toEqual(a);
    expect(await planSubGraphs(def, false)).toEqual(await planSubGraphs(def, true));
    return b;
}

const count = (t: string[]) => t.reduce((m, x) => ((m[x] = (m[x] ?? 0) + 1), m), {} as any);

describe('KIRuntime with the visited-set sub-graph walk runs exactly as before', () => {
    // if1 true: s1 -> p, q -> r (diamond) -> if2 true: t1, t2 -> u (diamond) / false: f1
    // if1 false: e1 -> e2, e3 -> e4 (diamond)
    const nested = fd(
        [
            iff('if1', 'Arguments.a'),
            rec('s1', ['if1.true']),
            rec('p', ['s1.output']),
            rec('q', ['s1.output']),
            rec('r', ['p.output', 'q.output']),
            iff('if2', 'Arguments.b', ['r.output']),
            rec('t1', ['if2.true']),
            rec('t2', ['if2.true']),
            rec('u', ['t1.output', 't2.output']),
            rec('f1', ['if2.false']),
            rec('e1', ['if1.false']),
            rec('e2', ['e1.output']),
            rec('e3', ['e1.output']),
            rec('e4', ['e2.output', 'e3.output']),
            rec('end', ['if1.output']),
        ],
        ['a', 'b'],
    );

    test.each([
        [true, true],
        [true, false],
        [false, true],
        [false, false],
    ])('nested If branches with diamonds, a=%s b=%s', async (a, b) => {
        const r = await sameAsOld(nested, { a, b });
        const expected = a
            ? ['s1', 'p', 'q', 'r', ...(b ? ['t1', 't2', 'u'] : ['f1'])]
            : ['e1', 'e2', 'e3', 'e4'];
        // every step of the taken branches exactly once, `end` after the If is done
        expect([...r.trace].sort()).toEqual([...expected, 'end'].sort());
        expect(r.trace[r.trace.length - 1]).toBe('end');
    });

    test('ForEachLoop with a diamond body and an If inside', async () => {
        const def = fd(
            [
                st('loop', 'System.Loop', 'ForEachLoop', { source: ['E', 'Arguments.list'] }),
                rec('b1', ['loop.iteration'], 'Steps.loop.iteration.index'),
                rec('b2', ['b1.output'], 'Steps.loop.iteration.index'),
                rec('b3', ['b1.output'], 'Steps.loop.iteration.index'),
                rec('b4', ['b2.output', 'b3.output'], 'Steps.loop.iteration.index'),
                iff('odd', 'Steps.loop.iteration.each % 2 = 1', ['b4.output']),
                rec('o1', ['odd.true'], 'Steps.loop.iteration.index'),
                rec('o2', ['odd.true'], 'Steps.loop.iteration.index'),
                rec('o3', ['o1.output', 'o2.output'], 'Steps.loop.iteration.index'),
                rec('done', ['loop.output']),
            ],
            ['list'],
        );
        const r = await sameAsOld(def, { list: [1, 2, 3] });
        const c = count(r.trace);
        for (const i of [0, 1, 2]) for (const s of ['b1', 'b2', 'b3', 'b4']) expect(c[s + i]).toBe(1);
        expect(c.o10).toBe(1);
        expect(c.o12).toBe(1);
        expect(c.o11).toBeUndefined();
        expect(r.trace.length).toBe(3 * 4 + 2 * 3 + 1);
        expect(r.trace[r.trace.length - 1]).toBe('done');
    });

    test('CountLoop inside an If branch, diamond body', async () => {
        const def = fd(
            [
                iff('go', 'Arguments.go'),
                st('loop', 'System.Loop', 'CountLoop', { count: ['V', 4] }, ['go.true']),
                rec('c1', ['loop.iteration'], 'Steps.loop.iteration.index'),
                rec('c2', ['c1.output'], 'Steps.loop.iteration.index'),
                rec('c3', ['c1.output'], 'Steps.loop.iteration.index'),
                rec('c4', ['c2.output', 'c3.output'], 'Steps.loop.iteration.index'),
                rec('after', ['loop.output']),
                rec('no', ['go.false']),
            ],
            ['go'],
        );
        const yes = await sameAsOld(def, { go: true });
        const c = count(yes.trace);
        for (const i of [0, 1, 2, 3]) for (const s of ['c1', 'c2', 'c3', 'c4']) expect(c[s + i]).toBe(1);
        expect(yes.trace.length).toBe(17);
        expect(yes.trace[16]).toBe('after');
        expect(yes.trace).not.toContain('no');
        const no = await sameAsOld(def, { go: false });
        expect(no.trace).toEqual(['no']);
    });

    test('If over a 14 level diamond ladder: same run as before', async () => {
        const steps: any[] = [iff('g', 'Arguments.go'), rec('L0', ['g.false'])];
        for (let i = 0; i < 14; i++)
            steps.push(
                rec(`a${i}`, [`L${i}.output`]),
                rec(`b${i}`, [`L${i}.output`]),
                rec(`L${i + 1}`, [`a${i}.output`, `b${i}.output`]),
            );
        steps.push(rec('picked', ['g.true']));
        const def = fd(steps, ['go']);
        const r = await sameAsOld(def, { go: false });
        expect(r.trace.length).toBe(1 + 14 * 3); // each step once
        expect(new Set(r.trace).size).toBe(r.trace.length);
        expect(r.trace[r.trace.length - 1]).toBe('L14');
        expect(r.trace).not.toContain('picked');
        expect((await sameAsOld(def, { go: true })).trace).toEqual(['picked']);
    });

    test('If over a 40 level diamond ladder (2^40 paths) runs at once', async () => {
        const steps: any[] = [iff('g', 'Arguments.go'), rec('L0', ['g.false'])];
        for (let i = 0; i < 40; i++)
            steps.push(
                rec(`a${i}`, [`L${i}.output`]),
                rec(`b${i}`, [`L${i}.output`]),
                rec(`L${i + 1}`, [`a${i}.output`, `b${i}.output`]),
            );
        const def = fd(steps, ['go']);
        const t = performance.now();
        const r = await run(def, { go: false }, false);
        expect(performance.now() - t).toBeLessThan(2000);
        expect(r.trace.length).toBe(1 + 40 * 3);
        expect(new Set(r.trace).size).toBe(r.trace.length);
        expect(r.trace[r.trace.length - 1]).toBe('L40');
    });
});

import { ExecutionGraph, GraphVertex, GraphVertexType, LinkedList } from '../../../../src';

// ADZ-1423: GraphVertex.getSubGraphOfType walked every path below a branch instead of every
// vertex, so a branch over a chain of diamonds cost 2^depth. These tests pin the result of the
// visited-set walk to the result of the old walk (copied below verbatim, as shipped in 3.17.2):
// same vertices, same insertion order, each vertex once.

class V implements GraphVertexType<string> {
    constructor(private readonly key: string) {}
    getUniqueKey(): string {
        return this.key;
    }
    getDepenedencies(): Set<string> {
        return new Set();
    }
}

type G = ExecutionGraph<string, V>;
type GV = GraphVertex<string, V>;

function graphOf(edges: [string, string, string][], vertices: string[] = []): G {
    const g: G = new ExecutionGraph();
    for (const v of vertices) g.addVertex(new V(v));
    for (const [from, type, to] of edges) {
        const f = g.addVertex(new V(from));
        const t = g.addVertex(new V(to));
        f.addOutEdgeTo(type, t);
    }
    return g;
}

const keys = (g: G) => Array.from(g.getNodeMap().keys());

// Verbatim 3.17.2 walk (no visited set). `budget` stops it on graphs where it would explode.
function oldSubGraphKeys(vertex: GV, type: string, budget = Number.MAX_SAFE_INTEGER) {
    const subGraph: G = new ExecutionGraph(true);
    var typeVertices = new LinkedList(Array.from(vertex.getOutVertices().get(type) ?? []));
    typeVertices.map((e) => e.getData()).forEach((e) => subGraph.addVertex(e));
    let pops = 0;
    while (!typeVertices.isEmpty()) {
        if (++pops > budget) return { keys: undefined, pops };
        var v = typeVertices.pop();
        Array.from(v.getOutVertices().values())
            .flatMap((e) => Array.from(e))
            .forEach((e) => {
                subGraph.addVertex(e.getData());
                typeVertices.add(e);
            });
    }
    return { keys: keys(subGraph), pops };
}

// Verbatim 3.17.2 ExecutionGraph.isCyclic.
function oldIsCyclic(g: G): boolean {
    let list: LinkedList<GV> = new LinkedList(g.getVerticesWithNoIncomingEdges());
    let visited: Set<string> = new Set();
    let vertex: GV;
    while (!list.isEmpty()) {
        if (visited.has(list.getFirst().getKey())) return true;
        vertex = list.removeFirst();
        visited.add(vertex.getKey());
        if (vertex.hasOutgoingEdges())
            list.addAll(Array.from(vertex.getOutVertices().values()).flatMap((e) => Array.from(e)));
    }
    return false;
}

// Counts how many times a walk reads a vertex's out edges, i.e. how many times it visits it.
class CountingMap<K2, V2> extends Map<K2, V2> {
    static reads = 0;
    values() {
        CountingMap.reads++;
        return super.values();
    }
}
function countVisits(g: G) {
    for (const v of g.getNodeMap().values()) v.setOutVertices(new CountingMap(v.getOutVertices()));
    CountingMap.reads = 0;
}

// If -true-> L0, Li -> ai, Li -> bi, ai -> L(i+1), bi -> L(i+1): 2^levels paths to the end.
function ladder(levels: number): G {
    const e: [string, string, string][] = [['if', 'true', 'L0']];
    for (let i = 0; i < levels; i++) {
        e.push([`L${i}`, 'output', `a${i}`], [`L${i}`, 'output', `b${i}`]);
        e.push([`a${i}`, 'output', `L${i + 1}`], [`b${i}`, 'output', `L${i + 1}`]);
    }
    e.push(['if', 'false', 'f0']);
    return graphOf(e);
}

// Every vertex of layer i feeds every vertex of layer i+1: width^depth paths.
function wideLadder(depth: number, width: number): G {
    const e: [string, string, string][] = [];
    for (let w = 0; w < width; w++) e.push(['if', 'true', `x0_${w}`]);
    for (let d = 0; d + 1 < depth; d++)
        for (let a = 0; a < width; a++)
            for (let b = 0; b < width; b++) e.push([`x${d}_${a}`, 'output', `x${d + 1}_${b}`]);
    return graphOf(e);
}

// Small deterministic PRNG so the fuzz cases are the same on every run.
function prng(seed: number) {
    let s = seed >>> 0;
    return () => {
        s = (s * 1664525 + 1013904223) >>> 0;
        return s / 4294967296;
    };
}

describe('GraphVertex.getSubGraphOfType', () => {
    test('diamond: the shared descendant appears once, order as before', () => {
        const g = graphOf([
            ['if', 'true', 'A'],
            ['A', 'output', 'B'],
            ['A', 'output', 'C'],
            ['B', 'output', 'D'],
            ['C', 'output', 'D'],
            ['D', 'output', 'E'],
        ]);
        const ifV = g.getVertex('if')!;
        const old = oldSubGraphKeys(ifV, 'true');
        countVisits(g);
        const sub = ifV.getSubGraphOfType('true');
        expect(keys(sub)).toEqual(['A', 'B', 'C', 'D', 'E']);
        expect(keys(sub)).toEqual(old.keys);
        expect(CountingMap.reads).toBe(5); // each vertex once
        expect(old.pops).toBe(7); // the old walk visited D and E twice
    });

    test('branch with no shared descendants: same vertices and order as before', () => {
        const g = graphOf([
            ['if', 'true', 'A'],
            ['if', 'true', 'B'],
            ['if', 'false', 'X'],
            ['A', 'output', 'A1'],
            ['A', 'output', 'A2'],
            ['B', 'output', 'B1'],
            ['A1', 'output', 'A11'],
            ['X', 'output', 'X1'],
        ]);
        const ifV = g.getVertex('if')!;
        for (const type of ['true', 'false', 'output', 'missing']) {
            expect(keys(ifV.getSubGraphOfType(type))).toEqual(oldSubGraphKeys(ifV, type).keys);
        }
        expect(keys(ifV.getSubGraphOfType('true'))).toEqual(['A', 'B', 'A1', 'A2', 'B1', 'A11']);
        expect(keys(ifV.getSubGraphOfType('false'))).toEqual(['X', 'X1']);
        expect(keys(ifV.getSubGraphOfType('missing'))).toEqual([]);
    });

    test('sub-graph is a fresh, edgeless, cached sub graph holding the same vertex data', () => {
        const g = graphOf([
            ['if', 'true', 'A'],
            ['A', 'output', 'B'],
        ]);
        const ifV = g.getVertex('if')!;
        const sub = ifV.getSubGraphOfType('true');
        expect(sub.isSubGraph()).toBe(true);
        expect(sub.areEdgesBuilt()).toBe(false);
        for (const v of sub.getNodeMap().values()) {
            expect(v.hasIncomingEdges()).toBe(false);
            expect(v.hasOutgoingEdges()).toBe(false);
            expect(v.getGraph()).toBe(sub);
            expect(v.getData()).toBe(g.getVertex(v.getKey())!.getData());
        }
        expect(ifV.getSubGraphOfType('true')).toBe(sub); // cached: same instance
    });

    test('a branch target reachable from another branch target is not added twice', () => {
        const g = graphOf([
            ['if', 'true', 'A'],
            ['if', 'true', 'B'],
            ['A', 'output', 'B'],
            ['B', 'output', 'C'],
            ['A', 'output', 'C'],
        ]);
        const ifV = g.getVertex('if')!;
        expect(keys(ifV.getSubGraphOfType('true'))).toEqual(oldSubGraphKeys(ifV, 'true').keys);
        expect(keys(ifV.getSubGraphOfType('true'))).toEqual(['A', 'B', 'C']);
    });

    test('nested branches: inner If, its branches and everything after them are collected once', () => {
        const g = graphOf([
            ['if1', 'true', 's1'],
            ['if1', 'false', 'e1'],
            ['s1', 'output', 'p'],
            ['s1', 'output', 'q'],
            ['p', 'output', 'r'],
            ['q', 'output', 'r'],
            ['r', 'output', 'if2'],
            ['if2', 'true', 't1'],
            ['if2', 'true', 't2'],
            ['if2', 'false', 'f1'],
            ['t1', 'output', 'u'],
            ['t2', 'output', 'u'],
            ['f1', 'output', 'u'],
            ['if2', 'output', 'w'],
            ['u', 'output', 'w'],
        ]);
        for (const [k, type] of [
            ['if1', 'true'],
            ['if1', 'false'],
            ['if2', 'true'],
            ['if2', 'false'],
        ]) {
            const v = g.getVertex(k)!;
            expect(keys(v.getSubGraphOfType(type))).toEqual(oldSubGraphKeys(v, type).keys);
        }
        expect(keys(g.getVertex('if1')!.getSubGraphOfType('true'))).toEqual([
            's1', 'p', 'q', 'r', 'if2', 't1', 't2', 'f1', 'w', 'u',
        ]);
        expect(keys(g.getVertex('if2')!.getSubGraphOfType('true'))).toEqual(['t1', 't2', 'u', 'w']);
    });

    test('loop iteration sub-graph with a diamond body', () => {
        const g = graphOf([
            ['loop', 'iteration', 'b1'],
            ['b1', 'output', 'b2'],
            ['b1', 'output', 'b3'],
            ['b2', 'output', 'b4'],
            ['b3', 'output', 'b4'],
            ['loop', 'output', 'done'],
        ]);
        const v = g.getVertex('loop')!;
        expect(keys(v.getSubGraphOfType('iteration'))).toEqual(oldSubGraphKeys(v, 'iteration').keys);
        expect(keys(v.getSubGraphOfType('iteration'))).toEqual(['b1', 'b2', 'b3', 'b4']);
        expect(keys(v.getSubGraphOfType('output'))).toEqual(['done']);
    });

    test('diamond ladder, 12 levels: same result as the old walk, linear instead of exponential', () => {
        const g = ladder(12);
        const ifV = g.getVertex('if')!;
        const old = oldSubGraphKeys(ifV, 'true');
        countVisits(g);
        const sub = ifV.getSubGraphOfType('true');
        expect(keys(sub)).toEqual(old.keys);
        expect(sub.getNodeMap().size).toBe(13 + 24);
        expect(CountingMap.reads).toBe(13 + 24);
        expect(old.pops).toBeGreaterThan(2 ** 13); // the old walk: one visit per path
    });

    test('diamond ladder, 40 levels (2^40 paths): fast, every vertex once', () => {
        const g = ladder(40);
        const ifV = g.getVertex('if')!;
        // the old walk does not finish: still going after a million visits
        expect(oldSubGraphKeys(ifV, 'true', 1_000_000).keys).toBeUndefined();
        countVisits(g);
        const t = performance.now();
        const sub = ifV.getSubGraphOfType('true');
        const ms = performance.now() - t;
        expect(ms).toBeLessThan(100);
        expect(sub.getNodeMap().size).toBe(41 + 80);
        expect(CountingMap.reads).toBe(41 + 80);
        expect(new Set(keys(sub)).size).toBe(keys(sub).length);
        expect(keys(sub)[0]).toBe('L0');
        expect(keys(sub)[keys(sub).length - 1]).toBe('L40');
    });

    test('wide ladder (6^25 paths): fast, every vertex once; small one equals the old walk', () => {
        const small = wideLadder(6, 3);
        const sv = small.getVertex('if')!;
        expect(keys(sv.getSubGraphOfType('true'))).toEqual(oldSubGraphKeys(sv, 'true').keys);

        const g = wideLadder(25, 6);
        const t = performance.now();
        const sub = g.getVertex('if')!.getSubGraphOfType('true');
        expect(performance.now() - t).toBeLessThan(100);
        expect(sub.getNodeMap().size).toBe(25 * 6);
    });

    test('random DAGs with several edge types: identical to the old walk for every vertex and type', () => {
        const types = ['output', 'true', 'false', 'iteration'];
        for (let seed = 1; seed <= 300; seed++) {
            const rnd = prng(seed);
            const n = 5 + Math.floor(rnd() * 26);
            const p = 0.05 + rnd() * 0.2;
            const order = Array.from({ length: n }, (_, i) => `v${i}`);
            // shuffle the insertion order so map order is not the topological order
            for (let i = n - 1; i > 0; i--) {
                const j = Math.floor(rnd() * (i + 1));
                [order[i], order[j]] = [order[j], order[i]];
            }
            const edges: [string, string, string][] = [];
            for (let i = 0; i < n; i++)
                for (let j = i + 1; j < n; j++)
                    if (rnd() < p) edges.push([`v${i}`, types[Math.floor(rnd() * types.length)], `v${j}`]);
            // shuffle the edges too, so out-vertex set order varies
            for (let i = edges.length - 1; i > 0; i--) {
                const j = Math.floor(rnd() * (i + 1));
                [edges[i], edges[j]] = [edges[j], edges[i]];
            }
            const g = graphOf(edges, order);
            for (const v of g.getNodeMap().values())
                for (const type of types) {
                    const old = oldSubGraphKeys(v, type, 2_000_000);
                    if (!old.keys) continue; // too many paths for the old walk
                    expect([seed, v.getKey(), type, keys(v.getSubGraphOfType(type))]).toEqual([
                        seed,
                        v.getKey(),
                        type,
                        old.keys,
                    ]);
                }
        }
    });

    test('a cycle below a branch: the walk ends (the old walk never did) and holds each vertex once', () => {
        const g = graphOf([
            ['if', 'true', 'A'],
            ['A', 'output', 'B'],
            ['B', 'output', 'C'],
            ['C', 'output', 'A'],
            ['C', 'output', 'D'],
        ]);
        expect(oldSubGraphKeys(g.getVertex('if')!, 'true', 10_000).keys).toBeUndefined();
        expect(keys(g.getVertex('if')!.getSubGraphOfType('true'))).toEqual(['A', 'B', 'C', 'D']);
    });
});

describe('ExecutionGraph.isCyclic (unchanged, pinned to the old answer)', () => {
    test('a cycle reachable from a root is detected', () => {
        const g = graphOf([
            ['root', 'output', 'A'],
            ['A', 'output', 'B'],
            ['B', 'output', 'A'],
        ]);
        expect(g.isCyclic()).toBe(true);
        expect(oldIsCyclic(g)).toBe(true);
    });

    test('a self loop on a reachable vertex is detected', () => {
        const g = graphOf([
            ['root', 'output', 'A'],
            ['A', 'output', 'A'],
        ]);
        expect(g.isCyclic()).toBe(oldIsCyclic(g));
        expect(g.isCyclic()).toBe(true);
    });

    test('a chain and a tree are not cyclic', () => {
        const chain = graphOf([
            ['a', 'output', 'b'],
            ['b', 'output', 'c'],
        ]);
        const tree = graphOf([
            ['a', 'output', 'b'],
            ['a', 'true', 'c'],
            ['c', 'output', 'd'],
        ]);
        expect(chain.isCyclic()).toBe(false);
        expect(tree.isCyclic()).toBe(false);
    });

    test('known quirks kept as they were: a diamond reads as cyclic, a cycle with no root does not', () => {
        const diamond = graphOf([
            ['A', 'output', 'B'],
            ['A', 'output', 'C'],
            ['B', 'output', 'D'],
            ['C', 'output', 'D'],
        ]);
        const rootless = graphOf([
            ['A', 'output', 'B'],
            ['B', 'output', 'A'],
        ]);
        expect(diamond.isCyclic()).toBe(true);
        expect(rootless.isCyclic()).toBe(false);
    });

    test('a 40 level diamond ladder is answered quickly (each vertex is taken off the list once)', () => {
        const g = ladder(40);
        const t = performance.now();
        expect(g.isCyclic()).toBe(oldIsCyclic(g));
        expect(performance.now() - t).toBeLessThan(100);
    });

    test('random graphs, with and without cycles: same answer as the old code', () => {
        for (let seed = 1; seed <= 300; seed++) {
            const rnd = prng(seed * 7919);
            const n = 3 + Math.floor(rnd() * 15);
            const edges: [string, string, string][] = [];
            for (let i = 0; i < n; i++)
                for (let j = 0; j < n; j++)
                    if (rnd() < (j > i ? 0.2 : 0.03)) edges.push([`v${i}`, 'output', `v${j}`]);
            const g = graphOf(
                edges,
                Array.from({ length: n }, (_, i) => `v${i}`),
            );
            expect([seed, g.isCyclic()]).toEqual([seed, oldIsCyclic(g)]);
        }
    });
});

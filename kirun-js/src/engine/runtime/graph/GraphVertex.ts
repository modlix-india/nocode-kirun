import { LinkedList } from '../../util/LinkedList';
import { Tuple2 } from '../../util/Tuples';
import { ExecutionGraph } from './ExecutionGraph';
import { GraphVertexType } from './GraphVertexType';

export class GraphVertex<K, T extends GraphVertexType<K>> {
    private data: T;
    private outVertices: Map<string, Set<GraphVertex<K, T>>> = new Map();
    private inVertices: Set<Tuple2<GraphVertex<K, T>, string>> = new Set();
    private graph: ExecutionGraph<K, T>;
    private subGraphCache: Map<string, ExecutionGraph<K, T>> = new Map();

    public constructor(graph: ExecutionGraph<K, T>, data: T) {
        this.data = data;
        this.graph = graph;
    }

    public getData(): T {
        return this.data;
    }
    public setData(data: T): GraphVertex<K, T> {
        this.data = data;
        return this;
    }
    public getOutVertices(): Map<string, Set<GraphVertex<K, T>>> {
        return this.outVertices;
    }
    public setOutVertices(outVertices: Map<string, Set<GraphVertex<K, T>>>): GraphVertex<K, T> {
        this.outVertices = outVertices;
        return this;
    }
    public getInVertices(): Set<Tuple2<GraphVertex<K, T>, string>> {
        return this.inVertices;
    }
    public setInVertices(inVertices: Set<Tuple2<GraphVertex<K, T>, string>>): GraphVertex<K, T> {
        this.inVertices = inVertices;
        return this;
    }
    public getGraph(): ExecutionGraph<K, T> {
        return this.graph;
    }
    public setGraph(graph: ExecutionGraph<K, T>): GraphVertex<K, T> {
        this.graph = graph;
        return this;
    }

    public getKey(): K {
        return this.data.getUniqueKey();
    }

    // public addOutEdgeTo(type: string, data:T) :GraphVertex<K, T> {
    // 	return this.addOutEdgeTo(type, this.graph.addVertex(data));
    // }

    // public GraphVertex<K, T> addInEdgeTo(T data, String type) {
    // 	return this.addInEdgeTo(this.graph.addVertex(data), type);
    // }

    public addOutEdgeTo(type: string, vertex: GraphVertex<K, T>): GraphVertex<K, T> {
        if (!this.outVertices.has(type)) this.outVertices.set(type, new Set());
        this.outVertices.get(type)!.add(vertex);
        vertex.inVertices.add(new Tuple2(this, type));
        return vertex;
    }

    public addInEdgeTo(vertex: GraphVertex<K, T>, type: string): GraphVertex<K, T> {
        this.inVertices.add(new Tuple2(vertex, type));
        if (!vertex.outVertices.has(type)) vertex.outVertices.set(type, new Set());
        vertex.outVertices.get(type)!.add(this);
        return vertex;
    }

    public hasIncomingEdges(): boolean {
        return !!this.inVertices.size;
    }

    public hasOutgoingEdges(): boolean {
        return !!this.outVertices.size;
    }

    public getSubGraphOfType(type: string): ExecutionGraph<K, T> {
        // Check cache first
        const cached = this.subGraphCache.get(type);
        if (cached) {
            return cached;
        }

        let subGraph: ExecutionGraph<K, T> = new ExecutionGraph(true);

        const start = Array.from(this.outVertices.get(type) ?? []);
        const typeVertices = new LinkedList(start);

        for (const e of start) subGraph.addVertex(e.getData());

        // Breadth first, each vertex queued once. Without the visited set a vertex was queued
        // once per PATH that reaches it, so a branch over a chain of diamonds cost 2^depth
        // (ADZ-1423: 87 million iterations for one If over 811 steps). The queue is FIFO, so
        // the vertices are added to the sub-graph in exactly the same order as before.
        const visited: Set<GraphVertex<K, T>> = new Set(start);

        while (!typeVertices.isEmpty()) {
            const vertex = typeVertices.pop();
            for (const outs of vertex.outVertices.values()) {
                for (const e of outs) {
                    if (visited.has(e)) continue;
                    visited.add(e);
                    subGraph.addVertex(e.getData());
                    typeVertices.add(e);
                }
            }
        }

        // Cache for reuse
        this.subGraphCache.set(type, subGraph);
        return subGraph;
    }

    public toString(): string {
        var ins = Array.from(this.getInVertices())
            .map((e) => e.getT1().getKey() + '(' + e.getT2() + ')')
            .join(', ');

        var outs = Array.from(this.outVertices.entries())
            .map(
                ([key, value]) =>
                    key +
                    ': ' +
                    Array.from(value)
                        .map((e) => e.getKey())
                        .join(','),
            )
            .join('\n\t\t');

        return this.getKey() + ':\n\tIn: ' + ins + '\n\tOut: \n\t\t' + outs;
    }
}

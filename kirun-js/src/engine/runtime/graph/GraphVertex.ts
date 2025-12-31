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

        var typeVertices = new LinkedList(Array.from(this.outVertices.get(type) ?? []));

        typeVertices.map((e) => e.getData()).forEach((e) => subGraph.addVertex(e));

        while (!typeVertices.isEmpty()) {
            var vertex = typeVertices.pop();
            Array.from(vertex.outVertices.values())
                .flatMap((e) => Array.from(e))
                .forEach((e) => {
                    subGraph.addVertex(e.getData());
                    typeVertices.add(e);
                });
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

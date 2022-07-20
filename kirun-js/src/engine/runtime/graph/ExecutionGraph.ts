import { LinkedList } from '../../util/LinkedList';
import { GraphVertex } from './GraphVertex';
import { GraphVertexType } from './GraphVertexType';

export class ExecutionGraph<K, T extends GraphVertexType<K>> {
    private nodeMap: Map<K, GraphVertex<K, T>> = new Map();
    private isSubGrph: boolean;

    public constructor(isSubGrph: boolean = false) {
        this.isSubGrph = isSubGrph;
    }

    public getVerticesData(): T[] {
        return Array.from(this.nodeMap.values()).map((e) => e.getData());
    }

    public addVertex(data: T): GraphVertex<K, T> {
        if (!this.nodeMap.has(data.getUniqueKey())) {
            let t = new GraphVertex(this, data);
            this.nodeMap.set(data.getUniqueKey(), t);
        }
        return this.nodeMap.get(data.getUniqueKey());
    }

    public getVertex(key: K): GraphVertex<K, T> {
        return this.nodeMap.get(key);
    }

    public getVertexData(key: K): T {
        if (this.nodeMap.has(key)) return this.nodeMap.get(key).getData();
        return undefined;
    }

    public getVerticesWithNoIncomingEdges(): GraphVertex<K, T>[] {
        return Array.from(this.nodeMap.values()).filter((e) => !e.hasIncomingEdges());
    }

    public isCyclic(): boolean {
        let list: LinkedList<GraphVertex<K, T>> = new LinkedList(
            this.getVerticesWithNoIncomingEdges(),
        );
        let visited: Set<K> = new Set();

        let vertex: GraphVertex<K, T>;
        while (!list.isEmpty()) {
            if (visited.has(list.getFirst().getKey())) return true;

            vertex = list.removeFirst();

            visited.add(vertex.getKey());
            if (vertex.hasOutgoingEdges())
                list.addAll(
                    Array.from(vertex.getOutVertices().values()).flatMap((e) => Array.from(e)),
                );
        }

        return false;
    }

    public addVertices(values: T[]): void {
        for (const value of values) this.addVertex(value);
    }

    public getNodeMap(): Map<K, GraphVertex<K, T>> {
        return this.nodeMap;
    }

    public isSubGraph(): boolean {
        return this.isSubGrph;
    }

    public toString(): string {
        return (
            'Execution Graph : \n' +
            Array.from(this.nodeMap.values())

                .map((e) => e.toString())
                .join('\n')
        );
    }
}

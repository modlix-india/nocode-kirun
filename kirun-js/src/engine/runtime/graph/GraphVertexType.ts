export interface GraphVertexType<K> {
    getUniqueKey(): K;
    getDepenedencies(): Set<string>;
}

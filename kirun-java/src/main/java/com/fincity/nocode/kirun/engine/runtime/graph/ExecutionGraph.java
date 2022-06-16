package com.fincity.nocode.kirun.engine.runtime.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

public class ExecutionGraph<K, T extends GraphVertexType<K>> {

	private Map<K, GraphVertex<K, T>> nodeMap = new ConcurrentHashMap<>();

	public List<T> getVerticesData() {

		return nodeMap.values()
		        .stream()
		        .map(GraphVertex::getData)
		        .toList();
	}

	public Flux<T> getVerticesDataFlux() {

		return Flux.fromIterable(this.nodeMap.values())
		        .map(GraphVertex::getData);
	}

	public GraphVertex<K, T> addVertex(T data) {

		return nodeMap.computeIfAbsent(data.getUniqueKey(), k -> new GraphVertex<>(this, data));
	}

	public GraphVertex<K, T> getVertex(K key) {
		return nodeMap.get(key);
	}

	public T getVertexData(K key) {
		if (nodeMap.containsKey(key))
			return nodeMap.get(key)
			        .getData();
		return null;
	}

	public List<GraphVertex<K, T>> getVerticesWithNoIncomingEdges() {

		return nodeMap.values()
		        .stream()
		        .filter(e -> !e.hasIncomingEdges())
		        .collect(Collectors.toCollection(LinkedList::new));
	}

	public boolean isCyclic() {

		LinkedList<GraphVertex<K, T>> list = (LinkedList<GraphVertex<K, T>>) this.getVerticesWithNoIncomingEdges();
		HashSet<K> visited = new HashSet<>();

		GraphVertex<K, T> vertex;
		while (!list.isEmpty()) {

			if (visited.contains(list.getFirst()
			        .getKey()))
				return true;

			vertex = list.removeFirst();

			visited.add(vertex.getKey());
			if (vertex.hasOutgoingEdges())
				list.addAll(vertex.getOutVertices()
				        .values()
				        .stream()
				        .flatMap(Set::stream)
				        .toList());
		}

		return false;
	}

	public void addVertices(Collection<T> values) {

		for (T value : values)
			this.addVertex(value);
	}

	public Map<K, GraphVertex<K, T>> getNodeMap() {
		
		return this.nodeMap;
	}
}

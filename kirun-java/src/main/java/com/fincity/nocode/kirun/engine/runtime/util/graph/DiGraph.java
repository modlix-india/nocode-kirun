package com.fincity.nocode.kirun.engine.runtime.util.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

public class DiGraph<K, T extends GraphVertexType<K>> {

	private Map<K, DiGraphVertex<K, T>> nodeMap = new ConcurrentHashMap<>();

	public List<T> getVerticesData() {

		return nodeMap.values()
		        .stream()
		        .map(DiGraphVertex::getData)
		        .toList();
	}

	public Flux<T> getVerticesDataFlux() {

		return Flux.fromIterable(this.nodeMap.values())
		        .map(DiGraphVertex::getData);
	}

	public DiGraphVertex<K, T> addVertex(T data) {

		return nodeMap.computeIfAbsent(data.getUniqueKey(), k -> new DiGraphVertex<>(this, data));
	}

	public DiGraphVertex<K, T> getVertex(K key) {
		return nodeMap.get(key);
	}

	public T getVertexData(K key) {
		if (nodeMap.containsKey(key))
			return nodeMap.get(key)
			        .getData();
		return null;
	}

	public List<DiGraphVertex<K, T>> getVerticesWithNoIncomingEdges() {

		return nodeMap.values()
		        .stream()
		        .filter(e -> !e.hasIncomingEdges())
		        .collect(Collectors.toCollection(LinkedList::new));
	}

	public boolean isCyclic() {

		LinkedList<DiGraphVertex<K, T>> list = (LinkedList<DiGraphVertex<K, T>>) this.getVerticesWithNoIncomingEdges();
		HashSet<K> visited = new HashSet<>();

		DiGraphVertex<K, T> vertex;
		while (!list.isEmpty()) {

			if (visited.contains(list.getFirst()
			        .getKey()))
				return true;

			vertex = list.removeFirst();

			visited.add(vertex.getKey());
			if (vertex.hasOutgoingEdges())
				list.addAll(vertex.getOutVertices());
		}

		return false;
	}

	public void addVertices(Collection<T> values) {

		for (T value : values)
			this.addVertex(value);
	}

	public DiGraph<K, T> makeEdges() {

		this.nodeMap.values()
		        .stream()
		        .filter(e -> e.getData()
		                .getDepenedencies() != null)
		        .forEach(e -> e.getData()
		                .getDepenedencies()
		                .stream()
		                .forEach(d -> e.addInEdgeTo(this.nodeMap.get(d))));

		return this;
	}
}

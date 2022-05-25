package com.fincity.nocode.kirun.engine.runtime.util.graph;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Mono;

public class DiGraph<K, T extends GraphVertexType<K>> {

	private Map<K, DiGraphVertex<K, T>> nodeMap = new ConcurrentHashMap<>();

	public List<T> getVertices() {

		return nodeMap.values()
		        .stream()
		        .map(DiGraphVertex::getData)
		        .toList();
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
		        .toList();
	}

	public Mono<Boolean> checkCycles() {

		return Mono.just(false);
	}
}

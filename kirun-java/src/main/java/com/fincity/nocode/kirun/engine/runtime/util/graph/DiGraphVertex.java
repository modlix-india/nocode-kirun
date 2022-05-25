package com.fincity.nocode.kirun.engine.runtime.util.graph;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DiGraphVertex<K, T extends GraphVertexType<K>> {

	private T data;
	private Set<DiGraphVertex<K, T>> outVertices = new HashSet<>();
	private Set<DiGraphVertex<K, T>> inVertices = new HashSet<>();
	private DiGraph<K, T> graph;

	public DiGraphVertex(DiGraph<K, T> graph, T data) {

		this.data = data;
		this.graph = graph;
	}

	public DiGraphVertex<K, T> addOutEdgeTo(T data) {
		return this.addOutEdgeTo(this.graph.addVertex(data));
	}

	public DiGraphVertex<K, T> addInEdgeTo(T data) {
		return this.addInEdgeTo(this.graph.addVertex(data));
	}

	public DiGraphVertex<K, T> addOutEdgeTo(DiGraphVertex<K, T> vertex) {
		this.outVertices.add(vertex);
		vertex.inVertices.add(this);
		return vertex;
	}

	public DiGraphVertex<K, T> addInEdgeTo(DiGraphVertex<K, T> vertex) {
		this.inVertices.add(vertex);
		vertex.outVertices.add(this);
		return vertex;
	}

	public boolean hasIncomingEdges() {
		return !this.inVertices.isEmpty();
	}

	public boolean hasOutgoingEdges() {
		return !this.outVertices.isEmpty();
	}
}
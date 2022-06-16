package com.fincity.nocode.kirun.engine.runtime.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Data
@Accessors(chain = true)
public class GraphVertex<K, T extends GraphVertexType<K>> {

	private T data;
	private Map<String, Set<GraphVertex<K, T>>> outVertices = new ConcurrentHashMap<>();
	private Set<Tuple2<GraphVertex<K, T>, String>> inVertices = new HashSet<>();
	private ExecutionGraph<K, T> graph;

	public GraphVertex(ExecutionGraph<K, T> graph, T data) {

		this.data = data;
		this.graph = graph;
	}

	public K getKey() {
		return this.data.getUniqueKey();
	}

	public GraphVertex<K, T> addOutEdgeTo(String type, T data) {
		return this.addOutEdgeTo(type, this.graph.addVertex(data));
	}

	public GraphVertex<K, T> addInEdgeTo(T data, String type) {
		return this.addInEdgeTo(this.graph.addVertex(data), type);
	}

	public GraphVertex<K, T> addOutEdgeTo(String type, GraphVertex<K, T> vertex) {
		this.outVertices.computeIfAbsent(type, ty -> new HashSet<>())
		        .add(vertex);
		vertex.inVertices.add(Tuples.of(this, type));
		return vertex;
	}

	public GraphVertex<K, T> addInEdgeTo(GraphVertex<K, T> vertex, String type) {
		this.inVertices.add(Tuples.of(vertex, type));
		vertex.outVertices.computeIfAbsent(type, ty -> new HashSet<>())
		        .add(this);
		return vertex;
	}

	public boolean hasIncomingEdges() {
		return !this.inVertices.isEmpty();
	}

	public boolean hasOutgoingEdges() {
		return !this.outVertices.isEmpty();
	}

	public ExecutionGraph<K, T> getSubGraphOfType(String type) {

		ExecutionGraph<K, T> subGraph = new ExecutionGraph<>();

		var typeVertices = new LinkedList<>(outVertices.get(type));

		typeVertices.stream()
		        .map(GraphVertex::getData)
		        .forEach(subGraph::addVertex);

		while (!typeVertices.isEmpty()) {

			var vertex = typeVertices.pop();
			vertex.outVertices.values()
			        .stream()
			        .flatMap(Set::stream)
			        .forEach(e ->
				        {
					        subGraph.addVertex(e.getData());
					        typeVertices.add(e);
				        });
		}

		return subGraph;
	}
}
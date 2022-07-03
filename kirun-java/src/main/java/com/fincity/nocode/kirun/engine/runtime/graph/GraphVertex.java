package com.fincity.nocode.kirun.engine.runtime.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = { "graph", "outVertices", "inVertices" })
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

		ExecutionGraph<K, T> subGraph = new ExecutionGraph<>(true);

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

	public String toString() {

		var ins = this.getInVertices()
		        .stream()
		        .map(e -> e.getT1()
		                .getKey()
		                .toString() + "(" + e.getT2() + ")")
		        .map(Object::toString)
		        .collect(Collectors.joining(", "));

		var outs = this.outVertices.entrySet()
		        .stream()
		        .map(e -> e.getKey() + ": " + e.getValue()
		                .stream()
		                .map(GraphVertex::getKey)
		                .map(Object::toString)
		                .collect(Collectors.joining(", ")))
		        .collect(Collectors.joining("\n\t\t"));

		return this.getKey()
		        .toString() + ":\n\tIn: " + ins + "\n\tOut: \n\t\t" + outs;
	}
}
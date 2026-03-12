from __future__ import annotations

from typing import Any, Dict, Generic, Optional, Set, TypeVar

from kirun_py.runtime.graph.graph_vertex_type import GraphVertexType
from kirun_py.util.tuples import Tuple2

K = TypeVar('K')
T = TypeVar('T')


class GraphVertex(Generic[K, T]):

    def __init__(self, graph: Any, data: T) -> None:
        self._data: T = data
        self._out_vertices: Dict[str, Set[GraphVertex[K, T]]] = {}
        self._in_vertices: Set[Tuple2[GraphVertex[K, T], str]] = set()
        self._graph: Any = graph
        self._sub_graph_cache: Dict[str, Any] = {}

    def get_data(self) -> T:
        return self._data

    def set_data(self, data: T) -> GraphVertex[K, T]:
        self._data = data
        return self

    def get_out_vertices(self) -> Dict[str, Set[GraphVertex[K, T]]]:
        return self._out_vertices

    def set_out_vertices(
        self, out_vertices: Dict[str, Set[GraphVertex[K, T]]]
    ) -> GraphVertex[K, T]:
        self._out_vertices = out_vertices
        return self

    def get_in_vertices(self) -> Set[Tuple2[GraphVertex[K, T], str]]:
        return self._in_vertices

    def set_in_vertices(
        self, in_vertices: Set[Tuple2[GraphVertex[K, T], str]]
    ) -> GraphVertex[K, T]:
        self._in_vertices = in_vertices
        return self

    def get_graph(self) -> Any:
        return self._graph

    def set_graph(self, graph: Any) -> GraphVertex[K, T]:
        self._graph = graph
        return self

    def get_key(self) -> Any:
        return self._data.get_unique_key()

    def add_out_edge_to(self, type_: str, vertex: GraphVertex[K, T]) -> GraphVertex[K, T]:
        if type_ not in self._out_vertices:
            self._out_vertices[type_] = set()
        self._out_vertices[type_].add(vertex)
        vertex._in_vertices.add(Tuple2(self, type_))
        return vertex

    def add_in_edge_to(self, vertex: GraphVertex[K, T], type_: str) -> GraphVertex[K, T]:
        self._in_vertices.add(Tuple2(vertex, type_))
        if type_ not in vertex._out_vertices:
            vertex._out_vertices[type_] = set()
        vertex._out_vertices[type_].add(self)
        return vertex

    def has_incoming_edges(self) -> bool:
        return len(self._in_vertices) > 0

    def has_outgoing_edges(self) -> bool:
        return len(self._out_vertices) > 0

    def get_sub_graph_of_type(self, type_: str) -> Any:
        from kirun_py.runtime.graph.execution_graph import ExecutionGraph

        cached = self._sub_graph_cache.get(type_)
        if cached is not None:
            return cached

        sub_graph: ExecutionGraph = ExecutionGraph(is_sub_graph=True)

        type_vertices_set = self._out_vertices.get(type_)
        if type_vertices_set is None:
            type_vertices_set = set()

        type_vertices = list(type_vertices_set)

        for v in type_vertices:
            sub_graph.add_vertex(v.get_data())

        i = 0
        while i < len(type_vertices):
            vertex = type_vertices[i]
            for out_set in vertex._out_vertices.values():
                for e in out_set:
                    sub_graph.add_vertex(e.get_data())
                    type_vertices.append(e)
            i += 1

        self._sub_graph_cache[type_] = sub_graph
        return sub_graph

    def __str__(self) -> str:
        ins = ', '.join(
            str(e.get_t1().get_key()) + '(' + str(e.get_t2()) + ')'
            for e in self._in_vertices
        )

        out_parts = []
        for key, value in self._out_vertices.items():
            vertex_keys = ','.join(str(e.get_key()) for e in value)
            out_parts.append(f'{key}: {vertex_keys}')
        outs = '\n\t\t'.join(out_parts)

        return f'{self.get_key()}:\n\tIn: {ins}\n\tOut: \n\t\t{outs}'

    def __hash__(self) -> int:
        return hash(id(self))

    def __eq__(self, other: Any) -> bool:
        return self is other

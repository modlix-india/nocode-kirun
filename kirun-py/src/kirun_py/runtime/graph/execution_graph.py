from __future__ import annotations

from collections import deque
from typing import Any, Dict, Generic, List, Optional, TypeVar

from kirun_py.runtime.graph.graph_vertex import GraphVertex
from kirun_py.runtime.graph.graph_vertex_type import GraphVertexType

K = TypeVar('K')
T = TypeVar('T')


class ExecutionGraph(Generic[K, T]):

    def __init__(self, is_sub_graph: bool = False) -> None:
        self._node_map: Dict[Any, GraphVertex[K, T]] = {}
        self._is_sub_graph: bool = is_sub_graph
        self._edges_built: bool = False

    def are_edges_built(self) -> bool:
        return self._edges_built

    def set_edges_built(self, built: bool) -> None:
        self._edges_built = built

    def get_vertices_data(self) -> List[T]:
        return [v.get_data() for v in self._node_map.values()]

    def add_vertex(self, data: T) -> GraphVertex[K, T]:
        key = data.get_unique_key()
        if key not in self._node_map:
            vertex: GraphVertex[K, T] = GraphVertex(self, data)
            self._node_map[key] = vertex
        return self._node_map[key]

    def get_vertex(self, key: Any) -> Optional[GraphVertex[K, T]]:
        return self._node_map.get(key)

    def get_vertex_data(self, key: Any) -> Optional[T]:
        vertex = self._node_map.get(key)
        if vertex is not None:
            return vertex.get_data()
        return None

    def get_vertices_with_no_incoming_edges(self) -> List[GraphVertex[K, T]]:
        return [v for v in self._node_map.values() if not v.has_incoming_edges()]

    def is_cyclic(self) -> bool:
        queue: deque[GraphVertex[K, T]] = deque(self.get_vertices_with_no_incoming_edges())
        visited: set = set()

        while queue:
            first = queue[0]
            if first.get_key() in visited:
                return True

            vertex = queue.popleft()
            visited.add(vertex.get_key())

            if vertex.has_outgoing_edges():
                for out_set in vertex.get_out_vertices().values():
                    for v in out_set:
                        queue.append(v)

        return False

    def add_vertices(self, values: List[T]) -> None:
        for value in values:
            self.add_vertex(value)

    def get_node_map(self) -> Dict[Any, GraphVertex[K, T]]:
        return self._node_map

    def is_sub_graph(self) -> bool:
        return self._is_sub_graph

    def __str__(self) -> str:
        vertex_strs = '\n'.join(str(v) for v in self._node_map.values())
        return f'Execution Graph : \n{vertex_strs}'

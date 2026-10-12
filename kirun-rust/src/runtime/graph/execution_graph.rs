//! Port of kirun-js `runtime/graph/ExecutionGraph.ts`.

use std::fmt;

use crate::runtime::graph::graph_vertex::GraphVertex;
use crate::runtime::graph::graph_vertex_type::GraphVertexType;
use crate::Map;

#[derive(Debug, Clone)]
pub struct ExecutionGraph<T> {
    node_map: Map<GraphVertex<T>>,
    is_sub_grph: bool,
    edges_built: bool,
}

impl<T: GraphVertexType + Clone> ExecutionGraph<T> {
    pub fn new(is_sub_graph: bool) -> Self {
        Self { node_map: Map::new(), is_sub_grph: is_sub_graph, edges_built: false }
    }

    pub fn are_edges_built(&self) -> bool {
        self.edges_built
    }

    pub fn set_edges_built(&mut self, built: bool) {
        self.edges_built = built;
    }

    pub fn get_vertices_data(&self) -> Vec<&T> {
        self.node_map.values().map(|v| v.get_data()).collect()
    }

    /// Adds the vertex unless one with the same key exists; returns the stored vertex.
    pub fn add_vertex(&mut self, data: T) -> &mut GraphVertex<T> {
        let key = data.get_unique_key().to_string();
        self.node_map.entry(key).or_insert_with(|| GraphVertex::new(data))
    }

    pub fn add_vertices(&mut self, values: Vec<T>) {
        for v in values {
            self.add_vertex(v);
        }
    }

    pub fn get_vertex(&self, key: &str) -> Option<&GraphVertex<T>> {
        self.node_map.get(key)
    }

    pub fn get_vertex_mut(&mut self, key: &str) -> Option<&mut GraphVertex<T>> {
        self.node_map.get_mut(key)
    }

    pub fn get_vertex_data(&self, key: &str) -> Option<&T> {
        self.node_map.get(key).map(|v| v.get_data())
    }

    pub fn get_vertices_with_no_incoming_edges(&self) -> Vec<&GraphVertex<T>> {
        self.node_map.values().filter(|v| !v.has_incoming_edges()).collect()
    }

    /// JS `from.addOutEdgeTo(type, to)` (also records the in-edge on `to`).
    pub fn add_edge(&mut self, from_key: &str, edge_type: &str, to_key: &str) {
        if !self.node_map.contains_key(from_key) || !self.node_map.contains_key(to_key) {
            return;
        }
        if let Some(from) = self.node_map.get_mut(from_key) {
            from.out_vertices.entry(edge_type.to_string()).or_default().insert(to_key.to_string());
        }
        if let Some(to) = self.node_map.get_mut(to_key) {
            to.in_vertices.insert((from_key.to_string(), edge_type.to_string()));
        }
    }

    /// JS `vertex.getSubGraphOfType(type)`: everything reachable through `edge_type` edges
    /// of the vertex `key`. Like JS, the sub-graph's vertices start without edges.
    pub fn get_sub_graph_of_type(&self, key: &str, edge_type: &str) -> ExecutionGraph<T> {
        let mut sub = ExecutionGraph::new(true);
        let Some(vertex) = self.node_map.get(key) else {
            return sub;
        };
        let mut queue: std::collections::VecDeque<String> =
            vertex.out_vertices.get(edge_type).map(|s| s.iter().cloned().collect()).unwrap_or_default();
        for k in &queue {
            if let Some(v) = self.node_map.get(k) {
                sub.add_vertex(v.data.clone());
            }
        }
        // JS pops from the head and appends to the tail.
        let mut seen: indexmap::IndexSet<String> = queue.iter().cloned().collect();
        while let Some(k) = queue.pop_front() {
            let Some(v) = self.node_map.get(&k) else { continue };
            for outs in v.out_vertices.values() {
                for o in outs {
                    if let Some(ov) = self.node_map.get(o) {
                        sub.add_vertex(ov.data.clone());
                    }
                    // JS re-queues already-visited vertices; on a DAG that only repeats work.
                    if seen.insert(o.clone()) {
                        queue.push_back(o.clone());
                    }
                }
            }
        }
        sub
    }

    pub fn is_cyclic(&self) -> bool {
        let mut list: std::collections::VecDeque<String> =
            self.get_vertices_with_no_incoming_edges().iter().map(|v| v.get_key().to_string()).collect();
        let mut visited = indexmap::IndexSet::new();
        while let Some(first) = list.front() {
            if visited.contains(first) {
                return true;
            }
            let key = list.pop_front().unwrap();
            visited.insert(key.clone());
            if let Some(v) = self.node_map.get(&key) {
                for outs in v.out_vertices.values() {
                    list.extend(outs.iter().cloned());
                }
            }
        }
        false
    }

    pub fn get_node_map(&self) -> &Map<GraphVertex<T>> {
        &self.node_map
    }

    pub fn is_sub_graph(&self) -> bool {
        self.is_sub_grph
    }

    /// The same graph (vertices, edges, flags) with each vertex's data mapped by `f`.
    pub fn map_data<U: GraphVertexType + Clone>(&self, f: impl Fn(&T) -> U) -> ExecutionGraph<U> {
        let node_map = self
            .node_map
            .iter()
            .map(|(k, v)| {
                (
                    k.clone(),
                    GraphVertex {
                        data: f(&v.data),
                        out_vertices: v.out_vertices.clone(),
                        in_vertices: v.in_vertices.clone(),
                    },
                )
            })
            .collect();
        ExecutionGraph { node_map, is_sub_grph: self.is_sub_grph, edges_built: self.edges_built }
    }
}

impl<T: GraphVertexType + Clone> fmt::Display for ExecutionGraph<T> {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let vertices: Vec<String> = self
            .node_map
            .values()
            .map(|v| {
                let ins = v.in_vertices.iter().map(|(k, t)| format!("{k}({t})")).collect::<Vec<_>>().join(", ");
                let outs = v
                    .out_vertices
                    .iter()
                    .map(|(t, ks)| format!("{t}: {}", ks.iter().cloned().collect::<Vec<_>>().join(",")))
                    .collect::<Vec<_>>()
                    .join("\n\t\t");
                format!("{}:\n\tIn: {ins}\n\tOut: \n\t\t{outs}", v.get_key())
            })
            .collect();
        write!(f, "Execution Graph : \n{}", vertices.join("\n"))
    }
}

//! Port of kirun-js `runtime/graph/GraphVertex.ts`. JS vertices point at each other; here
//! the graph owns every vertex in its node map and edges are stored as vertex keys.

use indexmap::IndexSet;

use crate::runtime::graph::graph_vertex_type::GraphVertexType;
use crate::Map;

#[derive(Debug, Clone)]
pub struct GraphVertex<T> {
    pub data: T,
    /// Edge type (`output`, `true`, `iteration`, ...) → keys of the vertices it leads to.
    pub out_vertices: Map<IndexSet<String>>,
    /// JS `Set<Tuple2<GraphVertex, string>>`: (parent vertex key, edge type).
    pub in_vertices: IndexSet<(String, String)>,
}

impl<T: GraphVertexType> GraphVertex<T> {
    pub fn new(data: T) -> Self {
        Self { data, out_vertices: Map::new(), in_vertices: IndexSet::new() }
    }

    pub fn get_data(&self) -> &T {
        &self.data
    }

    pub fn get_data_mut(&mut self) -> &mut T {
        &mut self.data
    }

    pub fn get_key(&self) -> &str {
        self.data.get_unique_key()
    }

    pub fn get_out_vertices(&self) -> &Map<IndexSet<String>> {
        &self.out_vertices
    }

    pub fn get_in_vertices(&self) -> &IndexSet<(String, String)> {
        &self.in_vertices
    }

    pub fn has_incoming_edges(&self) -> bool {
        !self.in_vertices.is_empty()
    }

    pub fn has_outgoing_edges(&self) -> bool {
        !self.out_vertices.is_empty()
    }
}

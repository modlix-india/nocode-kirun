//! Parity with kirun-js `GraphVertexSubGraphTest.ts` (ADZ-1423): the sub-graph below a branch visits each vertex
//! once. kirun-js, kirun-java and kirun-py walked every PATH (2^depth on a chain of diamonds) until 2026-11; this
//! runtime always kept a `seen` set, and these tests keep it that way.

use std::time::Instant;

use indexmap::IndexSet;
use kirun::runtime::graph::{ExecutionGraph, GraphVertexType};

#[derive(Clone, Debug)]
struct V {
    key: String,
    deps: IndexSet<String>,
}

impl GraphVertexType for V {
    fn get_unique_key(&self) -> &str {
        &self.key
    }
    fn get_dependencies(&self) -> &IndexSet<String> {
        &self.deps
    }
}

fn graph_of(edges: &[(String, &str, String)]) -> ExecutionGraph<V> {
    let mut g = ExecutionGraph::new(false);
    for (from, ty, to) in edges {
        g.add_vertex(V { key: from.clone(), deps: IndexSet::new() });
        g.add_vertex(V { key: to.clone(), deps: IndexSet::new() });
        g.add_edge(from, ty, to);
    }
    g
}

fn keys(g: &ExecutionGraph<V>) -> Vec<String> {
    g.get_node_map().keys().cloned().collect()
}

/// if -true-> L0, Li -> ai, Li -> bi, ai -> L(i+1), bi -> L(i+1): 2^levels paths to the end.
fn ladder(levels: usize) -> ExecutionGraph<V> {
    let mut e = vec![("if".to_string(), "true", "L0".to_string())];
    for i in 0..levels {
        e.push((format!("L{i}"), "output", format!("a{i}")));
        e.push((format!("L{i}"), "output", format!("b{i}")));
        e.push((format!("a{i}"), "output", format!("L{}", i + 1)));
        e.push((format!("b{i}"), "output", format!("L{}", i + 1)));
    }
    e.push(("if".to_string(), "false", "f0".to_string()));
    graph_of(&e)
}

#[test]
fn diamond_holds_the_shared_descendant_once() {
    let s = |x: &str| x.to_string();
    let g = graph_of(&[
        (s("if"), "true", s("A")),
        (s("A"), "output", s("B")),
        (s("A"), "output", s("C")),
        (s("B"), "output", s("D")),
        (s("C"), "output", s("D")),
        (s("D"), "output", s("E")),
    ]);
    assert_eq!(keys(&g.get_sub_graph_of_type("if", "true")), vec!["A", "B", "C", "D", "E"]);
    assert!(g.get_sub_graph_of_type("if", "missing").get_node_map().is_empty());
}

#[test]
fn ladder_of_40_diamonds_is_fast_and_holds_each_vertex_once() {
    let g = ladder(40);
    let t = Instant::now();
    let sub = g.get_sub_graph_of_type("if", "true");
    assert!(t.elapsed().as_millis() < 100);
    let k = keys(&sub);
    assert_eq!(k.len(), 41 + 80);
    assert_eq!(k.first().map(String::as_str), Some("L0"));
    assert_eq!(k.last().map(String::as_str), Some("L40"));
    assert!(sub.is_sub_graph());
}

#[test]
fn a_cycle_below_a_branch_ends() {
    let s = |x: &str| x.to_string();
    let g = graph_of(&[
        (s("if"), "true", s("A")),
        (s("A"), "output", s("B")),
        (s("B"), "output", s("C")),
        (s("C"), "output", s("A")),
        (s("C"), "output", s("D")),
    ]);
    assert_eq!(keys(&g.get_sub_graph_of_type("if", "true")), vec!["A", "B", "C", "D"]);
}

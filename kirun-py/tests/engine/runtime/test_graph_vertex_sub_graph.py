"""GraphVertex.get_sub_graph_of_type visits each vertex once (ADZ-1423).

Port of kirun-js __tests__/engine/runtime/graph/GraphVertexSubGraphTest.ts. The old walk (copied below) queued a
vertex once per PATH reaching it, so a branch over a chain of diamonds cost 2^depth. The new walk must give the
same vertices in the same order, each once.
"""
from __future__ import annotations

import random
import time

from kirun_py.runtime.graph.execution_graph import ExecutionGraph


class V:
    def __init__(self, key: str) -> None:
        self.key = key

    def get_unique_key(self) -> str:
        return self.key

    def get_depenedencies(self) -> set:
        return set()


def graph_of(edges, vertices=()):
    g = ExecutionGraph()
    for v in vertices:
        g.add_vertex(V(v))
    for frm, type_, to in edges:
        f = g.add_vertex(V(frm))
        t = g.add_vertex(V(to))
        f.add_out_edge_to(type_, t)
    return g


def keys(g) -> list:
    return list(g.get_node_map().keys())


def old_sub_graph_keys(vertex, type_, budget=10**18):
    """The walk as shipped before the fix (no visited set); None when it runs past `budget` visits."""
    sub = ExecutionGraph(is_sub_graph=True)
    queue = list(vertex.get_out_vertices().get(type_) or set())
    for v in queue:
        sub.add_vertex(v.get_data())
    i = 0
    while i < len(queue):
        if i >= budget:
            return None, i
        for out_set in queue[i].get_out_vertices().values():
            for e in out_set:
                sub.add_vertex(e.get_data())
                queue.append(e)
        i += 1
    return keys(sub), i


def ladder(levels: int):
    """if -true-> L0, Li -> ai, Li -> bi, ai -> L(i+1), bi -> L(i+1): 2^levels paths to the end."""
    e = [('if', 'true', 'L0')]
    for i in range(levels):
        e += [(f'L{i}', 'output', f'a{i}'), (f'L{i}', 'output', f'b{i}'),
              (f'a{i}', 'output', f'L{i + 1}'), (f'b{i}', 'output', f'L{i + 1}')]
    e.append(('if', 'false', 'f0'))
    return graph_of(e)


def test_diamond_shared_descendant_once_order_as_before():
    g = graph_of([('if', 'true', 'A'), ('A', 'output', 'B'), ('A', 'output', 'C'),
                  ('B', 'output', 'D'), ('C', 'output', 'D'), ('D', 'output', 'E')])
    v = g.get_vertex('if')
    old, pops = old_sub_graph_keys(v, 'true')
    assert keys(v.get_sub_graph_of_type('true')) == old
    assert sorted(old) == ['A', 'B', 'C', 'D', 'E']
    assert pops == 7  # the old walk visited D and E twice


def test_sub_graph_is_fresh_edgeless_and_cached():
    g = graph_of([('if', 'true', 'A'), ('A', 'output', 'B')])
    v = g.get_vertex('if')
    sub = v.get_sub_graph_of_type('true')
    assert sub.is_sub_graph()
    for sv in sub.get_node_map().values():
        assert not sv.has_incoming_edges() and not sv.has_outgoing_edges()
        assert sv.get_data() is g.get_vertex(sv.get_key()).get_data()
    assert v.get_sub_graph_of_type('true') is sub
    assert keys(v.get_sub_graph_of_type('missing')) == []


def test_ladder_40_levels_is_fast_and_holds_each_vertex_once():
    g = ladder(40)
    v = g.get_vertex('if')
    assert old_sub_graph_keys(v, 'true', 1_000_000)[0] is None  # the old walk does not finish
    t = time.perf_counter()
    sub = v.get_sub_graph_of_type('true')
    assert time.perf_counter() - t < 0.5
    assert len(sub.get_node_map()) == 41 + 80
    assert keys(sub)[0] == 'L0' and keys(sub)[-1] == 'L40'


def test_ladder_12_levels_same_result_as_the_old_walk():
    g = ladder(12)
    v = g.get_vertex('if')
    old, pops = old_sub_graph_keys(v, 'true')
    assert keys(v.get_sub_graph_of_type('true')) == old
    assert pops > 2 ** 13


def test_a_cycle_below_a_branch_ends():
    g = graph_of([('if', 'true', 'A'), ('A', 'output', 'B'), ('B', 'output', 'C'),
                  ('C', 'output', 'A'), ('C', 'output', 'D')])
    v = g.get_vertex('if')
    assert old_sub_graph_keys(v, 'true', 10_000)[0] is None
    assert sorted(keys(v.get_sub_graph_of_type('true'))) == ['A', 'B', 'C', 'D']


def test_random_dags_identical_to_the_old_walk():
    types = ['output', 'true', 'false', 'iteration']
    for seed in range(1, 301):
        rnd = random.Random(seed)
        n = 5 + rnd.randrange(26)
        p = 0.05 + rnd.random() * 0.2
        order = [f'v{i}' for i in range(n)]
        rnd.shuffle(order)
        edges = [(f'v{i}', rnd.choice(types), f'v{j}') for i in range(n) for j in range(i + 1, n) if rnd.random() < p]
        rnd.shuffle(edges)
        g = graph_of(edges, order)
        for v in g.get_node_map().values():
            for type_ in types:
                old, _ = old_sub_graph_keys(v, type_, 2_000_000)
                if old is None:
                    continue
                assert (seed, v.get_key(), type_, keys(v.get_sub_graph_of_type(type_))) == \
                       (seed, v.get_key(), type_, old)

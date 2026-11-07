package com.fincity.nocode.kirun.engine.runtime.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * ADZ-1423: getSubGraphOfType walked every PATH below a branch instead of every vertex, so a branch over a chain of
 * diamonds cost 2^depth. These tests pin the visited-set walk to the old walk (copied below as it was): the same
 * vertices, each once.
 */
class GraphVertexSubGraphTest {

	record V(String key) implements GraphVertexType<String> {

		@Override
		public String getUniqueKey() {
			return key;
		}

		@Override
		public Set<String> getDepenedencies() {
			return Set.of();
		}
	}

	private static ExecutionGraph<String, V> graphOf(String[][] edges, String... vertices) {
		ExecutionGraph<String, V> g = new ExecutionGraph<>();
		for (String v : vertices)
			g.addVertex(new V(v));
		for (String[] e : edges)
			g.addVertex(new V(e[0]))
			        .addOutEdgeTo(e[1], g.addVertex(new V(e[2])));
		return g;
	}

	private static Set<String> keys(ExecutionGraph<String, V> g) {
		return new HashSet<>(g.getNodeMap()
		        .keySet());
	}

	/** The old walk (no visited set). Returns null when it goes past {@code budget} visits. */
	private static Set<String> oldSubGraphKeys(GraphVertex<String, V> vertex, String type, long budget,
	        long[] visits) {
		ExecutionGraph<String, V> subGraph = new ExecutionGraph<>(true);
		Set<GraphVertex<String, V>> set = vertex.getOutVertices()
		        .get(type);
		if (set == null)
			return keys(subGraph);
		var typeVertices = new LinkedList<>(set);
		typeVertices.stream()
		        .map(GraphVertex::getData)
		        .forEach(subGraph::addVertex);
		long pops = 0;
		while (!typeVertices.isEmpty()) {
			if (++pops > budget)
				return null;
			var v = typeVertices.pop();
			v.getOutVertices()
			        .values()
			        .stream()
			        .flatMap(Set::stream)
			        .forEach(e -> {
				        subGraph.addVertex(e.getData());
				        typeVertices.add(e);
			        });
		}
		if (visits != null)
			visits[0] = pops;
		return keys(subGraph);
	}

	private static String[][] ladder(int levels) {
		List<String[]> e = new ArrayList<>();
		e.add(new String[] { "if", "true", "L0" });
		e.add(new String[] { "if", "false", "f0" });
		for (int i = 0; i < levels; i++) {
			e.add(new String[] { "L" + i, "output", "a" + i });
			e.add(new String[] { "L" + i, "output", "b" + i });
			e.add(new String[] { "a" + i, "output", "L" + (i + 1) });
			e.add(new String[] { "b" + i, "output", "L" + (i + 1) });
		}
		return e.toArray(new String[0][]);
	}

	@Test
	void diamondSharedDescendantOnce() {
		var g = graphOf(new String[][] { { "if", "true", "A" }, { "A", "output", "B" }, { "A", "output", "C" },
		        { "B", "output", "D" }, { "C", "output", "D" }, { "D", "output", "E" } });
		var ifV = g.getVertex("if");
		long[] visits = new long[1];
		var old = oldSubGraphKeys(ifV, "true", Long.MAX_VALUE, visits);
		var sub = ifV.getSubGraphOfType("true");
		assertEquals(Set.of("A", "B", "C", "D", "E"), keys(sub));
		assertEquals(old, keys(sub));
		assertEquals(5, sub.getNodeMap()
		        .size());
		assertEquals(7, visits[0]); // the old walk visited D and E twice
		assertTrue(sub.isSubGraph());
		assertSame(sub, ifV.getSubGraphOfType("true"));
	}

	@Test
	void branchWithoutSharedDescendants() {
		var g = graphOf(new String[][] { { "if", "true", "A" }, { "if", "true", "B" }, { "if", "false", "X" },
		        { "A", "output", "A1" }, { "B", "output", "B1" }, { "X", "output", "X1" } });
		var ifV = g.getVertex("if");
		for (String type : List.of("true", "false", "missing"))
			assertEquals(oldSubGraphKeys(ifV, type, Long.MAX_VALUE, null), keys(ifV.getSubGraphOfType(type)));
		assertEquals(Set.of("A", "B", "A1", "B1"), keys(ifV.getSubGraphOfType("true")));
		assertEquals(Set.of(), keys(ifV.getSubGraphOfType("missing")));
	}

	@Test
	void nestedBranchesAndLoopBody() {
		var g = graphOf(new String[][] { { "if1", "true", "s1" }, { "s1", "output", "p" }, { "s1", "output", "q" },
		        { "p", "output", "r" }, { "q", "output", "r" }, { "r", "output", "if2" }, { "if2", "true", "t1" },
		        { "if2", "true", "t2" }, { "t1", "output", "u" }, { "t2", "output", "u" }, { "if2", "false", "f1" },
		        { "loop", "iteration", "b1" }, { "b1", "output", "b2" }, { "b1", "output", "b3" },
		        { "b2", "output", "b4" }, { "b3", "output", "b4" } });
		for (String[] b : new String[][] { { "if1", "true" }, { "if2", "true" }, { "if2", "false" },
		        { "loop", "iteration" } }) {
			var v = g.getVertex(b[0]);
			assertEquals(oldSubGraphKeys(v, b[1], Long.MAX_VALUE, null), keys(v.getSubGraphOfType(b[1])));
		}
		assertEquals(Set.of("s1", "p", "q", "r", "if2", "t1", "t2", "u", "f1"), keys(g.getVertex("if1")
		        .getSubGraphOfType("true")));
		assertEquals(Set.of("b1", "b2", "b3", "b4"), keys(g.getVertex("loop")
		        .getSubGraphOfType("iteration")));
	}

	@Test
	void deepDiamondLadder() {
		var small = graphOf(ladder(12));
		assertEquals(oldSubGraphKeys(small.getVertex("if"), "true", Long.MAX_VALUE, null), keys(small.getVertex("if")
		        .getSubGraphOfType("true")));

		var g = graphOf(ladder(40));
		assertNull(oldSubGraphKeys(g.getVertex("if"), "true", 1_000_000, null)); // 2^40 paths: never finishes
		long t = System.nanoTime();
		var sub = g.getVertex("if")
		        .getSubGraphOfType("true");
		long ms = (System.nanoTime() - t) / 1_000_000;
		assertTrue(ms < 500, "took " + ms + " ms");
		assertEquals(41 + 80, sub.getNodeMap()
		        .size());
	}

	@Test
	void randomDagsSameAsOldWalk() {
		String[] types = { "output", "true", "false", "iteration" };
		for (int seed = 1; seed <= 300; seed++) {
			Random rnd = new Random(seed);
			int n = 5 + rnd.nextInt(26);
			double p = 0.05 + rnd.nextDouble() * 0.2;
			List<String[]> edges = new ArrayList<>();
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
					if (rnd.nextDouble() < p)
						edges.add(new String[] { "v" + i, types[rnd.nextInt(types.length)], "v" + j });
			String[] vs = new String[n];
			for (int i = 0; i < n; i++)
				vs[i] = "v" + i;
			var g = graphOf(edges.toArray(new String[0][]), vs);
			for (var v : g.getNodeMap()
			        .values())
				for (String type : types) {
					var old = oldSubGraphKeys(v, type, 2_000_000, null);
					if (old == null)
						continue;
					assertEquals(old, keys(v.getSubGraphOfType(type)), "seed " + seed + " " + v.getKey() + "." + type);
				}
		}
	}

	@Test
	void cycleBelowBranchEnds() {
		var g = graphOf(new String[][] { { "if", "true", "A" }, { "A", "output", "B" }, { "B", "output", "A" },
		        { "B", "output", "C" } });
		assertNull(oldSubGraphKeys(g.getVertex("if"), "true", 10_000, null));
		assertEquals(Set.of("A", "B", "C"), keys(g.getVertex("if")
		        .getSubGraphOfType("true")));
	}

	@Test
	void isCyclicUnchanged() {
		assertTrue(graphOf(new String[][] { { "root", "output", "A" }, { "A", "output", "B" }, { "B", "output", "A" } })
		        .isCyclic());
		assertFalse(graphOf(new String[][] { { "a", "output", "b" }, { "b", "output", "c" } }).isCyclic());
		// known quirks, kept as they were: a diamond reads as cyclic, a cycle with no root does not
		assertTrue(graphOf(new String[][] { { "A", "output", "B" }, { "A", "output", "C" }, { "B", "output", "D" },
		        { "C", "output", "D" } }).isCyclic());
		assertFalse(graphOf(new String[][] { { "A", "output", "B" }, { "B", "output", "A" } }).isCyclic());
		long t = System.nanoTime();
		graphOf(ladder(40)).isCyclic();
		assertTrue((System.nanoTime() - t) / 1_000_000 < 500);
	}
}

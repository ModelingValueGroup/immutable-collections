package org.modelingvalue.collections.test;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.Graph;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {
    @Test
    public void graphInit() {
        Graph<String, Integer> graph = Graph.of();
        assertNotNull(graph);
    }

    @Test
    public void addAndContainsNode() {
        Graph<String, Integer> graph = Graph.of();
        assertTrue(graph.addNode("a").containsNode("a"));
    }

    @Test
    public void addExistingNode() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a");
        assertEquals(graph, graph.addNode("a"));
    }

    @Test
    public void addNullNode() {
        Graph<String, Integer> graph = Graph.of();
        assertThrows(NullPointerException.class, () -> graph.addNode(null));
    }

    @Test
    public void addAndRemoveNode() {
        Graph<String, Integer> graph = Graph.of();
        assertFalse(graph.addNode("a").removeNode("a").containsNode("a"));
    }

    @Test
    public void removeNode() {
        Graph<String, Integer> graph = Graph.of();
        assertFalse(graph.removeNode("a").containsNode("a"));
    }

    @Test
    public void removeNullNode() {
        Graph<String, Integer> graph = Graph.of();
        graph.removeNode(null);
    }

    @Test
    public void containsNode() {
        Graph<String, Integer> graph = Graph.of();
        assertFalse(graph.containsNode("a"));
    }

    @Test
    public void containsNull() {
        Graph<String, Integer> graph = Graph.of();
        assertFalse(graph.containsNode(null));
    }

    @Test
    public void getAllNodesInEmptyGraph() {
        Graph<String, Integer> graph = Graph.of();
        assertEquals(0, graph.getAllNodes().size());
    }

    @Test
    public void getAllNodes() {
        Graph<String, Integer> graph = Graph.of();
        assertArrayEquals(
                new String[]{"a", "b"},
                graph.addNode("a").addNode("b").getAllNodes().sorted().toArray()
        );
    }

    @Test
    public void putEdgeWithNullNodes() {
        Graph<String, Integer> graph = Graph.of();
        assertThrows(NullPointerException.class, () -> graph.putEdge(null, null, null));
    }

    @Test
    public void putNullEdge() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a").addNode("b").putEdge("a", "b", null);
        assertFalse(graph.containsEdge("a", "b"));
        assertNull(graph.getEdge("a", "b"));
    }

    @Test
    public void putEdge() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a").addNode("b").putEdge("a", "b", 5);
        assertTrue(graph.containsEdge("a", "b"));
        assertEquals(5, graph.getEdge("a", "b"));
    }

    @Test
    public void putExistingEdge() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a").addNode("b").putEdge("a", "b", 5);
        assertEquals(graph, graph.putEdge("a", "b", 5));
    }

    @Test
    public void getEdgeWithNullNodes() {
        Graph<String, Integer> graph = Graph.of();
        assertNull(graph.getEdge("a", null));

        graph = graph.addNode("a");
        assertNull(graph.getEdge(null, null));
        assertNull(graph.getEdge("a", null));
    }

    @Test
    public void putAndRemoveEdge() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.putEdge("a", "b", 1).removeEdge("a", "b");
        assertNull(graph.getEdge("a", "b"));
    }

    @Test
    public void containsNullEdge() {
        Graph<String, Integer> graph = Graph.of();
        assertFalse(graph.containsEdge(null, null));
    }

    @Test
    public void getIncomingEdges() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a");

        assertEquals(0, graph.getIncomingEdges("a").size());

        graph = graph.putEdge("b", "a", 1);

        assertEquals(1, graph.getIncomingEdges("a").size());
        assertEquals(1, graph.getIncomingEdges("a").get("b"));

        graph = graph.putEdge("c", "a", 2);

        assertEquals(2, graph.getIncomingEdges("a").size());
        assertEquals(1, graph.getIncomingEdges("a").get("b"));
        assertEquals(2, graph.getIncomingEdges("a").get("c"));
    }

    @Test
    public void getIncomingEdgesToNullNode() {
        Graph<String, Integer> graph = Graph.of();
        assertNull(graph.getIncomingEdges(null));
    }

    @Test
    public void getOutgoingEdges() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.addNode("a");

        assertEquals(0, graph.getOutgoingEdges("a").size());

        graph = graph.putEdge("a", "b", 1);

        assertEquals(1, graph.getOutgoingEdges("a").size());
        assertEquals(1, graph.getOutgoingEdges("a").get("b"));

        graph = graph.putEdge("a", "c", 2);

        assertEquals(2, graph.getOutgoingEdges("a").size());
        assertEquals(1, graph.getOutgoingEdges("a").get("b"));
        assertEquals(2, graph.getOutgoingEdges("a").get("c"));
    }

    @Test
    public void getOutgoingEdgesToNullNode() {
        Graph<String, Integer> graph = Graph.of();
        assertNull(graph.getOutgoingEdges(null));
    }

    @Test
    public void cycle1() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.putEdge("a", "a", 1);
        assertTrue(graph.hasCycles());
    }

    @Test
    public void cycle2() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.putEdge("a", "b", 1).putEdge("b", "a", 2);
        assertTrue(graph.hasCycles());
    }

    @Test
    public void cycle3() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph
                .putEdge("a", "b", 1)
                .putEdge("b", "c", 2)
                .putEdge("c", "a", 3);

        assertTrue(graph.hasCycles());
    }

    @Test
    public void cycle4() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.putEdge("a", "b", 1);
        assertFalse(graph.hasCycles());
    }

    @Test
    public void cycle5() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph.putEdge("a", "b", 1).putEdge("c", "b", 2);
        assertFalse(graph.hasCycles());
    }

    @Test
    public void cycle6() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph
                .putEdge("a", "b", 1)
                .putEdge("a", "c", 2)
                .putEdge("b", "d", 3)
                .putEdge("c", "d", 4);

        assertFalse(graph.hasCycles());
    }

    @Test
    public void size() {
        Graph<String, Integer> graph = Graph.of();

        assertEquals(0, graph.size());

        graph = graph.addNode("node1");

        assertEquals(1, graph.size());

        graph = graph.putEdge("node2", "node3", 1);

        assertEquals(3, graph.size());

        graph = graph.putEdge("node3", "node4", 1);

        assertEquals(4, graph.size());
    }

    @Test
    public void fullGraphTest() {
        Graph<String, Integer> graph = Graph.of();
        graph = graph
                .putEdge("a", "c", 1)
                .putEdge("a", "d", 2)
                .putEdge("b", "d", 3)
                .putEdge("d", "e", 4)
                .putEdge("c", "f", 5)
                .putEdge("d", "f", 6);

        assertArrayEquals(
                new String[]{},
                graph.getIncomingEdges("a").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{"c", "d"},
                graph.getOutgoingEdges("a").toKeys().sorted().toArray()
        );

        assertArrayEquals(
                new String[]{},
                graph.getIncomingEdges("b").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{"d"},
                graph.getOutgoingEdges("b").toKeys().sorted().toArray()
        );

        assertArrayEquals(
                new String[]{"a"},
                graph.getIncomingEdges("c").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{"f"},
                graph.getOutgoingEdges("c").toKeys().sorted().toArray()
        );

        assertArrayEquals(
                new String[]{"a", "b"},
                graph.getIncomingEdges("d").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{"e", "f"},
                graph.getOutgoingEdges("d").toKeys().sorted().toArray()
        );

        assertArrayEquals(
                new String[]{"d"},
                graph.getIncomingEdges("e").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{},
                graph.getOutgoingEdges("e").toKeys().sorted().toArray()
        );

        assertArrayEquals(
                new String[]{"c", "d"},
                graph.getIncomingEdges("f").toKeys().sorted().toArray()
        );
        assertArrayEquals(
                new String[]{},
                graph.getOutgoingEdges("f").toKeys().sorted().toArray()
        );

        assertFalse(graph.hasCycles());
    }

    @Test
    public void hash() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of();

        assertEquals(graph1.hashCode(), graph2.hashCode());

        graph1 = graph1.putEdge("node1", "node2", 1);

        assertNotEquals(graph1.hashCode(), graph2.hashCode());
        graph2 = graph2.putEdge("node1", "node2", 1);

        assertEquals(graph1.hashCode(), graph2.hashCode());
    }

    @Test
    public void edgeEquals() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of();

        assertEquals(graph1, graph2);

        graph1 = graph1.putEdge("node1", "node2", 1);

        assertNotEquals(graph1, graph2);

        graph2 = graph2.putEdge("node1", "node2", 1);

        assertEquals(graph1, graph2);

        graph2 = graph2.putEdge("node1", "node2", 2);

        assertNotEquals(graph1, graph2);
    }


    @Test
    public void nodeEquals() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of();

        assertEquals(graph1, graph2);

        graph1 = graph1.addNode("node1");

        assertNotEquals(graph1, graph2);

        graph2 = graph2.addNode("node1");

        assertEquals(graph1, graph2);

        graph2 = graph2.addNode("node2");

        assertNotEquals(graph1, graph2);
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2026 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
//                                                                                                                       ~
//  Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in       ~
//  compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0   ~
//  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on  ~
//  an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the   ~
//  specific language governing permissions and limitations under the License.                                           ~
//                                                                                                                       ~
//  Maintainers:                                                                                                         ~
//      Wim Bast, Tom Brus                                                                                               ~
//                                                                                                                       ~
//  Contributors:                                                                                                        ~
//      Ronald Krijgsheld ✝, Arjan Kok, Carel Bast                                                                       ~
// --------------------------------------------------------------------------------------------------------------------- ~
//  In Memory of Ronald Krijgsheld, 1972 - 2023                                                                          ~
//      Ronald was suddenly and unexpectedly taken from us. He was not only our long-term colleague and team member      ~
//      but also our friend. "He will live on in many of the lines of code you see below."                               ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.Graph;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.mutable.MutableSet;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;
import org.modelingvalue.collections.util.Triple;

public class GraphTest {
    @Test
    public void constructor() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 1, "b"), Triple.of("a", 2, "d"));
        assertTrue(graph.containsNode("a"));
        assertTrue(graph.containsNode("b"));
        assertFalse(graph.containsNode("c"));
        assertTrue(graph.containsNode("d"));
        assertTrue(graph.containsEdge("a", "b", 1));
        assertFalse(graph.containsEdge("a", "d", 1));
        assertFalse(graph.containsEdge("a", "b", 2));
        assertTrue(graph.containsEdge("a", "d", 2));
        assertFalse(graph.containsEdge("a", "c", 1));
        assertFalse(graph.containsEdge("a", "c", 2));
    }

    @Test
    public void emptyGraph() {
        Graph<String, Integer> graph = Graph.of();
        assertNotNull(graph);
        assertEquals(0, graph.size());
    }

    @Test
    public void removeNode1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "a", 0);
        assertTrue(actual.containsNode("a"));
        actual = actual.removeNode("a");

        assertEquals(expected, actual);
        assertFalse(expected.containsNode("a"));
        assertFalse(actual.containsNode("a"));
    }

    @Test
    public void removeNode2() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.removeNode("a");

        assertEquals(expected, actual);
        assertFalse(expected.containsNode("a"));
        assertFalse(actual.containsNode("a"));
    }

    @Test
    public void removeNode3() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.removeNode(null);

        assertEquals(expected, actual);
    }

    @Test
    public void containsNode1() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        assertFalse(actual.containsNode("a"));

        actual = actual.putEdge("b", "b", 0);

        assertFalse(actual.containsNode("a"));

        actual = actual.putEdge("a", "a", 0);

        assertEquals(expected, actual);
        assertTrue(expected.containsNode("a"));
        assertTrue(actual.containsNode("a"));
    }

    @Test
    public void containsNode2() {
        Graph<String, Integer> graph = Graph.of();

        assertFalse(graph.containsNode(null));
    }

    @Test
    public void getNodes1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getNodes(), actual.getNodes());
        assertEquals(Set.of(), actual.getNodes());
    }

    @Test
    public void getNodes2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "b", 0);

        assertEquals(Set.of("b"), actual.getNodes());

        actual = actual.removeNode("b");

        assertEquals(Set.of(), actual.getNodes());

        actual = actual.putEdge("a", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getNodes(), actual.getNodes());
        assertEquals(Set.of("a"), actual.getNodes());
    }

    @Test
    public void getNodes3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"), Triple.of("c", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("c", "c", 0).putEdge("b", "b", 0).putEdge("a", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getNodes(), actual.getNodes());
        assertEquals(Set.of("a", "b", "c"), actual.getNodes());
    }

    @Test
    public void getNodes4() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));
        Set<String> expected = Set.of("a", "b");

        assertEquals(expected, graph.getNodes());
    }

    @Test
    public void putEdge1() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertTrue(expected.containsEdge("a", "b", 0));
        assertTrue(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void putEdge2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);
        actual = actual.putEdge("b", "b", 0);

        assertEquals(expected, actual);
        assertTrue(expected.containsEdge("a", "b", 0));
        assertTrue(expected.containsEdge("b", "b", 0));
    }

    @Test
    public void putEdge3() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge(null, "a", 0).putEdge("a", null, 0).putEdge("a", "a", null);

        assertEquals(expected, actual);
    }

    @Test
    public void getEdges1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertNull(actual.getEdges("a", "b"));
    }

    @Test
    public void getEdges2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(Set.of(0), actual.getEdges("a", "b"));
    }

    @Test
    public void getEdges3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertNull(actual.getEdges("a", "b"));
    }

    @Test
    public void getEdges4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"), Triple.of("e", 0, "e"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1).putEdge("e", "e", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(expected.getEdges("a", "c"), actual.getEdges("a", "c"));
        assertEquals(expected.getEdges("a", "d"), actual.getEdges("a", "d"));
        assertEquals(expected.getEdges("a", "e"), actual.getEdges("a", "e"));
        assertEquals(Set.of(0), actual.getEdges("a", "b"));
        assertEquals(Set.of(1), actual.getEdges("a", "c"));
        assertNull(actual.getEdges("a", "d"));
        assertEquals(Set.of(), actual.getEdges("a", "e"));
    }

    @Test
    public void getEdges5() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(Set.of(0), actual.getEdges("a", "b"));
    }

    @Test
    public void getEdges6() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "b", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(Set.of(0, 1), actual.getEdges("a", "b"));
    }

    @Test
    public void getEdges7() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));

        assertNull(graph.getEdges(null, "b"));
        assertNull(graph.getEdges("a", null));
    }

    @Test
    public void containsEdge1() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "c"), Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        assertFalse(actual.containsEdge("a", "b", 0));

        actual = actual.putEdge("a", "c", 0).putEdge("a", "b", 1);

        assertFalse(actual.containsEdge("a", "b", 0));

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertTrue(expected.containsEdge("a", "b", 0));
        assertTrue(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void containsEdge2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));

        assertFalse(graph.containsEdge(null, "b", 0));
        assertFalse(graph.containsEdge("a", null, 0));
        assertFalse(graph.containsEdge("a", "b", null));
    }

    @Test
    public void removeEdge1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);
        assertTrue(actual.containsEdge("a", "b", 0));
        actual = actual.removeEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void removeEdge2() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.removeEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void removeEdge3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 1);
        actual = actual.removeEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
        assertTrue(expected.containsEdge("a", "b", 1));
        assertTrue(actual.containsEdge("a", "b", 1));
    }

    @Test
    public void removeEdge4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.removeEdge(null, "b", 0).removeEdge("a", null, 0).removeEdge("a", "b", null);

        assertEquals(expected, actual);
    }

    @Test
    public void removeEdges1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.removeEdges("a", "b");

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertFalse(actual.containsNode("a"));
    }

    @Test
    public void removeEdges2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "c", 0);
        actual = actual.removeEdges("a", "b");

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(expected.getEdges("a", "c"), actual.getEdges("a", "c"));
        assertTrue(actual.containsNode("a"));
        assertFalse(actual.containsNode("b"));
    }

    @Test
    public void removeEdges3() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);
        actual = actual.removeEdges("a", "b");

        assertEquals(expected, actual);
        assertFalse(actual.containsEdge("a", "b", 0));
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
    }

    @Test
    public void removeEdges4() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "b", 1);
        actual = actual.removeEdges("a", "b");

        assertEquals(expected, actual);
        assertFalse(actual.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 1));
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
    }

    @Test
    public void removeEdges5() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "b", 1).putEdge("a", "c", 0);
        actual = actual.removeEdges("a", "b");

        assertEquals(expected, actual);
        assertFalse(actual.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 1));
        assertTrue(actual.containsEdge("a", "c", 0));
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(expected.getEdges("a", "c"), actual.getEdges("a", "c"));
    }

    @Test
    public void removeEdges6() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "c"), Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "c", 0).putEdge("a", "b", 1).putEdge("a", "a", 0);
        actual = actual.removeEdges("a", "a");

        assertEquals(expected, actual);
        assertFalse(actual.containsEdge("a", "a", 0));
        assertTrue(actual.containsEdge("a", "c", 0));
        assertTrue(actual.containsEdge("a", "b", 1));
        assertEquals(expected.getEdges("a", "a"), actual.getEdges("a", "a"));
        assertEquals(expected.getEdges("a", "b"), actual.getEdges("a", "b"));
        assertEquals(expected.getEdges("a", "c"), actual.getEdges("a", "c"));
    }

    @Test
    public void removeEdges7() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.removeEdges(null, "b").removeEdges("a", null);

        assertEquals(expected, actual);
    }

    @Test
    public void removeEdges8() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of(Triple.of("b", 0, "b"));

        actual = actual.removeEdges("b", "b");

        assertEquals(expected, actual);
        assertEquals(expected.getEdges("b", "b"), actual.getEdges("b", "b"));
        assertFalse(actual.containsNode("b"));
    }

    @Test
    public void getIncoming1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a"), actual.getIncoming("a"));
        assertNull(actual.getIncoming("a"));
    }

    @Test
    public void getIncoming2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a"), actual.getIncoming("a"));
        assertEquals(Set.of("b"), actual.getIncoming("a").get((Integer) 0));
    }

    @Test
    public void getIncoming3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a"), actual.getIncoming("a"));
        assertEquals(Set.of("b"), actual.getIncoming("a").get((Integer) 0));
        assertEquals(Set.of("c"), actual.getIncoming("a").get((Integer) 1));
    }

    @Test
    public void getIncoming4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a"), actual.getIncoming("a"));
        assertEquals(Set.of("b"), actual.getIncoming("a").get((Integer) 0));
        assertEquals(Set.of("c"), actual.getIncoming("a").get((Integer) 1));
    }

    @Test
    public void getIncoming5() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a", 0), actual.getIncoming("a", 0));
        assertNull(actual.getIncoming("a", 0));
    }

    @Test
    public void getIncoming6() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a", 0), actual.getIncoming("a", 0));
        assertEquals(Set.of("b"), actual.getIncoming("a", 0));
    }

    @Test
    public void getIncoming7() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a", 0), actual.getIncoming("a", 0));
        assertEquals(expected.getIncoming("a", 1), actual.getIncoming("a", 1));
        assertEquals(Set.of("b"), actual.getIncoming("a", 0));
        assertEquals(Set.of("c"), actual.getIncoming("a", 1));
    }

    @Test
    public void getIncoming8() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncoming("a", 0), actual.getIncoming("a", 0));
        assertEquals(expected.getIncoming("a", 1), actual.getIncoming("a", 1));
        assertEquals(Set.of("b"), actual.getIncoming("a", 0));
        assertEquals(Set.of("c"), actual.getIncoming("a", 1));
    }

    @Test
    public void getIncoming9() {
        Graph<String, Integer> graph = Graph.of(Triple.of("b", 0, "a"));

        assertNull(graph.getIncoming(null));
        assertNull(graph.getIncoming(null, 0));
        assertNull(graph.getIncoming("a", null));
    }

    @Test
    public void getOutgoing1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a"), actual.getOutgoing("a"));
        assertNull(actual.getOutgoing("a"));
    }

    @Test
    public void getOutgoing2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a"), actual.getOutgoing("a"));
        assertEquals(Set.of("b"), actual.getOutgoing("a").get((Integer) 0));
    }

    @Test
    public void getOutgoing3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a"), actual.getOutgoing("a"));
        assertEquals(Set.of("b"), actual.getOutgoing("a").get((Integer) 0));
        assertEquals(Set.of("c"), actual.getOutgoing("a").get((Integer) 1));
    }

    @Test
    public void getOutgoing4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a"), actual.getOutgoing("a"));
        assertEquals(Set.of("b"), actual.getOutgoing("a").get((Integer) 0));
        assertEquals(Set.of("c"), actual.getOutgoing("a").get((Integer) 1));
    }

    @Test
    public void getOutgoing5() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a", 0), actual.getOutgoing("a", 0));
        assertNull(actual.getOutgoing("a", 0));
    }

    @Test
    public void getOutgoing6() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a", 0), actual.getOutgoing("a", 0));
        assertEquals(Set.of("b"), actual.getOutgoing("a", 0));
    }

    @Test
    public void getOutgoing7() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a", 0), actual.getOutgoing("a", 0));
        assertEquals(expected.getOutgoing("a", 1), actual.getOutgoing("a", 1));
        assertEquals(Set.of("b"), actual.getOutgoing("a", 0));
        assertEquals(Set.of("c"), actual.getOutgoing("a", 1));
    }

    @Test
    public void getOutgoing8() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoing("a", 0), actual.getOutgoing("a", 0));
        assertEquals(expected.getOutgoing("a", 1), actual.getOutgoing("a", 1));
        assertEquals(Set.of("b"), actual.getOutgoing("a", 0));
        assertEquals(Set.of("c"), actual.getOutgoing("a", 1));
    }

    @Test
    public void getOutgoing9() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));

        assertNull(graph.getOutgoing(null));
        assertNull(graph.getOutgoing(null, 0));
        assertNull(graph.getOutgoing("a", null));
    }

    @Test
    public void getIncomingEdges1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingEdges("a"), actual.getIncomingEdges("a"));
        assertNull(actual.getIncomingEdges("a"));
    }

    @Test
    public void getIncomingEdges2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingEdges("a"), actual.getIncomingEdges("a"));
        assertEquals(Set.of(0), actual.getIncomingEdges("a"));
    }

    @Test
    public void getIncomingEdges3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingEdges("a"), actual.getIncomingEdges("a"));
        assertEquals(Set.of(0, 1), actual.getIncomingEdges("a"));
    }

    @Test
    public void getIncomingEdges4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingEdges("a"), actual.getIncomingEdges("a"));
        assertEquals(Set.of(0, 1), actual.getIncomingEdges("a"));
    }

    @Test
    public void getIncomingEdges5() {
        Graph<String, Integer> graph = Graph.of(Triple.of("b", 0, "a"));

        assertNull(graph.getIncomingEdges(null));
    }

    @Test
    public void getOutgoingEdges1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingEdges("a"), actual.getOutgoingEdges("a"));
        assertNull(actual.getOutgoingEdges("a"));
    }

    @Test
    public void getOutgoingEdges2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingEdges("a"), actual.getOutgoingEdges("a"));
        assertEquals(Set.of(0), actual.getOutgoingEdges("a"));
    }

    @Test
    public void getOutgoingEdges3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingEdges("a"), actual.getOutgoingEdges("a"));
        assertEquals(Set.of(0, 1), actual.getOutgoingEdges("a"));
    }

    @Test
    public void getOutgoingEdges4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingEdges("a"), actual.getOutgoingEdges("a"));
        assertEquals(Set.of(0, 1), actual.getOutgoingEdges("a"));
    }

    @Test
    public void getOutgoingEdges5() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));

        assertNull(graph.getOutgoingEdges(null));
    }

    @Test
    public void getIncomingNodes1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingNodes("a"), actual.getIncomingNodes("a"));
        assertNull(actual.getIncomingNodes("a"));
    }

    @Test
    public void getIncomingNodes2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingNodes("a"), actual.getIncomingNodes("a"));
        assertEquals(Set.of("b"), actual.getIncomingNodes("a"));
    }

    @Test
    public void getIncomingNodes3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingNodes("a"), actual.getIncomingNodes("a"));
        assertEquals(Set.of("b", "c"), actual.getIncomingNodes("a"));
    }

    @Test
    public void getIncomingNodes4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"), Triple.of("c", 1, "a"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("b", "a", 0).putEdge("c", "a", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getIncomingNodes("a"), actual.getIncomingNodes("a"));
        assertEquals(Set.of("b", "c"), actual.getIncomingNodes("a"));
    }

    @Test
    public void getIncomingNodes5() {
        Graph<String, Integer> graph = Graph.of(Triple.of("b", 0, "a"));

        assertNull(graph.getIncomingNodes(null));
    }

    @Test
    public void getOutgoingNodes1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingNodes("a"), actual.getOutgoingNodes("a"));
        assertNull(actual.getOutgoingNodes("a"));
    }

    @Test
    public void getOutgoingNodes2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingNodes("a"), actual.getOutgoingNodes("a"));
        assertEquals(Set.of("b"), actual.getOutgoingNodes("a"));
    }

    @Test
    public void getOutgoingNodes3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingNodes("a"), actual.getOutgoingNodes("a"));
        assertEquals(Set.of("b", "c"), actual.getOutgoingNodes("a"));
    }

    @Test
    public void getOutgoingNodes4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 1, "c"), Triple.of("b", 0, "c"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0).putEdge("a", "c", 1).putEdge("b", "c", 0);

        assertEquals(expected, actual);
        assertEquals(expected.getOutgoingNodes("a"), actual.getOutgoingNodes("a"));
        assertEquals(Set.of("b", "c"), actual.getOutgoingNodes("a"));
    }

    @Test
    public void getOutgoingNodes5() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"));

        assertNull(graph.getOutgoingNodes(null));
    }

    @Test
    public void inverted1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        assertEquals(expected, actual.inverted());
    }

    @Test
    public void inverted2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("b", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);

        assertEquals(expected, actual.inverted());
    }

    @Test
    public void inverted3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "a"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "a", 0);

        assertEquals(expected, actual.inverted());
        assertEquals(actual, actual.inverted());
    }

    @Test
    public void inverted4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "a"), Triple.of("c", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "a", 0).putEdge("b", "c", 1);

        assertEquals(expected, actual.inverted());
    }

    @Test
    public void numEdges() {
        Graph<String, Integer> graph = Graph.of();
        Graph<String, Integer> graph0 = Graph.of();
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "b"));
        Graph<String, Integer> graph3 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "b"), Triple.of("b", 0, "b"));

        assertEquals(0, graph0.size());

        graph = graph.putEdge("a", "b", 0);

        assertEquals(1, graph1.size());
        assertEquals(1, graph.size());

        graph = graph.putEdge("b", "b", 0);

        assertEquals(2, graph2.size());
        assertEquals(2, graph.size());

        graph = graph.putEdge("b", "b", 0);

        assertEquals(2, graph3.size());
        assertEquals(2, graph.size());
    }

    @Test
    public void contains1() {
        Graph<String, Integer> graph = Graph.of();

        assertFalse(graph.contains(Triple.of("a", 0, "a")));
    }

    @Test
    public void contains2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));

        assertFalse(graph.contains(Triple.of("a", 1, "a")));
        assertFalse(graph.contains(Triple.of("a", 0, "b")));
        assertTrue(graph.contains(Triple.of("a", 0, "a")));
        assertFalse(graph.contains(Triple.of(null, 0, "a")));
        assertFalse(graph.contains(Triple.of("a", null, "a")));
        assertFalse(graph.contains(Triple.of("a", 0, null)));
    }

    @Test
    public void hashCode1() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of();

        assertEquals(graph1.hashCode(), graph2.hashCode());
    }

    @Test
    public void hashCode2() {
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "a"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(graph1.hashCode(), graph2.hashCode());
    }

    @Test
    public void hashCode3() {
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("b", 0, "b"), Triple.of("a", 0, "a"));

        assertEquals(graph1.hashCode(), graph2.hashCode());
    }

    @Test
    public void equals1() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of();

        assertEquals(graph1, graph2);
    }

    @Test
    public void equals2() {
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "a"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(graph1, graph2);
    }

    @Test
    public void equals3() {
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("b", 0, "b"), Triple.of("a", 0, "a"));

        assertEquals(graph1, graph2);
    }

    @Test
    public void spliterator1() {
        Graph<String, Integer> graph = Graph.of();

        assertEquals(0, graph.spliterator().estimateSize());
    }

    @Test
    public void spliterator2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));
        MutableSet<Triple<String, Integer, String>> edges = MutableSet.of(Set.of(Triple.of("a", 0, "a")));
        Spliterator<Triple<String, Integer, String>> spliterator = graph.spliterator();

        assertEquals(1, spliterator.estimateSize());
        spliterator.forEachRemaining(edges::remove);
        assertEquals(0, edges.size());
    }

    @Test
    public void spliterator3() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        MutableSet<Triple<String, Integer, String>> edges = MutableSet.of(Set.of(Triple.of("b", 0, "b"), Triple.of("a", 0, "a")));
        Spliterator<Triple<String, Integer, String>> spliterator = graph.spliterator();

        assertEquals(2, spliterator.estimateSize());
        spliterator.forEachRemaining(edges::remove);
        assertEquals(0, edges.size());
    }

    @Test
    public void iterator1() {
        Graph<String, Integer> graph = Graph.of();

        assertFalse(graph.iterator().hasNext());
    }

    @Test
    public void iterator2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));
        Iterator<Triple<String, Integer, String>> iterator = graph.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(Triple.of("a", 0, "a"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void iterator3() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        MutableSet<Triple<String, Integer, String>> edges = MutableSet.of(Set.of(Triple.of("b", 0, "b"), Triple.of("a", 0, "a")));
        Iterator<Triple<String, Integer, String>> iterator = graph.iterator();

        assertTrue(iterator.hasNext());
        edges.remove(iterator.next());
        assertTrue(iterator.hasNext());
        edges.remove(iterator.next());
        assertFalse(iterator.hasNext());
        assertEquals(0, edges.size());
    }

    @Test
    public void isEmpty() {
        Graph<String, Integer> graph = Graph.of();

        assertTrue(graph.isEmpty());

        graph = graph.putEdge("a", "a", 0);

        assertFalse(graph.isEmpty());
    }

    @Test
    public void linked1() {
        Graph<String, Integer> graph = Graph.of();
        AtomicInteger counter = new AtomicInteger();

        graph.linked((triple1, triple2, triple3) -> {
            counter.getAndIncrement();
        });
        graph.linked((triple1, triple2, triple3) -> {
            return counter.getAndIncrement();
        });

        assertEquals(0, counter.get());
    }

    @Test
    public void linked2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));
        AtomicInteger counter = new AtomicInteger();

        graph.linked((triple1, triple2, triple3) -> {
            counter.getAndIncrement();
        });
        graph.linked((triple1, triple2, triple3) -> {
            return counter.getAndIncrement();
        });
        assertDoesNotThrow(() -> graph.linked((TriConsumer<Triple<String, Integer, String>, Triple<String, Integer, String>, Triple<String, Integer, String>>) null));
        assertDoesNotThrow(() -> graph.linked((TriFunction<Triple<String, Integer, String>, Triple<String, Integer, String>, Triple<String, Integer, String>, ?>) null));

        assertEquals(0, counter.get());
    }

    @Test
    public void linked3() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"));
        AtomicInteger counter = new AtomicInteger();

        graph.linked((triple1, triple2, triple3) -> {
            counter.getAndIncrement();
        });
        graph.linked((triple1, triple2, triple3) -> {
            return counter.getAndIncrement();
        });

        assertEquals(0, counter.get());
    }

    @Test
    public void linked4() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"), Triple.of("c", 0, "c"));
        AtomicInteger counter = new AtomicInteger();

        graph.linked((triple1, triple2, triple3) -> {
            counter.getAndIncrement();
        });
        List<Integer> result = graph.linked((triple1, triple2, triple3) -> {
            return counter.getAndIncrement();
        }).asList();

        int sum = 0;
        for (Integer num : result)
            sum += num;

        assertEquals(2, counter.get());
        assertEquals(1, sum);
    }

    @Test
    public void linked5() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 0, "b"), Triple.of("c", 0, "c"), Triple.of("d", 0, "d"));
        AtomicInteger counter = new AtomicInteger();

        graph.linked((triple1, triple2, triple3) -> {
            counter.getAndIncrement();
        });
        List<Integer> result = graph.linked((triple1, triple2, triple3) -> {
            return counter.getAndIncrement();
        }).asList();

        int sum = 0;
        for (Integer num : result)
            sum += num;

        assertEquals(4, counter.get());
        assertEquals(5, sum);
    }

    @Test
    public void indexed1() {
        Graph<String, Integer> graph = Graph.of();

        List<Integer> result = graph.indexed((triple, idx) -> idx + triple.b()).asList();

        int sum = 0;
        for (Integer num : result)
            sum += num;

        assertEquals(0, sum);
    }

    @Test
    public void indexed2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 2, "a"));

        List<Integer> result = graph.indexed((triple, idx) -> idx + triple.b()).asList();

        int sum = 0;
        for (Integer num : result)
            sum += num;

        assertEquals(2, sum);
    }

    @Test
    public void indexed3() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 1, "a"), Triple.of("a", 2, "a"));

        List<Integer> result = graph.indexed((triple, idx) -> idx + triple.b()).asList();
        assertThrows(NullPointerException.class, () -> graph.indexed(null));

        int sum = 0;
        for (Integer num : result)
            sum += num;

        assertEquals(4, sum);
    }

    @Test
    public void get1() {
        Graph<String, Integer> graph = Graph.of();

        assertThrows(IndexOutOfBoundsException.class, () -> graph.get(0));
    }

    @Test
    public void get2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(Triple.of("a", 0, "a"), graph.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.get(1));
    }

    @Test
    public void get3() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("b", 1, "b"));
        Set<Triple<String, Integer, String>> set = Set.of(Triple.of("a", 0, "a"), Triple.of("b", 1, "b"));

        set = set.remove(graph.get(0)).remove(graph.get(1));
        assertEquals(0, set.size());
    }

    @Test
    public void remove1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 0);
        assertTrue(actual.containsEdge("a", "b", 0));
        actual = actual.remove(Triple.of("a", 0, "b"));

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void remove2() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.remove(Triple.of("a", 0, "b"));

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void remove3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.putEdge("a", "b", 1);
        actual = actual.remove(Triple.of("a", 0, "b"));

        assertEquals(expected, actual);
        assertFalse(expected.containsEdge("a", "b", 0));
        assertFalse(actual.containsEdge("a", "b", 0));
        assertTrue(expected.containsEdge("a", "b", 1));
        assertTrue(actual.containsEdge("a", "b", 1));
    }

    @Test
    public void remove4() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.remove(Triple.of(null, 0, "b")).remove(Triple.of("a", null, "b")).remove(Triple.of("a", 0, null));

        assertEquals(expected, actual);
    }

    @Test
    public void removeAll1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.removeAll(Set.of());

        assertEquals(expected, actual);
    }

    @Test
    public void removeAll2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"));

        actual = actual.removeAll(Set.of(Triple.of("a", 0, "a")));

        assertEquals(expected, actual);
    }

    @Test
    public void removeAll3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"), Triple.of("a", 1, "b"));

        actual = actual.removeAll(Set.of(Triple.of("a", 0, "a"), Triple.of("a", 1, "b")));

        assertEquals(expected, actual);
    }

    @Test
    public void removeAll4() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(graph, graph.removeAll((Set<?>) null));
        assertEquals(graph, graph.removeAll(Set.of((Triple<String, Integer, String>) null)));
    }

    @Test
    public void add1() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.add(Triple.of("a", 0, "b"));

        assertEquals(expected, actual);
        assertTrue(expected.containsEdge("a", "b", 0));
        assertTrue(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void add2() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.add(Triple.of(null, 0, "a")).add(Triple.of("a", null, "a")).add(Triple.of("a", 0, null));

        assertEquals(expected, actual);
    }

    @Test
    public void addAll1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.addAll(Set.of());

        assertEquals(expected, actual);
    }

    @Test
    public void addAll2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.addAll(Set.of(Triple.of("a", 0, "a")));

        assertEquals(expected, actual);
    }

    @Test
    public void addAll3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"), Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.addAll(Set.of(Triple.of("a", 0, "a"), Triple.of("a", 1, "b")));

        assertEquals(expected, actual);
    }

    @Test
    public void addAll4() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(graph, graph.addAll(null));
        assertEquals(graph, graph.addAll(Set.of((Triple<String, Integer, String>) null)));
    }

    @Test
    public void addUnique1() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> actual = Graph.of();

        actual = actual.addUnique(Triple.of("a", 0, "b"));

        assertEquals(expected, actual);
        assertTrue(expected.containsEdge("a", "b", 0));
        assertTrue(actual.containsEdge("a", "b", 0));
    }

    @Test
    public void addUnique2() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.addUnique(Triple.of(null, 0, "a")).addUnique(Triple.of("a", null, "a")).addUnique(Triple.of("a", 0, null));

        assertEquals(expected, actual);
    }

    @Test
    public void addAllUnique1() {
        Graph<String, Integer> expected = Graph.of();
        Graph<String, Integer> actual = Graph.of();

        actual = actual.addAllUnique(Set.of());

        assertEquals(expected, actual);
    }

    @Test
    public void addAllUnique2() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.addAllUnique(Set.of(Triple.of("a", 0, "a")));

        assertEquals(expected, actual);
    }

    @Test
    public void addAllUnique3() {
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "a"), Triple.of("a", 1, "b"));
        Graph<String, Integer> actual = Graph.of(Triple.of("a", 0, "b"));

        actual = actual.addAllUnique(Set.of(Triple.of("a", 0, "a"), Triple.of("a", 1, "b")));

        assertEquals(expected, actual);
    }

    @Test
    public void addAllUnique4() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"));

        assertEquals(graph, graph.addAllUnique(null));
        assertEquals(graph, graph.addAllUnique(Set.of((Triple<String, Integer, String>) null)));
    }

    @Test
    public void replace() {
        Graph<String, Integer> graph1 = Graph.of();
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "a"));
        Graph<String, Integer> graph3 = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> graph4 = Graph.of(Triple.of("a", 0, "a"), Triple.of("a", 0, "b"));
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"));

        assertEquals(graph1, graph1.replace(Triple.of("a", 0, "a"), Triple.of("a", 0, "b")));
        assertEquals(expected, graph2.replace(Triple.of("a", 0, "a"), Triple.of("a", 0, "b")));
        assertEquals(graph3, graph3.replace(Triple.of("a", 0, "a"), Triple.of("a", 0, "b")));
        assertEquals(expected, graph4.replace(Triple.of("a", 0, "a"), Triple.of("a", 0, "b")));
    }

    @Test
    public void clear() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("a", 0, "b"));

        assertEquals(Graph.of(), graph.clear());
    }

    @Test
    public void listIterator() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "a"), Triple.of("a", 0, "b"));

        Set<Triple<String, Integer, String>> expected = Set.of(Triple.of("a", 0, "a"), Triple.of("a", 0, "b"));
        Set<Triple<String, Integer, String>> actual = Set.of(Triple.of("a", 0, "a"), Triple.of("a", 0, "b"));

        var test = graph.listIteratorAtEnd();

        while (test.hasPrevious()) {
            actual = actual.remove(test.previous());
        }

        assertEquals(0, actual.size());

        while (test.hasNext()) {
            actual = actual.add(test.next());
        }

        assertEquals(expected, actual);

        test = graph.listIterator();

        while (test.hasNext()) {
            actual = actual.remove(test.next());
        }

        assertEquals(0, actual.size());

        while (test.hasPrevious()) {
            actual = actual.add(test.previous());
        }

        assertEquals(expected, actual);

        var it1 = graph.listIterator();
        var it2 = graph.listIterator(0);
        var it3 = graph.listIteratorAtEnd();

        while (it1.hasNext() || it2.hasNext()) {
            assertTrue(it1.hasNext() && it2.hasNext());
            assertEquals(it1.next(), it2.next());
        }

        while (it1.hasPrevious() || it2.hasPrevious() || it3.hasPrevious()) {
            assertTrue(it1.hasPrevious() && it2.hasPrevious() && it3.hasPrevious());
            var value = it1.previous();
            assertEquals(value, it2.previous());
            assertEquals(value, it3.previous());
        }
    }

    @Test
    public void compare1() {
        Graph<String, Integer> graph = Graph.of();
        Set<Triple<String, Integer, String>> set = Set.of();

        for (char src = 'a'; src <= 'j'; src++) {
            for (char dst = 'a'; dst <= 'j'; dst++) {
                for (int val = 1; val <= 10; val++) {
                    graph = graph.putEdge(src + "", dst + "", val);
                    if (src == 'a') {
                        set = set.add(Triple.of(src + "", val, dst + ""));
                    }
                }
            }
        }

        var mutated = graph.removeEdge("a", "b", 1).removeEdge("b", "c", 2).putEdge("a", "b", 0);

        Set<Graph<String, Integer>> expectedBefore = Set.of(Graph.of(), Graph.of(Triple.of("a", 1, "b")), Graph.of(Triple.of("b", 2, "c")));
        Set<Graph<String, Integer>> expectedAfter = Set.of(Graph.of(), Graph.of(Triple.of("a", 0, "b")));

        MutableSet<Graph<String, Integer>> actualBefore = MutableSet.of(Set.of());
        MutableSet<Graph<String, Integer>> actualAfter = MutableSet.of(Set.of());

        graph.compare(mutated).forEach(e -> {
            var before = e[0];
            var after = e[1];
            actualBefore.add(before);
            actualAfter.add(after);
        });

        assertEquals(expectedBefore, actualBefore.toImmutable());
        assertEquals(expectedAfter, actualAfter.toImmutable());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void merge1() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"), Triple.of("c", 1, "d"));
        Graph<String, Integer> expected = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "c"));

        List<Graph<String, Integer>> others = List.of();
        others = others.add(graph.putEdge("a", "c", 0));
        others = others.add(graph.removeEdge("c", "d", 1));
        assertEquals(expected, graph.merge(others.toArray(Graph[]::new)));
    }

    @Test
    public void cycle1() {
        Graph<String, Integer> graph0 = Graph.of();
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "b"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 1, "a"));
        Graph<String, Integer> graph3 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "a"));

        Predicate<String> passNode = n -> true;
        Predicate<Triple<String, Integer, String>> passEdge = edge -> true;
        Predicate<String> rejectNode = n -> false;
        Predicate<Triple<String, Integer, String>> rejectEdge = edge -> false;

        assertFalse(graph0.hasCycles(passNode, passEdge));
        assertFalse(graph1.hasCycles(passNode, passEdge));
        assertTrue(graph2.hasCycles(passNode, passEdge));
        assertTrue(graph3.hasCycles(passNode, passEdge));

        assertFalse(graph2.hasCycles(passNode, rejectEdge));
        assertFalse(graph2.hasCycles(rejectNode, passEdge));
        assertFalse(graph2.hasCycles(rejectNode, rejectEdge));
    }

    @Test
    public void cycle2() {
        Graph<String, Integer> graph = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "b"));
        assertFalse(graph.hasCycles(node -> true, edge -> !Objects.equals(edge.a(), edge.c())));
    }

    @Test
    public void cycle3() {
        Graph<String, Integer> graph1 = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "c"), Triple.of("c", 0, "b"));
        Graph<String, Integer> graph2 = Graph.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "c"), Triple.of("c", 0, "b"));
        Graph<String, Integer> graph3 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 0, "a"));
        Graph<String, Integer> graph4 = Graph.of(Triple.of("a", 0, "b"), Triple.of("b", 1, "a"));
        assertFalse(graph1.hasCycles(node -> true, edge -> true));
        assertFalse(graph2.hasCycles(node -> true, edge -> true));
        assertTrue(graph3.hasCycles(node -> true, edge -> true));
        assertTrue(graph4.hasCycles(node -> true, edge -> true));
        assertFalse(graph4.hasCycles(node -> false, edge -> true));
        assertFalse(graph4.hasCycles(node -> true, edge -> false));
        assertFalse(graph4.hasCycles(node -> false, edge -> false));
        assertFalse(graph4.hasCycles(node -> !node.equals("a"), edge -> true));
        assertFalse(graph4.hasCycles(node -> true, edge -> edge.b() != 0));
        assertFalse(graph4.hasCycles(node -> true, edge -> edge.b() != 1));
        assertTrue(graph4.hasCycles(node -> true, edge -> edge.b() != 2));
    }

    @Test
    public void baseStream1() {
        Graph<String, Integer> graph = Graph.of();

        assertEquals(Set.of(), graph.asSet());

        graph = graph.putEdge("a", "b", 0);
        assertEquals(Set.of(Triple.of("a", 0, "b")), graph.asSet());

        graph = graph.putEdge("a", "b", 0);
        assertEquals(Set.of(Triple.of("a", 0, "b")), graph.asSet());

        graph = graph.putEdge("a", "c", 0);
        assertEquals(Set.of(Triple.of("a", 0, "b"), Triple.of("a", 0, "c")), graph.asSet());
    }
}

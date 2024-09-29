//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.DirGraph;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;

public class DirGraphTest {
    @Test
    public void constructor() {
        DirGraph<String> graph = DirGraph.of(//
                "c", "a", //
                "a", "b", //
                "b", "c").setBegin("a");
        assertTrue(graph.containsEdge("a", "b"));
        assertTrue(graph.containsEdge("b", "c"));
        assertFalse(graph.containsEdge("c", "a"));
        assertEquals(Set.of("a"), graph.begin());
        assertEquals(Set.of("c"), graph.end());
        assertEquals(3, graph.size());
    }

    @Test
    public void emptyDag() {
        DirGraph<String> graph1 = DirGraph.of();
        DirGraph<String> graph2 = DirGraph.of();
        assertNotNull(graph1);
        assertEquals(0, graph1.size());
        assertTrue(graph1 == graph2);
    }

    @Test
    public void rmoveEdges() {
        DirGraph<String> graph1 = DirGraph.of();
        DirGraph<String> graph2 = DirGraph.of(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        DirGraph<String> graph3 = graph1.addEdges(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        assertEquals(0, graph1.size());
        assertEquals(4, graph2.size());
        assertEquals(4, graph3.size());
        assertEquals(graph2, graph3);
        graph2 = graph2.removeEdges(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        assertTrue(graph1 == graph2.removeNodes(graph2.unconnected()));
    }

    @RepeatedTest(36)
    public void dagsMergeToDag() {
        int size = 10_000;
        Random random = new Random(System.currentTimeMillis());

        DirGraph<Integer> graph1 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph1 = graph1.addEdge(random.nextInt(size), random.nextInt(size));
        }
        Dag<Integer> dag1 = graph1.removeCycles();

        DirGraph<Integer> graph2 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph2 = graph2.addEdge(random.nextInt(size), random.nextInt(size));
        }
        Dag<Integer> dag2 = graph2.removeCycles();

        assertEquals(dag1.navigable(), dag1.nodes().asSet());
        assertEquals(0, dag1.cycles().size());
        assertEquals(dag2.navigable(), dag2.nodes().asSet());
        assertEquals(0, dag2.cycles().size());

        Set<Integer> begin = dag1.begin().addAll(dag2.begin());
        DirGraph<Integer> merged = Dag.<Integer> of().merge(dag1, dag2);

        merged = merged.setBegin(begin);
        assertEquals(begin, merged.begin());

        Dag<Integer> dag = merged.removeCycles();
        dag = dag.setBegin(begin);

        assertEquals(dag.navigable(), dag.nodes().asSet());
        assertEquals(0, dag.cycles().size());

        assertEquals(merged.size(), dag.size());
        assertEquals(begin, dag.begin());
    }

    @RepeatedTest(36)
    public void disjunct() {
        int size = 10_000;
        Random random = new Random(System.currentTimeMillis());

        DirGraph<Integer> graph1 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph1 = graph1.addEdge(random.nextInt(size), random.nextInt(size));
        }
        Dag<Integer> dag1 = graph1.removeCycles();

        DirGraph<Integer> graph2 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph2 = graph2.addEdge(-random.nextInt(size) - 1, -random.nextInt(size) - 1);
        }
        Dag<Integer> dag2 = graph2.removeCycles();

        assertEquals(dag1.navigable().size(), dag1.size());
        assertEquals(dag2.navigable().size(), dag2.size());

        DirGraph<Integer> merged = Dag.<Integer> of().merge(dag1, dag2);

        assertEquals(0, merged.cycles().size());
    }

    @RepeatedTest(36)
    public void chained() {
        int size = 10_000;
        Random random = new Random(System.currentTimeMillis());

        DirGraph<Integer> graph1 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph1 = graph1.addEdge(random.nextInt(size), -random.nextInt(size) - 1);
        }
        Dag<Integer> dag1 = graph1.removeCycles();

        DirGraph<Integer> graph2 = DirGraph.of();
        for (int i = 0; i < size; i++) {
            graph2 = graph2.addEdge(-random.nextInt(size) - 1, random.nextInt(size) + 100_000);
        }
        Dag<Integer> dag2 = graph2.removeCycles();

        dag1 = dag1.setEnd(dag2.begin());
        dag2 = dag2.setBegin(dag1.end());

        DirGraph<Integer> merged = Dag.<Integer> of().merge(dag1, dag2);

        assertEquals(dag1.begin(), merged.begin());
        assertEquals(dag2.end(), merged.end());
    }

    @RepeatedTest(36)
    public void bigDag() {
        int size = 10_000;
        DirGraph<Integer> graph = DirGraph.of();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {
            graph = graph.addEdge(random.nextInt(size), random.nextInt(size));
        }
        graph = graph.retainNavigable().invRetainNavigable();
        graph = graph.putBegin(-1, graph.begin());
        graph = graph.putEnd(-2, graph.end());
        assertEquals(Set.of(-1), graph.begin());
        assertEquals(Set.of(-2), graph.end());
        List<Integer> top = graph.topologicalNodes();
        assertEquals(graph.size(), top.size());
        assertEquals(-1, top.first());
        assertEquals(-2, top.last());
    }

    @Test
    public void merge1() {
        DirGraph<String> graph1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        DirGraph<String> graph2 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(graph1, graph2);
        DirGraph<String> merged = Dag.<String> of().merge(graph1, graph2);
        assertEquals(merged, graph1);
        assertEquals(merged, graph2);
    }

    @Test
    public void merge2() {
        DirGraph<String> dag0 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        DirGraph<String> graph1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5");
        DirGraph<String> graph2 = DirGraph.of(//
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        DirGraph<String> merged = DirGraph.<String> of().merge(graph1, graph2);
        assertEquals(merged, dag0);
        assertEquals(merged, dag0);
    }

    @Test
    public void merge3() {
        Dag<String> dag0 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "7", "8", //
                "8", "5").removeCycles();
        assertEquals(8, dag0.edges().size());
        DirGraph<String> graph1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "8", "5");
        DirGraph<String> graph2 = DirGraph.of(//
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8", //
                "6", "3");
        DirGraph<String> merged0 = Dag.<String> of().merge(graph1, graph2);
        Dag<String> merged1 = merged0.removeCycles();
        assertEquals(merged1, dag0);
    }

    @Test
    public void topologicalSort() {
        DirGraph<String> graph = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(Set.of("1", "2"), graph.begin());
        assertEquals(Set.of("6", "8"), graph.end());
        assertEquals(8, graph.size());

        List<String> top = graph.topologicalNodes();
        assertEquals(List.of("2", "5", "4", "7", "8", "1", "3", "6"), top);
    }

    @Test
    public void putBeginEnd() {
        DirGraph<String> graph1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(8, graph1.size());
        assertEquals(Set.of("1", "2"), graph1.begin());
        assertEquals(Set.of("6", "8"), graph1.end());

        DirGraph<String> graph2 = graph1.putBegin("x", "1", "2").putEnd("y", "6", "8");
        assertEquals(10, graph2.size());
        assertEquals(Set.of("x"), graph2.begin());
        assertEquals(Set.of("y"), graph2.end());
    }

}

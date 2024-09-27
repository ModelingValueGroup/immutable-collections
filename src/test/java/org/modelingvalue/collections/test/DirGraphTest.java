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
        DirGraph<String> dag = DirGraph.of(//
                "c", "a", //
                "a", "b", //
                "b", "c").setBegin("a");
        assertTrue(dag.containsEdge("a", "b"));
        assertTrue(dag.containsEdge("b", "c"));
        assertFalse(dag.containsEdge("c", "a"));
        assertEquals(Set.of("a"), dag.begin());
        assertEquals(Set.of("c"), dag.end());
        assertEquals(3, dag.size());
    }

    @Test
    public void emptyDag() {
        DirGraph<String> dag1 = DirGraph.of();
        DirGraph<String> dag2 = DirGraph.of();
        assertNotNull(dag1);
        assertEquals(0, dag1.size());
        assertTrue(dag1 == dag2);
    }

    @Test
    public void rmoveEdges() {
        DirGraph<String> dag1 = DirGraph.of();
        DirGraph<String> dag2 = DirGraph.of(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        DirGraph<String> dag3 = dag1.addEdges(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        assertEquals(0, dag1.size());
        assertEquals(4, dag2.size());
        assertEquals(4, dag3.size());
        assertEquals(dag2, dag3);
        dag2 = dag2.removeEdges(//
                "a", "b", //
                "b", "c", //
                "c", "d");
        assertTrue(dag1 == dag2);
    }

    @RepeatedTest(10)
    public void bigDag() {
        int size = 10_000;
        DirGraph<Integer> graph = DirGraph.of();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {
            graph = graph.addEdge(random.nextInt(size), random.nextInt(size));
        }
        graph = graph.removeDisconnected();
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
        DirGraph<String> dag1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        DirGraph<String> dag2 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(dag1, dag2);
        DirGraph<String> merged = DirGraph.<String> of().merge(dag1, dag2);
        assertEquals(merged, dag1);
        assertEquals(merged, dag2);
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
        DirGraph<String> dag1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5");
        DirGraph<String> dag2 = DirGraph.of(//
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        DirGraph<String> merged = DirGraph.<String> of().merge(dag1, dag2);
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
        DirGraph<String> dag1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "8", "5");
        DirGraph<String> dag2 = DirGraph.of(//
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8", //
                "6", "3");
        DirGraph<String> merged0 = Dag.<String> of().merge(dag1, dag2);
        Dag<String> merged1 = merged0.removeCycles();
        assertEquals(merged1, dag0);
    }

    @Test
    public void topologicalSort() {
        DirGraph<String> dag = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(Set.of("1", "2"), dag.begin());
        assertEquals(Set.of("6", "8"), dag.end());
        assertEquals(8, dag.size());

        List<String> top = dag.topologicalNodes();
        assertEquals(List.of("2", "5", "4", "7", "8", "1", "3", "6"), top);
    }

    @Test
    public void putBeginEnd() {
        DirGraph<String> dag1 = DirGraph.of(//
                "1", "3", //
                "3", "6", //
                "2", "4", //
                "2", "5", //
                "4", "6", //
                "4", "7", //
                "5", "7", //
                "7", "8");
        assertEquals(8, dag1.size());
        assertEquals(Set.of("1", "2"), dag1.begin());
        assertEquals(Set.of("6", "8"), dag1.end());

        DirGraph<String> dag2 = dag1.putBegin("x", "1", "2").putEnd("y", "6", "8");
        assertEquals(10, dag2.size());
        assertEquals(Set.of("x"), dag2.begin());
        assertEquals(Set.of("y"), dag2.end());
    }

}

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

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;

public class DagTest {
    @Test
    public void constructor() {
        Dag<String> dag = Dag.of("c", "a", "a", "b", "b", "c");
        assertFalse(dag.containsEdge("a", "b"));
        assertTrue(dag.containsEdge("b", "c"));
        assertTrue(dag.containsEdge("c", "a"));
        assertEquals(Set.of("b"), dag.begin());
        assertEquals(Set.of("a"), dag.end());
        assertEquals(3, dag.size());
    }

    @Test
    public void emptyDag() {
        Dag<String> dag1 = Dag.of();
        Dag<String> dag2 = Dag.of();
        assertNotNull(dag1);
        assertEquals(0, dag1.size());
        assertTrue(dag1 == dag2);
    }

    @Test
    public void rmoveEdges() {
        Dag<String> dag1 = Dag.of();
        Dag<String> dag2 = Dag.of("a", "b", "b", "c", "c", "d");
        Dag<String> dag3 = dag1.addEdges("a", "b", "b", "c", "c", "d");
        assertEquals(0, dag1.size());
        assertEquals(4, dag2.size());
        assertEquals(4, dag3.size());
        assertEquals(dag2, dag3);
        dag2 = dag2.removeEdges("a", "b", "b", "c", "c", "d");
        assertTrue(dag1 == dag2);
    }

    @Test
    public void topologicalSort() {
        Dag<String> dag = Dag.of(//
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

        List<String> top = dag.topological();
        assertEquals(List.of("2", "5", "4", "7", "8", "1", "3", "6"), top);
    }

    @Test
    public void putBeginEnd() {
        Dag<String> dag1 = Dag.of(//
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

        Dag<String> dag2 = dag1.putBegin("x", "1", "2").putEnd("y", "6", "8");
        assertEquals(10, dag2.size());
        assertEquals(Set.of("x"), dag2.begin());
        assertEquals(Set.of("y"), dag2.end());
    }

}

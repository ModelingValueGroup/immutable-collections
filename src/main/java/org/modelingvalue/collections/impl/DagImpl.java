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

package org.modelingvalue.collections.impl;

import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.util.Pair;

public class DagImpl<N> extends DirGraphImpl<N> implements Dag<N> {
    private static final long serialVersionUID = 1419504275082662008L;

    public DagImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        super(begin, end, vertices);
    }

    @Override
    protected Dag<N> construct(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        return new DagImpl<N>(begin, end, vertices);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> prune(N node) {
        return (Dag<N>) super.prune(node);
    }

    @Override
    public Dag<N> removeCycles() {
        return this;
    }

    @Override
    public Set<Pair<N, N>> cycles() {
        return Set.of();
    }

    @Override
    public Dag<N> addNode(N node) {
        return (Dag<N>) super.addNode(node);
    }

    @Override
    public Dag<N> removeEdge(N from, N to) {
        return (Dag<N>) super.removeEdge(from, to);
    }

    @Override
    public Dag<N> removeNode(N node) {
        return (Dag<N>) super.removeNode(node);
    }

    @Override
    public Dag<N> clear(N node) {
        return (Dag<N>) super.clear(node);
    }

    @Override
    public Dag<N> clearOuts(N node) {
        return (Dag<N>) super.clearOuts(node);
    }

    @Override
    public Dag<N> clearIns(N node) {
        return (Dag<N>) super.clearIns(node);
    }

    @Override
    public Dag<N> remove(N node, Set<N> ins, Set<N> outs) {
        return (Dag<N>) super.remove(node, ins, outs);
    }

    @Override
    public Dag<N> removeOuts(N node, Set<N> outs) {
        return (Dag<N>) super.removeOuts(node, outs);
    }

    @Override
    public Dag<N> removeIns(N node, Set<N> ins) {
        return (Dag<N>) super.removeIns(node, ins);
    }

}

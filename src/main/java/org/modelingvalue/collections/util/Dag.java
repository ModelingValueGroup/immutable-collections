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

package org.modelingvalue.collections.util;

import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;

public class Dag<N> extends DirGraph<N> {

    private static final long serialVersionUID = 1419504275082662008L;

    protected Dag(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        super(begin, end, vertices);
    }

    @Override
    protected Dag<N> construct(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        return new Dag<N>(begin, end, vertices);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> pruneOuts(N node) {
        return (Dag<N>) super.pruneOuts(node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> pruneIns(N node) {
        return (Dag<N>) super.pruneIns(node);
    }

}

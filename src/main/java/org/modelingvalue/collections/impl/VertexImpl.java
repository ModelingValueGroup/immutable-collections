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

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.struct.impl.Struct3Impl;

public final class VertexImpl<N> extends Struct3Impl<N, Set<N>, Set<N>> implements Vertex<N> {
    private static final long serialVersionUID = -3578650096220973873L;

    public VertexImpl(N node, Set<N> ins, Set<N> outs) {
        super(node, ins, outs);
    }

    @Override
    public N node() {
        return get0();
    }

    @Override
    public Set<N> ins() {
        return get1();
    }

    @Override
    public Set<N> outs() {
        return get2();
    }

    @Override
    public Vertex<N> merge(Vertex<N>[] branches, int length) {
        Set<N> ins = ins();
        Set<N> outs = outs();
        for (int i = 0; i < length; i++) {
            ins = ins.addAll(branches[i].ins());
            outs = outs.addAll(branches[i].outs());
        }
        return new VertexImpl<N>(node(), ins, outs);
    }

    @Override
    public Vertex<N> getMerger() {
        return new VertexImpl<N>(node(), Set.of(), Set.of());
    }

    @Override
    public Class<?> getMeetClass() {
        return Vertex.class;
    }
}

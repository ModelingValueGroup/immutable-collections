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

package org.modelingvalue.collections.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ContextSpliterator<A> implements Spliterator<A> {
    private final Spliterator<A> sp;
    private final Object[]       ctx;

    public static <T> Spliterator<T> of(Spliterator<T> sp) {
        return new ContextSpliterator<T>(sp, ContextThread.getContext());
    }

    private ContextSpliterator(Spliterator<A> sp, Object[] ctx) {
        this.sp = sp;
        this.ctx = ctx;
    }

    @Override
    public void forEachRemaining(Consumer<? super A> action) {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            sp.forEachRemaining(action);
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super A> action) {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            return sp.tryAdvance(action);
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public Spliterator<A> trySplit() {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            Spliterator<A> tsp = sp.trySplit();
            return tsp != null ? new ContextSpliterator<A>(tsp, ctx) : null;
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public long getExactSizeIfKnown() {
        return sp.getExactSizeIfKnown();
    }

    @Override
    public long estimateSize() {
        return sp.estimateSize();
    }

    @Override
    public int characteristics() {
        return sp.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return sp.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super A> getComparator() {
        return sp.getComparator();
    }

}

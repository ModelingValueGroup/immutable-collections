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

package org.modelingvalue.collections.mutable;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Set;

public abstract class MutableSet<T> extends AbstractSet<T> implements Mutable<T> {

    public static <T> MutableSet<T> of(Set<T> set) {
        return new Impl<T>(set);
    }

    public static <T> MutableSet<T> concurrent(Set<T> set) {
        return new ConcurrentImpl<T>(set);
    }

    protected abstract Set<T> get();

    protected abstract boolean set(UnaryOperator<Set<T>> oper);

    @Override
    public Set<T> toImmutable() {
        return get();
    }

    @Override
    public int size() {
        return get().size();
    }

    @Override
    public boolean isEmpty() {
        return get().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return get().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it   = get().iterator();
            private T                 last = null;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                last = it.next();
                return last;
            }

            @Override
            public void remove() {
                set(set -> set.remove(last));
            }
        };
    }

    @Override
    public Object[] toArray() {
        return get().toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] a) {
        return get().toArray(i -> (E[]) Array.newInstance(a.getClass().getComponentType(), i));
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        return get().containsAll(Set.fromMutable(c));
    }

    @Override
    public boolean add(T e) {
        return set(set -> set.add(e));
    }

    @Override
    public boolean remove(Object o) {
        return set(set -> set.remove(o));
    }

    @Override
    public boolean addAll(java.util.Collection<? extends T> c) {
        Set<? extends T> all = Set.fromMutable(c);
        return set(set -> set.addAll(all));
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        Set<?> all = Set.fromMutable(c);
        return set(set -> set.retainAll(all));
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        Set<?> all = Set.fromMutable(c);
        return set(set -> set.removeAll(all));
    }

    @Override
    public void clear() {
        set(Set::clear);
    }

    @Override
    public String toString() {
        return get().toString();
    }

    private static class Impl<T> extends MutableSet<T> {

        private Set<T> set;

        private Impl(Set<T> set) {
            this.set = set;
        }

        @Override
        protected Set<T> get() {
            return set;
        }

        @Override
        protected boolean set(UnaryOperator<Set<T>> oper) {
            Set<T> pre = set;
            set = oper.apply(pre);
            return pre != set;
        }

    }

    private static class ConcurrentImpl<T> extends MutableSet<T> {

        private AtomicReference<Set<T>> ref;

        private ConcurrentImpl(Set<T> set) {
            this.ref = new AtomicReference<>(set);
        }

        @Override
        protected Set<T> get() {
            return ref.get();
        }

        @Override
        protected boolean set(UnaryOperator<Set<T>> oper) {
            Set<T> prev = ref.get(), next = null;
            for (boolean haveNext = false;;) {
                if (!haveNext)
                    next = oper.apply(prev);
                if (ref.weakCompareAndSetVolatile(prev, next))
                    return prev != next;
                haveNext = (prev == (prev = ref.get()));
            }
        }

    }
}

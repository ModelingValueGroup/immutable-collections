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
import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;

public abstract class MutableList<T> extends AbstractList<T> implements Mutable<T> {

    public static <T> MutableList<T> of(List<T> list) {
        return new Impl<T>(list);
    }

    public static <T> MutableList<T> concurrent(List<T> list) {
        return new ConcurrentImpl<T>(list);
    }

    protected abstract List<T> get();

    protected abstract boolean set(UnaryOperator<List<T>> oper);

    protected abstract List<T> getAndSet(UnaryOperator<List<T>> oper);

    @Override
    public List<T> toImmutable() {
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
    public boolean containsAll(java.util.Collection<?> c) {
        return get().containsAll(Collection.of(c));
    }

    @Override
    public int indexOf(Object o) {
        return get().firstIndexOf(o);
    }

    @Override
    public T get(int index) {
        return get().get(index);
    }

    @Override
    public int lastIndexOf(Object o) {
        return get().lastIndexOf(o);
    }

    @Override
    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return get().sublist(fromIndex, toIndex).toMutable();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it   = get().iterator();
            private int               last = -1;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                last++;
                return it.next();
            }

            @Override
            public void remove() {
                set(list -> list.removeIndex(last));
            }
        };
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListIterator<T>() {
            private final ListIterator<T> it   = get().listIterator(index);
            private int                   last = index - 1;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                last++;
                return it.next();
            }

            @Override
            public void remove() {
                MutableList.this.set(list -> list.removeIndex(last));
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public T previous() {
                last--;
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void set(T e) {
                MutableList.this.set(list -> list.replace(last, e));
            }

            @Override
            public void add(T e) {
                MutableList.this.set(list -> list.insert(last + 1, e));
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
    public boolean add(T e) {
        return set(list -> list.add(e));
    }

    @Override
    public boolean remove(Object o) {
        return set(list -> list.remove(o));
    }

    @Override
    public void add(int index, T element) {
        set(list -> list.insert(index, element));
    }

    @Override
    public T set(int index, T element) {
        return getAndSet(list -> list.replace(index, element)).get(index);
    }

    @Override
    public T remove(int index) {
        return getAndSet(list -> list.removeIndex(index)).get(index);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends T> c) {
        List<? extends T> all = List.fromMutable(c);
        return set(list -> list.appendList(all));
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends T> c) {
        List<? extends T> all = List.fromMutable(c);
        return set(list -> list.insertList(index, all));
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        return set(list -> list.removeAll(Collection.of(c)));
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        return set(list -> list.removeAll(list.exclude(c::contains)));
    }

    @Override
    public void clear() {
        set(List::clear);
    }

    @Override
    public String toString() {
        return get().toString();
    }

    private static class Impl<T> extends MutableList<T> {

        private List<T> list;

        private Impl(List<T> list) {
            this.list = list;
        }

        @Override
        protected List<T> get() {
            return list;
        }

        @Override
        protected boolean set(UnaryOperator<List<T>> oper) {
            List<T> pre = list;
            list = oper.apply(pre);
            return pre != list;
        }

        @Override
        protected List<T> getAndSet(UnaryOperator<List<T>> oper) {
            List<T> pre = list;
            list = oper.apply(pre);
            return pre;
        }

    }

    private static class ConcurrentImpl<T> extends MutableList<T> {

        private AtomicReference<List<T>> ref;

        public ConcurrentImpl(List<T> list) {
            this.ref = new AtomicReference<>(list);
        }

        @Override
        protected List<T> get() {
            return ref.get();
        }

        @Override
        protected boolean set(UnaryOperator<List<T>> oper) {
            List<T> prev = ref.get(), next = null;
            for (boolean haveNext = false;;) {
                if (!haveNext)
                    next = oper.apply(prev);
                if (ref.weakCompareAndSetVolatile(prev, next))
                    return prev != next;
                haveNext = (prev == (prev = ref.get()));
            }
        }

        @Override
        protected List<T> getAndSet(UnaryOperator<List<T>> oper) {
            return ref.getAndUpdate(oper);
        }

    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2022 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.mutable;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.ListIterator;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;

public class MutableList<T> implements java.util.List<T>, Mutable<T> {

    private List<T> list;

    public MutableList(List<T> list) {
        this.list = list;
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            MutableList other = (MutableList) obj;
            return list.equals(other.list);
        }
    }

    @Override
    public List<T> toImmutable() {
        return list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        return list.containsAll(Collection.of(c));
    }

    @Override
    public int indexOf(Object o) {
        return list.firstIndexOf(o);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return list.sublist(fromIndex, toIndex).toMutable();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it   = list.iterator();
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
                list = list.removeIndex(last);
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
            private final ListIterator<T> it   = list.listIterator(index);
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
                list = list.removeIndex(last);
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
                list = list.replace(last, e);
            }

            @Override
            public void add(T e) {
                list = list.insert(last + 1, e);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] a) {
        return list.toArray(i -> (E[]) Array.newInstance(a.getClass().getComponentType(), i));
    }

    @Override
    public boolean add(T e) {
        List<T> pre = list;
        list = list.add(e);
        return pre != list;
    }

    @Override
    public boolean remove(Object o) {
        List<T> pre = list;
        list = list.remove(o);
        return pre != list;
    }

    @Override
    public T set(int index, T element) {
        T pre = list.get(index);
        list = list.replace(index, element);
        return pre;
    }

    @Override
    public void add(int index, T element) {
        list = list.insert(index, element);
    }

    @Override
    public T remove(int index) {
        T pre = list.get(index);
        list = list.removeIndex(index);
        return pre;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends T> c) {
        List<T> pre = list;
        list = list.appendList(Collection.of(c).toList());
        return pre != list;
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends T> c) {
        List<T> pre = list;
        list = list.insertList(index, Collection.of(c).toList());
        return pre != list;
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        List<T> pre = list;
        list = list.removeAll(Collection.of(c));
        return pre != list;
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        List<T> pre = list;
        list = list.removeAll(list.exclude(c::contains));
        return pre != list;
    }

    @Override
    public void clear() {
        list = list.clear();
    }

    @Override
    public String toString() {
        return list.toString();
    }

}

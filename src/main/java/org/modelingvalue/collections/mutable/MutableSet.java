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

import org.modelingvalue.collections.Set;

public class MutableSet<T> implements java.util.Set<T>, Mutable<T> {

    private Set<T> set;

    public MutableSet(Set<T> set) {
        this.set = set;
    }

    @Override
    public Set<T> toImmutable() {
        return set;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it   = set.iterator();
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
                set = set.remove(last);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] a) {
        return set.toArray(i -> (E[]) Array.newInstance(a.getClass().getComponentType(), i));
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        return set.containsAll(Set.fromMutable(c));
    }

    @Override
    public boolean add(T e) {
        Set<T> pre = set;
        set = set.add(e);
        return pre != set;
    }

    @Override
    public boolean remove(Object o) {
        Set<T> pre = set;
        set = set.remove(o);
        return pre != set;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends T> c) {
        Set<T> pre = set;
        set = set.addAll(Set.fromMutable(c));
        return pre != set;
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        Set<T> pre = set;
        set = set.retainAll(Set.fromMutable(c));
        return pre != set;
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        Set<T> pre = set;
        set = set.removeAll(Set.fromMutable(c));
        return pre != set;
    }

    @Override
    public void clear() {
        set = set.clear();
    }
}

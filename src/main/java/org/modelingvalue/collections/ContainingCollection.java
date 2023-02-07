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

package org.modelingvalue.collections;

import java.util.ListIterator;
import java.util.Spliterator;

import org.modelingvalue.collections.util.Deserializer;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.Serializer;

@SuppressWarnings("unused")
public interface ContainingCollection<T> extends Collection<T>, Internable {

    <R extends ContainingCollection<T>> StreamCollection<R[]> compare(R other);

    T get(int index);

    ContainingCollection<T> remove(Object e);

    ContainingCollection<T> removeAll(Collection<?> e);

    ContainingCollection<T> add(T e);

    ContainingCollection<T> addAll(Collection<? extends T> e);

    ContainingCollection<T> addUnique(T e);

    ContainingCollection<T> addAllUnique(Collection<? extends T> e);

    ContainingCollection<T> replace(Object pre, T post);

    ContainingCollection<T> clear();

    @Override
    boolean contains(Object e);

    default boolean notContains(Object e) {
        return !contains(e);
    }

    Collection<T> reverse();

    Spliterator<T> reverseSpliterator();

    ListIterator<T> listIterator();

    ListIterator<T> listIterator(int index);

    ListIterator<T> listIteratorAtEnd();

    void javaSerialize(Serializer s);

    void javaDeserialize(Deserializer s);
}

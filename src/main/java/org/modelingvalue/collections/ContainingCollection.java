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

package org.modelingvalue.collections;

import java.util.ListIterator;
import java.util.Spliterator;

import org.modelingvalue.collections.util.Deserializer;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.Serializer;
import org.modelingvalue.collections.util.Triple;

@SuppressWarnings("unused")
public interface ContainingCollection<T> extends Collection<T>, Internable {

    <R extends ContainingCollection<T>> StreamCollection<R[]> compare(R other);

    /**
     * Returns the element at the provided index in this collection.
     *
     * @param index the position whose associated value is to be returned
     * @return the element at the provided index in this collection
     */
    T get(int index);

    /**
     * Removes the provided value from this collection and returns the updated collection.
     *
     * @param e value to be removed
     * @return a new collection consisting of the elements of this collection without the provided
     * value.
     */
    ContainingCollection<T> remove(Object e);

    /**
     * Removes all the values in the provided collection from this collection and returns the
     * updated collection.
     *
     * @param e collection of values to be removed
     * @return a new collection consisting of the elements of this collection without any of the
     * provided values
     */
    ContainingCollection<T> removeAll(Collection<?> e);

    /**
     * Adds the provided value to this collection and returns the updated collection.
     *
     * @param e value to be added
     * @return a new collection consisting of the elements of this collection with the provided
     * value added
     */
    ContainingCollection<T> add(T e);

    /**
     * Adds all the values in the provided collection to this collection and returns the updated
     * collection.
     *
     * @param e collection of values to be added
     * @return a new collection consisting of the elements of this collection with all the provided
     * values added
     */
    ContainingCollection<T> addAll(Collection<? extends T> e);

    /**
     * Adds the provided value to this collection if it does not exist in this collection and
     * returns the updated collection.
     *
     * @param e value to be added
     * @return a new collection consisting of the elements of this collection with the provided
     * value added if it does not already exist
     */
    ContainingCollection<T> addUnique(T e);

    /**
     * Adds each of the provided values to this collection if it does not exist in this collection
     * and returns the updated collection.
     *
     * @param e collection of values to be added
     * @return a new collection consisting of the elements of this collection with each of the
     * provided values added if it does not already exist
     */
    ContainingCollection<T> addAllUnique(Collection<? extends T> e);

    /**
     * All instances of {@code pre} are replaced with {@code post} and the updated collection is
     * returned.
     *
     * @param pre value to be removed
     * @param post value to be added
     * @return a new collection with all instances of {@code pre} replaced with {@code post} in
     * this collection
     */
    ContainingCollection<T> replace(Object pre, T post);

    /**
     * The first instance of {@code pre} is replaced with {@code post} and the updated collection
     * is returned.
     *
     * @param pre value to be removed
     * @param post value to be added
     * @return a new collection with the first instance of {@code pre} replaced with {@code post}
     * in this collection
     */
    ContainingCollection<T> replaceFirst(Object pre, T post);

    /**
     * Constructs and returns an empty collection.
     *
     * @return an empty collection
     */
    ContainingCollection<T> clear();

    @Override
    boolean contains(Object e);

    /**
     * Return true if the provided value does not exist in this collection.
     * @param e value to be checked for
     * @return true if the provided value does not exist in this collection
     */
    default boolean notContains(Object e) {
        return !contains(e);
    }

    /**
     * Reverses the order of the elements in this collection and returns the updated collection.
     *
     * @return a new collection consisting of the elements of this collection in a reversed order
     */
    Collection<T> reverse();


    /**
     * Creates a {@link Spliterator} of the elements of this collection in a reversed order.
     *
     * @return a {@link Spliterator} of the elements of this collection in a reversed order
     */
    Spliterator<T> reverseSpliterator();

    /**
     * Creates a {@link ListIterator} of the elements of this collection.
     *
     * @return a {@link ListIterator} of the elements of this collection
     */
    ListIterator<T> listIterator();

    /**
     * Creates a {@link ListIterator}
     * @param index
     * @return
     */
    ListIterator<T> listIterator(int index);

    ListIterator<T> listIteratorAtEnd();

    void javaSerialize(Serializer s);

    void javaDeserialize(Deserializer s);
}

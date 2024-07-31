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

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.modelingvalue.collections.impl.DefaultMapImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;

@SuppressWarnings("unused")
public interface DefaultMap<K, V> extends ContainingCollection<Entry<K, V>>, Mergeable<DefaultMap<K, V>> {

    /**
     * Constructs an immutable map with the specified key-value pairs and returns it.
     *
     * @param defaultFunction a {@code SerializableFunction} to use whenever
     *      * {@link DefaultMap#get(Object)} is called with a key that does not exist in the map
     * @param e array of key-value pairs to be added
     * @return the constructed immutable map
     * @throws NullPointerException if null is passed in as a key-value pair
     */
    @SafeVarargs
    static <K, V> DefaultMap<K, V> of(SerializableFunction<K, V> defaultFunction, Entry<K, V>... e) {
        return new DefaultMapImpl<>(e, defaultFunction);
    }

    /**
     * Returns the value that the provided key maps to in this map. If this key does not exist in
     * the map, the key is passed through the {@code defaultFunction} and the result is returned.
     *
     * @param key key to be checked
     * @return the value in the key-value pair with the provided key if it exists,
     * otherwise, the result of passing {@code key} through the {@code defaultFunction}
     */
    V get(K key);

    /**
     * Returns the key-value pair that contains the provided key as its key. If the provided key
     * does not exist in this map, {@code null} is returned.
     *
     * @param key key to be checked
     * @return the key-value pair with the provided key if it exists, otherwise {@code null} is
     * returned
     */
    Entry<K, V> getEntry(K key);


    /**
     * Returns a set of key-value pairs that have equal hashes for their keys.
     *
     * @param key key whose hash is to be checked
     * @return a set of key-value pairs that have equal hashes for their keys
     */
    Set<Entry<K, V>> allWithEqualhash(K key);

    Collection<V> getAll(Set<K> keys);

    DefaultMap<K, V> put(Entry<K, V> entry);

    DefaultMap<K, V> put(K key, V value);

    DefaultMap<K, V> putAll(DefaultMap<? extends K, ? extends V> c);

    DefaultMap<K, V> add(Entry<K, V> entry, BinaryOperator<V> merger);

    DefaultMap<K, V> add(K key, V value, BinaryOperator<V> merger);

    DefaultMap<K, V> addAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger);

    DefaultMap<K, V> remove(Entry<K, V> entry, BinaryOperator<V> merger);

    DefaultMap<K, V> remove(K key, V value, BinaryOperator<V> merger);

    DefaultMap<K, V> removeAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger);

    DefaultMap<K, V> removeKey(K key);

    DefaultMap<K, V> removeAllKey(Collection<?> c);

    DefaultMap<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate);

    <V2> DefaultMap<K, V> removeAllKey(DefaultMap<K, V2> m);

    void deduplicate(DefaultMap<K, V> other);

    Collection<K> toKeys();

    Collection<V> toValues();

    @Override
    DefaultMap<K, V> clear();

    DefaultMap<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, DefaultMap<K, V>[] branches, int length);

    @Override
    DefaultMap<K, V> remove(Object e);

    @Override
    DefaultMap<K, V> removeAll(Collection<?> e);

    @Override
    DefaultMap<K, V> add(Entry<K, V> e);

    @Override
    DefaultMap<K, V> replace(Object pre, Entry<K, V> post);

    @Override
    DefaultMap<K, V> addAll(Collection<? extends Entry<K, V>> es);

    Collection<Entry<K, Pair<V, V>>> diff(DefaultMap<K, V> other);

    SerializableFunction<K, V> defaultFunction();

    void forEach(BiConsumer<K, V> action);
}

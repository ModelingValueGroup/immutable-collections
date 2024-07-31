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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.modelingvalue.collections.impl.StreamCollectionImpl;
import org.modelingvalue.collections.mutable.Mutable;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

@SuppressWarnings("unused")
public interface Collection<T> extends Stream<T>, Iterable<T>, Serializable {

    int                  PARALLELISM     = Math.max(2, Integer.getInteger("PARALLELISM", ForkJoinPool.getCommonPoolParallelism()));

    ThreadLocal<Boolean> SEQUENTIAL_ONLY = ThreadLocal.withInitial(() -> false);

    /**
     * Returns a {@code Runnable} that executes the given runnable within a context where the
     * {@code SEQUENTIAL_ONLY} thread-local is set to {@code true}. Sets {@code SEQUENTIAL_ONLY}
     * back to its original value after.
     *
     * @param runnable runnable to be executed
     * @return the result of the supplier where the {@code SEQUENTIAL_ONLY} thread-local is set to
     * {@code true}.
     */
    static Runnable sequential(Runnable runnable) {
        return () -> {
            boolean old = SEQUENTIAL_ONLY.get();
            SEQUENTIAL_ONLY.set(true);
            try {
                runnable.run();
            } finally {
                SEQUENTIAL_ONLY.set(old);
            }
        };
    }

    /**
     * Executes the {@code Supplier} and returns the result within a context where the
     * {@code SEQUENTIAL_ONLY} thread-local is set to {@code true}. Sets {@code SEQUENTIAL_ONLY}
     * back to its original value after.
     *
     * @param supplier supplier to be called
     * @return the result of the supplier where the {@code SEQUENTIAL_ONLY} thread-local is set to
     * {@code true}.
     */
    static <T> T getSequential(Supplier<T> supplier) {
        boolean old = SEQUENTIAL_ONLY.get();
        SEQUENTIAL_ONLY.set(true);
        try {
            return supplier.get();
        } finally {
            SEQUENTIAL_ONLY.set(old);
        }
    }

    @Override
    Spliterator<T> spliterator();

    @Override
    Iterator<T> iterator();

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     */
    int size();

    /**
     * Returns true if there are no elements in this collection.
     *
     * @return true if there are no elements in this collection
     */
    boolean isEmpty();

    /**
     * Returns true if one of the elements in this collection is equal to {@code e}.
     *
     * @param e element to be checked
     * @return true if this collection contains {@code e}.
     */
    boolean contains(Object e);

    /**
     * Returns this collection with only elements that are instances of the specified type
     *
     * @param type class of the type to filter
     * @return this collection with only the elements that are instances of the specified type
     */
    <F extends T> Collection<F> filter(Class<F> type);

    /**
     * Returns this collection with only elements that are non-null.
     *
     * @return this collection with only the non-null elements
     */
    Collection<T> notNull();

    /**
     * Returns this collection if none of the elements are null.
     *
     * @return {@code this}
     * @throws NullPointerException if any of the elements are null
     */
    Collection<T> requireNonNull();

    @Override
    Collection<T> filter(Predicate<? super T> predicate);

    /**
     * Returns a collection consisting of the elements of this collection that do not match the
     * given {@code Predicate}.
     * @param predicate a predicate to apply to each element to determine if it should be not
     *                  included
     * @return a collection consisting of the elements of this collection that do not match the
     * given predicate.
     */
    default Collection<T> exclude(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    @Override
    <R> Collection<R> map(Function<? super T, ? extends R> mapper);

    @Override
    <R> Collection<R> flatMap(Function<? super T, ? extends java.util.stream.Stream<? extends R>> mapper);

    @Override
    Collection<T> distinct();

    @Override
    Collection<T> sorted();

    /**
     * Returns a collection consisting of the elements of this collection in a random order.
     *
     * @return a collection consisting of the elements of this collection in a random order
     */
    Collection<T> random();

    @Override
    Collection<T> sorted(Comparator<? super T> comparator);

    /**
     * Returns a collection consisting of the elements of this collection sorted in ascending
     * order by the result of passing each element through the given {@code Function}. The output
     * of the {@code Function} must be a subtype of {@link Comparable}.
     *
     * @param by the {@code Function} to pass each element through
     * @return a collection consisting of the elements of this collection sorted in ascending
     * order by the result of passing each element through the given {@code Function}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default <C extends Comparable> Collection<T> sortedBy(Function<T, C> by) {
        return sorted((o1, o2) -> by.apply(o1).compareTo(by.apply(o2)));
    }

    /**
     * Returns a collection consisting of the elements of this collection sorted in descending
     * order by the result of passing each element through the given {@code Function}. The output
     * of the {@code Function} must be a subtype of {@link Comparable}.
     *
     * @param by the {@code Function} to pass each element through
     * @return a collection consisting of the elements of this collection sorted in descending
     * order by the result of passing each element through the given {@code Function}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default <C extends Comparable> Collection<T> sortedByDesc(Function<T, C> by) {
        return sorted((o1, o2) -> by.apply(o2).compareTo(by.apply(o1)));
    }

    @Override
    Collection<T> peek(Consumer<? super T> action);

    @Override
    Collection<T> limit(long maxSize);

    @Override
    Collection<T> skip(long n);

    @Override
    Collection<T> sequential();

    @Override
    Collection<T> parallel();

    @Override
    Collection<T> unordered();

    @Override
    Collection<T> onClose(Runnable closeHandler);

    @Override
    void forEach(Consumer<? super T> action);

    /**
     * Constructs and returns a {@link Set} consisting of the elements of this collection.
     *
     * @return a {@link Set} consisting of the elements of this collection
     */
    default Set<T> asSet() {
        return reduce(Set.of(), Set::add, Set::addAll);
    }

    /**
     * Constructs and returns a {@link List} consisting of the elements of this collection.
     *
     * @return a {@link List} consisting of the elements of this collection
     */
    default List<T> asList() {
        return reduce(List.of(), List::append, List::appendList);
    }

    /**
     * Constructs and returns a {@link Map} where each {@link Entry} in the map is the result of
     * passing each element of this collection through the provided function.
     *
     * @param entry {@code Function} to convert each element of this collection into an entry
     * @return a {@link Map} that consists of the elements of this collection passed through the
     * provided function
     */
    default <K, V> Map<K, V> asMap(Function<T, Entry<K, V>> entry) {
        return reduce(Map.of(), (s, a) -> s.put(entry.apply(a)), Map::putAll);
    }

    /**
     * Constructs and returns a {@link DefaultMap} where each {@link Entry} in the map is the result of
     * passing each element of this collection through the provided function.
     *
     * @param defaultFunction a {@code SerializableFunction} to use whenever
     * {@link DefaultMap#get(Object)} is called with a key that does not exist in the map
     * @param entry {@code Function} to convert each element of this collection into an entry
     * @return a {@link DefaultMap} that consists of the elements of this collection passed through the
     * provided function
     */
    default <K, V> DefaultMap<K, V> asDefaultMap(SerializableFunction<K, V> defaultFunction, Function<T, Entry<K, V>> entry) {
        return reduce(DefaultMap.of(defaultFunction), (s, a) -> s.put(entry.apply(a)), DefaultMap::putAll);
    }

    /**
     * Constructs and returns a {@link QualifiedSet} consisting of the elements of this collection.
     *
     * @param qualifier a {@code SerializableFunction} as the qualifier
     * @return a {@link QualifiedSet} consisting of the elements of this collection
     */
    @SuppressWarnings("unchecked")
    default <K, V> QualifiedSet<K, V> asQualifiedSet(SerializableFunction<V, K> qualifier) {
        return reduce(QualifiedSet.of(qualifier), (s, a) -> s.add((V) a), QualifiedSet::addAll);
    }

    @SuppressWarnings("unchecked")
    default <K, V> QualifiedDefaultSet<K, V> asQualifiedDefaultSet(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction) {
        return reduce(QualifiedDefaultSet.of(qualifier, defaultFunction), (s, a) -> s.add((V) a), QualifiedDefaultSet::addAll);
    }

    /**
     * Constructs and returns a collection consisting of the elements of the provided
     * {@code BaseStream}.
     *
     * @param base stream that will be converted to a collection
     * @return a collection consisting of the elements of the provided stream
     */
    @SuppressWarnings("rawtypes")
    static <T> Collection<T> of(BaseStream<T, ? extends BaseStream> base) {
        return new StreamCollectionImpl<>(base);
    }

    /**
     * Constructs and returns a collection consisting of the elements of the provided
     * {@code Stream}.
     *
     * @param base stream that will be converted to a collection
     * @return a collection consisting of the elements of the provided stream
     */
    static <T> Collection<T> of(Stream<T> base) {
        return base instanceof Collection ? (Collection<T>) base : new StreamCollectionImpl<>(base);
    }

    /**
     * Constructs and returns a collection consisting of the elements of the provided
     * {@code Spliterator}.
     *
     * @param base spliterator that will be converted to a collection
     * @return a collection consisting of the elements of the provided spliterator
     */
    static <T> Collection<T> of(Spliterator<T> base) {
        return new StreamCollectionImpl<>(base);
    }

    /**
     * Constructs and returns a collection consisting of the elements of the provided
     * {@code Iterable}.
     *
     * @param base iterable that will be converted to a collection
     * @return a collection consisting of the elements of the provided iterable
     */
    @SuppressWarnings("unchecked")
    static <T> Collection<T> of(Iterable<T> base) {
        return base instanceof Collection ? (Collection<T>) base : base instanceof Mutable ? ((Mutable<T>) base).toImmutable() : new StreamCollectionImpl<>(base);
    }

    /**
     * Constructs and returns a collection consisting of infinite elements where each element is
     * generated by the provided {@code Supplier}.
     *
     * @param base supplier that will be converted to a collection
     * @return a collection consisting of the elements of the provided iterable
     */
    static <T> Collection<T> of(Supplier<T> base) {
        return new StreamCollectionImpl<>(Stream.generate(base));
    }

    /**
     * Constructs and returns a collection consisting of the provided elements.
     *
     * @param elements array of elements that will be contained in the constructed collection
     * @return a collection consisting of the provided elements.
     */
    @SafeVarargs
    static <T> Collection<T> of(T... elements) {
        return new StreamCollectionImpl<>(Stream.of(elements));
    }

    /**
     * Constructs and returns a collection consisting of integers in the provided range.
     *
     * @param from inclusive start to the range
     * @param to exclusive end to the range
     * @return a collection consisting of integers in the provided range
     */
    static Collection<Integer> range(int from, int to) {
        return of(IntStream.range(from, to));
    }

    /**
     * Constructs and returns a collection consisting of a range of integers from 0 up to but not
     * including the provided size.
     *
     * @param size exclusive end to the range
     * @return a collection consisting of integers in the range from 0 up to but not including size
     */
    static Collection<Integer> range(int size) {
        return range(0, size);
    }

    /**
     * Builds an object of type {@code U} starting with {@code identity}, and iterating through the
     * elements of this collection, replacing the object with the result of calling the
     * {@code BiFunction} on the current object and the next element in this collection.
     *
     * @param identity initial object
     * @param accumulator function to pass previous object and current element in this collection
     *                    to get current object
     * @return an object of type {@code U} that starts with {@code identity} and is passed through
     * a function call with each element in this collection, replacing its object with the result
     * each time
     */
    <U extends Mergeable<U>> U reduce(U identity, BiFunction<U, ? super T, U> accumulator);

    /**
     * Returns a collection with the result of passing each three adjacent elements of this
     * collection through the provided {@code Function}.
     *
     * @param function function that each three adjacent elements in this collection are passed
     *                 through
     * @return a collection with the result of passing each three adjacent elements of this
     *      * collection through the provided function
     */
    <R> Collection<R> linked(TriFunction<T, T, T, R> function);

    /**
     * Passes each three adjacent elements of this collection through the provided
     * {@code Consumer}.
     *
     * @param consumer consumer that each three adjacent elements in this collection are passed
     *                 through
     */
    void linked(TriConsumer<T, T, T> consumer);

    /**
     * Returns a collection with the result of passing each element and its index in this
     * collection through the provided {@code Function}.
     * @param function function that each element and its index in this collection is passed
     *                 through
     * @return a collection with the result of passing each element and its index in this
     * collection through the provided {@code Function}
     */
    <R> Collection<R> indexed(BiFunction<T, Integer, R> function);

    /**
     * Returns a collection consisting of all the elements in the four provided collections in the
     * order they are provided.
     *
     * @return a collection consisting of all the elements in the four provided collections in the
     * order they are provided
     */
    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c, Collection<? extends E> d) {
        return Collection.of(Stream.concat(Stream.concat(Stream.concat(a, b), c), d));
    }

    /**
     * Returns a collection consisting of all the elements in the three provided collections in the
     * order they are provided.
     *
     * @return a collection consisting of all the elements in the three provided collections in the
     * order they are provided
     */
    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c) {
        return Collection.of(Stream.concat(Stream.concat(a, b), c));
    }

    /**
     * Returns a collection consisting of all the elements in the two provided collections in the
     * order they are provided.
     *
     * @return a collection consisting of all the elements in the two provided collections in the
     * order they are provided
     */
    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(a, b));
    }

    /**
     * Returns a collection consisting of all the elements in the provided collection with
     * {@code b} appended to the end.
     *
     * @param a initial collection
     * @param b value to be appended to end of the provided collection
     * @return a collection consisting of the provided collection with {@code b} appended to the
     * end
     */
    static <E> Collection<E> concat(Collection<? extends E> a, E b) {
        return Collection.of(Stream.concat(a, Collection.of(b)));
    }

    /**
     * Returns a collection consisting of {@code a} followed by all the elements in the provided
     * collection.
     *
     * @param a value to be the first in the new collection
     * @param b collection containing the rest of the elements in the new collection
     * @return a collection consisting of {@code a} followed by all the elements in the provided
     * collection
     */
    static <E> Collection<E> concat(E a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(Collection.of(a), b));
    }

}

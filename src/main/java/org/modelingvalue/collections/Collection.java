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

    int PARALLELISM = Math.max(2, Integer.getInteger("PARALLELISM", ForkJoinPool.getCommonPoolParallelism()));

    @Override
    Spliterator<T> spliterator();

    @Override
    Iterator<T> iterator();

    int size();

    boolean isEmpty();

    <F extends T> Collection<F> filter(Class<F> type);

    Collection<T> notNull();

    @Override
    Collection<T> filter(Predicate<? super T> predicate);

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

    Collection<T> random();

    @Override
    Collection<T> sorted(Comparator<? super T> comparator);

    @SuppressWarnings({"rawtypes", "unchecked"})
    default <C extends Comparable> Collection<T> sortedBy(Function<T, C> by) {
        return sorted((o1, o2) -> by.apply(o1).compareTo(by.apply(o2)));
    }

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

    default Set<T> toSet() {
        return reduce(Set.of(), Set::add, Set::addAll);
    }

    default List<T> toList() {
        return reduce(List.of(), List::append, List::appendList);
    }

    default <K, V> Map<K, V> toMap(Function<T, Entry<K, V>> entry) {
        return reduce(Map.of(), (s, a) -> s.put(entry.apply(a)), Map::putAll);
    }

    default <K, V> DefaultMap<K, V> toDefaultMap(SerializableFunction<K, V> defaultFunction, Function<T, Entry<K, V>> entry) {
        return reduce(DefaultMap.of(defaultFunction), (s, a) -> s.put(entry.apply(a)), DefaultMap::putAll);
    }

    @SuppressWarnings("unchecked")
    default <K, V> QualifiedSet<K, V> toQualifiedSet(SerializableFunction<V, K> qualifier) {
        return reduce(QualifiedSet.of(qualifier), (s, a) -> s.add((V) a), QualifiedSet::addAll);
    }

    @SuppressWarnings("unchecked")
    default <K, V> QualifiedDefaultSet<K, V> toQualifiedDefaultSet(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction) {
        return reduce(QualifiedDefaultSet.of(qualifier, defaultFunction), (s, a) -> s.add((V) a), QualifiedDefaultSet::addAll);
    }

    @SuppressWarnings("rawtypes")
    static <T> Collection<T> of(BaseStream<T, ? extends BaseStream> base) {
        return new StreamCollectionImpl<>(base);
    }

    static <T> Collection<T> of(Stream<T> base) {
        return base instanceof Collection ? (Collection<T>) base : new StreamCollectionImpl<>(base);
    }

    static <T> Collection<T> of(Spliterator<T> base) {
        return new StreamCollectionImpl<>(base);
    }

    @SuppressWarnings("unchecked")
    static <T> Collection<T> of(Iterable<T> base) {
        return base instanceof Collection ? (Collection<T>) base : base instanceof Mutable ? ((Mutable<T>) base).toImmutable() : new StreamCollectionImpl<>(base);
    }

    static <T> Collection<T> of(Supplier<T> base) {
        return new StreamCollectionImpl<>(Stream.generate(base));
    }

    @SafeVarargs
    static <T> Collection<T> of(T... elements) {
        return new StreamCollectionImpl<>(Stream.of(elements));
    }

    static Collection<Integer> range(int from, int to) {
        return of(IntStream.range(from, to));
    }

    static Collection<Integer> range(int size) {
        return range(0, size);
    }

    <U extends Mergeable<U>> U reduce(U identity, BiFunction<U, ? super T, U> accumulator);

    <R> Collection<R> linked(TriFunction<T, T, T, R> function);

    void linked(TriConsumer<T, T, T> consumer);

    <R> Collection<R> indexed(BiFunction<T, Integer, R> function);

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c, Collection<? extends E> d) {
        return Collection.of(Stream.concat(Stream.concat(Stream.concat(a, b), c), d));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c) {
        return Collection.of(Stream.concat(Stream.concat(a, b), c));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(a, b));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, E b) {
        return Collection.of(Stream.concat(a, Collection.of(b)));
    }

    static <E> Collection<E> concat(E a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(Collection.of(a), b));
    }

}

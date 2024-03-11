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

package org.modelingvalue.collections.impl;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.StreamCollection;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

@SuppressWarnings("serial")
public final class StreamCollectionImpl<T> extends CollectionImpl<T> implements StreamCollection<T> {

    private final Stream<T> stream;

    protected StreamCollectionImpl(Stream<T> stream) {
        this.stream = runParallel() ? stream : stream.sequential();
    }

    @SuppressWarnings("rawtypes")
    public StreamCollectionImpl(BaseStream<T, ? extends BaseStream> stream) {
        this(stream.spliterator(), stream.isParallel());
    }

    public StreamCollectionImpl(Spliterator<T> spliterator) {
        this(spliterator, runParallel());
    }

    public StreamCollectionImpl(Spliterator<T> spliterator, boolean parallel) {
        this(StreamSupport.stream(spliterator, parallel));
    }

    public StreamCollectionImpl(Iterable<T> it) {
        this(it.spliterator(), runParallel());
    }

    @Override
    protected Stream<T> baseStream() {
        return stream;
    }

    @Override
    public int size() {
        return (int) stream.count();
    }

    @Override
    public boolean isEmpty() {
        return stream.findAny().isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        //noinspection SuspiciousToArrayCall
        return stream.toArray(generator);
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    @Override
    public <R> Collection<R> linked(TriFunction<T, T, T, R> function) {
        return asList().linked(function);
    }

    @Override
    public void linked(TriConsumer<T, T, T> consumer) {
        asList().linked(consumer);
    }

    @Override
    public <R> Collection<R> indexed(BiFunction<T, Integer, R> function) {
        return asList().indexed(function);
    }

    @Override
    public boolean contains(Object e) {
        return anyMatch(e::equals);
    }
}

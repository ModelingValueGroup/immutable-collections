//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2019 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

package org.modelingvalue.collections.impl;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.*;
import org.modelingvalue.collections.util.*;

public class MapImpl<K, V> extends HashCollectionImpl<Entry<K, V>> implements Map<K, V> {

    private static final long serialVersionUID = 7758359458143777562L;

    @SuppressWarnings("rawtypes")
    private static final Function<Entry, Object> KEY   = Entry::getKey;
    @SuppressWarnings("rawtypes")
    public static final  Map                     EMPTY = new MapImpl((Object) null);

    public MapImpl(Entry<K, V>[] entries) {
        this.value = entries.length == 1 ? entries[0] : putAll(null, key(), entries);
    }

    protected MapImpl(Object value) {
        this.value = value;
    }

    public MapImpl(Deserializer s) {
        javaDeserialize(s);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Function<Entry<K, V>, Object> key() {
        return (Function) KEY;
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return new DistinctCollectionSpliterator<>(value, 0, length(value), size(value), false);
    }

    @Override
    public Spliterator<Entry<K, V>> reverseSpliterator() {
        return new DistinctCollectionSpliterator<>(value, 0, length(value), size(value), true);
    }

    @Override
    public Map<K, V> put(K key, V val) {
        return put(Entry.of(key, val));
    }

    @Override
    public Map<K, V> put(Entry<K, V> entry) {
        return create(put(value, key(), entry, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> putAll(Map<? extends K, ? extends V> c) {
        return create(put(value, key(), ((MapImpl) c).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deduplicate(Map<K, V> other) {
        deduplicate(value, key(), ((MapImpl) other).value, key());
    }

    @Override
    public Map<K, V> removeKey(K key) {
        return create(remove(value, key(), key, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> removeAllKey(Collection<?> c) {
        return create(remove(value, key(), ((SetImpl) c.toSet()).value, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> Map<K, V> removeAllKey(Map<K, X> m) {
        return create(remove(value, key(), ((MapImpl) m).value, key()));
    }

    @Override
    public Map<K, V> add(K key, V val, BinaryOperator<V> merger) {
        return add(Entry.of(key, val), merger);
    }

    @Override
    public Map<K, V> add(Entry<K, V> entry, BinaryOperator<V> merger) {
        return create(add(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> addAll(Map<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(add(value, key(), ((MapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @Override
    public Map<K, V> add(Entry<K, V> entry) {
        return add(entry, (a, b) -> Mergeables.merge(null, a, b));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map<K, V> addAll(Collection<? extends Entry<K, V>> es) {
        return addAll(es instanceof Map ? (Map) es : es.toMap(e -> e), (a, b) -> Mergeables.merge(null, a, b));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Map<K, V> remove(Object e) {
        return e instanceof Entry ? remove((Entry) e, (a, b) -> Mergeables.merge(b, null, a)) : this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Map<K, V> removeAll(Collection<?> es) {
        return removeAll(es instanceof Map ? (Map) es : es.map(e -> e instanceof Entry ? (Entry) e : null).notNull().toMap(e -> e), //
                (a, b) -> Mergeables.merge(b, null, a));
    }

    @Override
    public Map<K, V> remove(K key, V val, BinaryOperator<V> merger) {
        return remove(Entry.of(key, val), merger);
    }

    @Override
    public Map<K, V> remove(Entry<K, V> entry, BinaryOperator<V> merger) {
        return create(remove(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> removeAll(Map<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(remove(value, key(), ((MapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    protected Object mergeEntry(Map<K, V> map1, Map<K, V> map2, BinaryOperator<V> merger) {
        return ((MapImpl<K, V>) map1.toMap(e1 -> {
            Entry<K, V> e2 = map2.getEntry(e1.getKey());
            V val = merger.apply(e1.getValue(), e2.getValue());
            return Objects.equals(val, e1.getValue()) ? e1 : Objects.equals(val, e2.getValue()) ? e2 : Entry.of(e1.getKey(), val);
        })).value;
    }

    @Override
    public Map<K, V> merge(Map<K, V>[] branches, int length) {
        return merge((k, v, vs, l) -> Mergeables.merge(v, vs, l), branches, length);
    }

    @Override
    public Map<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, Map<K, V>[] branches, int length) {
        return create(visit((a, l) -> {
            Object r = a[0];
            for (int i = 1; i < l; i++) {
                if (!Objects.equals(a[i], a[0]) && !Objects.equals(a[i], r)) {
                    if (!Objects.equals(a[0], r)) {
                        return merge(merger, a, l);
                    } else {
                        r = a[i];
                    }
                }
            }
            return r;
        }, branches, length));
    }

    @SuppressWarnings("unchecked")
    private Object merge(QuadFunction<K, V, V[], Integer, V> merger, Object[] es, int el) {
        K key = es[0] != null ? ((Entry<K, V>) es[0]).getKey() : null;
        V v = key != null ? ((Entry<K, V>) es[0]).getValue() : null;
        V[] vs = null;
        for (int i = 1; i < el; i++) {
            if (es[i] != null) {
                if (key == null) {
                    key = ((Entry<K, V>) es[i]).getKey();
                }
                vs = ArrayUtil.set(vs, i - 1, ((Entry<K, V>) es[i]).getValue(), el - 1);
            }
        }
        V result = merger.apply(key, v, vs, el - 1);
        if (result == null) {
            return null;
        } else {
            for (int i = 0; i < el; i++) {
                if (es[i] != null && Objects.equals(result, ((Entry<K, V>) es[i]).getValue())) {
                    return es[i];
                }
            }
            return Entry.of(key, result);
        }
    }

    @Override
    public Collection<Entry<K, Pair<V, V>>> diff(Map<K, V> toCompare) {
        return compare(toCompare).flatMap(a -> {
            if (a[0] == null) {
                return a[1].map(e -> Entry.of(e.getKey(), Pair.of(null, a[1].get(e.getKey()))));
            } else if (a[1] == null) {
                return a[0].map(e -> Entry.of(e.getKey(), Pair.of(a[0].get(e.getKey()), null)));
            } else {
                return a[0].toKeys().toSet().addAll(a[1].toKeys()).map(k -> Entry.of(k, Pair.of(a[0].get(k), a[1].get(k))));
            }
        });
    }

    @Override
    public Collection<K> toKeys() {
        return map(Entry::getKey);
    }

    @Override
    public Collection<V> toValues() {
        return map(Entry::getValue);
    }

    @Override
    public V get(K key) {
        Entry<K, V> result = getEntry(key);
        return result != null ? result.getValue() : null;
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return get(value, key(), key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<V> getAll(Set<K> keys) {
        return create(retain(value, key(), ((SetImpl) keys).value, identity())).map(Entry::getValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MapImpl<K, V> create(Object val) {
        return val != value ? (val == null ? (MapImpl<K, V>) EMPTY : new MapImpl<>(val)) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> getMerger() {
        return EMPTY;
    }

    @Override
    public Map<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate) {
        return filter(e -> keyPredicate.test(e.getKey()) && valuePredicate.test(e.getValue())).toMap(Function.identity());
    }

    @Override
    public void forEach(BiConsumer<K, V> action) {
        forEach(e -> action.accept(e.getKey(), e.getValue()));
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Serializer.wrap(s, this::javaSerialize);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Deserializer.wrap(s, this::javaDeserialize);
    }

    @SuppressWarnings("unused")
    private void serialize(Serializer s) {
        s.writeInt(size());
        for (Entry<K, V> e : this) {
            s.writeObject(e);
        }
    }

    @SuppressWarnings({"unchecked", "unused", "rawtypes"})
    private static <K, V> MapImpl<K, V> deserialize(Deserializer s) {
        Entry[] entries = s.readArray(new Entry[]{});
        //Collection.of(stream).toMap(e->e);
        return entries.length == 0 ? (MapImpl<K, V>) MapImpl.EMPTY : new MapImpl<K, V>(entries);
    }
}

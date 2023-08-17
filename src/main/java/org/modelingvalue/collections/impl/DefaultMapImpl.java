//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2023 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.ArrayUtil;
import org.modelingvalue.collections.util.Deserializer;
import org.modelingvalue.collections.util.Mergeables;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.Serializer;

public class DefaultMapImpl<K, V> extends HashCollectionImpl<Entry<K, V>> implements DefaultMap<K, V> {
    private static final long                    serialVersionUID = 2424304733060404412L;
    @SuppressWarnings("rawtypes")
    private static final Function<Entry, Object> KEY              = Entry::getKey;

    private SerializableFunction<K, V>           defaultFunction;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DefaultMapImpl(Entry[] es, SerializableFunction<K, V> defaultFunction) {
        this.value = es.length == 1 ? es[0] : putAll(null, key(), es);
        this.defaultFunction = defaultFunction.of();
    }

    protected DefaultMapImpl(Object value, SerializableFunction<K, V> defaultFunction) {
        this.value = value;
        this.defaultFunction = defaultFunction.of();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ defaultFunction.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && defaultFunction.equals(((DefaultMapImpl<?, ?>) obj).defaultFunction);
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
    public DefaultMap<K, V> put(K key, V val) {
        return Objects.equals(val, defaultFunction.apply(key)) ? removeKey(key) : create(put(value, key(), Entry.of(key, val), key()));
    }

    @Override
    public DefaultMap<K, V> put(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : create(put(value, key(), entry, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> putAll(DefaultMap<? extends K, ? extends V> c) {
        return create(put(value, key(), ((DefaultMapImpl) c).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deduplicate(DefaultMap<K, V> other) {
        deduplicate(value, key(), ((DefaultMapImpl) other).value, key());
    }

    @Override
    public DefaultMap<K, V> removeKey(K key) {
        return create(remove(value, key(), key, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> removeAllKey(Collection<?> c) {
        return create(remove(value, key(), ((SetImpl) c.asSet()).value, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> DefaultMap<K, V> removeAllKey(DefaultMap<K, X> m) {
        return create(remove(value, key(), ((DefaultMapImpl) m).value, key()));
    }

    @Override
    public DefaultMap<K, V> add(K key, V val, BinaryOperator<V> merger) {
        return Objects.equals(val, defaultFunction.apply(key)) ? removeKey(key) : //
                create(add(value, key(), Entry.of(key, val), key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @Override
    public DefaultMap<K, V> add(Entry<K, V> entry, BinaryOperator<V> merger) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : //
                create(add(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> addAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(add(value, key(), ((DefaultMapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @Override
    public DefaultMap<K, V> add(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : put(entry);
    }

    @Override
    public DefaultMap<K, V> replace(Object pre, Entry<K, V> post) {
        DefaultMap<K, V> rem = remove(pre);
        return rem != this ? rem.add(post) : this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DefaultMap<K, V> addAll(Collection<? extends Entry<K, V>> es) {
        return putAll(es instanceof DefaultMap ? (DefaultMap) es : es.asDefaultMap(defaultFunction, e -> e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultMap<K, V> remove(Object e) {
        return e instanceof Entry ? removeKey(((Entry<K, V>) e).getKey()) : this;
    }

    @SuppressWarnings("resource")
    @Override
    public DefaultMap<K, V> removeAll(Collection<?> e) {
        DefaultMap<K, V> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

    @Override
    public DefaultMap<K, V> remove(K key, V val, BinaryOperator<V> merger) {
        return remove(Entry.of(key, val), merger);
    }

    @Override
    public DefaultMap<K, V> remove(Entry<K, V> entry, BinaryOperator<V> merger) {
        return create(remove(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> removeAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(remove(value, key(), ((DefaultMapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    private Object mergeEntry(DefaultMap<K, V> map1, DefaultMap<K, V> map2, BinaryOperator<V> merger) {
        return ((DefaultMapImpl<K, V>) map1.map(e1 -> {
            Entry<K, V> e2 = map2.getEntry(e1.getKey());
            V val = merger.apply(e1.getValue(), e2.getValue());
            return Objects.equals(val, defaultFunction.apply(e1.getKey())) ? null : //
                    Objects.equals(val, e1.getValue()) ? e1 : Objects.equals(val, e2.getValue()) ? e2 : Entry.of(e1.getKey(), val);
        }).notNull().asDefaultMap(defaultFunction, e -> e)).value;
    }

    @Override
    public DefaultMap<K, V> merge(DefaultMap<K, V>[] branches, int length) {
        return merge((k, v, vs, l) -> Mergeables.merge(v, vs, l), branches, length);
    }

    @Override
    public DefaultMap<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, DefaultMap<K, V>[] branches, int length) {
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
        V def = key != null ? defaultFunction.apply(key) : null;
        V v = es[0] != null ? ((Entry<K, V>) es[0]).getValue() : def;
        V[] vs = null;
        V b;
        boolean noKey;
        for (int i = 1; i < el; i++) {
            if (es[i] != null) {
                noKey = key == null;
                if (noKey) {
                    key = ((Entry<K, V>) es[i]).getKey();
                    def = defaultFunction.apply(key);
                    v = def;
                }
                b = ((Entry<K, V>) es[i]).getValue();
                if (def != null && noKey) {
                    for (int ii = 0; ii < i - 1; ii++) {
                        vs = ArrayUtil.set(vs, ii, def, el - 1);
                    }
                }
                if (b != null) {
                    vs = ArrayUtil.set(vs, i - 1, b, el - 1);
                }
            } else if (def != null) {
                vs = ArrayUtil.set(vs, i - 1, def, el - 1);
            }
        }
        V result = merger.apply(key, v, vs, el - 1);
        if (Objects.equals(result, def)) {
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
    public Collection<Entry<K, Pair<V, V>>> diff(DefaultMap<K, V> toCompare) {
        return compare(toCompare).flatMap(a -> {
            if (a[0] == null) {
                return a[1].map(e -> Entry.of(e.getKey(), Pair.of(defaultFunction.apply(e.getKey()), a[1].get(e.getKey()))));
            } else if (a[1] == null) {
                return a[0].map(e -> Entry.of(e.getKey(), Pair.of(a[0].get(e.getKey()), defaultFunction.apply(e.getKey()))));
            } else {
                return a[0].toKeys().asSet().addAll(a[1].toKeys()).map(k -> Entry.of(k, Pair.of(a[0].get(k), a[1].get(k))));
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
        return result != null ? result.getValue() : defaultFunction.apply(key);
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

    @Override
    protected DefaultMapImpl<K, V> create(Object val) {
        return val != value ? new DefaultMapImpl<>(val, defaultFunction) : this;
    }

    @Override
    public DefaultMap<K, V> getMerger() {
        return create(null);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<DefaultMap> getMeetClass() {
        return DefaultMap.class;
    }

    @Override
    public DefaultMap<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate) {
        return filter(e -> keyPredicate.test(e.getKey()) && valuePredicate.test(e.getValue())).asDefaultMap(defaultFunction, Function.identity());
    }

    @Override
    public SerializableFunction<K, V> defaultFunction() {
        return defaultFunction;
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

    @Override
    @SuppressWarnings("unused")
    public void javaSerialize(Serializer s) {
        s.writeObject(defaultFunction.original());
        super.javaSerialize(s);
    }

    @Override
    @SuppressWarnings({"unused", "unchecked"})
    public void javaDeserialize(Deserializer s) {
        defaultFunction = ((SerializableFunction<K, V>) s.readObject()).of();
        super.javaDeserialize(s);
    }

    @SuppressWarnings("unused")
    private void serialize(Serializer s) {
        s.writeObject(defaultFunction.original());
        s.writeInt(size());
        for (Entry<K, V> e : this) {
            s.writeObject(e);
        }
    }

    @SuppressWarnings({"unchecked", "unused"})
    private static <K, V> DefaultMapImpl<K, V> deserialize(Deserializer s) {
        SerializableFunction<K, V> defaultFunction = ((SerializableFunction<K, V>) s.readObject()).of();
        Entry<K, V>[] entries = s.readArray(new Entry[]{});
        return new DefaultMapImpl<>(entries, defaultFunction);
    }

    @Override
    public DefaultMap<K, V> clear() {
        return create(null);
    }

}

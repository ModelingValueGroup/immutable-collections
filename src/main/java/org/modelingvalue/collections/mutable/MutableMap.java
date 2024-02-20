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

package org.modelingvalue.collections.mutable;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;

public class MutableMap<K, V> extends AbstractMap<K, V> implements Mutable<Entry<K, V>> {

    private Map<K, V> map;

    public MutableMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public Map<K, V> toImmutable() {
        return map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey((K) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.toValues().anyMatch(v -> Objects.equals(v, value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return map.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        V pre = map.get(key);
        map = map.put(key, value);
        return pre;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        V pre = map.get((K) key);
        map = map.remove(key);
        return pre;
    }

    @Override
    public void putAll(java.util.Map<? extends K, ? extends V> m) {
        map = map.putAll(Map.fromMutable(m));
    }

    @Override
    public void clear() {
        map = map.clear();
    }

    @Override
    public java.util.Set<K> keySet() {
        return new java.util.Set<K>() {

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean contains(Object o) {
                return map.containsKey((K) o);
            }

            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    private final Iterator<org.modelingvalue.collections.Entry<K, V>> it   = map.iterator();
                    private org.modelingvalue.collections.Entry<K, V>                 last = null;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public K next() {
                        last = it.next();
                        return last.getKey();
                    }

                    @Override
                    public void remove() {
                        map = map.remove(last);
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return map.toKeys().toArray();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                return map.toKeys().toArray(i -> (T[]) Array.newInstance(a.getClass().getComponentType(), i));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean containsAll(java.util.Collection<?> c) {
                return Stream.of(c).map(k -> (K) k).allMatch(map::containsKey);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean remove(Object o) {
                Map<K, V> pre = map;
                map = map.removeKey((K) o);
                return pre != map;
            }

            @Override
            public boolean retainAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAllKey(map.toKeys().exclude(c::contains));
                return pre != map;
            }

            @Override
            public boolean removeAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAllKey(Collection.of(c));
                return pre != map;
            }

            @Override
            public void clear() {
                map = map.clear();
            }

            @Override
            public boolean add(K e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(java.util.Collection<? extends K> c) {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public java.util.Collection<V> values() {
        return new java.util.Collection<V>() {

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return map.toValues().anyMatch(v -> Objects.equals(v, o));
            }

            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    private final Iterator<org.modelingvalue.collections.Entry<K, V>> it   = map.iterator();
                    private org.modelingvalue.collections.Entry<K, V>                 last = null;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public V next() {
                        last = it.next();
                        return last.getValue();
                    }

                    @Override
                    public void remove() {
                        map = map.remove(last);
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return map.toValues().toArray();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                return map.toValues().toArray(i -> (T[]) Array.newInstance(a.getClass().getComponentType(), i));
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean containsAll(java.util.Collection<?> c) {
                return Stream.of(c).allMatch(v -> map.anyMatch(e -> Objects.equals(e.getValue(), v)));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean remove(Object o) {
                Optional<org.modelingvalue.collections.Entry<K, V>> found = map.filter(e -> Objects.equals(e.getValue(), o)).findFirst();
                if (found.isPresent()) {
                    map = map.remove(found.get());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean retainAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAll(map.exclude(e -> c.contains(e.getValue())));
                return pre != map;
            }

            @Override
            public boolean removeAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAll(map.filter(e -> c.contains(e.getValue())));
                return pre != map;
            }

            @Override
            public void clear() {
                map = map.clear();
            }

            @Override
            public boolean add(V e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(java.util.Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
        return new java.util.Set<java.util.Map.Entry<K, V>>() {

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return map.contains(o);
            }

            @Override
            public Iterator<java.util.Map.Entry<K, V>> iterator() {
                return new Iterator<java.util.Map.Entry<K, V>>() {
                    private final Iterator<org.modelingvalue.collections.Entry<K, V>> it   = map.iterator();
                    private org.modelingvalue.collections.Entry<K, V>                 last = null;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public java.util.Map.Entry<K, V> next() {
                        last = it.next();
                        return new EntryImpl(last.getKey(), last.getValue());
                    }

                    @Override
                    public void remove() {
                        map = map.remove(last);
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return map.map(null).toArray();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                return map.map(e -> new EntryImpl(e.getKey(), e.getValue())).toArray(i -> (T[]) Array.newInstance(a.getClass().getComponentType(), i));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean remove(Object o) {
                if (o instanceof java.util.Map.Entry) {
                    java.util.Map.Entry<K, V> entry = (java.util.Map.Entry<K, V>) o;
                    Map<K, V> pre = map;
                    map = map.remove(org.modelingvalue.collections.Entry.of(entry.getKey(), entry.getValue()));
                    return pre != map;
                } else {
                    return false;
                }
            }

            @Override
            public boolean containsAll(java.util.Collection<?> c) {
                return Stream.of(c).allMatch(map::contains);
            }

            @Override
            public boolean retainAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAll(map.exclude(e -> c.contains(new EntryImpl(e.getKey(), e.getValue()))));
                return pre != map;
            }

            @SuppressWarnings("rawtypes")
            @Override
            public boolean removeAll(java.util.Collection<?> c) {
                Map<K, V> pre = map;
                map = map.removeAll(Collection.of(c).map(e -> e instanceof java.util.Map.Entry ? org.modelingvalue.collections.Entry.of(((java.util.Map.Entry) e).getKey(), ((java.util.Map.Entry) e).getValue()) : null).notNull());
                return pre != map;
            }

            @Override
            public void clear() {
                map = map.clear();
            }

            @Override
            public boolean add(java.util.Map.Entry<K, V> e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(java.util.Collection<? extends java.util.Map.Entry<K, V>> c) {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private final class EntryImpl extends java.util.AbstractMap.SimpleEntry<K, V> {
        private static final long serialVersionUID = 1L;

        private EntryImpl(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V value) {
            V pre = super.setValue(value);
            map = map.put(getKey(), value);
            return pre;
        }
    }
}

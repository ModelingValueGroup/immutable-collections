//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2026 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

import java.io.Serial;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Graph;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.StreamCollection;
import org.modelingvalue.collections.util.*;

/**
 * <p>
 * An implementation of the {@link Graph} interface. This implementation does not permit
 * {@code null} vertices or edge weights.
 * </p>
 * <p>
 * This implementation provides constant-time performance for the basic operations (putEdge,
 * removeEdge, and containsEdge).
 * </p>
 * 
 * @param <V>
 *            the type of vertices in this graph
 * @param <E>
 *            the type of edge weights in this graph
 * @implNote The graph is stored as a map where each vertex is mapped to a {@link Quadruple} that
 *           contains four maps that represents the incoming and outgoing edges for the vertex each in two different ways.
 *           (incoming - maps an edge weight to all the vertices that have an edge with that edge weight to
 */
public class GraphImpl<V, E> extends CollectionImpl<Triple<V, E, V>> implements Graph<V, E> {

    private static final SerializableFunction<Object, Set<Object>>                                                            EMPTY_SET_FUNCTION         = new SerializableFunction.SerializableFunctionImpl<>(i -> Set.of());
    private static final SerializableFunction<Object, Pair<DefaultMap<Object, Set<Object>>, DefaultMap<Object, Set<Object>>>> EMPTY_DEFAULT_MAP_FUNCTION = new SerializableFunction.SerializableFunctionImpl<>(k -> Pair.of(DefaultMap.of(EMPTY_SET_FUNCTION), DefaultMap.of(EMPTY_SET_FUNCTION)));
    private static final DefaultMap<Object, Pair<DefaultMap<Object, Set<Object>>, DefaultMap<Object, Set<Object>>>>           EMPTY_DEFAULT_MAP          = DefaultMap.of(EMPTY_DEFAULT_MAP_FUNCTION);
    @SuppressWarnings("rawtypes")
    public static final Graph                                                                                                 EMPTY                      = new GraphImpl();
    @Serial
    private static final long                                                                                                 serialVersionUID           = 4576688697509607005L;

    protected DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>                                               outgoing;
    protected DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>                                               incoming;

    /**
     * Constructs an empty immutable directed graph.
     */
    @SuppressWarnings("unchecked")
    public GraphImpl() {
        this.outgoing = (DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>) (Object) EMPTY_DEFAULT_MAP;
        this.incoming = (DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>) (Object) EMPTY_DEFAULT_MAP;
    }

    /**
     * Constructs an immutable directed graph with the specified directed edges.
     *
     * @param edges
     *            array of edges, each represented by a {@link Triple} structured as (source
     *            vertex, edge weight, destination vertex)
     * @throws NullPointerException
     *             if any of the edges are null
     */
    public GraphImpl(Triple<V, E, V>[] edges) {
        this();

        for (Triple<V, E, V> edge : edges) {
            GraphImpl<V, E> next = this.putEdge(edge.a(), edge.c(), edge.b());
            this.outgoing = next.outgoing;
            this.incoming = next.incoming;
        }
    }

    @SuppressWarnings("unchecked")
    private DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> getReversedMap(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> graph) {
        DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> reversed = (DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>) (Object) EMPTY_DEFAULT_MAP;

        for (var outer : graph) {
            for (var inner : outer.getValue().a()) {
                for (var val : inner.getValue()) {
                    V src = outer.getKey(), dst = inner.getKey();
                    var maps = reversed.get(dst);
                    var ve = maps.a();
                    var ev = maps.b();

                    ve = ve.put(src, ve.get(src).add(val));
                    ev = ev.put(val, ev.get(val).add(src));
                    reversed = reversed.put(dst, Pair.of(ve, ev));
                }
            }
        }

        return reversed;
    }

    @SuppressWarnings("unchecked")
    protected GraphImpl(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> outgoing, DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> incoming) {
        if (incoming == null && outgoing == null) {
            this.incoming = (DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>) (Object) EMPTY_DEFAULT_MAP;
            this.outgoing = (DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>) (Object) EMPTY_DEFAULT_MAP;
        } else if (incoming == null) {
            this.incoming = getReversedMap(outgoing);
            this.outgoing = outgoing;
        } else if (outgoing == null) {
            this.incoming = incoming;
            this.outgoing = getReversedMap(incoming);
        } else {
            this.incoming = incoming;
            this.outgoing = outgoing;
        }
    }

    @Override
    public Graph<V, E> removeNode(V node) {
        if (!containsNode(node))
            return this;

        return new GraphImpl<>(removeNodeHelper(outgoing, incoming, node).removeKey(node), removeNodeHelper(incoming, outgoing, node).removeKey(node));
    }

    private static <V, E> DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> removeNodeHelper(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> map, DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> reference, V node) {
        for (var entry : reference.get(node).a()) {
            var other = map.get(entry.getKey());
            var ve = other.a();
            var ev = other.b();

            ve = ve.removeKey(node);

            for (E val : entry.getValue()) {
                ev = ev.put(val, ev.get(val).remove(node));
                if (ev.get(val).isEmpty()) {
                    ev = ev.removeKey(val);
                }
            }

            map = map.put(entry.getKey(), Pair.of(ve, ev));
        }

        return map;
    }

    @Override
    public boolean containsNode(V node) {
        return node != null && (outgoing.getEntry(node) != null || incoming.getEntry(node) != null);
    }

    @Override
    public Set<V> getNodes() {
        return outgoing.toKeys().asSet().addAll(incoming.toKeys());
    }

    @Override
    public GraphImpl<V, E> putEdge(V src, V dst, E val) {
        if (src == null || dst == null || val == null || containsEdge(src, dst, val))
            return this;

        var newIncoming = incoming;
        var newOutgoing = outgoing;

        var srcPairOutgoing = newOutgoing.get(src);
        var dstPairIncoming = newIncoming.get(dst);

        newOutgoing = newOutgoing.put(src, Pair.of(putEdgeHelperVE(srcPairOutgoing.a(), dst, val), putEdgeHelperEV(srcPairOutgoing.b(), dst, val)));
        newIncoming = newIncoming.put(dst, Pair.of(putEdgeHelperVE(dstPairIncoming.a(), src, val), putEdgeHelperEV(dstPairIncoming.b(), src, val)));

        return new GraphImpl<>(newOutgoing, newIncoming);
    }

    private static <V, E> DefaultMap<V, Set<E>> putEdgeHelperVE(DefaultMap<V, Set<E>> map, V node, E val) {
        return map.put(node, map.get(node).add(val));
    }

    private static <V, E> DefaultMap<E, Set<V>> putEdgeHelperEV(DefaultMap<E, Set<V>> map, V node, E val) {
        return map.put(val, map.get(val).add(node));
    }

    @Override
    public Set<E> getEdges(V src, V dst) {
        if (!containsNode(src) || !containsNode(dst))
            return null;
        return outgoing.get(src).a().get(dst);
    }

    @Override
    public boolean containsEdge(V src, V dst, E val) {
        return src != null && dst != null && val != null && outgoing.getEntry(src) != null && outgoing.get(src).b().getEntry(val) != null && outgoing.get(src).b().get(val).contains(dst);
    }

    @Override
    public Graph<V, E> removeEdge(V src, V dst, E val) {
        if (!containsEdge(src, dst, val))
            return this;

        return new GraphImpl<>(removeEdgeHelper(outgoing, src, dst, val), removeEdgeHelper(incoming, dst, src, val));
    }

    private static <V, E> DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> removeEdgeHelper(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> graph, V src, V dst, E val) {
        var pair = graph.get(src);

        var ve = pair.a();
        ve = ve.put(dst, ve.get(dst).remove(val));
        if (ve.get(dst).isEmpty())
            ve = ve.removeKey(dst);

        if (ve.isEmpty())
            return graph.removeKey(src);

        var ev = pair.b();
        ev = ev.put(val, ev.get(val).remove(dst));
        if (ev.get(val).isEmpty())
            ev = ev.removeKey(val);

        if (ev.isEmpty())
            return graph.removeKey(src);

        pair = Pair.of(ve, ev);
        return graph.put(src, pair);
    }

    @Override
    public Graph<V, E> removeEdges(V src, V dst) {
        if (src == null || dst == null || outgoing.getEntry(src) == null || !outgoing.get(src).a().containsKey(dst))
            return this;

        return new GraphImpl<>(removeEdgesHelper(outgoing, src, dst), removeEdgesHelper(incoming, dst, src));
    }

    private static <V, E> DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> removeEdgesHelper(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> graph, V src, V dst) {
        var pair = graph.get(src);

        var ve = pair.a();
        ve = ve.removeKey(dst);

        if (ve.isEmpty()) {
            return graph.removeKey(src);
        }

        var ev = pair.b();
        for (var entry : ev) {
            ev = ev.put(entry.getKey(), entry.getValue().remove(dst));
            if (ev.get(entry.getKey()).isEmpty()) {
                ev = ev.removeKey(entry.getKey());
            }
        }

        pair = Pair.of(ve, ev);
        return graph.put(src, pair);
    }

    @Override
    public DefaultMap<E, Set<V>> getIncoming(V node) {
        if (!containsNode(node))
            return null;
        return incoming.get(node).b();
    }

    @Override
    public Set<V> getIncoming(V node, E val) {
        if (val == null || !containsNode(node))
            return null;
        return incoming.get(node).b().get(val);
    }

    @Override
    public DefaultMap<E, Set<V>> getOutgoing(V node) {
        if (!containsNode(node))
            return null;
        return outgoing.get(node).b();
    }

    @Override
    public Set<V> getOutgoing(V node, E val) {
        if (val == null || !containsNode(node))
            return null;
        return outgoing.get(node).b().get(val);
    }

    @Override
    public Set<E> getIncomingEdges(V node) {
        if (!containsNode(node))
            return null;
        return incoming.get(node).b().toKeys().asSet();
    }

    @Override
    public Set<E> getOutgoingEdges(V node) {
        if (!containsNode(node))
            return null;
        return outgoing.get(node).b().toKeys().asSet();
    }

    @Override
    public Set<V> getIncomingNodes(V node) {
        if (!containsNode(node))
            return null;
        return incoming.get(node).a().toKeys().asSet();
    }

    @Override
    public Set<V> getOutgoingNodes(V node) {
        if (!containsNode(node))
            return null;
        return outgoing.get(node).a().toKeys().asSet();
    }

    @Override
    public Graph<V, E> inverted() {
        if (isEmpty())
            return this;
        return new GraphImpl<>(incoming, outgoing);
    }

    @Override
    public int size() {
        return this.outgoing.flatMap(a -> a.getValue().b().toValues()).mapToInt(Collection::size).sum();
    }

    @Override
    public int hashCode() {
        return outgoing.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        @SuppressWarnings("unchecked")
        GraphImpl<V, E> other = (GraphImpl<V, E>) obj;

        if (!this.outgoing.equals(other.outgoing))
            return false;

        if (Age.age(this.outgoing) > Age.age(other.outgoing)) {
            other.outgoing = this.outgoing;
        } else {
            this.outgoing = other.outgoing;
        }

        if (Age.age(this.incoming) > Age.age(other.incoming)) {
            other.incoming = this.incoming;
        } else {
            this.incoming = other.incoming;
        }

        return true;
    }

    @Override
    protected Stream<Triple<V, E, V>> baseStream() {
        return outgoing.flatMap(s -> s.getValue().b().flatMap(e -> e.getValue().map(t -> Triple.of(s.getKey(), e.getKey(), t))));
    }

    @Override
    public Spliterator<Triple<V, E, V>> spliterator() {
        return baseStream().spliterator();
    }

    @Override
    public Iterator<Triple<V, E, V>> iterator() {
        return baseStream().iterator();
    }

    @Override
    public boolean isEmpty() {
        return outgoing.isEmpty();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean contains(Object e) {
        if (!(e instanceof Triple triple))
            return false;

        return containsEdge((V) triple.a(), (V) triple.c(), (E) triple.b());
    }

    @Override
    public <R> Collection<R> linked(TriFunction<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>, R> function) {
        List<R> list = List.of();
        Triple<V, E, V> last1 = null, last2 = null;

        for (var s : outgoing) {
            for (var e : s.getValue().b()) {
                for (var t : e.getValue()) {
                    var curr = Triple.of(s.getKey(), e.getKey(), t);

                    if (last1 != null) {
                        list = list.add(function.apply(last1, last2, curr));
                    }

                    last1 = last2;
                    last2 = curr;
                }
            }
        }

        return list;
    }

    @Override
    public void linked(TriConsumer<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>> consumer) {
        TriFunction<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>, Integer> tri = (a, b, c) -> {
            consumer.accept(a, b, c);
            return 0;
        };

        linked(tri);
    }

    @Override
    public <R> Collection<R> indexed(BiFunction<Triple<V, E, V>, Integer, R> function) {
        List<R> list = List.of();
        int index = 0;

        for (var s : outgoing) {
            for (var e : s.getValue().b()) {
                for (var t : e.getValue()) {
                    var curr = Triple.of(s.getKey(), e.getKey(), t);

                    list = list.add(function.apply(curr, index++));
                }
            }
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends ContainingCollection<Triple<V, E, V>>> StreamCollection<R[]> compare(R other) {
        return (StreamCollection<R[]>) outgoing.diff(((GraphImpl<V, E>) other).outgoing).flatMap(s -> s.getValue().a().a().diff(s.getValue().b().a()).flatMap(t -> t.getValue().a().compare(t.getValue().b()).map(changes -> (R[]) new Graph[]{changes[0] == null ? EMPTY : Graph.of(Triple.of(s.getKey(), changes[0].get(0), t.getKey())), changes[1] == null ? EMPTY : Graph.of(Triple.of(s.getKey(), changes[1].get(0), t.getKey()))})));
    }

    @Override
    public Triple<V, E, V> get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        for (var s : outgoing) {
            for (var e : s.getValue().b()) {
                if (index >= e.getValue().size()) {
                    index -= e.getValue().size();
                    continue;
                }

                for (var t : e.getValue()) {
                    if (index-- == 0)
                        return Triple.of(s.getKey(), e.getKey(), t);
                }
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Graph<V, E> remove(Object e) {
        if (e == null)
            return this;

        Triple<V, E, V> triple = (Triple) e;
        return removeEdge(triple.a(), triple.c(), triple.b());
    }

    @Override
    public Graph<V, E> removeAll(Collection<?> e) {
        if (e == null)
            return this;

        Graph<V, E> result = this;

        for (Object edge : e) {
            result = result.remove(edge);
        }

        return result;
    }

    @Override
    public Graph<V, E> add(Triple<V, E, V> e) {
        if (e == null)
            return this;
        return putEdge(e.a(), e.c(), e.b());
    }

    @Override
    public Graph<V, E> addAll(Collection<? extends Triple<V, E, V>> e) {
        if (e == null)
            return this;

        Graph<V, E> result = this;

        for (Triple<V, E, V> edge : e) {
            result = result.add(edge);
        }

        return result;
    }

    @Override
    public Graph<V, E> addUnique(Triple<V, E, V> e) {
        return add(e);
    }

    @Override
    public Graph<V, E> addAllUnique(Collection<? extends Triple<V, E, V>> e) {
        return addAll(e);
    }

    @Override
    public Graph<V, E> replace(Object pre, Triple<V, E, V> post) {
        return contains(pre) ? remove(pre).add(post) : this;
    }

    @Override
    public Graph<V, E> replaceFirst(Object pre, Triple<V, E, V> post) {
        return contains(pre) ? remove(pre).add(post) : this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Graph<V, E> clear() {
        return EMPTY;
    }

    @Override
    public Collection<Triple<V, E, V>> reverse() {
        return this;
    }

    @Override
    public Spliterator<Triple<V, E, V>> reverseSpliterator() {
        return spliterator();
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIterator() {
        return new GraphIterator<>(List.of(this.toList()), 0, size());
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIterator(int index) {
        return new GraphIterator<>(List.of(this.toList()), index, size());
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIteratorAtEnd() {
        int size = size();
        return new GraphIterator<>(List.of(this.toList()), size, size);
    }

    @Override
    public void javaSerialize(Serializer s) {
        s.writeInt(size());
        for (Object e : this) {
            s.writeObject(e);
        }
    }

    @Override
    public void javaDeserialize(Deserializer s) {
        int size = s.readInt();

        for (int i = 0; i < size; i++) {
            Triple<V, E, V> curr = s.readObject();
            GraphImpl<V, E> next = (GraphImpl<V, E>) add(curr);
            this.outgoing = next.outgoing;
            this.incoming = next.incoming;
        }
    }

    @SuppressWarnings("unchecked")
    private static <V, E> DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> partialMerge(DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>> self, DefaultMap<V, Pair<DefaultMap<V, Set<E>>, DefaultMap<E, Set<V>>>>[] branches, int length) {
        return self.merge((o, s, ss, l) -> Pair.of(s.a().merge(Arrays.stream(ss).map(Pair::a).toArray(DefaultMap[]::new)), s.b().merge(Arrays.stream(ss).map(Pair::b).toArray(DefaultMap[]::new))), branches, length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Graph<V, E> merge(Graph<V, E>[] branches, int length) {
        return new GraphImpl<>(partialMerge(outgoing, Arrays.stream(branches).map(branch -> ((GraphImpl<V, E>) branch).outgoing).toArray(DefaultMap[]::new), length), partialMerge(incoming, Arrays.stream(branches).map(branch -> ((GraphImpl<V, E>) branch).incoming).toArray(DefaultMap[]::new), length));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Graph<V, E> getMerger() {
        return EMPTY;
    }

    @Override
    public Class<?> getMeetClass() {
        return Graph.class;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Triple<V, E, V> edge : this) {
            sb.append(StringUtil.toString(edge));
        }
        return sb.append(']').toString();
    }

    private static final class GraphIterator<V, E> implements ListIterator<Triple<V, E, V>> {
        List<Triple<V, E, V>> edges;
        int                   idx, size;

        private GraphIterator(List<Triple<V, E, V>> edges, int idx, int size) {
            if (idx < 0 || idx > size) {
                throw new IndexOutOfBoundsException();
            }

            this.edges = edges;
            this.idx = idx;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return idx < size;
        }

        @Override
        public Triple<V, E, V> next() {
            if (!hasNext())
                throw new NoSuchElementException();

            return edges.get(idx++);
        }

        @Override
        public boolean hasPrevious() {
            return idx > 0;
        }

        @Override
        public Triple<V, E, V> previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();

            return edges.get(--idx);
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Triple<V, E, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Triple<V, E, V> e) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int index(Object e) {
        throw new UnsupportedOperationException();
    }

}

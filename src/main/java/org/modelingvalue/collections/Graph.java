//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2025 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

import java.util.HashSet;
import java.util.function.Predicate;

import org.modelingvalue.collections.impl.GraphImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Triple;

/**
 * <p>
 * An immutable directed graph. The graph cannot contain duplicate edges
 * (edges that have the same source vertex, edge weight, and destination vertex) but can contain
 * multiple edges with different edge weights from the same source and destination vertices.
 * </p>
 * <p>
 * Since the graph is immutable, none of the data modification methods will affect the data in
 * that instance of the graph but will return a new updated graph with that change made.
 * </p>
 * <p>
 * The {@link GraphImpl} implementation of this interface is used to construct graphs returned
 * by the {@link #of(Triple[])} method.
 * </p>
 * <p>
 * This graph represents a triplestore such that each edge is a {@link Triple} in the form of
 * (source vertex, edge weight, destination vertex).
 * </p>
 * 
 * @param <V>
 *            the type of vertices in this graph
 * @param <E>
 *            the type of edge weights in this graph
 */
public interface Graph<V, E> extends ContainingCollection<Triple<V, E, V>>, Mergeable<Graph<V, E>> {
    /**
     * Constructs an immutable directed graph with the specified directed edges and returns it.
     *
     * @param e
     *            array of edges, each represented by a {@link Triple} structured as (source vertex,
     *            edge weight, destination vertex)
     * @return the constructed immutable graph, or an empty graph if no edges are provided
     * @throws NullPointerException
     *             if any of the edges are null
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <V, E> Graph<V, E> of(Triple<V, E, V>... e) {
        return e.length == 0 ? GraphImpl.EMPTY : new GraphImpl<>(e);
    }

    /**
     * Returns a set of the vertices contained in this graph. Only returns vertices that are either
     * a source or destination vertex in at least one edge.
     *
     * @return a set of the vertices contained in this graph
     */
    Set<V> getNodes();

    /**
     * Removes the specified vertex and all edges connected to it from this graph and returns the
     * updated graph. If no operation is done, {@code this} is returned.
     *
     * @param node
     *            vertex to be removed
     * @return a new graph without the specified vertex or any of the edges connected to it
     */
    Graph<V, E> removeNode(V node);

    /**
     * Returns true if this graph contains the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return {@code true} if this graph contains the specified vertex
     */
    boolean containsNode(V node);

    /**
     * Adds a new edge with the specified weight between the source and destination vertices and
     * returns the updated graph. If either vertex does not exist in the graph, they will be added.
     * If no operation is done, {@code this} is returned.
     *
     * @param src
     *            source vertex of the edge
     * @param dst
     *            destination vertex of the edge
     * @param val
     *            weight of the edge
     * @return a new graph with the added edge and vertices if they did not exist before
     */
    Graph<V, E> putEdge(V src, V dst, E val);

    /**
     * Returns true if the graph contains an edge with the specified weight between the source and
     * destination vertices.
     *
     * @param src
     *            source vertex of the edge
     * @param dst
     *            destination vertex of the edge
     * @param val
     *            weight of the edge
     * @return true if the graph contains the specified edge
     */
    boolean containsEdge(V src, V dst, E val);

    /**
     * Removes the edge with the specified weight between the source and destination vertices and
     * returns the updated graph. If either vertex has no other edges connected to it after the
     * removal, that vertex is also removed. If no operation is done, {@code this} is returned.
     *
     * @param src
     *            source vertex of the edge
     * @param dst
     *            destination vertex of the edge
     * @param val
     *            weight of the edge
     * @return a new graph without the specified edge or either vertex if that vertex no longer has
     *         any connected edges
     */
    Graph<V, E> removeEdge(V src, V dst, E val);

    /**
     * Removes all edges between the source and destination vertices and returns the updated
     * graph. If either vertex has no other edges connected to it after the removal, that vertex is
     * also removed. If no operation is done, {@code this} is returned.
     *
     * @param src
     *            source vertex
     * @param dst
     *            destination vertex
     * @return a new graph without any edges from the source to the destination or either vertex if
     *         that vertex no loner has any connected edges
     */
    Graph<V, E> removeEdges(V src, V dst);

    /**
     * Returns a set of the edge weights between the source and destination vertices.
     *
     * @param src
     *            source vertex
     * @param dst
     *            destination vertex
     * @return a set of the edge weights from the source vertex to the destination vertex, or null
     *         if {@code src} or {@code dst} is null or does not exist in this graph
     */
    Set<E> getEdges(V src, V dst);

    /**
     * Returns a map where each key is an edge weight and each value is a set of vertices that have
     * edges with that weight directed to the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return a map where each key is an edge weight and each value is a set of vertices that have
     *         edges with that weight directed to the specified vertex, or null if {@code node} is null or
     *         does not exist in this graph
     */
    DefaultMap<E, Set<V>> getIncoming(V node);

    /**
     * Returns a set of vertices that have edges with the specified weight directed to the
     * specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @param val
     *            edge weight
     * @return a set of vertices that have edges with the specified weight directed to the
     *         specified vertex, or null if {@code node} is null or does not exist in this graph or
     *         {@code val} is null
     */
    Set<V> getIncoming(V node, E val);

    /**
     * Returns a map where each key is an edge weight and each value is a set of vertices that have
     * edges with that weight directed from the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return a map where each key is an edge weight and each value is a set of vertices that have
     *         edges with that weight directed from the specified vertex, or null if {@code node} is null
     *         or does not exist in this graph
     */
    DefaultMap<E, Set<V>> getOutgoing(V node);

    /**
     * Returns a set of vertices that have edges with the specified weight directed from the
     * specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @param val
     *            edge weight
     * @return a set of vertices that have edges with the specified weight directed from the
     *         specified vertex, or null if {@code node} is null or does not exist in this graph or
     *         {@code val} is null
     */
    Set<V> getOutgoing(V node, E val);

    /**
     * Returns a set of the weights of the edges directed to the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return a set of the weights of the edges directed to the specified vertex, or null if
     *         {@code node} is null or does not exist in this graph
     */
    Set<E> getIncomingEdges(V node);

    /**
     * Returns a set of the weights of the edges directed from the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return a set of the weights of the edges directed from the specified vertex, or null if
     *         {@code node} is null or does not exist in this graph
     */
    Set<E> getOutgoingEdges(V node);

    /**
     * Returns a set of vertices that have edges directed to the specified vertex.
     *
     * @param node
     *            vertex to be queried
     * @return a set of the vertices that have edges directed to the specified vertex, or null if
     *         {@code node} is null or does not exist in this graph
     */
    Set<V> getIncomingNodes(V node);

    /**
     * Returns a set of vertices that have edges directed from the specified vertex.
     *
     * @param node
     *            vertex to be checked
     * @return a set of the vertices that have edges directed from the specified vertex, or null if
     *         {@code node} is null or does not exist in this graph
     */
    Set<V> getOutgoingNodes(V node);

    /**
     * Returns true if the graph contains cycles when only considering the nodes and edges
     * specified by the given predicates.
     *
     * @param nodePredicate
     *            a predicate that returns {@code true} if a node should be considered
     *            when detecting cycles
     * @param edgePredicate
     *            a predicate that returns {@code true} if an edge should be considered
     *            when detecting cycles
     * @return {@code true} if there are cycles when only considering the nodes and edges specified
     *         by the predicates
     * @throws NullPointerException
     *             if any of the predicates are null
     */
    default boolean hasCycles(Predicate<V> nodePredicate, Predicate<Triple<V, E, V>> edgePredicate) {
        HashSet<V> safe = new HashSet<>();
        HashSet<V> visited = new HashSet<>();

        for (V node : getNodes()) {
            if (cycleDetectionHelper(nodePredicate, edgePredicate, safe, visited, node)) {
                return true;
            }

            safe.addAll(visited);
            visited = new HashSet<>();
        }

        return false;
    }

    private boolean cycleDetectionHelper(Predicate<V> nodePredicate, Predicate<Triple<V, E, V>> edgePredicate, HashSet<V> safe, HashSet<V> visited, V curr) {
        if (safe.contains(curr))
            return false;
        if (visited.contains(curr))
            return true;
        visited.add(curr);

        for (V next : getOutgoingNodes(curr)) {
            if (nodePredicate.test(next) && getEdges(curr, next).anyMatch(e -> edgePredicate.test(Triple.of(curr, e, next))) && cycleDetectionHelper(nodePredicate, edgePredicate, safe, visited, next)) {
                return true;
            }
        }

        visited.remove(curr);
        return false;
    }

    /**
     * Returns a new graph with all directed edges from this graph reversed such that each
     * edge's source and destinations vertices are swapped.
     *
     * @return a new graph with all directed edges reversed
     */
    Graph<V, E> inverted();

    /**
     * Removes the specified edge and returns the updated graph. If either vertex from the edge has
     * no other edges connected to it after the removal, that vertex is also removed. If no
     * operation is done, {@code this} is returned.
     *
     * @param e
     *            edge to be removed, represented as a {@link Triple} in the form of (source vertex,
     *            edge weight, destination vertex)
     * @return a new graph without the specified edge and without either vertex if it no longer has
     *         any connected edges
     */
    @Override
    Graph<V, E> remove(Object e);

    /**
     * Removes the specified edges and returns the updated graph. If any vertex from the edges has
     * no other edges connected to it after the removal, that vertex is also removed. If no
     * operation is done, {@code this} is returned.
     *
     * @param e
     *            collection of edges to be removed, each represented as a {@link Triple} in the
     *            form of (source vertex, edge weight, destination vertex)
     * @return a new graph without the specified edges and without any vertices that no longer have
     *         any connected edges
     */
    @Override
    Graph<V, E> removeAll(Collection<?> e);

    /**
     * Adds the specified edge and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added. If no operation is done, {@code this} is returned.
     *
     * @param e
     *            edge to be added
     * @return a new graph with the added edge and vertices if they did not exist before
     */
    @Override
    Graph<V, E> add(Triple<V, E, V> e);

    /**
     * Adds the specified edges and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added. If no operation is done, {@code node} this is returned.
     *
     * @param e
     *            collection of edges to be added
     * @return a new graph with the added edges and vertices if they did not exist before
     */
    @Override
    Graph<V, E> addAll(Collection<? extends Triple<V, E, V>> e);

    /**
     * Adds the specified edge and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added. If no operation is done, {@code this} is returned.
     *
     * @param e
     *            edge to be added
     * @return a new graph with the added edge and vertices if they did not exist before
     */
    @Override
    Graph<V, E> addUnique(Triple<V, E, V> e);

    /**
     * Adds the specified edges and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added. If no operation is done, {@code node} this is returned.
     *
     * @param e
     *            collection of edges to be added
     * @return a new graph with the added edges and vertices if they did not exist before
     */
    @Override
    Graph<V, E> addAllUnique(Collection<? extends Triple<V, E, V>> e);

    /**
     * If {@code pre} is a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex) and that edge exists in this graph, {@code pre} is removed and {@code post} is added
     * and the updated graph is returned. If no operation is done, {@code this} is returned.
     *
     * @param pre
     *            edge to be removed
     * @param post
     *            edge to be added
     * @return a new graph with {@code pre} removed and {@code post} added if {@code pre} exists in
     *         this graph, otherwise, {@code this} is returned
     */
    @Override
    Graph<V, E> replace(Object pre, Triple<V, E, V> post);

    /**
     * If {@code pre} is a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex) and that edge exists in this graph, {@code pre} is removed and {@code post} is added
     * and the updated graph is returned. If no operation is done, {@code this} is returned.
     *
     * @param pre
     *            edge to be removed
     * @param post
     *            edge to be added
     * @return a new graph with {@code pre} removed and {@code post} added if {@code pre} exists in
     *         this graph, otherwise, {@code this} is returned
     */
    @Override
    Graph<V, E> replaceFirst(Object pre, Triple<V, E, V> post);

    /**
     * Returns an empty graph with no vertices or edges.
     *
     * @return an empty graph
     */
    @Override
    Graph<V, E> clear();

    @Override
    default Graph<V, E> removeAll(Predicate<? super Triple<V, E, V>> predicate) {
        return (Graph<V, E>) ContainingCollection.super.removeAll(predicate);
    }

    @Override
    default Graph<V, E> retainAll(Predicate<? super Triple<V, E, V>> predicate) {
        return (Graph<V, E>) ContainingCollection.super.retainAll(predicate);
    }
}

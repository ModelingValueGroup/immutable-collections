package org.modelingvalue.collections.impl;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.DirGraph;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.TriFunction;

public class DagImpl<N> extends DirGraphImpl<N> implements Dag<N> {

    private static final long serialVersionUID = -5506725852159954397L;

    protected DagImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        super(begin, end, vertices);
    }

    protected DagImpl(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        this(beginEnd[0], beginEnd[1], vertices);
    }

    @Override
    protected DirGraph<N> construct(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices, boolean possibleCycleAdded) {
        if (possibleCycleAdded) {
            return super.construct(beginEnd, vertices, possibleCycleAdded);
        } else {
            return vertices.isEmpty() ? Dag.of() : this.vertices().equals(vertices) ? this : new DagImpl<N>(beginEnd, vertices);
        }
    }

    @Override
    public Set<Pair<N, N>> cycles() {
        return Set.of();
    }

    @Override
    public Dag<N> removeNodes(Set<N> e) {
        return (Dag<N>) super.removeNodes(e);
    }

    @Override
    public Dag<N> retainNodes(Set<N> e) {
        return (Dag<N>) super.retainNodes(e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> removeNodes(N... e) {
        return (Dag<N>) super.removeNodes(e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> retainNodes(N... e) {
        return (Dag<N>) super.retainNodes(e);
    }

    @Override
    public Dag<N> removeDisconnected() {
        return this;
    }

    @Override
    public Dag<N> invRemoveDisconnected() {
        return this;
    }

    @Override
    public Dag<N> removeCycles() {
        return this;
    }

    @Override
    public Dag<N> invRemoveCycles() {
        return this;
    }

    @Override
    public <A> A dfs(A acc, TriFunction<A, N, N, A> func) {
        return dfs(acc, (a, f, t, c) -> func.apply(a, f, t));
    }

    @Override
    public <A> A invDfs(A acc, TriFunction<A, N, N, A> func) {
        return invDfs(acc, (a, f, t, c) -> func.apply(a, f, t));
    }

    @Override
    public <A> A dfs(A acc, QuadFunction<A, N, N, Boolean, A> func) {
        return dfs(vertices(), begin(), acc, func, true);
    }

    @Override
    public <A> A invDfs(A acc, QuadFunction<A, N, N, Boolean, A> func) {
        return dfs(vertices(), end(), acc, func, false);
    }

    @Override
    public Dag<N> setBegin(Set<N> begin) {
        return (Dag<N>) super.setBegin(begin);
    }

    @Override
    public Dag<N> setEnd(Set<N> end) {
        return (Dag<N>) super.setEnd(end);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> setBegin(N... begin) {
        return (Dag<N>) super.setBegin(begin);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> setEnd(N... end) {
        return (Dag<N>) super.setEnd(end);
    }

    @Override
    public Dag<N> removeEdge(N from, N to) {
        return (Dag<N>) super.removeEdge(from, to);
    }

    @Override
    public Dag<N> clear(N node) {
        return (Dag<N>) super.clear(node);
    }

    @Override
    public Dag<N> clearOuts(N node) {
        return (Dag<N>) super.clearOuts(node);
    }

    @Override
    public Dag<N> clearIns(N node) {
        return (Dag<N>) super.clearIns(node);
    }

    @Override
    public Dag<N> putBegin(N node, Set<N> outs) {
        return (Dag<N>) super.putBegin(node, outs);
    }

    @Override
    public Dag<N> putEnd(N node, Set<N> ins) {
        return (Dag<N>) super.putEnd(node, ins);
    }

    @Override
    public Dag<N> remove(N node, Set<N> ins, Set<N> outs) {
        return (Dag<N>) super.remove(node, ins, outs);
    }

    @Override
    public Dag<N> removeOuts(N node, Set<N> outs) {
        return (Dag<N>) super.removeOuts(node, outs);
    }

    @Override
    public Dag<N> removeIns(N node, Set<N> ins) {
        return (Dag<N>) super.removeIns(node, ins);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> removeEdges(N... edges) {
        return (Dag<N>) super.removeEdges(edges);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> putBegin(N node, N... outs) {
        return (Dag<N>) super.putBegin(node, outs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> putEnd(N node, N... ins) {
        return (Dag<N>) super.putEnd(node, ins);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> removeOuts(N node, N... outs) {
        return (Dag<N>) super.removeOuts(node, outs);

    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> removeIns(N node, N... ins) {
        return (Dag<N>) super.removeIns(node, ins);
    }

    @Override
    public Dag<N> remove(Object e) {
        return (Dag<N>) super.remove(e);
    }

    @Override
    public Dag<N> removeAll(Collection<?> e) {
        return (Dag<N>) super.removeAll(e);
    }

}

package org.modelingvalue.collections.impl;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Graph;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Pair;

import java.util.Objects;

public class GraphImpl<V, E> implements Graph<V, E> {
    private Map<V, Pair<Map<V, E>, Map<V, E>>> g;

    public GraphImpl(Map<V, Pair<Map<V, E>, Map<V, E>>> g) {
        this.g = g;
    }

    @Override
    public GraphImpl<V, E> addNode(V node) {
        Objects.requireNonNull(node);
        if (containsNode(node)) return this;
        return new GraphImpl<>(this.g.put(node, Pair.of(Map.of(), Map.of())));
    }

    @Override
    public Graph<V, E> removeNode(V node) {
        if (!containsNode(node)) return this;
        return new GraphImpl<>(g.removeKey(node));
    }

    @Override
    public boolean containsNode(V node) {
        return node != null && g.containsKey(node);
    }

    @Override
    public Collection<V> getAllNodes() {
        return g.toKeys();
    }

    @Override
    public GraphImpl<V, E> putEdge(V src, V dest, E val) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(dest);
        if (Objects.equals(getEdge(src, dest),val)) return this;

        var res = addNode(src).addNode(dest);

        var srcP = res.g.get(src);
        var destP = res.g.get(dest);

        if (val == null) {
            srcP = Pair.of(srcP.a(), srcP.b().removeKey(dest));
            destP = Pair.of(destP.a().removeKey(src), destP.b());
        } else if (src.equals(dest)) {
            srcP = Pair.of(srcP.a().put(src, val), srcP.b().put(src, val));
        } else {
            srcP = Pair.of(srcP.a(), srcP.b().put(dest, val));
            destP = Pair.of(destP.a().put(src, val), destP.b());
        }

        res.g = res.g.put(dest, destP).put(src, srcP);
        return res;
    }

    @Override
    public E getEdge(V src, V dest) {
        return containsNode(src) ? g.get(src).b().get(dest) : null;
    }

    @Override
    public Graph<V, E> removeEdge(V src, V dest) {
        return putEdge(src, dest, null);
    }

    @Override
    public Map<V, E> getIncomingEdges(V node) {
        return g.containsKey(node) ? g.get(node).a() : null;
    }

    @Override
    public Map<V, E> getOutgoingEdges(V node) {
        return g.containsKey(node) ? g.get(node).b() : null;
    }

    @Override
    public int size() {
        return g.size();
    }

    @Override
    public int hashCode() {
        return this.g.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        @SuppressWarnings("unchecked")
        GraphImpl<V,E> other = (GraphImpl<V,E>) obj;

        if (!this.g.equals(other.g))
            return false;

        if (Age.age(this.g) > Age.age(other.g)) {
            other.g = this.g;
        } else {
            this.g = other.g;
        }

        return true;
    }
}
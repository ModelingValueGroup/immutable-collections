package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.logic.Lists.ListCons;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.Structure;

public final class ListImpl<E extends Structure> extends StructureImpl<ListCons<E>> {
    private static final long                                   serialVersionUID = -916406585584150604L;

    public org.modelingvalue.collections.List<StructureImpl<E>> list;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(FunctorImpl functor) {
        super(functor);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(Functor functor, E head, ListCons<E> tail) {
        super(functor, head, tail);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(FunctorImpl functor, StructureImpl<E> head, ListImpl<E> tail) {
        super(functor, head, tail);
    }

    private ListImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ListCons<E> proxy() {
        return (ListCons<E>) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{ListCons.class}, this);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ListImpl<E> struct(Object[] array) {
        return new ListImpl(array);
    }

    @SuppressWarnings("unchecked")
    protected StructureImpl<E> head() {
        return (StructureImpl<E>) get(1);
    }

    @SuppressWarnings("unchecked")
    protected ListImpl<E> tail() {
        return (ListImpl<E>) get(2);
    }

    @Override
    public String toString() {
        return list().toString().substring(4);
    }

    public org.modelingvalue.collections.List<StructureImpl<E>> list() {
        if (list == null) {
            list = length() == 3 ? tail().list().prepend(head()) : org.modelingvalue.collections.List.of();
        }
        return list;
    }

    @Override
    public ListImpl<E> set(int i, Object... a) {
        return (ListImpl<E>) super.set(i, a);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class<ListCons<E>> type() {
        return (Class) ListCons.class;
    }
}

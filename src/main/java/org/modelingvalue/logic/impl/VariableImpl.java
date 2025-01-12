package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.Logic.Variable;

public final class VariableImpl<F extends Structure> extends StructureImpl<F> {
    private static final long serialVersionUID = -8998368070388908726L;

    public VariableImpl(Class<F> type, String name) {
        super(type, name);
        Logic.updateSpecs(type);
    }

    private VariableImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final F proxy() {
        return (F) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{type(), Variable.class}, this);
    }

    @Override
    public String toString() {
        return get(1).toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected VariableImpl<F> struct(Object[] array) {
        return new VariableImpl<F>(array);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<F> type() {
        return (Class<F>) get(0);
    }
}

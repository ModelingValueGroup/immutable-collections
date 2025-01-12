package org.modelingvalue.logic.impl;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Predicate;

public final class TrueImpl extends PredicateImpl {
    private static final long serialVersionUID = -8515171118744898263L;

    public TrueImpl() {
        super(Logic.YES_FUNCTOR);
    }

    private TrueImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate proxy() {
        return Logic.YES_PROXY;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected TrueImpl struct(Object[] array) {
        return new TrueImpl(array);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        return Set.of(this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
        return vars;
    }

    @Override
    public TrueImpl set(int i, Object... a) {
        return (TrueImpl) super.set(i, a);
    }
}

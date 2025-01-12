package org.modelingvalue.logic.impl;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Predicate;

public final class FalseImpl extends PredicateImpl {
    private static final long serialVersionUID = -8515171118744898263L;

    public FalseImpl() {
        super(Logic.NO_FUNCTOR);
    }

    private FalseImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate proxy() {
        return Logic.NO_PROXY;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected FalseImpl struct(Object[] array) {
        return new FalseImpl(array);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        return Set.of();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
        return vars;
    }

    @Override
    public FalseImpl set(int i, Object... a) {
        return (FalseImpl) super.set(i, a);
    }
}

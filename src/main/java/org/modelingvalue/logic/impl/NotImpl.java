package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Predicate;

public final class NotImpl extends PredicateImpl {
    private static final long serialVersionUID = -4543178470298951866L;

    public NotImpl(Predicate pred) {
        super(Logic.NOT_FUNCTOR_PROXY, pred);
    }

    private NotImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate proxy() {
        return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected NotImpl struct(Object[] array) {
        return new NotImpl(array);
    }

    @SuppressWarnings("rawtypes")
    protected final PredicateImpl pred() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> r = pred().match(((NotImpl) goal).pred(), der, rec, database);
        return r.isEmpty() ? Set.of(this) : r.retainAll(PredicateImpl::isIncomplete);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
        return vars;
    }

    @Override
    public NotImpl set(int i, Object... a) {
        return (NotImpl) super.set(i, a);
    }
}

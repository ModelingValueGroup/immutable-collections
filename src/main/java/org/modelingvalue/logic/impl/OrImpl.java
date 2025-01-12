package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Predicate;

public final class OrImpl extends PredicateImpl {
    private static final long serialVersionUID = -1732549494864415986L;

    public OrImpl(PredicateImpl pred1, PredicateImpl pred2) {
        super(Logic.OR_FUNCTOR, pred1, pred2);
    }

    private OrImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate proxy() {
        return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected OrImpl struct(Object[] array) {
        return new OrImpl(array);
    }

    private List<int[]> idxList;

    @SuppressWarnings("rawtypes")
    private List<int[]> idxList() {
        if (idxList == null) {
            List<int[]> l = List.of();
            PredicateImpl p1 = pred1();
            if (p1 instanceof OrImpl) {
                l = l.prependList(((OrImpl) p1).idxList().replaceAll(Logic.ADD_ONE));
            } else {
                l = l.append(Logic.ONE_ARRAY);
            }
            PredicateImpl p2 = pred2();
            if (p2 instanceof OrImpl) {
                l = l.appendList(((OrImpl) p2).idxList().replaceAll(Logic.ADD_TWO));
            } else {
                l = l.append(Logic.TWO_ARRAY);
            }
            idxList = l;
        }
        return idxList;
    }

    @SuppressWarnings("rawtypes")
    protected final PredicateImpl pred1() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    protected final PredicateImpl pred2() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> r = Set.of();
        for (int[] i : ((OrImpl) goal).idxList()) {
            PredicateImpl g = goal.getPred(i);
            Set<PredicateImpl> m = getPred(i).match(g, der, rec, database);
            if (m.anyMatch(PredicateImpl::isToDepthIcomplete)) {
                return m;
            } else {
                r = r.addAll(m.replaceAll(t -> t.isIncomplete() ? t : goal.setBinding(this, g.getBinding(t, Map.of()))));
            }
        }
        return r;
    }

    @Override
    public OrImpl set(int i, Object... a) {
        return (OrImpl) super.set(i, a);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean contains(PredicateImpl cond) {
        return super.contains(cond) || pred1().contains(cond) || pred2().contains(cond);
    }
}

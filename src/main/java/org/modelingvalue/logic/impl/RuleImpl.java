package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Predicate;
import org.modelingvalue.logic.Logic.Relation;
import org.modelingvalue.logic.Logic.Rule;

public final class RuleImpl extends StructureImpl<Rule> {
    private static final long serialVersionUID = -4602043866952049391L;

    public RuleImpl(Relation pred, Predicate goal) {
        super(Logic.RULE_FUNCTOR_PROXY, pred, goal);
    }

    private RuleImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Rule proxy() {
        return (Rule) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Rule.class}, this);
    }

    @Override
    protected RuleImpl struct(Object[] array) {
        return new RuleImpl(array);
    }

    @SuppressWarnings("rawtypes")
    private Map<VariableImpl, Object> variables;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<VariableImpl, Object> variables() {
        if (variables == null) {
            variables = super.variables();
        }
        return variables;
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl cons() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl cond() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Set<PredicateImpl> eval(PredicateImpl pred, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        PredicateImpl cons = cons();
        Map<VariableImpl, Object> binding = cons.getBinding(pred, Map.of());
        if (binding == null) {
            return Set.of();
        }
        if (Logic.TRACE_LOGIC) {
            System.err.println("LOGIC " + "  ".repeat(der.size()) + this + " " + binding.toString().substring(3));
        }
        PredicateImpl cond = cond();
        Set<PredicateImpl> match = cond.setBinding(cond, cond.variables().putAll(binding)).match(cond, der, rec, database);
        return match.replaceAll(t -> t.isIncomplete() ? t : cons.setBinding(pred, cond.getBinding(t, Map.of())));
    }

    public int rulePrio() {
        return cond().totalLength();
    }

    @Override
    public RuleImpl set(int i, Object... a) {
        return (RuleImpl) super.set(i, a);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Rule> type() {
        return Rule.class;
    }
}

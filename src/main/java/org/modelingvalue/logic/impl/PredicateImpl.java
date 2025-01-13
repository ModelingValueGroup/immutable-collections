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

package org.modelingvalue.logic.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Database.Memoiz;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;

public class PredicateImpl extends StructureImpl<Predicate> {
    private static final long                                                              serialVersionUID    = -1605559565948158856L;

    private static final int                                                               MAX_LOGIC_MEMOIZ_D4 = Logic.MAX_LOGIC_MEMOIZ / 4;
    private static final int                                                               MAX_LOGIC_DEPTH_D2  = Logic.MAX_LOGIC_DEPTH / 2;

    protected static final int[]                                                           ONE_ARRAY           = new int[]{1};
    protected static final int[]                                                           TWO_ARRAY           = new int[]{2};
    protected static final UnaryOperator<int[]>                                            ADD_ONE             = a -> {
                                                                                                                   int[] r = new int[a.length + 1];
                                                                                                                   System.arraycopy(a, 0, r, 1, a.length);
                                                                                                                   r[0] = 1;
                                                                                                                   return r;
                                                                                                               };
    protected static final UnaryOperator<int[]>                                            ADD_TWO             = a -> {
                                                                                                                   int[] r = new int[a.length + 1];
                                                                                                                   System.arraycopy(a, 0, r, 1, a.length);
                                                                                                                   r[0] = 2;
                                                                                                                   return r;
                                                                                                               };

    @SuppressWarnings("rawtypes")
    private static final BiFunction<Set<PredicateImpl>, PredicateImpl, Set<PredicateImpl>> ADD_FACT            = (s, e) -> s == null ? Set.of(e) : s.add(e);
    @SuppressWarnings("unchecked")
    private static final BiFunction<List<RuleImpl>, RuleImpl, List<RuleImpl>>              ADD_RULE            = (l, e) -> {
                                                                                                                   if (l == null) {
                                                                                                                       return List.of(e);
                                                                                                                   } else {
                                                                                                                       int p = e.rulePrio();
                                                                                                                       for (int i = 0; i < l.size(); i++) {
                                                                                                                           RuleImpl r = l.get(i);
                                                                                                                           if (r.equals(e)) {
                                                                                                                               return l;
                                                                                                                           } else if (r.cons().equals(e.cons())) {
                                                                                                                               if (r.cond().contains(e.cond())) {
                                                                                                                                   return l;
                                                                                                                               } else {
                                                                                                                                   return l.replace(i, r.set(2, new OrImpl(r.cond(), e.cond())));
                                                                                                                               }
                                                                                                                           } else if (r.rulePrio() > p) {
                                                                                                                               return l.insert(i, e);
                                                                                                                           }
                                                                                                                       }
                                                                                                                       return l.append(e);
                                                                                                                   }
                                                                                                               };

    public PredicateImpl(Functor<Predicate> functor, Object... args) {
        super(functor, args);
    }

    public PredicateImpl(FunctorImpl<Predicate> functor, Object... args) {
        super(functor, args);
    }

    protected PredicateImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected PredicateImpl struct(Object[] array) {
        return new PredicateImpl(array);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public final void makeFact(Database database) {
        if (functor().logic() != null) {
            throw new IllegalArgumentException("No facts of a functor with a logic lambda allowed. " + this);
        }
        if (database.rules.get().get(signature()) != null) {
            throw new IllegalArgumentException("No facts of a functor with rules allowed. " + this);
        }
        database.facts.updateAndGet(m -> {
            List<Class> args = functor().args();
            m = m.put(this, PredicateImpl.ADD_FACT.apply(m.get(this), this));
            for (int i = 1; i < length(); i++) {
                m = addFact(m, set(i, getType(i)), i, args.get(i - 1));
            }
            return m;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<PredicateImpl, Set<PredicateImpl>> addFact(Map<PredicateImpl, Set<PredicateImpl>> m, PredicateImpl pred, int i, Class a) {
        Class t = pred.getType(i);
        if (a.isAssignableFrom(t)) {
            m = m.put(pred, PredicateImpl.ADD_FACT.apply(m.get(pred), this));
            if (!a.equals(t)) {
                for (Type g : t.getGenericInterfaces()) {
                    while (g instanceof ParameterizedType) {
                        g = ((ParameterizedType) g).getRawType();
                    }
                    if (g instanceof Class) {
                        m = addFact(m, pred.set(i, g), i, a);
                    }
                }
            }
        }
        return m;
    }

    @SuppressWarnings("rawtypes")
    protected PredicateImpl getPred(int[] ii) {
        PredicateImpl r = this;
        for (int i = 0; i < ii.length; i++) {
            r = (PredicateImpl) r.get(ii[i]);
        }
        return r;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<PredicateImpl> incomplete() {
        return Set.of(incompleteImpl(List.of(this)));
    }

    public boolean isIncomplete() {
        return Logic.INCOMPLETE_FUNCTOR.equals(functor());
    }

    @SuppressWarnings("rawtypes")
    protected boolean equalFunctor(PredicateImpl other) {
        return get(0).equals(other.get(0));
    }

    @SuppressWarnings("rawtypes")
    protected boolean isIncomplete(PredicateImpl other) {
        return other.isIncomplete() && ((List) other.get(1)).last().equals(this);
    }

    @SuppressWarnings("rawtypes")
    protected boolean isToDepthIcomplete() {
        return isIncomplete() && ((List) get(1)).size() >= Logic.MAX_LOGIC_DEPTH;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PredicateImpl setBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
        return (PredicateImpl) super.setBinding(pred, vars);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl signature() {
        Object[] array = null;
        for (int i = 1; i < length(); i++) {
            Object v = get(i);
            Object s = typeOf(v);
            if (!Objects.equals(s, v)) {
                if (array == null) {
                    array = toArray();
                }
                array[i] = s;
            }
        }
        return array != null ? struct(array) : this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<PredicateImpl, List<RuleImpl>> addRule(RuleImpl ruleImpl, Map<PredicateImpl, List<RuleImpl>> rules, Map<Class, Set<Class>> specs) {
        rules = rules.put(this, PredicateImpl.ADD_RULE.apply(rules.get(this), ruleImpl));
        for (int i = 1; i < length(); i++) {
            Object v = get(i);
            if (v instanceof Class) {
                for (Class g : specs.get((Class) v)) {
                    PredicateImpl p = set(i, g);
                    rules = p.addRule(ruleImpl, rules, specs);
                }
            }
        }
        return rules;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class getType(int i) {
        Object v = get(i);
        return v instanceof Class ? (Class) v : v instanceof StructureImpl ? ((StructureImpl) v).type() : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        FunctorImpl<Predicate> functor = functor();
        LogicLambda logic = functor.logic();
        if (logic != null) {
            return logic.apply((PredicateImpl) this);
        }
        int non = nrOfNulls();
        if (non > 1 || non >= totalLength()) {
            return Set.of(incompleteImpl(der.append(this)));
        }
        Set<PredicateImpl> facts = database.facts.get().get(this);
        if (facts != null) {
            return facts;
        }
        PredicateImpl signature = signature();
        List<RuleImpl> rules = database.rules.get().get(signature);
        if (rules != null) {
            Set<PredicateImpl> r = rec.get(this);
            if (r != null) {
                return r;
            }
            for (QualifiedSet<PredicateImpl, Memoiz> m : database.memoiz.get()) {
                Memoiz memoiz = m.get(this);
                if (memoiz != null) {
                    memoiz.count++;
                    return memoiz.set();
                }
            }
            int li = der.lastIndexOf(this);
            if (li >= 0) {
                return Set.of(incompleteImpl(der.append(this)));
            }
            if (der.size() >= Logic.MAX_LOGIC_DEPTH) {
                return Set.of(incompleteImpl(der.append(this)));
            }
            Set<PredicateImpl> set = fixpoint(rules, non, der.append(this), rec, database);
            if (der.size() >= PredicateImpl.MAX_LOGIC_DEPTH_D2) {
                Optional<? extends StructureImpl> ic = set.findAny(PredicateImpl::isToDepthIcomplete);
                if (ic.isPresent()) {
                    if (der.size() == PredicateImpl.MAX_LOGIC_DEPTH_D2) {
                        List<PredicateImpl> list = (List) ic.get().get(1);
                        List<PredicateImpl> todo = list.sublist(der.size(), list.size());
                        while (todo.size() > 0) {
                            PredicateImpl p = todo.last();
                            FunctorImpl<Predicate> pf = p.functor();
                            set = p.fixpoint(database.rules.get().get(p.signature()), p.nrOfNulls(), der.append(p), rec, database);
                            ic = set.findAny(PredicateImpl::isToDepthIcomplete);
                            if (ic.isPresent()) {
                                list = (List) ic.get().get(1);
                                todo = todo.appendList(list.sublist(der.size(), list.size()));
                            } else {
                                p.memoization(pf, set, database);
                                todo = todo.removeLast();
                            }
                        }
                    }
                    return set;
                }
            }
            memoization(functor, set, database);
            return set;
        }
        return Set.of();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<PredicateImpl> fixpoint(List<RuleImpl> rules, int non, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> result = Set.of(), added = Set.of();
        Set<PredicateImpl> found = Set.of();
        boolean incomplete = false;
        do {
            added = evalRules(rules, non, der, found.isEmpty() ? rec : rec.put(this, found), database).removeAll(result);
            found = (Set) added.retainAll(this::equalFunctor);
            incomplete |= added.anyMatch(this::isIncomplete);
            if (incomplete && result.isEmpty() && !found.isEmpty()) {
                result = result.addAll(found);
            } else {
                result = result.addAll(added);
            }
        } while (incomplete && !found.isEmpty());
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void memoization(FunctorImpl<Predicate> functor, Set<PredicateImpl> all, Database database) {
        if (all.noneMatch(PredicateImpl::isIncomplete)) {
            Set<PredicateImpl> set = (Set) all;
            if (functor.factual) {
                database.facts.updateAndGet(m -> {
                    m = m.put(this, set);
                    for (PredicateImpl e : set) {
                        m = m.put(e, Set.of(e));
                    }
                    return m;
                });
            } else if (!functor.derived) {
                QualifiedSet<PredicateImpl, Memoiz>[] mem = database.memoiz.updateAndGet(a -> {
                    a = a.clone();
                    if (a[0].size() >= PredicateImpl.MAX_LOGIC_MEMOIZ_D4) {
                        a[2] = a[2].putAll(a[1]);
                        a[1] = a[0];
                        a[0] = Database.EMPTY_MEMOIZ;
                    }
                    a[0] = a[0].put(new Database.Memoiz(this, set));
                    for (PredicateImpl e : set) {
                        a[0] = a[0].put(new Database.Memoiz(e, Set.of(e)));
                    }
                    return a;
                });
                if (mem[2].size() > Logic.MAX_LOGIC_MEMOIZ && mem[0].size() == set.size() + 1) {
                    Logic.LOGIC_POOL.execute(database::cleanup);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private Set<PredicateImpl> evalRules(List<RuleImpl> rules, int non, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> r = Set.of();
        for (RuleImpl rule : rules) {
            Set<PredicateImpl> eval = rule.eval(this, der, rec, database);
            if (eval.anyMatch(PredicateImpl::isToDepthIcomplete)) {
                return eval;
            } else {
                r = r.addAll(eval);
            }
        }
        return r;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PredicateImpl eq(StructureImpl<Predicate> other) {
        return (PredicateImpl) super.eq(other);
    }

    @SuppressWarnings("rawtypes")
    public boolean contains(PredicateImpl cond) {
        return equals(cond);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public PredicateImpl set(int i, Object... a) {
        return (PredicateImpl) super.set(i, a);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Logic.Predicate> PredicateImpl of(FunctorImpl<P> functor, Object... args) {
        return new PredicateImpl((FunctorImpl<Logic.Predicate>) functor, args);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PredicateImpl incompleteImpl(List<PredicateImpl> der) {
        return PredicateImpl.of(Logic.INCOMPLETE_FUNCTOR, der);
    }
}

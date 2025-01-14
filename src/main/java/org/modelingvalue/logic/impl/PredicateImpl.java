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

import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.Incomplete;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;

public class PredicateImpl extends StructureImpl<Predicate> {
    private static final long                   serialVersionUID   = -1605559565948158856L;

    private static final int                    MAX_LOGIC_DEPTH    = Integer.getInteger("MAX_LOGIC_DEPTH", 32);
    private static final int                    MAX_LOGIC_DEPTH_D2 = MAX_LOGIC_DEPTH / 2;

    protected static final int[]                ONE_ARRAY          = new int[]{1};
    protected static final int[]                TWO_ARRAY          = new int[]{2};
    protected static final UnaryOperator<int[]> ADD_ONE            = a -> {
                                                                       int[] r = new int[a.length + 1];
                                                                       System.arraycopy(a, 0, r, 1, a.length);
                                                                       r[0] = 1;
                                                                       return r;
                                                                   };
    protected static final UnaryOperator<int[]> ADD_TWO            = a -> {
                                                                       int[] r = new int[a.length + 1];
                                                                       System.arraycopy(a, 0, r, 1, a.length);
                                                                       r[0] = 2;
                                                                       return r;
                                                                   };

    public static final FunctorImpl<Incomplete> INCOMPLETE_FUNCTOR = FunctorImpl.<Incomplete, List<Predicate>> of(Logic::incomplete);

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
        return Set.of(PredicateImpl.of(INCOMPLETE_FUNCTOR, List.of(this)));
    }

    private Set<PredicateImpl> incomplete(List<PredicateImpl> der) {
        return Set.of(PredicateImpl.of(INCOMPLETE_FUNCTOR, der.append(this)));
    }

    public boolean isIncomplete() {
        return INCOMPLETE_FUNCTOR.equals(functor());
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
        return isIncomplete() && ((List) get(1)).size() >= MAX_LOGIC_DEPTH;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class getType(int i) {
        Object v = get(i);
        return v instanceof Class ? (Class) v : v instanceof StructureImpl ? ((StructureImpl) v).type() : null;
    }

    public Set<PredicateImpl> match(PredicateImpl decl, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        FunctorImpl<Predicate> functor = functor();
        LogicLambda logic = functor.logic();
        if (logic != null) {
            return logic.apply((PredicateImpl) this);
        }
        if (!der.isEmpty()) {
            int nou = nrOfUnbound();
            if (nou > 1) {
                return incomplete(der);
            }
            if (nou == 1 && decl.nrOfVariables() == 1) {
                return incomplete(der);
            }
        }
        Set<PredicateImpl> facts = database.getFacts(this);
        if (facts != null) {
            return facts;
        }
        PredicateImpl signature = signature();
        List<RuleImpl> rules = database.getRules(signature);
        if (rules != null) {
            Set<PredicateImpl> r = rec.get(this);
            if (r != null) {
                return r;
            }
            r = database.getMemoiz(this);
            if (r != null) {
                return r;
            }
            if (der.lastIndexOf(this) >= 0) {
                return incomplete(der);
            }
            if (der.size() >= MAX_LOGIC_DEPTH) {
                return incomplete(der);
            }
            Set<PredicateImpl> set = fixpoint(rules, der.append(this), rec, database);
            if (der.size() >= MAX_LOGIC_DEPTH_D2) {
                Optional<PredicateImpl> ic = set.findAny(PredicateImpl::isToDepthIcomplete);
                if (ic.isPresent()) {
                    if (der.size() == MAX_LOGIC_DEPTH_D2) {
                        return flatten(set, ic, der, rec, database);
                    }
                    return set;
                }
            }
            database.memoization(this, set);
            return set;
        }
        return Set.of();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<PredicateImpl> flatten(Set<PredicateImpl> set, Optional<PredicateImpl> ic, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        List<PredicateImpl> list = (List) ic.get().get(1);
        List<PredicateImpl> todo = list.sublist(der.size(), list.size());
        while (todo.size() > 0) {
            PredicateImpl p = todo.last();
            set = p.fixpoint(database.getRules(p.signature()), der.append(p), rec, database);
            ic = set.findAny(PredicateImpl::isToDepthIcomplete);
            if (ic.isPresent()) {
                list = (List) ic.get().get(1);
                todo = todo.appendList(list.sublist(der.size(), list.size()));
            } else {
                database.memoization(p, set);
                todo = todo.removeLast();
            }
        }
        return set;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<PredicateImpl> fixpoint(List<RuleImpl> rules, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> result = Set.of(), added = Set.of();
        Set<PredicateImpl> found = Set.of();
        boolean incomplete = false;
        do {
            added = evalRules(rules, der, found.isEmpty() ? rec : rec.put(this, found), database).removeAll(result);
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

    @SuppressWarnings("rawtypes")
    private Set<PredicateImpl> evalRules(List<RuleImpl> rules, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
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
    public static <P extends Predicate> PredicateImpl of(FunctorImpl<P> functor, Object... args) {
        return new PredicateImpl((FunctorImpl<Predicate>) functor, args);
    }
}

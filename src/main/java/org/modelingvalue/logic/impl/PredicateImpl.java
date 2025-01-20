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
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;

public class PredicateImpl extends StructureImpl<Predicate> {
    private static final long                   serialVersionUID   = -1605559565948158856L;

    static final int                            MAX_LOGIC_DEPTH    = Integer.getInteger("MAX_LOGIC_DEPTH", 32);
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

    public Conclusion infer(PredicateImpl declaration, InferContext context) {
        FunctorImpl<Predicate> functor = functor();
        LogicLambda logic = functor.logic();
        if (logic != null) {
            return logic.apply((PredicateImpl) this, context);
        }
        int nrOfUnbound = nrOfUnbound();
        if (nrOfUnbound > 1 || (nrOfUnbound == 1 && functor.args().size() == 1)) {
            return context.incomplete(this);
        }
        KnowledgeBaseImpl knowledgebase = context.knowledgebase();
        Conclusion conclusion = knowledgebase.getFacts(this);
        if (conclusion != null) {
            return conclusion;
        }
        List<RuleImpl> rules = knowledgebase.getRules(this);
        if (rules != null) {
            conclusion = context.cycleConclusion().get(this);
            if (conclusion != null) {
                return conclusion;
            }
            conclusion = knowledgebase.getMemoiz(this);
            if (conclusion != null) {
                return conclusion;
            }
            List<PredicateImpl> stack = context.stack();
            if (stack.size() >= MAX_LOGIC_DEPTH || stack.lastIndexOf(this) >= 0) {
                return context.incomplete(this);
            }
            conclusion = fixpoint(rules, context.pushOnStack(this));
            if (stack.size() >= MAX_LOGIC_DEPTH_D2) {
                List<PredicateImpl> overflow = conclusion.stackOverflow();
                if (overflow != null) {
                    if (stack.size() == MAX_LOGIC_DEPTH_D2) {
                        return flatten(conclusion, overflow, context);
                    }
                    return conclusion;
                }
            }
            knowledgebase.memoization(this, conclusion);
            return conclusion;
        }
        return Conclusion.EMPTY;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Conclusion flatten(Conclusion conclusion, List<PredicateImpl> overflow, InferContext context) {
        int stackSize = context.stack().size();
        List<PredicateImpl> todo = overflow.sublist(stackSize, overflow.size());
        KnowledgeBaseImpl knowledgebase = context.knowledgebase();
        while (todo.size() > 0) {
            PredicateImpl predicate = todo.last();
            conclusion = predicate.fixpoint(knowledgebase.getRules(predicate), context.pushOnStack(predicate));
            overflow = conclusion.stackOverflow();
            if (overflow != null) {
                todo = todo.appendList(overflow.sublist(stackSize, overflow.size()));
            } else {
                knowledgebase.memoization(predicate, conclusion);
                todo = todo.removeLast();
            }
        }
        return conclusion;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Conclusion fixpoint(List<RuleImpl> rules, InferContext context) {
        Conclusion conclusion = Conclusion.EMPTY, next;
        Set<PredicateImpl> added = Set.of();
        boolean cycle = false;
        do {
            next = inferRules(rules, added.isEmpty() ? context : context.putCycleConclusion(this, added));
            if (next.hasStackOverflow()) {
                return next;
            }
            added = next.positive().removeAll(conclusion.positive());
            cycle |= conclusion == Conclusion.EMPTY && !added.isEmpty() && next.hasCycleWith(this);
            if (cycle && conclusion == Conclusion.EMPTY) {
                conclusion = Conclusion.of(added);
            } else {
                conclusion = conclusion.add(next);
            }
        } while (cycle && !added.isEmpty());
        return conclusion;
    }

    @SuppressWarnings("rawtypes")
    private Conclusion inferRules(List<RuleImpl> rules, InferContext context) {
        Conclusion conclusion = Conclusion.EMPTY, eval;
        for (RuleImpl rule : rules) {
            eval = rule.infer(this, context);
            if (eval.hasStackOverflow()) {
                return eval;
            } else {
                conclusion = conclusion.add(eval);
            }
        }
        return conclusion;
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

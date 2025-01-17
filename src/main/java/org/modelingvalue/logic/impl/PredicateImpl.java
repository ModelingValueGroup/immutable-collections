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
import org.modelingvalue.logic.Logic.Functor;
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

    public interface Match {
        Match EMPTY = new Match() {
            @Override
            public Set<PredicateImpl> positive() {
                return Set.of();
            }

            @Override
            public Set<List<PredicateImpl>> incomplete() {
                return Set.of();
            }
        };

        Set<PredicateImpl> positive();

        Set<List<PredicateImpl>> incomplete();

        static Match of(Set<PredicateImpl> positive, Set<List<PredicateImpl>> incomplete) {
            return new Match() {
                @Override
                public Set<PredicateImpl> positive() {
                    return positive;
                }

                @Override
                public Set<List<PredicateImpl>> incomplete() {
                    return incomplete;
                }
            };
        }

        default Match positive(Set<PredicateImpl> positive) {
            return of(positive, incomplete());
        }

        default Match incomplete(Set<List<PredicateImpl>> incomplete) {
            return of(positive(), incomplete);
        }

        default Match add(Match match) {
            return of(positive().addAll(match.positive()), incomplete().addAll(match.incomplete()));
        }

        default boolean hasCycleWith(PredicateImpl predicate) {
            return incomplete().anyMatch(l -> l.last().equals(predicate));
        }

        default Optional<List<PredicateImpl>> stackOverflow() {
            return incomplete().findAny(l -> l.size() >= MAX_LOGIC_DEPTH);
        }

        default boolean hasStackOverflow() {
            return incomplete().anyMatch(l -> l.size() >= MAX_LOGIC_DEPTH);
        }
    }

    public interface Context {
        DatabaseImpl database();

        List<PredicateImpl> stack();

        Map<PredicateImpl, Match> cyclic();

        static Context of(DatabaseImpl database, List<PredicateImpl> stack, Map<PredicateImpl, Match> cyclic) {
            return new Context() {
                @Override
                public DatabaseImpl database() {
                    return database;
                }

                @Override
                public List<PredicateImpl> stack() {
                    return stack;
                }

                @Override
                public Map<PredicateImpl, Match> cyclic() {
                    return cyclic;
                }
            };
        }

        default Context stack(PredicateImpl predicate) {
            return of(database(), stack().append(predicate), cyclic());
        }

        default Context cycle(PredicateImpl predicate, Set<PredicateImpl> found) {
            return of(database(), stack(), cyclic().put(predicate, Match.EMPTY.positive(found)));
        }
    }

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
    public Match incomplete() {
        return Match.EMPTY.incomplete(Set.of(List.of(this)));
    }

    private Match incomplete(Context context) {
        return Match.EMPTY.incomplete(Set.of(context.stack().append(this)));
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

    public Match match(PredicateImpl declaration, Context context) {
        FunctorImpl<Predicate> functor = functor();
        LogicLambda logic = functor.logic();
        if (logic != null) {
            return logic.apply((PredicateImpl) this);
        }
        int nrOfUnbound = nrOfUnbound();
        if (nrOfUnbound > 1 || (nrOfUnbound == 1 && functor.args().size() == 1)) {
            return incomplete(context);
        }
        DatabaseImpl database = context.database();
        Match match = database.getFacts(this);
        if (match != null) {
            return match;
        }
        List<RuleImpl> rules = database.getRules(this);
        if (rules != null) {
            match = context.cyclic().get(this);
            if (match != null) {
                return match;
            }
            match = database.getMemoiz(this);
            if (match != null) {
                return match;
            }
            List<PredicateImpl> stack = context.stack();
            if (stack.size() >= MAX_LOGIC_DEPTH || stack.lastIndexOf(this) >= 0) {
                return incomplete(context);
            }
            match = fixpoint(rules, context.stack(this));
            if (stack.size() >= MAX_LOGIC_DEPTH_D2) {
                Optional<List<PredicateImpl>> overflow = match.stackOverflow();
                if (overflow.isPresent()) {
                    if (stack.size() == MAX_LOGIC_DEPTH_D2) {
                        return flatten(match, overflow, context);
                    }
                    return match;
                }
            }
            database.memoization(this, match);
            return match;
        }
        return Match.EMPTY;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Match flatten(Match match, Optional<List<PredicateImpl>> overflow, Context context) {
        List<PredicateImpl> list = overflow.get(), stack = context.stack(), todo = list.sublist(stack.size(), list.size());
        DatabaseImpl database = context.database();
        while (todo.size() > 0) {
            PredicateImpl predicate = todo.last();
            match = predicate.fixpoint(database.getRules(predicate), context.stack(predicate));
            overflow = match.stackOverflow();
            if (overflow.isPresent()) {
                list = overflow.get();
                todo = todo.appendList(list.sublist(stack.size(), list.size()));
            } else {
                database.memoization(predicate, match);
                todo = todo.removeLast();
            }
        }
        return match;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Match fixpoint(List<RuleImpl> rules, Context context) {
        Match match = Match.EMPTY, next;
        Set<PredicateImpl> added = Set.of();
        boolean cycle = false;
        do {
            next = evalRules(rules, added.isEmpty() ? context : context.cycle(this, added));
            if (next.hasStackOverflow()) {
                return next;
            }
            added = next.positive().removeAll(match.positive());
            cycle |= match == Match.EMPTY && !added.isEmpty() && next.hasCycleWith(this);
            if (cycle && match == Match.EMPTY) {
                match = match.positive(added);
            } else {
                match = match.add(next);
            }
        } while (cycle && !added.isEmpty());
        return match;
    }

    @SuppressWarnings("rawtypes")
    private Match evalRules(List<RuleImpl> rules, Context context) {
        Match match = Match.EMPTY, eval;
        for (RuleImpl rule : rules) {
            eval = rule.eval(this, context);
            if (eval.hasStackOverflow()) {
                return eval;
            } else {
                match = match.add(eval);
            }
        }
        return match;
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

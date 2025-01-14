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

package org.modelingvalue.logic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.struct.impl.Struct2Impl;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.ContextPool;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.logic.Logic.Predicate;
import org.modelingvalue.logic.Logic.Rule;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.impl.FunctorImpl;
import org.modelingvalue.logic.impl.OrImpl;
import org.modelingvalue.logic.impl.PredicateImpl;
import org.modelingvalue.logic.impl.RuleImpl;
import org.modelingvalue.logic.impl.StructureImpl;

@SuppressWarnings("rawtypes")
public final class Database {

    public static final Context<Database>                                                  CURRENT             = Context.of();

    private static final ContextPool                                                       POOL                = ContextThread.createPool();
    @SuppressWarnings("rawtypes")
    private static final AtomicReference<Map<Class, Set<Class>>>                           SPECIALIZATIONS     = new AtomicReference<>(Map.of());
    @SuppressWarnings("rawtypes")
    private static final QualifiedSet<PredicateImpl, Memoization>                          EMPTY_MEMOIZ        = QualifiedSet.of(Memoization::pred);
    private static final int                                                               MAX_LOGIC_MEMOIZ    = Integer.getInteger("MAX_LOGIC_MEMOIZ", 512);
    private static final int                                                               MAX_LOGIC_MEMOIZ_D4 = Database.MAX_LOGIC_MEMOIZ / 4;
    private static final int                                                               INITIAL_USAGE_COUNT = Integer.getInteger("INITIAL_USAGE_COUNT", 4);
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

    @SuppressWarnings("rawtypes")
    private static class Memoization extends Struct2Impl<PredicateImpl, Set<PredicateImpl>> {
        private static final long serialVersionUID = 1531759272582548244L;

        public int                count            = INITIAL_USAGE_COUNT;

        public Memoization(PredicateImpl t, Set<PredicateImpl> s) {
            super(t, s);
        }

        public PredicateImpl pred() {
            return get0();
        }

        public Set<PredicateImpl> match() {
            return get1();
        }

        protected boolean keep() {
            return count-- > 0;
        }
    }

    private static final class LogicTask extends ForkJoinTask<Database> {
        private static final long serialVersionUID = -1375078574164947441L;

        private final Runnable    runnable;
        private final Database    database;

        public LogicTask(Runnable runnable, Database init) {
            this.runnable = runnable;
            this.database = new Database(init);
        }

        @Override
        public Database getRawResult() {
            return database;
        }

        @Override
        protected void setRawResult(Database database) {
        }

        @Override
        protected boolean exec() {
            CURRENT.run(database, runnable);
            database.stopped = true;
            return true;
        }
    }

    public static final Database run(Runnable runnable, Database init) {
        return POOL.invoke(new LogicTask(runnable, init));
    }

    @SuppressWarnings("rawtypes")
    public static <F extends Structure> void updateSpecializations(Class type) {
        if (!SPECIALIZATIONS.get().containsKey(type)) {
            SPECIALIZATIONS.updateAndGet(m -> addToSpecializations(m, type));
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class, Set<Class>> addToSpecializations(Map<Class, Set<Class>> specs, Class type) {
        if (!specs.containsKey(type)) {
            specs = specs.put(type, Set.of());
            for (java.lang.reflect.Type g : type.getGenericInterfaces()) {
                while (g instanceof ParameterizedType) {
                    g = ((ParameterizedType) g).getRawType();
                }
                if (g instanceof Class && !g.equals(Structure.class)) {
                    specs = addToSpecializations(specs, (Class) g);
                    specs = specs.put((Class) g, specs.get((Class) g).add(type));
                }
            }
        }
        return specs;
    }

    private final AtomicReference<Map<PredicateImpl, Set<PredicateImpl>>>     facts;
    private final AtomicReference<Map<PredicateImpl, List<RuleImpl>>>         rules;
    private final AtomicReference<QualifiedSet<PredicateImpl, Memoization>[]> memoization;

    private boolean                                                           stopped;

    @SuppressWarnings("unchecked")
    public Database(Database init) {
        facts = new AtomicReference<>(init != null ? init.facts.get() : Map.of());
        rules = new AtomicReference<>(init != null ? init.rules.get() : Map.of());
        memoization = new AtomicReference<>(init != null ? init.memoization.get() : new QualifiedSet[]{EMPTY_MEMOIZ, EMPTY_MEMOIZ, EMPTY_MEMOIZ});
    }

    public Set<PredicateImpl> getFacts(PredicateImpl pred) {
        return facts.get().get(pred);
    }

    public List<RuleImpl> getRules(PredicateImpl pred) {
        return rules.get().get(pred.signature());
    }

    public Set<PredicateImpl> getMemoiz(PredicateImpl pred) {
        for (QualifiedSet<PredicateImpl, Memoization> m : memoization.get()) {
            Memoization memoiz = m.get(pred);
            if (memoiz != null) {
                memoiz.count++;
                return memoiz.match();
            }
        }
        return null;
    }

    public Map<Structure, List<Rule>> rules() {
        return rules.get().replaceAll(e -> {
            Structure k = e.getKey().proxy();
            List<Rule> v = e.getValue().replaceAll(RuleImpl::proxy);
            return Entry.of(k, v);
        });
    }

    public Map<Structure, Set<Structure>> facts() {
        return facts.get().replaceAll(e -> {
            Structure k = e.getKey().proxy();
            Set<Structure> v = e.getValue().replaceAll(StructureImpl::proxy);
            return Entry.of(k, v);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void memoization(PredicateImpl pred, Set<PredicateImpl> match) {
        if (match.noneMatch(PredicateImpl::isIncomplete)) {
            FunctorImpl<Predicate> functor = pred.functor();
            Set<PredicateImpl> set = (Set) match;
            if (functor.factual()) {
                facts.updateAndGet(m -> {
                    m = m.put(pred, set);
                    for (PredicateImpl e : set) {
                        m = m.put(e, Set.of(e));
                    }
                    return m;
                });
            } else if (!functor.derived()) {
                QualifiedSet<PredicateImpl, Memoization>[] mem = memoization.updateAndGet(a -> {
                    a = a.clone();
                    if (a[0].size() >= MAX_LOGIC_MEMOIZ_D4) {
                        a[2] = a[2].putAll(a[1]);
                        a[1] = a[0];
                        a[0] = EMPTY_MEMOIZ;
                    }
                    a[0] = a[0].put(new Memoization(pred, set));
                    for (PredicateImpl e : set) {
                        a[0] = a[0].put(new Memoization(e, Set.of(e)));
                    }
                    return a;
                });
                if (mem[2].size() > MAX_LOGIC_MEMOIZ && mem[0].size() == set.size() + 1) {
                    POOL.execute(this::cleanup);
                }
            }
        }
    }

    private void cleanup() {
        QualifiedSet<PredicateImpl, Memoization>[] mem = memoization.get();
        while (mem[2].size() > MAX_LOGIC_MEMOIZ) {
            for (int i = 0; i < mem[2].size(); i++) {
                if (stopped) {
                    return;
                }
                Memoization m = mem[2].get(i);
                if (!m.keep()) {
                    mem = memoization.updateAndGet(a -> {
                        a = a.clone();
                        a[2] = a[2].removeKey(m.pred());
                        return a;
                    });
                    i--;
                }
            }
        }
    }

    public void addRule(RuleImpl ruleImpl) {
        Map<Class, Set<Class>> specs = SPECIALIZATIONS.get();
        PredicateImpl consImpl = ruleImpl.cons();
        rules.updateAndGet(m -> addRule(consImpl.signature(), ruleImpl, m, specs));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<PredicateImpl, List<RuleImpl>> addRule(PredicateImpl signature, RuleImpl ruleImpl, Map<PredicateImpl, List<RuleImpl>> rules, Map<Class, Set<Class>> specs) {
        rules = rules.put(signature, ADD_RULE.apply(rules.get(signature), ruleImpl));
        for (int i = 1; i < signature.length(); i++) {
            Object v = signature.get(i);
            if (v instanceof Class) {
                for (Class g : specs.get((Class) v)) {
                    PredicateImpl p = signature.set(i, g);
                    rules = addRule(p, ruleImpl, rules, specs);
                }
            }
        }
        return rules;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public final void addFact(PredicateImpl pred) {
        FunctorImpl<Predicate> functor = pred.functor();
        if (functor.logic() != null) {
            throw new IllegalArgumentException("No facts of a functor with a logic lambda allowed. " + this);
        }
        if (getFacts(pred.signature()) != null) {
            throw new IllegalArgumentException("No facts of a functor with rules allowed. " + this);
        }
        facts.updateAndGet(m -> {
            List<Class> args = functor.args();
            m = m.put(pred, ADD_FACT.apply(m.get(pred), pred));
            for (int i = 1; i < pred.length(); i++) {
                m = addFact(m, pred, pred.set(i, pred.getType(i)), i, args.get(i - 1));
            }
            return m;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<PredicateImpl, Set<PredicateImpl>> addFact(Map<PredicateImpl, Set<PredicateImpl>> m, PredicateImpl pred, PredicateImpl ptrn, int i, Class a) {
        Class t = ptrn.getType(i);
        if (a.isAssignableFrom(t)) {
            m = m.put(ptrn, ADD_FACT.apply(m.get(ptrn), pred));
            if (!a.equals(t)) {
                for (Type g : t.getGenericInterfaces()) {
                    while (g instanceof ParameterizedType) {
                        g = ((ParameterizedType) g).getRawType();
                    }
                    if (g instanceof Class) {
                        m = addFact(m, pred, ptrn.set(i, g), i, a);
                    }
                }
            }
        }
        return m;
    }
}

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
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.*;
import org.modelingvalue.logic.impl.*;

public final class Logic {

    private Logic() {
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReference<Map<Class, Set<Class>>> SPECIALIZATIONS     = new AtomicReference<>(Map.of());

    public static final int                                      MAX_LOGIC_MEMOIZ    = Integer.getInteger("MAX_LOGIC_MEMOIZ", 512);
    public static final int                                      INITIAL_USAGE_COUNT = Integer.getInteger("INITIAL_USAGE_COUNT", 4);
    public static final int                                      MAX_LOGIC_DEPTH     = Integer.getInteger("MAX_LOGIC_DEPTH", 32);
    public static final boolean                                  TRACE_LOGIC         = Boolean.getBoolean("TRACE_LOGIC");

    public static final ContextPool                              LOGIC_POOL          = ContextThread.createPool();
    public static final Context<Database>                        DATABASE            = Context.of();

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

    // Run

    public static final Database run(Runnable runnable) {
        return run(runnable, null);
    }

    public static final Database run(Runnable runnable, Database init) {
        return LOGIC_POOL.invoke(new Database.LogicTask(runnable, init));
    }

    // Structures

    public interface Structure {
    }

    @SuppressWarnings("unchecked")
    public static <C extends Constant<T>, T extends Structure> C constant(Functor<C> functor, Object... args) {
        return new StructureImpl<C>(functor, args).normal().proxy();
    }

    @SuppressWarnings("unchecked")
    public static <F extends Function<T>, T extends Structure> F function(Functor<F> functor, Object... args) {
        return new StructureImpl<F>(functor, args).normal().proxy();
    }

    // Functor

    public interface Functor<T extends Structure> extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure> Functor<T> functor(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        return FunctorImpl.of(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A> Functor<T> functor(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        return FunctorImpl.of(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B> Functor<T> functor(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        return FunctorImpl.of(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B, C> Functor<T> functor(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        return FunctorImpl.of(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B, C, D> Functor<T> functor(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
        return FunctorImpl.of(method, modifiers).proxy();
    }

    public interface FunctorModifier {
    }

    public enum FunctorModifierEnum implements FunctorModifier {
        factual,
        derived,
    }

    @SuppressWarnings("rawtypes")
    @FunctionalInterface
    public interface LogicLambda extends java.util.function.Function<PredicateImpl, Set<PredicateImpl>>, LambdaReflection, FunctorModifier {

        @Override
        default LogicLambdaImpl of() {
            return this instanceof LogicLambdaImpl ? (LogicLambdaImpl) this : new LogicLambdaImpl(this);
        }

        class LogicLambdaImpl extends LambdaImpl<LogicLambda> implements LogicLambda {
            private static final long serialVersionUID = 3085315666688472574L;

            public LogicLambdaImpl(LogicLambda f) {
                super(f);
            }

            @SuppressWarnings("unchecked")
            @Override
            public final Set<PredicateImpl> apply(PredicateImpl t) {
                return f.apply(t);
            }

        }
    }

    @SuppressWarnings("rawtypes")
    @FunctionalInterface
    public interface NormalizeLambda extends UnaryOperator<StructureImpl<Structure>>, LambdaReflection, FunctorModifier {

        @Override
        default NormalizeLambdaImpl of() {
            return this instanceof NormalizeLambdaImpl ? (NormalizeLambdaImpl) this : new NormalizeLambdaImpl(this);
        }

        class NormalizeLambdaImpl extends LambdaImpl<NormalizeLambda> implements NormalizeLambda {
            private static final long serialVersionUID = -9099528018203410620L;

            public NormalizeLambdaImpl(NormalizeLambda f) {
                super(f);
            }

            @SuppressWarnings("unchecked")
            @Override
            public final StructureImpl<Structure> apply(StructureImpl<Structure> t) {
                return f.apply(t);
            }
        }
    }

    // Variables

    public interface Variable extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <F extends Structure> F var(Class<F> type, String id) {
        return new VariableImpl<F>(type, id).proxy();
    }

    // Predicates

    public interface Predicate extends Structure {
    }

    public interface Relation extends Predicate {
    }

    public static boolean isTrue(Predicate pred) {
        return match(pred).anyMatch(t -> !t.isIncomplete());
    }

    public static boolean isFalse(Predicate pred) {
        return match(pred).isEmpty();
    }

    public static boolean isIncomplete(Predicate pred) {
        return match(pred).anyMatch(PredicateImpl::isIncomplete);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Set<Map<Variable, Object>> getBindings(Predicate pred) {
        PredicateImpl impl = StructureImpl.<Predicate, PredicateImpl> unproxy(pred);
        Set<PredicateImpl> match = match(impl);
        Set<Map<VariableImpl, Object>> bindings = match.replaceAll(m -> m.isIncomplete() ? Map.of(Entry.of(INCOMPLETE_VAR, m)) : impl.getBinding(m, Map.of()));
        return bindings.replaceAll(m -> m.replaceAll(e -> Entry.of((Variable) e.getKey().proxy(), proxy(e.getValue()))));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<PredicateImpl> match(Predicate pred) {
        return match(StructureImpl.<Predicate, PredicateImpl> unproxy(pred));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<PredicateImpl> match(PredicateImpl impl) {
        return impl.setBinding(impl, impl.variables()).match(impl, List.of(), Map.of(), DATABASE.get());
    }

    @SuppressWarnings("rawtypes")
    private static final Object proxy(Object object) {
        return object instanceof StructureImpl ? ((StructureImpl) object).proxy() : object;
    }

    @SuppressWarnings("unchecked")
    public static <P extends Predicate> P pred(Functor<P> functor, Object... args) {
        return (P) new PredicateImpl((Functor<Predicate>) functor, args).proxy();
    }

    // Collect

    private static final FunctorImpl<Predicate> COLLECT_FUNCTOR       = FunctorImpl.<Predicate, Predicate, Predicate> of(Logic::collect);
    private static final Functor<Predicate>     COLLECT_FUNCTOR_PROXY = COLLECT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Predicate collect(Predicate pred, Predicate accum) {
        return new CollectImpl(COLLECT_FUNCTOR_PROXY, pred, accum).proxy();
    }

    // Yes

    private static final FunctorImpl<Predicate> YES_FUNCTOR = FunctorImpl.<Predicate> of(Logic::yes);
    private static final TrueImpl               YES         = new TrueImpl(YES_FUNCTOR);
    private static final Predicate              YES_PROXY   = (Predicate) Proxy.newProxyInstance(Predicate.class.getClassLoader(), new Class[]{Predicate.class}, YES);

    @SuppressWarnings("unchecked")
    public static Predicate yes() {
        return YES_PROXY;
    }

    // No

    private static final FunctorImpl<Predicate> NO_FUNCTOR = FunctorImpl.<Predicate> of(Logic::no);
    private static final FalseImpl              NO         = new FalseImpl(NO_FUNCTOR);
    private static final Predicate              NO_PROXY   = (Predicate) Proxy.newProxyInstance(Predicate.class.getClassLoader(), new Class[]{Predicate.class}, NO);

    @SuppressWarnings("unchecked")
    public static Predicate no() {
        return NO_PROXY;
    }

    // Not

    private static final FunctorImpl<Predicate> NOT_FUNCTOR       = FunctorImpl.<Predicate, Predicate> of(Logic::not);
    private static final Functor<Predicate>     NOT_FUNCTOR_PROXY = NOT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Predicate not(Predicate pred) {
        return new NotImpl(NOT_FUNCTOR_PROXY, pred).proxy();
    }

    // Or

    public static final FunctorImpl<Predicate> OR_FUNCTOR = FunctorImpl.<Predicate, Predicate, Predicate> of(Logic::or);

    private static Predicate or(Predicate p1, Predicate p2) {
        return new OrImpl(OR_FUNCTOR, StructureImpl.unproxy(p1), StructureImpl.unproxy(p2)).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate or(Predicate... ps) {
        PredicateImpl impl = NO;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == NO ? StructureImpl.unproxy(ps[i]) : new OrImpl(OR_FUNCTOR, StructureImpl.unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    // And

    private static final FunctorImpl<Predicate> AND_FUNCTOR = FunctorImpl.<Predicate, Predicate, Predicate> of(Logic::and);

    private static Predicate and(Predicate p1, Predicate p2) {
        return new AndImpl(AND_FUNCTOR, StructureImpl.unproxy(p1), StructureImpl.unproxy(p2)).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate and(Predicate... ps) {
        PredicateImpl impl = YES;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == YES ? StructureImpl.unproxy(ps[i]) : new AndImpl(AND_FUNCTOR, StructureImpl.unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    // Rules

    public interface Rule extends Structure {
    }

    private static final FunctorImpl<Rule> RULE_FUNCTOR       = FunctorImpl.<Rule, Relation, Predicate> of(Logic::rule);
    private static final Functor<Rule>     RULE_FUNCTOR_PROXY = RULE_FUNCTOR.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Rule rule(Relation consequence, Predicate condition) {
        RuleImpl ruleImpl = new RuleImpl(RULE_FUNCTOR_PROXY, consequence, condition);
        PredicateImpl consImpl = StructureImpl.<Predicate, PredicateImpl> unproxy(consequence);
        PredicateImpl signature = consImpl.signature();
        Map<Class, Set<Class>> specs = SPECIALIZATIONS.get();
        Database database = DATABASE.get();
        database.rules.updateAndGet(m -> signature.addRule(ruleImpl, m, specs));
        return ruleImpl.proxy();
    }

    // Incomplete

    public interface Incomplete extends Predicate {
    }

    @SuppressWarnings("rawtypes")
    public static final FunctorImpl<Incomplete>   INCOMPLETE_FUNCTOR       = FunctorImpl.<Incomplete, List<Predicate>> of(Logic::incomplete);
    private static final Functor<Incomplete>      INCOMPLETE_FUNCTOR_PROXY = INCOMPLETE_FUNCTOR.proxy();
    private static final VariableImpl<Incomplete> INCOMPLETE_VAR           = new VariableImpl<Incomplete>(Incomplete.class, "I");
    private static final Incomplete               INCOMPLETE_VAR_PROXY     = INCOMPLETE_VAR.proxy();

    @SuppressWarnings("unchecked")
    public static Incomplete incompleteVar() {
        return INCOMPLETE_VAR_PROXY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Incomplete incomplete(List<Predicate> der) {
        return pred(INCOMPLETE_FUNCTOR_PROXY, der);
    }

    // Facts

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void fact(Relation pred) {
        StructureImpl.<Predicate, PredicateImpl> unproxy(pred).makeFact(DATABASE.get());
    }

    // Bindings

    public static Map<Variable, Object> incomplete(Predicate... der) {
        return Map.of(Entry.of((Variable) incompleteVar(), incomplete(List.of(der))));
    }

    public static Map<Variable, Object> binding(Structure... varVal) {
        Map<Variable, Object> b = Map.of();
        for (int i = 0; i < varVal.length; i += 2) {
            b = b.add(Entry.of((Variable) varVal[i], varVal[i + 1]));
        }
        return b;
    }

    // Equals

    public interface Constant<T extends Structure> extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> EQ = Logic.<Predicate, Constant, Constant> functor(Logic::eq, (LogicLambda) t -> {
        StructureImpl at = t.getStruct(1);
        StructureImpl bt = t.getStruct(2);
        if (at == null && bt == null) {
            return t.incomplete();
        } else if (at == null) {
            return Set.of(t.set(1, bt));
        } else if (bt == null) {
            return Set.of(t.set(2, at));
        } else {
            StructureImpl eq = at.eq(bt);
            return eq == null ? Set.of() : Set.of(t.set(1, eq).set(2, eq));
        }
    });

    @SuppressWarnings("rawtypes")
    public static <T extends Structure> Predicate eq(Constant<T> a, Constant<T> b) {
        return pred(EQ, a, b);
    }

    // Is

    public interface Function<T extends Structure> extends Structure {
    }

    @SuppressWarnings("rawtypes")
    private static final Functor<Relation> IS = Logic.<Relation, Structure, Structure> functor(Logic::is);

    private static <T extends Structure> Relation is(T a, T b) {
        return pred(IS, a, b);
    }

    // Use this one for function definitions
    public static <T extends Structure> Relation is(T a, Constant<T> b) {
        return pred(IS, a, b);
    }

    // Implied by the above using the generic rules here
    public static <T extends Structure> Relation is(Constant<T> a, T b) {
        return pred(IS, a, b);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void isRules() {
        Constant A1 = var(Constant.class, "A1");
        Constant A2 = var(Constant.class, "A2");
        Function F1 = var(Function.class, "F1");
        Function F2 = var(Function.class, "F2");

        rule(is((Structure) A1, (Structure) A2), eq(A1, A2));
        rule(is(F1, F2), and(is(F2, A2), is(F1, A2)));
        rule(is(A1, F1), is(F1, A1));
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.struct.impl.Struct2Impl;
import org.modelingvalue.collections.struct.impl.StructImpl;
import org.modelingvalue.collections.util.*;
import org.modelingvalue.collections.util.SerializableBiFunction.SerializableBiFunctionImpl;
import org.modelingvalue.collections.util.SerializableFunction.SerializableFunctionImpl;
import org.modelingvalue.collections.util.SerializableQuadFunction.SerializableQuadFunctionImpl;
import org.modelingvalue.collections.util.SerializableSupplier.SerializableSupplierImpl;
import org.modelingvalue.collections.util.SerializableTriFunction.SerializableTriFunctionImpl;

public final class Logic {

    private Logic() {
    }

    public interface FunctorModifier {
    }

    public enum FunctorModifierEnum implements FunctorModifier {
        factual,
        derived;
    }

    @SuppressWarnings("rawtypes")
    @FunctionalInterface
    public interface LogicLambda extends Function<TermImpl, Collection<TermImpl>>, LambdaReflection, FunctorModifier {

        @Override
        default LogicLambdaImpl of() {
            return this instanceof LogicLambdaImpl ? (LogicLambdaImpl) this : new LogicLambdaImpl(this);
        }

        class LogicLambdaImpl extends LambdaImpl<LogicLambda> implements LogicLambda {
            private static final long serialVersionUID = 3085315666688472574L;

            public LogicLambdaImpl(LogicLambda f) {
                super(f);
            }

            @Override
            public final Collection<TermImpl> apply(TermImpl t) {
                return f.apply(t);
            }

        }
    }

    private static final int                                                  MAX_LOGIC_MEMOIZ    = Integer.getInteger("MAX_LOGIC_MEMOIZ", 512);
    private static final int                                                  MAX_LOGIC_MEMOIZ_D4 = MAX_LOGIC_MEMOIZ / 4;
    private static final int                                                  INITIAL_USAGE_COUNT = Integer.getInteger("INITIAL_USAGE_COUNT", 4);

    private static final int                                                  MAX_LOGIC_DEPTH     = Integer.getInteger("MAX_LOGIC_DEPTH", 32);
    private static final int                                                  MAX_LOGIC_DEPTH_D2  = MAX_LOGIC_DEPTH / 2;

    private static final boolean                                              TRACE_LOGIC         = Boolean.getBoolean("TRACE_LOGIC");

    private static final ContextPool                                          LOGIC_POOL          = ContextThread.createPool();
    private static final Context<Database>                                    DATABASE            = Context.of();

    @SuppressWarnings("rawtypes")
    private static final BiFunction<Set<TermImpl>, TermImpl, Set<TermImpl>>   ADD_FACT            = (s, e) -> s == null ? Set.of(e) : s.add(e);
    private static final BiFunction<List<RuleImpl>, RuleImpl, List<RuleImpl>> ADD_RULE            = (l, e) -> {
                                                                                                      if (l == null) {
                                                                                                          return List.of(e);
                                                                                                      } else {
                                                                                                          int p = e.rulePrio();
                                                                                                          for (int i = 0; i < l.size(); i++) {
                                                                                                              RuleImpl r = l.get(i);
                                                                                                              if (r.equals(e)) {
                                                                                                                  return l;
                                                                                                              } else if (r.rulePrio() > p) {
                                                                                                                  return l.insert(i, e);
                                                                                                              }
                                                                                                          }
                                                                                                          return l.append(e);
                                                                                                      }
                                                                                                  };

    public static final Database run(Runnable runnable) {
        return run(runnable, null);
    }

    public static final Database run(Runnable runnable, Database init) {
        return LOGIC_POOL.invoke(new LogicTask(runnable, init));
    }

    private static final class LogicTask extends ForkJoinTask<Database> {
        private static final long serialVersionUID = -1375078574164947441L;

        private final Runnable    runnable;
        private final Database    database;

        private LogicTask(Runnable runnable, Database init) {
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
            DATABASE.run(database, runnable);
            database.stopped = true;
            return true;
        }
    }

    @SuppressWarnings("rawtypes")
    private static final QualifiedSet<TermImpl, Memoiz> EMPTY_MEMOIZ = QualifiedSet.of(Memoiz::term);

    @SuppressWarnings("rawtypes")
    private static class Memoiz extends Struct2Impl<TermImpl, Set<TermImpl>> {
        private static final long serialVersionUID = 1531759272582548244L;

        private int               count            = INITIAL_USAGE_COUNT;

        private Memoiz(TermImpl t, Set<TermImpl> s) {
            super(t, s);
        }

        private TermImpl term() {
            return get0();
        }

        private Set<TermImpl> set() {
            return get1();
        }

        protected boolean keep() {
            return count-- > 0;
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class Database {
        private final AtomicReference<Map<TermImpl, Set<TermImpl>>>     facts;
        private final AtomicReference<Map<FunctImpl, List<RuleImpl>>>   rules;
        private final AtomicReference<QualifiedSet<TermImpl, Memoiz>[]> memoiz;

        private boolean                                                 stopped;

        @SuppressWarnings("unchecked")
        private Database(Database init) {
            facts = new AtomicReference<>(init != null ? init.facts.get() : Map.of());
            rules = new AtomicReference<>(init != null ? init.rules.get() : Map.of());
            memoiz = new AtomicReference<>(init != null ? init.memoiz.get() : new QualifiedSet[]{EMPTY_MEMOIZ, EMPTY_MEMOIZ, EMPTY_MEMOIZ});
        }

        protected void cleanup() {
            QualifiedSet<TermImpl, Memoiz>[] mem = memoiz.get();
            while (mem[2].size() > MAX_LOGIC_MEMOIZ) {
                for (int i = 0; i < mem[2].size(); i++) {
                    if (stopped) {
                        return;
                    }
                    Memoiz m = mem[2].get(i);
                    if (!m.keep()) {
                        mem = memoiz.updateAndGet(a -> {
                            a = a.clone();
                            a[2] = a[2].removeKey(m.term());
                            return a;
                        });
                        i--;
                    }
                }
            }
        }
    }

    // Clauses

    private static abstract class ClauseImpl<F extends Term> extends StructImpl implements InvocationHandler, Comparable<ClauseImpl<F>> {
        private static final long   serialVersionUID = 7315776001191198132L;

        private static final Method EQUALS;
        private static final Method HASHCODE;
        private static final Method TO_STRING;
        static {
            try {
                EQUALS = Object.class.getMethod("equals", Object.class);
                HASHCODE = Object.class.getMethod("hashCode");
                TO_STRING = Object.class.getMethod("toString");
            } catch (NoSuchMethodException | SecurityException e) {
                throw new Error(e);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(EQUALS)) {
                if (proxy == args[0]) {
                    return true;
                } else if (args[0] == null) {
                    return false;
                } else if (args[0].getClass() != proxy.getClass()) {
                    return false;
                } else {
                    return super.equals(Logic.unproxy(args[0]));
                }
            } else if (method.equals(HASHCODE)) {
                return super.hashCode();
            } else if (method.equals(TO_STRING)) {
                return toString();
            } else {
                throw new Error("No handler for " + method);
            }
        }

        protected ClauseImpl(Functor<F> functor, Object... args) {
            super(unproxy(functor, args));
        }

        protected ClauseImpl(FunctImpl<F> functor, Object... args) {
            super(array(functor, args));
        }

        protected ClauseImpl(Class<F> type, Object... args) {
            super(array(type, args));
        }

        protected ClauseImpl(Object[] args) {
            super(args);
        }

        private static final Object[] array(Object functor, Object[] args) {
            Object[] result = new Object[args.length + 1];
            result[0] = noProxy(functor);
            for (int i = 0; i < args.length; i++) {
                result[i + 1] = noProxy(args[i]);
            }
            return result;
        }

        @SuppressWarnings("rawtypes")
        private static final Object[] unproxy(Functor functor, Object[] args) {
            Object[] result = new Object[args.length + 1];
            result[0] = Logic.unproxy(functor);
            for (int i = 0; i < args.length; i++) {
                result[i + 1] = Logic.unproxy(args[i]);
            }
            return result;
        }

        protected abstract F proxy();

        protected abstract Class<F> type();

        protected abstract ClauseImpl<F> term(Object[] array);

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public int compareTo(ClauseImpl<F> o) {
            int r = length() - o.length();
            if (r != 0) {
                return r;
            }
            for (int i = 0; i < length(); i++) {
                Object tv = get(i);
                Object ov = o.get(i);
                if (tv instanceof Comparable && tv.getClass().equals(ov.getClass())) {
                    r = ((Comparable) tv).compareTo(ov);
                    if (r != 0) {
                        break;
                    }
                } else {
                    String ts = StringUtil.toString(tv);
                    String os = StringUtil.toString(ov);
                    r = ts.compareTo(os);
                    if (r != 0) {
                        break;
                    }
                }
            }
            return r;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected ClauseImpl<F> eq(ClauseImpl<F> other) {
            if (this == other) {
                return this;
            }
            Object[] array = toArray();
            for (int i = 0; i < array.length; i++) {
                Object tv = get(i);
                Object ov = other.get(i);
                if (tv != ov) {
                    if (tv instanceof ClauseImpl && ov instanceof ClauseImpl) {
                        ClauseImpl eq = ((ClauseImpl) tv).eq((ClauseImpl) ov);
                        if (eq != null) {
                            array[i] = eq;
                        } else {
                            return null;
                        }
                    } else if (tv instanceof ClauseImpl && ov instanceof Class) {
                        if (((Class) ov).isAssignableFrom(((ClauseImpl) tv).type())) {
                            array[i] = tv;
                        } else {
                            return null;
                        }
                    } else if (tv instanceof Class && ov instanceof ClauseImpl) {
                        if (((Class) tv).isAssignableFrom(((ClauseImpl) ov).type())) {
                            array[i] = ov;
                        } else {
                            return null;
                        }
                    } else if (!(tv instanceof Class) && ov instanceof Class) {
                        if (((Class) ov).isAssignableFrom(tv.getClass())) {
                            array[i] = tv;
                        } else {
                            return null;
                        }
                    } else if (tv instanceof Class && !(ov instanceof Class)) {
                        if (((Class) tv).isAssignableFrom(ov.getClass())) {
                            array[i] = ov;
                        } else {
                            return null;
                        }
                    } else if (!Objects.equals(tv, ov)) {
                        return null;
                    }
                }
            }
            return term(array);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Object noProxy(Object object) {
        if (object instanceof Term) {
            throw new IllegalArgumentException();
        } else if (object instanceof List) {
            for (Object e : (List) object) {
                noProxy(e);
            }
            return object;
        } else {
            return object;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static final Object unproxy(Object object) {
        if (object instanceof Term) {
            return Proxy.getInvocationHandler(object);
        } else if (object instanceof List) {
            List l = List.of();
            for (Object e : (List) object) {
                l = l.add(unproxy(e));
            }
            return l;
        } else {
            Objects.requireNonNull(object);
            return object;
        }
    }

    @SuppressWarnings("unchecked")
    private static final <T extends Term, R extends ClauseImpl<T>> R unproxy(T object) {
        return (R) Proxy.getInvocationHandler(object);
    }

    @SuppressWarnings("rawtypes")
    private static final Object proxy(Object object) {
        if (object instanceof ClauseImpl) {
            return ((ClauseImpl) object).proxy();
        } else {
            return object;
        }
    }

    // Functor

    public interface Functor<T> extends Term {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Term> FunctImpl<T> functImpl(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        SerializableSupplierImpl<T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term> Functor<T> functor(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Term, A> FunctImpl<T> functImpl(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        SerializableFunctionImpl<A, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A> Functor<T> functor(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Term, A, B> FunctImpl<T> functImpl(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        SerializableBiFunctionImpl<A, B, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A, B> Functor<T> functor(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Term, A, B, C> FunctImpl<T> functImpl(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        SerializableTriFunctionImpl<A, B, C, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A, B, C> Functor<T> functor(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Term, A, B, C, D> FunctImpl<T> functImpl(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
        SerializableQuadFunctionImpl<A, B, C, D, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A, B, C, D> Functor<T> functor(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    public static final class FunctImpl<T extends Term> extends ClauseImpl<Functor<T>> {
        private static final long serialVersionUID = 285147889847599160L;

        private final LogicLambda lambda;
        private final boolean     factual;
        private final boolean     derived;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private FunctImpl(Class<T> type, String name, List<Class<?>> args, FunctorModifier... modifiers) {
            super((Class) Functor.class, type, name, args);
            this.lambda = lambda(modifiers);
            this.factual = has(FunctorModifierEnum.factual, modifiers);
            this.derived = has(FunctorModifierEnum.derived, modifiers);
        }

        private static LogicLambda lambda(FunctorModifier... modifiers) {
            LogicLambda lambda = get(LogicLambda.class, modifiers);
            return lambda != null ? lambda.of() : null;
        }

        @SuppressWarnings("unchecked")
        private static <T extends FunctorModifier> T get(Class<T> t, FunctorModifier[] modifiers) {
            for (FunctorModifier m : modifiers) {
                if (t.isInstance(m)) {
                    return (T) m;
                }
            }
            return null;
        }

        private static boolean has(FunctorModifierEnum e, FunctorModifier[] modifiers) {
            for (FunctorModifier m : modifiers) {
                if (m == e) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private FunctImpl(Object[] args) {
            super(args);
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected final Functor<T> proxy() {
            return (Functor<T>) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Functor.class}, this);
        }

        @Override
        public String toString() {
            return ((String) get(2));
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected FunctImpl<T> term(Object[] array) {
            return new FunctImpl<T>(array);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Class<Functor<T>> type() {
            return (Class<Functor<T>>) get(0);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected List<Class> args() {
            return (List<Class>) get(3);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected LogicLambda lambda() {
            return lambda;
        }

        @SuppressWarnings("unchecked")
        protected Class<T> functType() {
            return (Class<T>) get(1);
        }
    }

    // Lists

    public interface L<E> extends Atom<L<E>> {
    }

    @SuppressWarnings("rawtypes")
    private static final FunctImpl<L> LIST_FUNCTOR       = functImpl((SerializableFunction<List, L>) Logic::l);
    @SuppressWarnings("rawtypes")
    private static final Functor<L>   LIST_FUNCTOR_PROXY = LIST_FUNCTOR.proxy();
    @SuppressWarnings("rawtypes")
    private static final TermImpl<L>  EMPTY_LIST         = termImpl(LIST_FUNCTOR, List.of());
    @SuppressWarnings("rawtypes")
    private static final L            EMPTY_LIST_PROXY   = EMPTY_LIST.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Term> L<E> l() {
        return EMPTY_LIST_PROXY;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Term> L<E> l(List<E> es) {
        return term(LIST_FUNCTOR_PROXY, es);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Term> L<E> l(E... es) {
        return l(List.of(es));
    }

    @SuppressWarnings("rawtypes")
    private static <E extends Term> TermImpl<L> lImpl(List<TermImpl<E>> es) {
        return termImpl(LIST_FUNCTOR, es);
    }

    // Add

    private static <E extends Term> List<TermImpl<E>> addOrdered(List<TermImpl<E>> l, TermImpl<E> e) {
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).compareTo(e) > 0) {
                return l.insert(i, e);
            }
        }
        return l.append(e);
    }

    private static <E extends Term> Set<List<TermImpl<E>>> remove(List<TermImpl<E>> l, TermImpl<E> e) {
        Set<List<TermImpl<E>>> ls = Set.of();
        for (int i = l.firstIndexOf(e); i >= 0; i = l.firstIndexOf(i, l.size(), e)) {
            ls = ls.add(l.removeIndex(i));
        }
        return ls;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final FunctImpl<Pred> ADD_FUNCTOR       = functImpl((SerializableTriFunction<Object, L, L, Pred>) Logic::add, (LogicLambda) t -> {
                                                               TermImpl<Term> e = t.getTerm(1);
                                                               TermImpl<L<Term>> i = t.getTerm(2);
                                                               TermImpl<L<Term>> o = t.getTerm(3);
                                                               List<TermImpl<Term>> il = i != null ? i.getVal(1) : null;
                                                               List<TermImpl<Term>> ol = o != null ? o.getVal(1) : null;
                                                               if (e != null && il != null && ol != null) {
                                                                   return addOrdered(il, e).equals(ol) ? Set.of(t) : Set.of();
                                                               } else if (e != null && il != null && ol == null) {
                                                                   return Set.of(t.set(3, lImpl(addOrdered(il, e))));
                                                               } else if (e != null && il == null && ol != null) {
                                                                   return remove(ol, e).map(r -> (TermImpl) t.set(2, r)).asSet();
                                                               } else if (e == null && il != null && ol != null) {
                                                                   if (il.anyMatch(ol::notContains)) {
                                                                       return Set.of();
                                                                   }
                                                                   return ol.removeAll(il).map(r -> (TermImpl) t.set(1, r)).asSet();
                                                               } else {
                                                                   return t.incomplete();
                                                               }
                                                           });
    @SuppressWarnings("rawtypes")
    private static final Functor<Pred>   ADD_FUNCTOR_PROXY = ADD_FUNCTOR.proxy();

    public static <E> Pred add(E e, L<E> i, L<E> o) {
        return term(ADD_FUNCTOR_PROXY, e, i, o);
    }

    // Variables

    public static interface Variable extends Term {
    }

    @SuppressWarnings("unchecked")
    public static <F extends Term> F var(Class<F> type, String id) {
        return new VarImpl<F>(type, id).proxy();
    }

    private static final class VarImpl<F extends Term> extends ClauseImpl<F> {
        private static final long serialVersionUID = -8998368070388908726L;

        private VarImpl(Class<F> type, String name) {
            super(type, name);
        }

        private VarImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final F proxy() {
            return (F) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{type(), Variable.class}, this);
        }

        @Override
        public String toString() {
            return get(1).toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected VarImpl<F> term(Object[] array) {
            return new VarImpl<F>(array);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Class<F> type() {
            return (Class<F>) get(0);
        }
    }

    // Terms

    public static interface Term {
    }

    public static interface Pred extends Term {
    }

    public static interface Rel extends Pred {
    }

    @SuppressWarnings("unchecked")
    public static <F extends Term> F term(Functor<F> functor, Object... args) {
        return new TermImpl<F>(functor, args).proxy();
    }

    private static <F extends Term> TermImpl<F> termImpl(FunctImpl<F> functor, Object... args) {
        return new TermImpl<F>(functor, args);
    }

    public static class TermImpl<F extends Term> extends ClauseImpl<F> {
        private static final long serialVersionUID = -1605559565948158856L;

        private TermImpl(Functor<F> functor, Object... args) {
            super(functor, args);
        }

        private TermImpl(FunctImpl<F> functor, Object... args) {
            super(functor, args);
        }

        private TermImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected F proxy() {
            return (F) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{type()}, this);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected TermImpl<F> term(Object[] array) {
            return new TermImpl<F>(array);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public String toString() {
            if (type() == L.class) {
                return get(1).toString().substring(4);
            } else {
                String string = super.toString();
                return string.substring(1, string.length() - 1).replaceFirst(",", "(") + ")";
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<F> type() {
            return functor().functType();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public FunctImpl<F> functor() {
            return (FunctImpl<F>) get(0);
        }

        @SuppressWarnings("rawtypes")
        protected final void makeFact(Database database) {
            if (functor().lambda() != null) {
                throw new IllegalArgumentException("No facts of a functor with a lambda allowed. " + this);
            }
            if (database.rules.get().get(functor()) != null) {
                throw new IllegalArgumentException("No facts of a functor with rules allowed. " + this);
            }
            database.facts.updateAndGet(m -> {
                m = m.put(this, ADD_FACT.apply(m.get(this), this));
                Object[] array = toArray();
                for (int i = 1; i < array.length; i++) {
                    array[i] = getType(i);
                    TermImpl<F> term = term(array);
                    m = m.put(term, ADD_FACT.apply(m.get(term), this));
                    array = toArray();
                }
                return m;
            });
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Map<VarImpl, Object> variables() {
            Map<VarImpl, Object> vars = Map.of();
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v instanceof VarImpl) {
                    vars = vars.put((VarImpl) v, ((VarImpl) v).type());
                } else if (v instanceof TermImpl) {
                    vars = vars.putAll(((TermImpl) v).variables());
                } else if (v instanceof List) {
                    for (TermImpl e : (List<TermImpl>) v) {
                        vars = vars.putAll(e.variables());
                    }
                }
            }
            return vars;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Map<VarImpl, Object> getBinding(TermImpl<F> term, Map<VarImpl, Object> vars) {
            if (get(0).equals(term.get(0))) {
                for (int i = 1; i < length(); i++) {
                    Object tv = term.get(i);
                    Class tt = typeOf(tv);
                    tv = tv instanceof Class ? null : tv;
                    if (get(i) instanceof VarImpl) {
                        VarImpl var = (VarImpl) get(i);
                        Object vv = vars.get(var);
                        Class vt = typeOf(vv);
                        vv = vv instanceof Class ? null : vv;
                        if (vv != null) {
                            if (tv != null && !tv.equals(vv)) {
                                return null;
                            }
                        } else if (tv != null) {
                            if (var.type().isAssignableFrom(tt)) {
                                vars = vars.put(var, tv);
                            } else {
                                return null;
                            }
                        } else if (tt == null || !var.type().isAssignableFrom(tt)) {
                            return null;
                        } else if (vt != null && !vt.equals(tt)) {
                            return null;
                        } else {
                            vars = vars.put(var, tt);
                        }
                    } else if (get(i) instanceof TermImpl) {
                        TermImpl t = (TermImpl) get(i);
                        if (tv != null) {
                            if (tv instanceof TermImpl) {
                                vars = t.getBinding((TermImpl) tv, vars);
                                if (vars == null) {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        } else if (tt == null || !t.type().isAssignableFrom(tt)) {
                            return null;
                        }
                    } else if (tv != null && !tv.equals(get(i))) {
                        return null;
                    }
                }
                return vars;
            } else {
                return null;
            }
        }

        @SuppressWarnings("rawtypes")
        private static Class typeOf(Object v) {
            return v instanceof ClauseImpl ? ((ClauseImpl) v).type() : v instanceof Class ? (Class) v : null;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected TermImpl setBinding(Map<VarImpl, Object> vars) {
            Object[] array = toArray();
            for (int i = 1; i < length(); i++) {
                if (get(i) instanceof VarImpl) {
                    Object v = vars.get((VarImpl) get(i));
                    if (v != null) {
                        array[i] = v;
                    }
                } else if (get(i) instanceof TermImpl) {
                    array[i] = ((TermImpl) get(i)).setBinding(vars);
                }
            }
            return term(array);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <V extends Term> TermImpl<V> getTerm(int i) {
            Object v = get(i);
            return v instanceof TermImpl ? (TermImpl<V>) v : null;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public Class getType(int i) {
            Object v = get(i);
            return v instanceof Class ? (Class) v : v instanceof TermImpl ? ((TermImpl) v).type() : null;
        }

        @SuppressWarnings("unchecked")
        public <V> V getVal(int i) {
            Object v = get(i);
            return v instanceof Class || v instanceof TermImpl ? null : (V) v;
        }

        public TermImpl<F> set(int i, Object v) {
            Object[] array = toArray();
            array[i] = v;
            return term(array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public Set<TermImpl> incomplete() {
            return Set.of(Logic.incompleteImpl(List.of(this)));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Collection<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            FunctImpl<F> functor = functor();
            LogicLambda lambda = functor.lambda();
            if (lambda != null) {
                return lambda.apply(this);
            }
            int non = nrOfNulls();
            if (non > 1 || non >= totalLength()) {
                return Set.of(Logic.incompleteImpl(der.append(this)));
            }
            Set<TermImpl> facts = database.facts.get().get(this);
            if (facts != null) {
                return facts;
            }
            List<RuleImpl> rules = database.rules.get().get(functor);
            if (rules != null) {
                Set<TermImpl> r = rec.get(this);
                if (r != null) {
                    return r;
                }
                for (QualifiedSet<TermImpl, Memoiz> m : database.memoiz.get()) {
                    Memoiz memoiz = m.get(this);
                    if (memoiz != null) {
                        memoiz.count++;
                        return memoiz.set();
                    }
                }
                int li = der.lastIndexOf(this);
                if (li >= 0) {
                    return Set.of(Logic.incompleteImpl(der.append(this)));
                }
                if (der.size() >= MAX_LOGIC_DEPTH) {
                    return Set.of(Logic.incompleteImpl(der.append(this)));
                }
                Set<TermImpl> set = fixpoint(rules, non, der.append(this), rec, database);
                if (der.size() >= MAX_LOGIC_DEPTH_D2) {
                    Optional<TermImpl> ic = set.findAny(TermImpl::isToDepthIcomplete);
                    if (ic.isPresent()) {
                        if (der.size() == MAX_LOGIC_DEPTH_D2) {
                            List<TermImpl> list = (List) ic.get().get(1);
                            List<TermImpl> todo = list.sublist(der.size(), list.size());
                            while (todo.size() > 0) {
                                TermImpl t = todo.last();
                                FunctImpl tf = t.functor();
                                set = t.fixpoint(database.rules.get().get(tf), t.nrOfNulls(), der.append(t), rec, database);
                                ic = set.findAny(TermImpl::isToDepthIcomplete);
                                if (ic.isPresent()) {
                                    list = (List) ic.get().get(1);
                                    todo = todo.appendList(list.sublist(der.size(), list.size()));
                                } else {
                                    t.memoization(tf, set, database);
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

        @SuppressWarnings("rawtypes")
        private Set<TermImpl> fixpoint(List<RuleImpl> rules, int non, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> set = Set.of(), add = Set.of();
            boolean found = false, incomplete = false;
            do {
                add = or(rules, non, der, add.isEmpty() ? rec : rec.put(this, add), database).removeAll(set);
                found = add.anyMatch(this::equalFunctor);
                incomplete |= add.anyMatch(this::isIncomplete);
                if (incomplete && found && set.isEmpty()) {
                    add = add.filter(this::equalFunctor).asSet();
                }
                set = set.addAll(add);
            } while (found && incomplete);
            return set;
        }

        @SuppressWarnings("rawtypes")
        private void memoization(FunctImpl<F> functor, Set<TermImpl> set, Database database) {
            if (functor.factual) {
                database.facts.updateAndGet(m -> {
                    m = m.put(this, set);
                    for (TermImpl e : set) {
                        m = m.put(e, Set.of(e));
                    }
                    return m;
                });
            } else if (!functor.derived) {
                QualifiedSet<TermImpl, Memoiz>[] mem = database.memoiz.updateAndGet(a -> {
                    a = a.clone();
                    if (a[0].size() >= MAX_LOGIC_MEMOIZ_D4) {
                        a[2] = a[2].putAll(a[1]);
                        a[1] = a[0];
                        a[0] = EMPTY_MEMOIZ;
                    }
                    a[0] = a[0].put(new Memoiz(this, set));
                    for (TermImpl e : set) {
                        a[0] = a[0].put(new Memoiz(e, Set.of(e)));
                    }
                    return a;
                });
                if (mem[2].size() > MAX_LOGIC_MEMOIZ && mem[0].size() == set.size() + 1) {
                    LOGIC_POOL.execute(database::cleanup);
                }
            }
        }

        @SuppressWarnings("rawtypes")
        protected int termPrio(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            int non = nrOfNulls();
            LogicLambda lambda = functor().lambda();
            if (lambda != null) {
                Collection<TermImpl> result = lambda.apply(this);
                if (result instanceof Set) {
                    Set<TermImpl> set = (Set<TermImpl>) result;
                    return set.anyMatch(TermImpl::isIncomplete) ? Integer.MAX_VALUE : Integer.MIN_VALUE + set.size();
                }
                return non - nrOfBindings(goal);
            }
            if (non > 1 || non >= totalLength()) {
                return Integer.MAX_VALUE;
            }
            Set<TermImpl> facts = database.facts.get().get(this);
            if (facts != null) {
                return Integer.MIN_VALUE + facts.size();
            }
            List<RuleImpl> rules = database.rules.get().get(functor());
            if (rules != null) {
                Set<TermImpl> r = rec.get(this);
                if (r != null) {
                    return Integer.MIN_VALUE + r.size();
                }
                for (int i = der.size() - 1; i >= 0; i--) {
                    TermImpl other = der.get(i);
                    if (equals(other) || moreNullsThen(other) >= 0) {
                        return Integer.MAX_VALUE;
                    }
                }
                return non - nrOfBindings(goal);
            }
            return Integer.MIN_VALUE;
        }

        @SuppressWarnings("rawtypes")
        private Set<TermImpl> or(List<RuleImpl> rules, int non, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> r = Set.of();
            for (RuleImpl rule : rules) {
                Set<TermImpl> eval = rule.eval(this, der, rec, database);
                if (non == 0 && eval.equals(Set.of(this))) {
                    return (Set<TermImpl>) eval;
                }
                r = r.addAll(eval);
            }
            return r;
        }

        @SuppressWarnings("rawtypes")
        private int moreNullsThen(TermImpl other) {
            if (!get(0).equals(other.get(0))) {
                return Integer.MIN_VALUE;
            }
            int[] nr = new int[2];
            for (int i = 1; i < length(); i++) {
                if (!Objects.equals(get(i), other.get(i))) {
                    if (get(i) == null || get(i) instanceof Class) {
                        nr[0]++;
                    } else if (other.get(i) == null || other.get(i) instanceof Class) {
                        nr[1]++;
                    } else if (get(i) instanceof TermImpl && other.get(i) instanceof TermImpl) {
                        int r = ((TermImpl) get(i)).moreNullsThen((TermImpl) other.get(i));
                        if (r == Integer.MIN_VALUE) {
                            return Integer.MIN_VALUE;
                        } else {
                            nr[0] += r;
                        }
                    } else {
                        return Integer.MIN_VALUE;
                    }
                }
            }
            return nr[0] - nr[1];
        }

        @SuppressWarnings("rawtypes")
        protected int nrOfNulls() {
            int nr = 0;
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v == null || v instanceof Class) {
                    nr++;
                } else if (v instanceof TermImpl) {
                    nr += ((TermImpl) v).nrOfNulls();
                }
            }
            return nr;
        }

        @SuppressWarnings("rawtypes")
        protected int totalLength() {
            int nr = 0;
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v instanceof TermImpl) {
                    nr += ((TermImpl) v).totalLength();
                }
                nr++;
            }
            return nr;
        }

        @SuppressWarnings("rawtypes")
        protected int nrOfBindings(TermImpl goal) {
            int nr = 0;
            for (int i = 1; i < length(); i++) {
                Object g = goal.get(i);
                Object v = get(i);
                if (g instanceof VarImpl && !(v == null || v instanceof Class)) {
                    nr++;
                } else if (g instanceof TermImpl && v instanceof TermImpl) {
                    nr += ((TermImpl) v).nrOfBindings((TermImpl) g);
                }
            }
            return nr;
        }

        @SuppressWarnings("rawtypes")
        protected boolean equalFunctor(TermImpl other) {
            return functor() == other.functor();
        }

        @SuppressWarnings("rawtypes")
        protected boolean isIncomplete(TermImpl other) {
            return other.isIncomplete() && ((List) other.get(1)).last().equals(this);
        }

        @SuppressWarnings("rawtypes")
        protected boolean isToDepthIcomplete() {
            return isIncomplete() && ((List) get(1)).size() >= MAX_LOGIC_DEPTH;
        }

        protected boolean isIncomplete() {
            return functor() == INCOMPLETE_FUNCTOR;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected TermImpl<F> eq(ClauseImpl<F> other) {
            return (TermImpl<F>) super.eq(other);
        }
    };

    // Collect

    private static final FunctImpl<Pred> COLLECT_FUNCTOR       = functImpl((SerializableBiFunction<Pred, Pred, Pred>) Logic::collect);
    private static final Functor<Pred>   COLLECT_FUNCTOR_PROXY = COLLECT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Pred collect(Pred pred, Pred accum) {
        return new CollectImpl(pred, accum).proxy();
    }

    @SuppressWarnings("rawtypes")
    protected static CollectImpl collectImpl(TermImpl pred, TermImpl accum) {
        return new CollectImpl(pred, accum);
    }

    private static final class CollectImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -2799691054715131197L;

        private CollectImpl(Pred pred, Pred accum) {
            super(COLLECT_FUNCTOR_PROXY, pred, accum);
        }

        @SuppressWarnings("rawtypes")
        private CollectImpl(TermImpl pred, TermImpl accum) {
            super(COLLECT_FUNCTOR, pred, accum);
        }

        private CollectImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return (Pred) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Pred.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected CollectImpl term(Object[] array) {
            return new CollectImpl(array);
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> accum() {
            return ((TermImpl) get(2));
        }

        @SuppressWarnings("rawtypes")
        private Map<VarImpl, Object> localVariables;

        @SuppressWarnings("rawtypes")
        protected Map<VarImpl, Object> localVariables() {
            if (localVariables == null) {
                Map<VarImpl, Object> predVars = pred().variables();
                Map<VarImpl, Object> accumVars = accum().variables();
                localVariables = predVars.filter(accumVars::contains).asMap(Function.identity());
            }
            return localVariables;
        }

        @SuppressWarnings("rawtypes")
        private Map<VarImpl, Object> variables;

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> variables() {
            if (variables == null) {
                Map<VarImpl, Object> predVars = pred().variables();
                Map<VarImpl, Object> accumVars = accum().variables();
                variables = Collection.concat(predVars.exclude(accumVars::contains), accumVars.exclude(predVars::contains)).asMap(Function.identity());
            }
            return variables;
        }

        private int identityIndex = -1;

        @SuppressWarnings("rawtypes")
        private int identityIndex() {
            if (identityIndex < 0) {
                TermImpl accum = accum();
                for (int i = 1; i < accum.length(); i++) {
                    if (accum.get(i) instanceof TermImpl) {
                        Class<?> rt = ((VarImpl) accum.get(resultIndex())).type();
                        Class<?> at = ((TermImpl) accum.get(i)).type();
                        if (rt.isAssignableFrom(at)) {
                            identityIndex = i;
                            break;
                        }
                    }
                }
            }
            return identityIndex;
        }

        private int resultIndex = -1;

        @SuppressWarnings("rawtypes")
        private int resultIndex() {
            if (resultIndex < 0) {
                TermImpl accum = accum();
                for (int i = 1; i < accum.length(); i++) {
                    if (accum.get(i) instanceof VarImpl && !localVariables().containsKey((VarImpl) accum.get(i))) {
                        resultIndex = i;
                        break;
                    }
                }
            }
            return resultIndex;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Collection<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Map<VarImpl, Object> localVars = ((CollectImpl) goal).localVariables();
            int ii = ((CollectImpl) goal).identityIndex();
            int ri = ((CollectImpl) goal).resultIndex();
            TermImpl goalPred = ((CollectImpl) goal).pred();
            TermImpl goalAccum = ((CollectImpl) goal).accum();
            TermImpl accum = accum();
            Set<TermImpl> rs = Set.of(accum.getTerm(ii));
            Set<TermImpl> inc = Set.of();
            for (TermImpl pm : ((TermImpl<?>) pred().setBinding(localVars)).match(goalPred, der, rec, database)) {
                if (pm.isIncomplete()) {
                    inc = inc.add(pm);
                } else {
                    Map<VarImpl, Object> b = goalPred.getBinding(pm, Map.of());
                    Set<TermImpl> a = Set.of();
                    for (TermImpl r : rs) {
                        TermImpl s = accum.setBinding(b).set(ii, r);
                        for (TermImpl am : ((TermImpl<?>) s).match(goalAccum, der, rec, database)) {
                            if (am.isIncomplete()) {
                                inc = inc.add(am);
                            } else {
                                a = a.add(am);
                            }
                        }
                    }
                    rs = a.map(t -> t.getTerm(ri)).asSet();
                }
            }
            return Collection.concat(inc, rs.map(t -> set(2, accum.set(ri, t))));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected int termPrio(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            return super.termPrio(goal, der, rec, database);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> getBinding(TermImpl<Pred> term, Map<VarImpl, Object> vars) {
            Map<VarImpl, Object> localVars = localVariables();
            return super.getBinding(term, vars).exclude(e -> localVars.containsKey(e.getKey())).asMap(Function.identity());
        }
    }

    // Rules

    public static interface Rule extends Term {
    }

    private static final FunctImpl<Rule> RULE_FUNCTOR       = functImpl((SerializableBiFunction<Pred, Goal, Rule>) Logic::rule);
    private static final Functor<Rule>   RULE_FUNCTOR_PROXY = RULE_FUNCTOR.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Rule rule(Pred pred, Goal goal) {
        RuleImpl ruleImpl = new RuleImpl(pred, goal);
        TermImpl termImpl = Logic.<Pred, TermImpl> unproxy(pred);
        FunctImpl functor = termImpl.functor();
        Database database = DATABASE.get();
        database.rules.updateAndGet(m -> m.put(functor, ADD_RULE.apply(m.get(functor), ruleImpl)));
        return ruleImpl.proxy();
    }

    private static final class RuleImpl extends TermImpl<Rule> {
        private static final long serialVersionUID = -4602043866952049391L;

        private RuleImpl(Term term, Goal goal) {
            super(RULE_FUNCTOR_PROXY, term, goal);
        }

        @SuppressWarnings("rawtypes")
        private RuleImpl(TermImpl term, GoalImpl goal) {
            super(RULE_FUNCTOR, term, goal);
        }

        private RuleImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final Rule proxy() {
            return (Rule) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Rule.class}, this);
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl term() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings("rawtypes")
        protected final GoalImpl goal() {
            return ((GoalImpl) get(2));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Set<TermImpl> eval(TermImpl term, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            TermImpl head = term();
            Map<VarImpl, Object> binding = head.getBinding(term, Map.of());
            if (binding == null) {
                return Set.of();
            }
            if (TRACE_LOGIC) {
                System.err.println("!!!!!!!!!!!!!! " + "  ".repeat(der.size()) + this + " " + binding.toString().substring(3));
            }
            Collection<Map<VarImpl, Object>> r = goal().eval(variables().putAll(binding), der, rec, database);
            return r.map(m -> {
                TermImpl it = (TermImpl) m.get(INCOMPLETE_VAR);
                return it != null ? it : head.setBinding(m);
            }).asSet();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected RuleImpl term(Object[] array) {
            return new RuleImpl(array);
        }

        protected int rulePrio() {
            return goal().goals().size();
        }
    }

    // Goals

    public static interface Goal extends Term {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final FunctImpl<Goal> GOAL_FUNCTOR       = functImpl((SerializableFunction<L, Goal>) Logic::goal);
    private static final Functor<Goal>   GOAL_FUNCTOR_PROXY = GOAL_FUNCTOR.proxy();

    private static GoalImpl getImpl(Goal goal) {
        return Logic.<Goal, GoalImpl> unproxy(goal);
    }

    public static boolean isTrue(Goal goal) {
        return getImpl(goal).eval().anyMatch(e -> !e.containsKey(INCOMPLETE_VAR));
    }

    public static boolean isFalse(Goal goal) {
        return getImpl(goal).eval().isEmpty();
    }

    public static boolean isIncomplete(Goal goal) {
        return getImpl(goal).eval().anyMatch(e -> e.containsKey(INCOMPLETE_VAR));
    }

    @SuppressWarnings("rawtypes")
    public static Set<Map<Variable, Object>> getBindings(Goal goal) {
        return getImpl(goal).eval().map(m -> m.asMap(e -> Entry.of((Variable) e.getKey().proxy(), proxy(e.getValue())))).asSet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Goal goal(Pred... goals) {
        return new GoalImpl(lImpl((List<TermImpl<Pred>>) unproxy(List.of(goals)))).proxy();
    }

    @SuppressWarnings("unchecked")
    public static Goal goal(L<Pred> goals) {
        return new GoalImpl(goals).proxy();
    }

    private static final class GoalImpl extends TermImpl<Goal> {
        private static final long serialVersionUID = -4100263206389367132L;

        private GoalImpl(L<Pred> goals) {
            super(GOAL_FUNCTOR_PROXY, goals);
        }

        @SuppressWarnings("rawtypes")
        private GoalImpl(TermImpl<L> goals) {
            super(GOAL_FUNCTOR, goals);
        }

        private GoalImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final Goal proxy() {
            return (Goal) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Goal.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected GoalImpl term(Object[] array) {
            return new GoalImpl(array);
        }

        @SuppressWarnings("rawtypes")
        protected Collection<Map<VarImpl, Object>> eval() {
            return eval(variables(), List.of(), Map.of(), DATABASE.get());
        }

        @SuppressWarnings("rawtypes")
        protected Collection<Map<VarImpl, Object>> eval(Map<VarImpl, Object> vars, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            return eval(goals(), Set.of(vars), der, rec, database);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected List<TermImpl> goals() {
            return (List<TermImpl>) ((TermImpl) get(1)).get(1);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Collection<Map<VarImpl, Object>> eval(List<TermImpl> goals, Collection<Map<VarImpl, Object>> vars, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            if (goals.isEmpty()) {
                return vars;
            }
            return vars.<Map<VarImpl, Object>> flatMap(v -> {
                if (v.containsKey(INCOMPLETE_VAR)) {
                    return Set.of(v);
                }
                List<TermImpl> actual = List.of();
                for (TermImpl g : goals) {
                    actual = actual.add(g.setBinding(v));
                }
                int i = first(actual, goals, der, rec, database);
                TermImpl f = actual.get(i);
                TermImpl g = goals.get(i);
                Collection<TermImpl> m = f.match(g, der, rec, database);
                return eval(goals.removeIndex(i), m.<Map<VarImpl, Object>> map(t -> {
                    if (t.type() == Incomplete.class) {
                        return Map.of(Entry.of(INCOMPLETE_VAR, t));
                    } else {
                        Map<VarImpl, Object> b = g.getBinding(t, Map.of());
                        return b == null ? Map.of() : v.putAll(b);
                    }
                }), der, rec, database);
            });
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static int first(List<TermImpl> actual, List<TermImpl> goals, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            int first = -1;
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < actual.size(); i++) {
                int prio = actual.get(i).termPrio(goals.get(i), der, rec, database);
                if (first == -1 || prio < min) {
                    first = i;
                    min = prio;
                }
            }
            return first;
        }
    }

    // Incomplete

    public interface Incomplete extends Term {
    }

    @SuppressWarnings("rawtypes")
    private static final FunctImpl<Incomplete> INCOMPLETE_FUNCTOR       = functImpl((SerializableFunction<List<Term>, Incomplete>) Logic::incomplete);
    private static final Functor<Incomplete>   INCOMPLETE_FUNCTOR_PROXY = INCOMPLETE_FUNCTOR.proxy();
    private static final VarImpl<Incomplete>   INCOMPLETE_VAR           = new VarImpl<Incomplete>(Incomplete.class, "I");
    private static final Incomplete            INCOMPLETE_VAR_PROXY     = INCOMPLETE_VAR.proxy();

    @SuppressWarnings("unchecked")
    public static Incomplete incompleteVar() {
        return INCOMPLETE_VAR_PROXY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Incomplete incomplete(List<Term> der) {
        return term(INCOMPLETE_FUNCTOR_PROXY, der);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static TermImpl<Incomplete> incompleteImpl(List<TermImpl> der) {
        return termImpl(INCOMPLETE_FUNCTOR, der);
    }

    // Equals

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> eq = functor(Logic::eq, (LogicLambda) t -> {
        TermImpl at = t.getTerm(1);
        TermImpl bt = t.getTerm(2);
        if (at == null && bt == null) {
            return t.incomplete();
        } else if (at == null) {
            return Set.of(t.set(1, bt));
        } else if (bt == null) {
            return Set.of(t.set(2, at));
        } else {
            TermImpl eq = at.eq(bt);
            return eq == null ? Set.of() : Set.of(t.set(1, eq).set(2, eq));
        }
    });

    public static Pred eq(Term a, Term b) {
        return term(eq, a, b);
    }

    // Facts

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void fact(Rel rel) {
        Logic.<Pred, TermImpl> unproxy(rel).makeFact(DATABASE.get());
    }

    // Is

    public static interface Atom<T extends Term> extends Term {

    }

    public static interface Func<T extends Term> extends Term {

    }

    @SuppressWarnings("rawtypes")
    private static Functor<Pred> is = functor((SerializableBiFunction<Func, Atom, Pred>) Logic::is);

    public static <T extends Term> Pred is(T a, T b) {
        return term(is, a, b);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void isRules() {
        Atom A1 = var(Atom.class, "A1");
        Atom A2 = var(Atom.class, "A2");
        Func F1 = var(Func.class, "F1");
        Func F2 = var(Func.class, "F2");

        rule(is(A1, A2), goal(eq(A1, A2)));
        rule(is(F1, F2), goal(is(F2, A2), is(F1, A2)));
        rule(is(A1, F1), goal(is(F1, A1)));
    }

    // Bindings

    public static Map<Variable, Object> incomplete(Term... der) {
        return Map.of(Entry.of((Variable) incompleteVar(), incomplete(List.of(der))));
    }

    public static Map<Variable, Object> binding(Term... varVal) {
        Map<Variable, Object> b = Map.of();
        for (int i = 0; i < varVal.length; i += 2) {
            b = b.add(Entry.of((Variable) varVal[i], varVal[i + 1]));
        }
        return b;
    }

}

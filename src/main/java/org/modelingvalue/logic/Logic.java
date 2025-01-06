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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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
    public interface LogicLambda extends Function<TermImpl<Term>, Set<TermImpl<Term>>>, LambdaReflection, FunctorModifier {

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
            public final Set<TermImpl<Term>> apply(TermImpl t) {
                return f.apply(t);
            }

        }
    }

    private static final int[]                                                ONE_ARRAY           = new int[]{1};
    private static final int[]                                                TWO_ARRAY           = new int[]{2};
    private static final UnaryOperator<int[]>                                 ADD_ONE             = a -> {
                                                                                                      int[] r = new int[a.length + 1];
                                                                                                      System.arraycopy(a, 0, r, 1, a.length);
                                                                                                      r[0] = 1;
                                                                                                      return r;
                                                                                                  };
    private static final UnaryOperator<int[]>                                 ADD_TWO             = a -> {
                                                                                                      int[] r = new int[a.length + 1];
                                                                                                      System.arraycopy(a, 0, r, 1, a.length);
                                                                                                      r[0] = 2;
                                                                                                      return r;
                                                                                                  };

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
    @SuppressWarnings("unchecked")
    private static final BiFunction<List<RuleImpl>, RuleImpl, List<RuleImpl>> ADD_RULE            = (l, e) -> {
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

        private final int hashCode;

        protected ClauseImpl(Functor<F> functor, Object... args) {
            super(unproxy(functor, args));
            this.hashCode = getHashCode();
        }

        protected ClauseImpl(FunctImpl<F> functor, Object... args) {
            super(array(functor, args));
            this.hashCode = getHashCode();
        }

        protected ClauseImpl(Class<F> type, Object... args) {
            super(array(type, args));
            this.hashCode = getHashCode();
        }

        protected ClauseImpl(Object[] args) {
            super(args);
            this.hashCode = getHashCode();
        }

        private int getHashCode() {
            int r = 1;
            for (int i = 1; i < length(); i++) {
                Object e = get(i);
                r = 31 * r + (e == null ? 0 : e.hashCode());
            }
            return 31 * r + get(0).hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        private static final Object[] array(Object functor, Object[] args) {
            Object[] result = new Object[args.length + 1];
            noProxy(functor);
            result[0] = functor;
            for (int i = 0; i < args.length; i++) {
                noProxy(args[i]);
                result[i + 1] = args[i];
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

        protected ClauseImpl<F> eq(ClauseImpl<F> other) {
            if (equals(other)) {
                return this;
            } else if (length() != other.length()) {
                return null;
            }
            boolean changed = false;
            Object[] array = toArray();
            for (int i = 0; i < array.length; i++) {
                Object eq = eq(get(i), other.get(i));
                if (eq == null) {
                    return null;
                } else if (!Objects.equals(eq, array[i])) {
                    array[i] = eq;
                    changed = true;
                }
            }
            return changed ? term(array) : this;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object eq(Object tv, Object ov) {
            if (tv != ov) {
                if (tv instanceof ClauseImpl && ov instanceof ClauseImpl) {
                    return ((ClauseImpl) tv).eq((ClauseImpl) ov);
                } else if (tv instanceof ClauseImpl && ov instanceof Class) {
                    return ((Class) ov).isAssignableFrom(((ClauseImpl) tv).type()) ? tv : null;
                } else if (tv instanceof Class && ov instanceof ClauseImpl) {
                    return ((Class) tv).isAssignableFrom(((ClauseImpl) ov).type()) ? ov : null;
                } else if (!(tv instanceof Class) && ov instanceof Class) {
                    return ((Class) ov).isAssignableFrom(tv.getClass()) ? tv : null;
                } else if (tv instanceof Class && !(ov instanceof Class)) {
                    return ((Class) tv).isAssignableFrom(ov.getClass()) ? ov : null;
                } else if (!Objects.equals(tv, ov)) {
                    return null;
                }
            }
            return tv;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final void noProxy(Object object) {
        if (object instanceof Term) {
            throw new IllegalArgumentException();
        } else if (object instanceof List) {
            ((List) object).forEach(Logic::noProxy);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Object unproxy(Object object) {
        if (object instanceof Term) {
            return Proxy.getInvocationHandler(object);
        } else if (object instanceof List) {
            return ((List) object).replaceAll(Logic::unproxy);
        } else {
            Objects.requireNonNull(object);
            return object;
        }
    }

    @SuppressWarnings("unchecked")
    protected static final <T extends Term, R extends ClauseImpl<T>> R unproxy(T object) {
        return (R) Proxy.getInvocationHandler(object);
    }

    // Functor

    public interface Functor<T> extends Term {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Term> FunctImpl<T> functImpl(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        SerializableSupplierImpl<T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term> Functor<T> functor(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Term, A> FunctImpl<T> functImpl(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        SerializableFunctionImpl<A, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A> Functor<T> functor(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Term, A, B> FunctImpl<T> functImpl(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        SerializableBiFunctionImpl<A, B, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A, B> Functor<T> functor(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Term, A, B, C> FunctImpl<T> functImpl(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        SerializableTriFunctionImpl<A, B, C, T> l = method.of();
        return new FunctImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Term, A, B, C> Functor<T> functor(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Term, A, B, C, D> FunctImpl<T> functImpl(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
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

    public static interface AtomPred extends Pred {
    }

    public static interface Rel extends AtomPred {
    }

    public static boolean isTrue(Pred pred) {
        return match(pred).anyMatch(t -> !t.isIncomplete());
    }

    public static boolean isFalse(Pred pred) {
        return match(pred).isEmpty();
    }

    public static boolean isIncomplete(Pred pred) {
        return match(pred).anyMatch(TermImpl::isIncomplete);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Set<Map<Variable, Object>> getBindings(Pred pred) {
        TermImpl<Pred> impl = Logic.<Pred, TermImpl<Pred>> unproxy(pred);
        Set<TermImpl> match = match(impl);
        Set<Map<VarImpl, Object>> bindings = match.replaceAll(m -> m.isIncomplete() ? Map.of(Entry.of(INCOMPLETE_VAR, m)) : impl.getBinding(m, Map.of()));
        return bindings.replaceAll(m -> m.replaceAll(e -> Entry.of((Variable) e.getKey().proxy(), proxy(e.getValue()))));
    }

    @SuppressWarnings("rawtypes")
    private static final Object proxy(Object object) {
        if (object instanceof ClauseImpl) {
            return ((ClauseImpl) object).proxy();
        } else {
            return object;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<TermImpl> match(Pred pred) {
        return match(Logic.<Pred, TermImpl<Pred>> unproxy(pred));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<TermImpl> match(TermImpl<Pred> impl) {
        return impl.setBinding(impl, impl.variables()).match(impl, List.of(), Map.of(), DATABASE.get());
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

        protected TermImpl(Functor<F> functor, Object... args) {
            super(functor, args);
        }

        protected TermImpl(FunctImpl<F> functor, Object... args) {
            super(functor, args);
        }

        protected TermImpl(Object[] args) {
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
            String string = super.toString();
            return string.substring(1, string.length() - 1).replaceFirst(",", "(") + ")";
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
                }
            }
            return vars;
        }

        @SuppressWarnings("rawtypes")
        protected Map<VarImpl, Object> getBinding(TermImpl<F> term, Map<VarImpl, Object> vars) {
            if (get(0).equals(term.get(0))) {
                for (int i = 1; i < length(); i++) {
                    vars = getBinding(get(i), term.get(i), vars);
                    if (vars == null) {
                        return null;
                    }
                }
                return vars;
            } else {
                return null;
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Map<VarImpl, Object> getBinding(Object v, Object tv, Map<VarImpl, Object> vars) {
            Class tt = typeOf(tv);
            tv = tv instanceof Class ? null : tv;
            if (v instanceof VarImpl) {
                VarImpl var = (VarImpl) v;
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
            } else if (v instanceof TermImpl) {
                TermImpl t = (TermImpl) v;
                if (tv != null) {
                    if (tv instanceof TermImpl) {
                        vars = t.getBinding((TermImpl) tv, vars);
                    } else {
                        return null;
                    }
                } else if (tt == null || !t.type().isAssignableFrom(tt)) {
                    return null;
                }
            } else if (tv != null && !tv.equals(v)) {
                return null;
            }
            return vars;
        }

        @SuppressWarnings("rawtypes")
        private static Class typeOf(Object v) {
            return v instanceof ClauseImpl ? ((ClauseImpl) v).type() : v instanceof Class ? (Class) v : null;
        }

        @SuppressWarnings("rawtypes")
        protected TermImpl setBinding(TermImpl<F> term, Map<VarImpl, Object> vars) {
            Object[] array = term.toArray();
            boolean changed = false;
            for (int i = 1; i < length(); i++) {
                Object b = setBinding(get(i), term.get(i), vars);
                if (!Objects.equals(b, array[i])) {
                    array[i] = b;
                    changed = true;
                }
            }
            return changed ? term.term(array) : term;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object setBinding(Object v, Object tv, Map<VarImpl, Object> vars) {
            if (v instanceof VarImpl) {
                Object vv = vars.get((VarImpl) v);
                if (vv != null) {
                    return vv;
                }
            } else if (v instanceof TermImpl) {
                if (tv instanceof TermImpl) {
                    return ((TermImpl) v).setBinding((TermImpl) tv, vars);
                } else if (tv instanceof Class && ((Class) tv).isAssignableFrom((((TermImpl) v).type()))) {
                    return ((TermImpl) v).setBinding((TermImpl) v, vars);
                }
            }
            return tv;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <V extends Term, T extends TermImpl<V>> T getTerm(int i) {
            Object v = get(i);
            return v instanceof TermImpl ? (T) v : null;
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
            if (!Objects.equals(v, array[i])) {
                array[i] = v;
                return term(array);
            } else {
                return this;
            }
        }

        @SuppressWarnings("rawtypes")
        protected TermImpl get(int[] ii) {
            TermImpl r = this;
            for (int i = 0; i < ii.length; i++) {
                r = (TermImpl) r.get(ii[i]);
            }
            return r;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public Set<TermImpl<Term>> incomplete() {
            return Set.of(Logic.incompleteImpl(List.of(this)));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            FunctImpl<F> functor = functor();
            LogicLambda lambda = functor.lambda();
            if (lambda != null) {
                return lambda.apply((TermImpl) this);
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
                add = evalRules(rules, non, der, add.isEmpty() ? rec : rec.put(this, add), database).removeAll(set);
                found = add.anyMatch(this::equalFunctor);
                incomplete |= add.anyMatch(this::isIncomplete);
                if (incomplete && found && set.isEmpty()) {
                    add = add.retainAll(this::equalFunctor);
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
        private Set<TermImpl> evalRules(List<RuleImpl> rules, int non, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> r = Set.of();
            for (RuleImpl rule : rules) {
                Set<TermImpl> eval = rule.eval(this, der, rec, database);
                if (eval.anyMatch(TermImpl::isToDepthIcomplete)) {
                    return eval;
                } else {
                    r = r.addAll(eval);
                }
            }
            return r;
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
        protected boolean equalFunctor(TermImpl other) {
            return functor().equals(other.functor());
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
            return INCOMPLETE_FUNCTOR.equals(functor());
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected TermImpl<F> eq(ClauseImpl<F> other) {
            return (TermImpl<F>) super.eq(other);
        }

        @SuppressWarnings("rawtypes")
        protected boolean contains(TermImpl cond) {
            return equals(cond);
        }
    };

    // Collect

    private static final FunctImpl<Pred> COLLECT_FUNCTOR       = functImpl((SerializableBiFunction<Pred, Pred, Pred>) Logic::collect);
    private static final Functor<Pred>   COLLECT_FUNCTOR_PROXY = COLLECT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Pred collect(Pred pred, Pred accum) {
        return new CollectImpl(pred, accum).proxy();
    }

    private static final class CollectImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -2799691054715131197L;

        private CollectImpl(Pred pred, Pred accum) {
            super(COLLECT_FUNCTOR_PROXY, pred, accum);
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
                localVariables = predVars.retainAll(accumVars::contains);
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
                variables = predVars.removeAll(accumVars::contains).addAll(accumVars.removeAll(predVars::contains));
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
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Map<VarImpl, Object> localVars = ((CollectImpl) goal).localVariables();
            int ii = ((CollectImpl) goal).identityIndex();
            int ri = ((CollectImpl) goal).resultIndex();
            TermImpl goalPred = ((CollectImpl) goal).pred();
            TermImpl goalAccum = ((CollectImpl) goal).accum();
            TermImpl accum = accum();
            Set<TermImpl> rs = Set.of(accum.getTerm(ii));
            Set<TermImpl> inc = Set.of();
            for (TermImpl pm : ((TermImpl<?>) goalPred.setBinding(pred(), localVars)).match(goalPred, der, rec, database)) {
                if (pm.isIncomplete()) {
                    inc = inc.add(pm);
                } else {
                    Map<VarImpl, Object> b = goalPred.getBinding(pm, Map.of());
                    Set<TermImpl> irs = Set.of();
                    for (TermImpl r : rs) {
                        TermImpl s = goalAccum.setBinding(accum, b).set(ii, r);
                        for (TermImpl am : ((TermImpl<?>) s).match(goalAccum, der, rec, database)) {
                            if (am.isIncomplete()) {
                                inc = inc.add(am);
                            } else {
                                irs = irs.add(am.getTerm(ri));
                            }
                        }
                    }
                    rs = irs;
                }
            }
            for (TermImpl t : rs) {
                inc = inc.add(set(2, accum.set(ri, t)));
            }
            return inc;
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> getBinding(TermImpl<Pred> term, Map<VarImpl, Object> vars) {
            Map<VarImpl, Object> localVars = localVariables();
            return super.getBinding(term, vars).removeAll(e -> localVars.containsKey(e.getKey()));
        }

        @Override
        public CollectImpl set(int i, Object v) {
            return (CollectImpl) super.set(i, v);
        }
    }

    // Yes

    private static final FunctImpl<Pred> YES_FUNCTOR = functImpl((SerializableSupplier<Pred>) Logic::yes);
    private static final YesImpl         YES         = new YesImpl();
    private static final Pred            YES_PROXY   = (Pred) Proxy.newProxyInstance(Pred.class.getClassLoader(), new Class[]{Pred.class}, YES);

    @SuppressWarnings("unchecked")
    public static Pred yes() {
        return YES_PROXY;
    }

    private static final class YesImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -8515171118744898263L;

        private YesImpl() {
            super(YES_FUNCTOR);
        }

        private YesImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return YES_PROXY;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected YesImpl term(Object[] array) {
            return new YesImpl(array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            return Set.of(this);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> getBinding(TermImpl<Pred> term, Map<VarImpl, Object> vars) {
            return vars;
        }

        @Override
        public YesImpl set(int i, Object v) {
            return (YesImpl) super.set(i, v);
        }
    }

    // No

    private static final FunctImpl<Pred> NO_FUNCTOR = functImpl((SerializableSupplier<Pred>) Logic::no);
    private static final NoImpl          NO         = new NoImpl();
    private static final Pred            NO_PROXY   = (Pred) Proxy.newProxyInstance(Pred.class.getClassLoader(), new Class[]{Pred.class}, NO);

    @SuppressWarnings("unchecked")
    public static Pred no() {
        return NO_PROXY;
    }

    private static final class NoImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -8515171118744898263L;

        private NoImpl() {
            super(NO_FUNCTOR);
        }

        private NoImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return NO_PROXY;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected NoImpl term(Object[] array) {
            return new NoImpl(array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            return Set.of();
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> getBinding(TermImpl<Pred> term, Map<VarImpl, Object> vars) {
            return vars;
        }

        @Override
        public NoImpl set(int i, Object v) {
            return (NoImpl) super.set(i, v);
        }
    }

    // Not

    private static final FunctImpl<Pred> NOT_FUNCTOR       = functImpl((SerializableFunction<Pred, Pred>) Logic::not);
    private static final Functor<Pred>   NOT_FUNCTOR_PROXY = NOT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Pred not(Pred pred) {
        return new NotImpl(pred).proxy();
    }

    private static final class NotImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -4543178470298951866L;

        private NotImpl(Pred pred) {
            super(NOT_FUNCTOR_PROXY, pred);
        }

        private NotImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return (Pred) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Pred.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected NotImpl term(Object[] array) {
            return new NotImpl(array);
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> r = pred().match(((NotImpl) goal).pred(), der, rec, database);
            return r.isEmpty() ? Set.of(this) : r.retainAll(TermImpl::isIncomplete);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VarImpl, Object> getBinding(TermImpl<Pred> term, Map<VarImpl, Object> vars) {
            return vars;
        }

        @Override
        public NotImpl set(int i, Object v) {
            return (NotImpl) super.set(i, v);
        }
    }

    // Or

    private static final FunctImpl<Pred> OR_FUNCTOR = functImpl((SerializableBiFunction<Pred, Pred, Pred>) Logic::or);

    private static Pred or(Pred p1, Pred p2) {
        return new OrImpl(unproxy(p1), unproxy(p2)).proxy();
    }

    @SuppressWarnings("unchecked")
    public static Pred or(Pred... ps) {
        TermImpl<Pred> impl = NO;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == NO ? unproxy(ps[i]) : new OrImpl(unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    private static final class OrImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -1732549494864415986L;

        private OrImpl(TermImpl<Pred> pred1, TermImpl<Pred> pred2) {
            super(OR_FUNCTOR, pred1, pred2);
        }

        private OrImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return (Pred) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Pred.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected OrImpl term(Object[] array) {
            return new OrImpl(array);
        }

        private List<int[]> idxList;

        @SuppressWarnings("rawtypes")
        private List<int[]> idxList() {
            if (idxList == null) {
                List<int[]> l = List.of();
                TermImpl p1 = pred1();
                if (p1 instanceof OrImpl) {
                    l = l.prependList(((OrImpl) p1).idxList().replaceAll(ADD_ONE));
                } else {
                    l = l.append(ONE_ARRAY);
                }
                TermImpl p2 = pred2();
                if (p2 instanceof OrImpl) {
                    l = l.appendList(((OrImpl) p2).idxList().replaceAll(ADD_TWO));
                } else {
                    l = l.append(TWO_ARRAY);
                }
                idxList = l;
            }
            return idxList;
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred1() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred2() {
            return ((TermImpl) get(2));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> r = Set.of();
            for (int[] i : ((OrImpl) goal).idxList()) {
                TermImpl g = goal.get(i);
                Set<TermImpl> m = get(i).match(g, der, rec, database);
                if (m.anyMatch(TermImpl::isToDepthIcomplete)) {
                    return m;
                } else {
                    r = r.addAll(m.replaceAll(t -> t.isIncomplete() ? t : goal.setBinding(this, g.getBinding(t, Map.of()))));
                }
            }
            return r;
        }

        @Override
        public OrImpl set(int i, Object v) {
            return (OrImpl) super.set(i, v);
        }

        @Override
        @SuppressWarnings("rawtypes")
        protected boolean contains(TermImpl cond) {
            return super.contains(cond) || pred1().contains(cond) || pred2().contains(cond);
        }
    }

    // And

    private static final FunctImpl<Pred> AND_FUNCTOR = functImpl((SerializableBiFunction<Pred, Pred, Pred>) Logic::and);

    private static Pred and(Pred p1, Pred p2) {
        return new AndImpl(unproxy(p1), unproxy(p2)).proxy();
    }

    @SuppressWarnings("unchecked")
    public static Pred and(Pred... ps) {
        TermImpl<Pred> impl = YES;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == YES ? unproxy(ps[i]) : new AndImpl(unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    private static final class AndImpl extends TermImpl<Pred> {
        private static final long serialVersionUID = -7248491569810098948L;

        private AndImpl(TermImpl<Pred> pred1, TermImpl<Pred> pred2) {
            super(AND_FUNCTOR, pred1, pred2);
        }

        private AndImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pred proxy() {
            return (Pred) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Pred.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected AndImpl term(Object[] array) {
            return new AndImpl(array);
        }

        private List<int[]> idxList;

        @SuppressWarnings("rawtypes")
        private List<int[]> idxList() {
            if (idxList == null) {
                List<int[]> l = List.of();
                TermImpl p1 = pred1();
                if (p1 instanceof AndImpl) {
                    l = l.prependList(((AndImpl) p1).idxList().replaceAll(ADD_ONE));
                } else {
                    l = l.append(ONE_ARRAY);
                }
                TermImpl p2 = pred2();
                if (p2 instanceof AndImpl) {
                    l = l.appendList(((AndImpl) p2).idxList().replaceAll(ADD_TWO));
                } else {
                    l = l.append(TWO_ARRAY);
                }
                idxList = l;
            }
            return idxList;
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred1() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl<?> pred2() {
            return ((TermImpl) get(2));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<TermImpl> match(TermImpl goal, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            Set<TermImpl> out = Set.of();
            Set<AndImpl> ands = Set.of(this);
            idxList = ((AndImpl) goal).idxList();
            do {
                Set<AndImpl> ands2 = ands;
                ands = Set.of();
                outer:
                for (AndImpl and : ands2) {
                    List<int[]> idxl = and.idxList;
                    if (idxl.isEmpty()) {
                        out = out.add(and);
                    } else {
                        Set<TermImpl> ic = Set.of();
                        for (int ii = 0; ii < idxl.size(); ii++) {
                            int[] i = idxl.get(ii);
                            TermImpl g = goal.get(i);
                            Set<TermImpl> ts = and.get(i).match(g, der, rec, database);
                            Set<TermImpl> in = ts.retainAll(TermImpl::isIncomplete);
                            if (in.isEmpty()) {
                                List<int[]> iil = idxl.removeIndex(ii);
                                ands = ands.addAll(ts.replaceAll(m -> {
                                    AndImpl a = (AndImpl) goal.setBinding(and, g.getBinding(m, Map.of()));
                                    a.idxList = iil;
                                    return a;
                                }));
                                continue outer;
                            } else if (in.anyMatch(TermImpl::isToDepthIcomplete)) {
                                return in;
                            } else {
                                ic = ic.addAll(in);
                            }
                        }
                        out = out.addAll(ic);
                    }

                }
            } while (!ands.isEmpty());
            return out;
        }

        @Override
        public AndImpl set(int i, Object v) {
            return (AndImpl) super.set(i, v);
        }
    }

    // Rules

    public static interface Rule extends Term {
    }

    private static final FunctImpl<Rule> RULE_FUNCTOR       = functImpl((SerializableBiFunction<AtomPred, Pred, Rule>) Logic::rule);
    private static final Functor<Rule>   RULE_FUNCTOR_PROXY = RULE_FUNCTOR.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Rule rule(AtomPred consequence, Pred condition) {
        RuleImpl ruleImpl = new RuleImpl(consequence, condition);
        TermImpl termImpl = Logic.<Pred, TermImpl> unproxy(consequence);
        FunctImpl functor = termImpl.functor();
        Database database = DATABASE.get();
        database.rules.updateAndGet(m -> m.put(functor, ADD_RULE.apply(m.get(functor), ruleImpl)));
        return ruleImpl.proxy();
    }

    private static final class RuleImpl extends TermImpl<Rule> {
        private static final long serialVersionUID = -4602043866952049391L;

        private RuleImpl(AtomPred pred, Pred goal) {
            super(RULE_FUNCTOR_PROXY, pred, goal);
        }

        private RuleImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final Rule proxy() {
            return (Rule) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Rule.class}, this);
        }

        @Override
        protected RuleImpl term(Object[] array) {
            return new RuleImpl(array);
        }

        @SuppressWarnings("rawtypes")
        private Map<VarImpl, Object> variables;

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Map<VarImpl, Object> variables() {
            if (variables == null) {
                variables = super.variables();
            }
            return variables;
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl cons() {
            return ((TermImpl) get(1));
        }

        @SuppressWarnings("rawtypes")
        protected final TermImpl cond() {
            return ((TermImpl) get(2));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Set<TermImpl> eval(TermImpl term, List<TermImpl> der, Map<TermImpl, Set<TermImpl>> rec, Database database) {
            TermImpl cons = cons();
            Map<VarImpl, Object> binding = cons.getBinding(term, Map.of());
            if (binding == null) {
                return Set.of();
            }
            if (TRACE_LOGIC) {
                System.err.println("LOGIC " + "  ".repeat(der.size()) + this + " " + binding.toString().substring(3));
            }
            TermImpl cond = cond();
            Set<TermImpl> match = cond.setBinding(cond, cond.variables().putAll(binding)).match(cond, der, rec, database);
            return match.replaceAll(t -> t.isIncomplete() ? t : cons.setBinding(term, cond.getBinding(t, Map.of())));
        }

        protected int rulePrio() {
            return cond().totalLength();
        }

        @Override
        public RuleImpl set(int i, Object v) {
            return (RuleImpl) super.set(i, v);
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
    protected static TermImpl incompleteImpl(List<TermImpl> der) {
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
    private static Functor<AtomPred> is = functor((SerializableBiFunction<Func, Atom, AtomPred>) Logic::is);

    public static <T extends Term> AtomPred is(T a, T b) {
        return term(is, a, b);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void isRules() {
        Atom A1 = var(Atom.class, "A1");
        Atom A2 = var(Atom.class, "A2");
        Func F1 = var(Func.class, "F1");
        Func F2 = var(Func.class, "F2");

        rule(is(A1, A2), eq(A1, A2));
        rule(is(F1, F2), and(is(F2, A2), is(F1, A2)));
        rule(is(A1, F1), is(F1, A1));
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

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.struct.impl.Struct2Impl;
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

    @SuppressWarnings("rawtypes")
    private static final AtomicReference<Map<Class, Set<Class>>>                           SPECS               = new AtomicReference<>(Map.of());
    private static final int[]                                                             ONE_ARRAY           = new int[]{1};
    private static final int[]                                                             TWO_ARRAY           = new int[]{2};
    private static final UnaryOperator<int[]>                                              ADD_ONE             = a -> {
                                                                                                                   int[] r = new int[a.length + 1];
                                                                                                                   System.arraycopy(a, 0, r, 1, a.length);
                                                                                                                   r[0] = 1;
                                                                                                                   return r;
                                                                                                               };
    private static final UnaryOperator<int[]>                                              ADD_TWO             = a -> {
                                                                                                                   int[] r = new int[a.length + 1];
                                                                                                                   System.arraycopy(a, 0, r, 1, a.length);
                                                                                                                   r[0] = 2;
                                                                                                                   return r;
                                                                                                               };

    private static final int                                                               MAX_LOGIC_MEMOIZ    = Integer.getInteger("MAX_LOGIC_MEMOIZ", 512);
    private static final int                                                               MAX_LOGIC_MEMOIZ_D4 = MAX_LOGIC_MEMOIZ / 4;
    private static final int                                                               INITIAL_USAGE_COUNT = Integer.getInteger("INITIAL_USAGE_COUNT", 4);

    private static final int                                                               MAX_LOGIC_DEPTH     = Integer.getInteger("MAX_LOGIC_DEPTH", 32);
    private static final int                                                               MAX_LOGIC_DEPTH_D2  = MAX_LOGIC_DEPTH / 2;

    private static final boolean                                                           TRACE_LOGIC         = Boolean.getBoolean("TRACE_LOGIC");

    private static final ContextPool                                                       LOGIC_POOL          = ContextThread.createPool();
    private static final Context<Database>                                                 DATABASE            = Context.of();

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
    private static <F extends Structure> void updateSpecs(Class type) {
        if (!SPECS.get().containsKey(type)) {
            SPECS.updateAndGet(m -> addToSpecs(m, type));
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class, Set<Class>> addToSpecs(Map<Class, Set<Class>> specs, Class type) {
        if (!specs.containsKey(type)) {
            specs = specs.put(type, Set.of());
            for (java.lang.reflect.Type g : type.getGenericInterfaces()) {
                while (g instanceof ParameterizedType) {
                    g = ((ParameterizedType) g).getRawType();
                }
                if (g instanceof Class && !g.equals(Structure.class)) {
                    specs = addToSpecs(specs, (Class) g);
                    specs = specs.put((Class) g, specs.get((Class) g).add(type));
                }
            }
        }
        return specs;
    }

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
    private static final QualifiedSet<PredicateImpl, Memoiz> EMPTY_MEMOIZ = QualifiedSet.of(Memoiz::pred);

    @SuppressWarnings("rawtypes")
    private static class Memoiz extends Struct2Impl<PredicateImpl, Set<PredicateImpl>> {
        private static final long serialVersionUID = 1531759272582548244L;

        private int               count            = INITIAL_USAGE_COUNT;

        private Memoiz(PredicateImpl t, Set<PredicateImpl> s) {
            super(t, s);
        }

        private PredicateImpl pred() {
            return get0();
        }

        private Set<PredicateImpl> set() {
            return get1();
        }

        protected boolean keep() {
            return count-- > 0;
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class Database {
        private final AtomicReference<Map<PredicateImpl, Set<PredicateImpl>>> facts;
        private final AtomicReference<Map<PredicateImpl, List<RuleImpl>>>     rules;
        private final AtomicReference<QualifiedSet<PredicateImpl, Memoiz>[]>  memoiz;

        private boolean                                                       stopped;

        @SuppressWarnings("unchecked")
        private Database(Database init) {
            facts = new AtomicReference<>(init != null ? init.facts.get() : Map.of());
            rules = new AtomicReference<>(init != null ? init.rules.get() : Map.of());
            memoiz = new AtomicReference<>(init != null ? init.memoiz.get() : new QualifiedSet[]{EMPTY_MEMOIZ, EMPTY_MEMOIZ, EMPTY_MEMOIZ});
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

        protected void cleanup() {
            QualifiedSet<PredicateImpl, Memoiz>[] mem = memoiz.get();
            while (mem[2].size() > MAX_LOGIC_MEMOIZ) {
                for (int i = 0; i < mem[2].size(); i++) {
                    if (stopped) {
                        return;
                    }
                    Memoiz m = mem[2].get(i);
                    if (!m.keep()) {
                        mem = memoiz.updateAndGet(a -> {
                            a = a.clone();
                            a[2] = a[2].removeKey(m.pred());
                            return a;
                        });
                        i--;
                    }
                }
            }
        }
    }

    // Structures

    public interface Structure {
    }

    @SuppressWarnings("rawtypes")
    private static final Object proxy(Object object) {
        if (object instanceof StructureImpl) {
            return ((StructureImpl) object).proxy();
        } else {
            return object;
        }
    }

    @SuppressWarnings("unchecked")
    public static <F extends Structure> F struct(Functor<F> functor, Object... args) {
        return new StructureImpl<F>(functor, args).normal().proxy();
    }

    @SuppressWarnings("unused")
    private static <F extends Structure> StructureImpl<F> structImpl(FunctorImpl<F> functor, Object... args) {
        return new StructureImpl<F>(functor, args).normal();
    }

    public static class StructureImpl<F extends Structure> extends org.modelingvalue.collections.struct.impl.StructImpl implements InvocationHandler, Comparable<StructureImpl<F>> {
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

        protected StructureImpl(Functor<F> functor, Object... args) {
            super(unproxy(functor, args));
            this.hashCode = getHashCode();
        }

        protected StructureImpl(FunctorImpl<F> functor, Object... args) {
            super(array(functor, args));
            this.hashCode = getHashCode();
        }

        protected StructureImpl(Class<F> type, Object... args) {
            super(array(type, args));
            this.hashCode = getHashCode();
        }

        protected StructureImpl(Object[] args) {
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

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public String toString() {
            String string = super.toString();
            string = string.substring(1, string.length() - 1);
            int i = string.indexOf(',');
            return i >= 0 ? string.substring(0, i) + "(" + string.substring(i + 1) + ")" : string + "()";
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

        @SuppressWarnings("unchecked")
        protected F proxy() {
            return (F) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{type()}, this);
        }

        @SuppressWarnings("unchecked")
        protected Class<F> type() {
            Object t = get(0);
            return t instanceof FunctorImpl ? ((FunctorImpl<F>) t).functType() : (Class<F>) t;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public FunctorImpl<F> functor() {
            Object t = get(0);
            return t instanceof FunctorImpl ? (FunctorImpl<F>) t : null;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public int compareTo(StructureImpl<F> o) {
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

        protected StructureImpl<F> eq(StructureImpl<F> other) {
            if (equals(other)) {
                return this;
            } else if (length() != other.length()) {
                return null;
            }
            Object[] array = null;
            for (int i = 0; i < length(); i++) {
                Object tv = get(i);
                Object eq = eq(tv, other.get(i));
                if (eq == null) {
                    return null;
                } else if (!Objects.equals(eq, tv)) {
                    if (array == null) {
                        array = toArray();
                    }
                    array[i] = eq;
                }
            }
            return array != null ? struct(array) : this;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object eq(Object tv, Object ov) {
            if (tv != ov) {
                if (tv instanceof StructureImpl && ov instanceof StructureImpl) {
                    return ((StructureImpl) tv).eq((StructureImpl) ov);
                } else if (tv instanceof StructureImpl && ov instanceof Class) {
                    return ((Class) ov).isAssignableFrom(((StructureImpl) tv).type()) ? tv : null;
                } else if (tv instanceof Class && ov instanceof StructureImpl) {
                    return ((Class) tv).isAssignableFrom(((StructureImpl) ov).type()) ? ov : null;
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

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Map<VariableImpl, Object> variables() {
            Map<VariableImpl, Object> vars = Map.of();
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v instanceof VariableImpl) {
                    vars = vars.put((VariableImpl) v, ((VariableImpl) v).type());
                } else if (v instanceof StructureImpl) {
                    vars = vars.putAll(((StructureImpl) v).variables());
                }
            }
            return vars;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <V extends Structure, T extends StructureImpl<V>> T getStruct(int i) {
            Object v = get(i);
            return v instanceof StructureImpl ? (T) v : null;
        }

        @SuppressWarnings("unchecked")
        public <V> V getVal(int i) {
            Object v = get(i);
            return v instanceof Class || v instanceof StructureImpl ? null : (V) v;
        }

        public StructureImpl<F> set(int f, Object... a) {
            Object[] array = null;
            for (int i = 0; i < a.length; i++) {
                Object v = get(i + f);
                if (!Objects.equals(a[i], v)) {
                    if (array == null) {
                        array = toArray();
                    }
                    array[i + f] = a[i];
                }
            }
            return array != null ? struct(array) : this;
        }

        @SuppressWarnings("unchecked")
        protected final StructureImpl<F> normal() {
            FunctorImpl<F> f = functor();
            NormalizeLambda n = f != null ? f.functNormal() : null;
            return n != null ? (StructureImpl<F>) n.apply((StructureImpl<Structure>) this) : this;
        }

        @SuppressWarnings("unchecked")
        protected StructureImpl<F> struct(Object[] array) {
            return new StructureImpl<F>(array).normal();
        }

        @SuppressWarnings("rawtypes")
        protected Map<VariableImpl, Object> getBinding(StructureImpl<F> struct, Map<VariableImpl, Object> vars) {
            if (get(0).equals(struct.get(0))) {
                for (int i = 1; i < length(); i++) {
                    vars = getBinding(get(i), struct.get(i), vars);
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
        private static Map<VariableImpl, Object> getBinding(Object v, Object tv, Map<VariableImpl, Object> vars) {
            Class tt = typeOf(tv);
            tv = tv instanceof Class ? null : tv;
            if (v instanceof VariableImpl) {
                VariableImpl var = (VariableImpl) v;
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
            } else if (v instanceof StructureImpl) {
                StructureImpl t = (StructureImpl) v;
                if (tv != null) {
                    if (tv instanceof StructureImpl) {
                        vars = t.getBinding((StructureImpl) tv, vars);
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
        public static Class typeOf(Object v) {
            return v instanceof StructureImpl ? ((StructureImpl) v).type() : v instanceof Class ? (Class) v : null;
        }

        @SuppressWarnings("rawtypes")
        protected StructureImpl setBinding(StructureImpl<F> struct, Map<VariableImpl, Object> vars) {
            Object[] array = null;
            for (int i = 1; i < struct.length(); i++) {
                Object tv = struct.get(i);
                Object b = setBinding(get(i), tv, vars);
                if (!Objects.equals(b, tv)) {
                    if (array == null) {
                        array = struct.toArray();
                    }
                    array[i] = b;
                }
            }
            return array != null ? struct.struct(array) : struct;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object setBinding(Object v, Object tv, Map<VariableImpl, Object> vars) {
            if (v instanceof VariableImpl) {
                Object vv = vars.get((VariableImpl) v);
                if (vv != null) {
                    return vv;
                }
            } else if (v instanceof StructureImpl) {
                if (tv instanceof StructureImpl) {
                    return ((StructureImpl) v).setBinding((StructureImpl) tv, vars);
                } else if (tv instanceof Class && ((Class) tv).isAssignableFrom((((StructureImpl) v).type()))) {
                    return ((StructureImpl) v).setBinding((StructureImpl) v, vars);
                }
            }
            return tv;
        }

        @SuppressWarnings("rawtypes")
        protected int nrOfNulls() {
            int nr = 0;
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v == null || v instanceof Class) {
                    nr++;
                } else if (v instanceof StructureImpl) {
                    nr += ((StructureImpl) v).nrOfNulls();
                }
            }
            return nr;
        }

        @SuppressWarnings("rawtypes")
        protected int totalLength() {
            int nr = 0;
            for (int i = 1; i < length(); i++) {
                Object v = get(i);
                if (v instanceof StructureImpl) {
                    nr += ((StructureImpl) v).totalLength();
                }
                nr++;
            }
            return nr;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final void noProxy(Object object) {
        if (object instanceof Structure) {
            throw new IllegalArgumentException();
        } else if (object instanceof List) {
            ((List) object).forEach(Logic::noProxy);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Object unproxy(Object object) {
        if (object instanceof Structure) {
            return Proxy.getInvocationHandler(object);
        } else if (object instanceof List) {
            return ((List) object).replaceAll(Logic::unproxy);
        } else {
            Objects.requireNonNull(object);
            return object;
        }
    }

    @SuppressWarnings("unchecked")
    protected static final <T extends Structure, R extends StructureImpl<T>> R unproxy(T object) {
        return (R) Proxy.getInvocationHandler(object);
    }

    // Functor

    public interface Functor<T extends Structure> extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Structure> FunctorImpl<T> functImpl(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        SerializableSupplierImpl<T> l = method.of();
        return new FunctorImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure> Functor<T> functor(SerializableSupplier<T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Structure, A> FunctorImpl<T> functImpl(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        SerializableFunctionImpl<A, T> l = method.of();
        return new FunctorImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A> Functor<T> functor(SerializableFunction<A, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Structure, A, B> FunctorImpl<T> functImpl(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        SerializableBiFunctionImpl<A, B, T> l = method.of();
        return new FunctorImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B> Functor<T> functor(SerializableBiFunction<A, B, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Structure, A, B, C> FunctorImpl<T> functImpl(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        SerializableTriFunctionImpl<A, B, C, T> l = method.of();
        return new FunctorImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B, C> Functor<T> functor(SerializableTriFunction<A, B, C, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends Structure, A, B, C, D> FunctorImpl<T> functImpl(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
        SerializableQuadFunctionImpl<A, B, C, D, T> l = method.of();
        return new FunctorImpl<T>((Class<T>) l.out(), l.getImplMethodName(), l.in(), modifiers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Structure, A, B, C, D> Functor<T> functor(SerializableQuadFunction<A, B, C, D, T> method, FunctorModifier... modifiers) {
        return functImpl(method, modifiers).proxy();
    }

    public static final class FunctorImpl<T extends Structure> extends StructureImpl<Functor<T>> {
        private static final long     serialVersionUID = 285147889847599160L;

        private final LogicLambda     logic;
        private final NormalizeLambda normal;
        private final boolean         factual;
        private final boolean         derived;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private FunctorImpl(Class<T> type, String name, List<Class<?>> args, FunctorModifier... modifiers) {
            super((Class) Functor.class, type, name, args);
            updateSpecs(type);
            for (Class arg : args) {
                updateSpecs(arg);
            }
            this.logic = logic(modifiers);
            this.normal = normal(modifiers);
            this.factual = has(FunctorModifierEnum.factual, modifiers);
            this.derived = has(FunctorModifierEnum.derived, modifiers);
        }

        private static LogicLambda logic(FunctorModifier... modifiers) {
            LogicLambda lambda = get(LogicLambda.class, modifiers);
            return lambda != null ? lambda.of() : null;
        }

        private static NormalizeLambda normal(FunctorModifier... modifiers) {
            NormalizeLambda lambda = get(NormalizeLambda.class, modifiers);
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
        private FunctorImpl(Object[] args) {
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
        protected FunctorImpl<T> struct(Object[] array) {
            return new FunctorImpl<T>(array);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected Class<Functor<T>> type() {
            return (Class) Functor.class;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected List<Class> args() {
            return (List<Class>) get(3);
        }

        protected LogicLambda logic() {
            return logic;
        }

        protected NormalizeLambda functNormal() {
            return normal;
        }

        @SuppressWarnings("unchecked")
        protected Class<T> functType() {
            return (Class<T>) get(1);
        }
    }

    // Variables

    public interface Variable extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <F extends Structure> F var(Class<F> type, String id) {
        return new VariableImpl<F>(type, id).proxy();
    }

    private static final class VariableImpl<F extends Structure> extends StructureImpl<F> {
        private static final long serialVersionUID = -8998368070388908726L;

        private VariableImpl(Class<F> type, String name) {
            super(type, name);
            updateSpecs(type);
        }

        private VariableImpl(Object[] args) {
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
        protected VariableImpl<F> struct(Object[] array) {
            return new VariableImpl<F>(array);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Class<F> type() {
            return (Class<F>) get(0);
        }
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
        PredicateImpl impl = Logic.<Predicate, PredicateImpl> unproxy(pred);
        Set<PredicateImpl> match = match(impl);
        Set<Map<VariableImpl, Object>> bindings = match.replaceAll(m -> m.isIncomplete() ? Map.of(Entry.of(INCOMPLETE_VAR, m)) : impl.getBinding(m, Map.of()));
        return bindings.replaceAll(m -> m.replaceAll(e -> Entry.of((Variable) e.getKey().proxy(), proxy(e.getValue()))));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<PredicateImpl> match(Predicate pred) {
        return match(Logic.<Predicate, PredicateImpl> unproxy(pred));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<PredicateImpl> match(PredicateImpl impl) {
        return impl.setBinding(impl, impl.variables()).match(impl, List.of(), Map.of(), DATABASE.get());
    }

    @SuppressWarnings("unchecked")
    public static <P extends Predicate> P pred(Functor<P> functor, Object... args) {
        return (P) new PredicateImpl((Functor<Predicate>) functor, args).proxy();
    }

    @SuppressWarnings("unchecked")
    private static <P extends Predicate> PredicateImpl predImpl(FunctorImpl<P> functor, Object... args) {
        return new PredicateImpl((FunctorImpl<Predicate>) functor, args);
    }

    public static class PredicateImpl extends StructureImpl<Predicate> {
        private static final long serialVersionUID = -1605559565948158856L;

        protected PredicateImpl(Functor<Predicate> functor, Object... args) {
            super(functor, args);
        }

        protected PredicateImpl(FunctorImpl<Predicate> functor, Object... args) {
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
        protected final void makeFact(Database database) {
            if (functor().logic() != null) {
                throw new IllegalArgumentException("No facts of a functor with a logic lambda allowed. " + this);
            }
            if (database.rules.get().get(signature()) != null) {
                throw new IllegalArgumentException("No facts of a functor with rules allowed. " + this);
            }
            database.facts.updateAndGet(m -> {
                List<Class> args = functor().args();
                m = m.put(this, ADD_FACT.apply(m.get(this), this));
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
                m = m.put(pred, ADD_FACT.apply(m.get(pred), this));
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
            return Set.of(Logic.incompleteImpl(List.of(this)));
        }

        protected boolean isIncomplete() {
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
        protected PredicateImpl setBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
            return (PredicateImpl) super.setBinding(pred, vars);
        }

        @SuppressWarnings("rawtypes")
        private final PredicateImpl signature() {
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
        private Map<PredicateImpl, List<RuleImpl>> addRule(RuleImpl ruleImpl, Map<PredicateImpl, List<RuleImpl>> rules, Map<Class, Set<Class>> specs) {
            rules = rules.put(this, ADD_RULE.apply(rules.get(this), ruleImpl));
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
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            FunctorImpl<Predicate> functor = functor();
            LogicLambda logic = functor.logic();
            if (logic != null) {
                return logic.apply((PredicateImpl) this);
            }
            int non = nrOfNulls();
            if (non > 1 || non >= totalLength()) {
                return Set.of(Logic.incompleteImpl(der.append(this)));
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
                    return Set.of(Logic.incompleteImpl(der.append(this)));
                }
                if (der.size() >= MAX_LOGIC_DEPTH) {
                    return Set.of(Logic.incompleteImpl(der.append(this)));
                }
                Set<PredicateImpl> set = fixpoint(rules, non, der.append(this), rec, database);
                if (der.size() >= MAX_LOGIC_DEPTH_D2) {
                    Optional<? extends StructureImpl> ic = set.findAny(PredicateImpl::isToDepthIcomplete);
                    if (ic.isPresent()) {
                        if (der.size() == MAX_LOGIC_DEPTH_D2) {
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
                        if (a[0].size() >= MAX_LOGIC_MEMOIZ_D4) {
                            a[2] = a[2].putAll(a[1]);
                            a[1] = a[0];
                            a[0] = EMPTY_MEMOIZ;
                        }
                        a[0] = a[0].put(new Memoiz(this, set));
                        for (PredicateImpl e : set) {
                            a[0] = a[0].put(new Memoiz(e, Set.of(e)));
                        }
                        return a;
                    });
                    if (mem[2].size() > MAX_LOGIC_MEMOIZ && mem[0].size() == set.size() + 1) {
                        LOGIC_POOL.execute(database::cleanup);
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
        protected PredicateImpl eq(StructureImpl<Predicate> other) {
            return (PredicateImpl) super.eq(other);
        }

        @SuppressWarnings("rawtypes")
        protected boolean contains(PredicateImpl cond) {
            return equals(cond);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public PredicateImpl set(int i, Object... a) {
            return (PredicateImpl) super.set(i, a);
        }
    };

    // Collect

    private static final FunctorImpl<Predicate> COLLECT_FUNCTOR       = Logic.<Predicate, Predicate, Predicate> functImpl(Logic::collect);
    private static final Functor<Predicate>     COLLECT_FUNCTOR_PROXY = COLLECT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Predicate collect(Predicate pred, Predicate accum) {
        return new CollectImpl(pred, accum).proxy();
    }

    private static final class CollectImpl extends PredicateImpl {
        private static final long serialVersionUID = -2799691054715131197L;

        private CollectImpl(Predicate pred, Predicate accum) {
            super(COLLECT_FUNCTOR_PROXY, pred, accum);
        }

        private CollectImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
            return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected CollectImpl struct(Object[] array) {
            return new CollectImpl(array);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl pred() {
            return (PredicateImpl) get(1);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl accum() {
            return (PredicateImpl) get(2);
        }

        @SuppressWarnings("rawtypes")
        private Map<VariableImpl, Object> localVariables;

        @SuppressWarnings("rawtypes")
        protected Map<VariableImpl, Object> localVariables() {
            if (localVariables == null) {
                Map<VariableImpl, Object> predVars = pred().variables();
                Map<VariableImpl, Object> accumVars = accum().variables();
                localVariables = predVars.retainAll(accumVars::contains);
            }
            return localVariables;
        }

        @SuppressWarnings("rawtypes")
        private Map<VariableImpl, Object> variables;

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VariableImpl, Object> variables() {
            if (variables == null) {
                Map<VariableImpl, Object> predVars = pred().variables();
                Map<VariableImpl, Object> accumVars = accum().variables();
                variables = predVars.removeAll(accumVars::contains).addAll(accumVars.removeAll(predVars::contains));
            }
            return variables;
        }

        private int identityIndex = -1;

        @SuppressWarnings("rawtypes")
        private int identityIndex() {
            if (identityIndex < 0) {
                PredicateImpl accum = accum();
                for (int i = 1; i < accum.length(); i++) {
                    Object v = accum.get(i);
                    if (!(v instanceof VariableImpl) && v instanceof StructureImpl) {
                        Class<?> rt = ((VariableImpl) accum.get(resultIndex())).type();
                        Class<?> at = ((StructureImpl) v).type();
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
                PredicateImpl accum = accum();
                for (int i = 1; i < accum.length(); i++) {
                    Object v = accum.get(i);
                    if (v instanceof VariableImpl && !localVariables().containsKey((VariableImpl) v)) {
                        resultIndex = i;
                        break;
                    }
                }
            }
            return resultIndex;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            Map<VariableImpl, Object> localVars = ((CollectImpl) goal).localVariables();
            int ii = ((CollectImpl) goal).identityIndex();
            int ri = ((CollectImpl) goal).resultIndex();
            PredicateImpl goalPred = ((CollectImpl) goal).pred();
            PredicateImpl goalAccum = ((CollectImpl) goal).accum();
            PredicateImpl accum = accum();
            StructureImpl id = accum.getStruct(ii);
            Set<StructureImpl> rs = Set.of(id);
            Set<PredicateImpl> inc = Set.of();
            for (PredicateImpl pm : goalPred.setBinding(pred(), localVars).match(goalPred, der, rec, database)) {
                if (pm.isIncomplete()) {
                    inc = inc.add(pm);
                } else {
                    Map<VariableImpl, Object> b = goalPred.getBinding(pm, Map.of());
                    Set<StructureImpl> irs = Set.of();
                    for (StructureImpl r : rs) {
                        PredicateImpl s = goalAccum.setBinding(accum, b).set(ii, r);
                        for (PredicateImpl am : s.match(goalAccum, der, rec, database)) {
                            if (am.isIncomplete()) {
                                inc = inc.add(am);
                            } else {
                                irs = irs.add(am.getStruct(ri));
                            }
                        }
                    }
                    rs = irs;
                }
            }
            for (StructureImpl t : rs) {
                inc = inc.add(set(2, accum.set(ri, t)));
            }
            return inc;
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
            Map<VariableImpl, Object> localVars = localVariables();
            return super.getBinding(pred, vars).removeAll(e -> localVars.containsKey(e.getKey()));
        }

        @Override
        public CollectImpl set(int i, Object... a) {
            return (CollectImpl) super.set(i, a);
        }
    }

    // Yes

    private static final FunctorImpl<Predicate> YES_FUNCTOR = Logic.<Predicate> functImpl(Logic::yes);
    private static final TrueImpl               YES         = new TrueImpl();
    private static final Predicate              YES_PROXY   = (Predicate) Proxy.newProxyInstance(Predicate.class.getClassLoader(), new Class[]{Predicate.class}, YES);

    @SuppressWarnings("unchecked")
    public static Predicate yes() {
        return YES_PROXY;
    }

    private static final class TrueImpl extends PredicateImpl {
        private static final long serialVersionUID = -8515171118744898263L;

        private TrueImpl() {
            super(YES_FUNCTOR);
        }

        private TrueImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
            return YES_PROXY;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected TrueImpl struct(Object[] array) {
            return new TrueImpl(array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            return Set.of(this);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
            return vars;
        }

        @Override
        public TrueImpl set(int i, Object... a) {
            return (TrueImpl) super.set(i, a);
        }
    }

    // No

    private static final FunctorImpl<Predicate> NO_FUNCTOR = Logic.<Predicate> functImpl(Logic::no);
    private static final FalseImpl              NO         = new FalseImpl();
    private static final Predicate              NO_PROXY   = (Predicate) Proxy.newProxyInstance(Predicate.class.getClassLoader(), new Class[]{Predicate.class}, NO);

    @SuppressWarnings("unchecked")
    public static Predicate no() {
        return NO_PROXY;
    }

    private static final class FalseImpl extends PredicateImpl {
        private static final long serialVersionUID = -8515171118744898263L;

        private FalseImpl() {
            super(NO_FUNCTOR);
        }

        private FalseImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
            return NO_PROXY;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected FalseImpl struct(Object[] array) {
            return new FalseImpl(array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            return Set.of();
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
            return vars;
        }

        @Override
        public FalseImpl set(int i, Object... a) {
            return (FalseImpl) super.set(i, a);
        }
    }

    // Not

    private static final FunctorImpl<Predicate> NOT_FUNCTOR       = Logic.<Predicate, Predicate> functImpl(Logic::not);
    private static final Functor<Predicate>     NOT_FUNCTOR_PROXY = NOT_FUNCTOR.proxy();

    @SuppressWarnings("unchecked")
    public static Predicate not(Predicate pred) {
        return new NotImpl(pred).proxy();
    }

    private static final class NotImpl extends PredicateImpl {
        private static final long serialVersionUID = -4543178470298951866L;

        private NotImpl(Predicate pred) {
            super(NOT_FUNCTOR_PROXY, pred);
        }

        private NotImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
            return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected NotImpl struct(Object[] array) {
            return new NotImpl(array);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl pred() {
            return (PredicateImpl) get(1);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            Set<PredicateImpl> r = pred().match(((NotImpl) goal).pred(), der, rec, database);
            return r.isEmpty() ? Set.of(this) : r.retainAll(PredicateImpl::isIncomplete);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
            return vars;
        }

        @Override
        public NotImpl set(int i, Object... a) {
            return (NotImpl) super.set(i, a);
        }
    }

    // Or

    private static final FunctorImpl<Predicate> OR_FUNCTOR = Logic.<Predicate, Predicate, Predicate> functImpl(Logic::or);

    private static Predicate or(Predicate p1, Predicate p2) {
        return new OrImpl(unproxy(p1), unproxy(p2)).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate or(Predicate... ps) {
        PredicateImpl impl = NO;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == NO ? unproxy(ps[i]) : new OrImpl(unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    private static final class OrImpl extends PredicateImpl {
        private static final long serialVersionUID = -1732549494864415986L;

        private OrImpl(PredicateImpl pred1, PredicateImpl pred2) {
            super(OR_FUNCTOR, pred1, pred2);
        }

        private OrImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
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
                    l = l.prependList(((OrImpl) p1).idxList().replaceAll(ADD_ONE));
                } else {
                    l = l.append(ONE_ARRAY);
                }
                PredicateImpl p2 = pred2();
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
        protected final PredicateImpl pred1() {
            return (PredicateImpl) get(1);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl pred2() {
            return (PredicateImpl) get(2);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
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
        protected boolean contains(PredicateImpl cond) {
            return super.contains(cond) || pred1().contains(cond) || pred2().contains(cond);
        }
    }

    // And

    private static final FunctorImpl<Predicate> AND_FUNCTOR = Logic.<Predicate, Predicate, Predicate> functImpl(Logic::and);

    private static Predicate and(Predicate p1, Predicate p2) {
        return new AndImpl(unproxy(p1), unproxy(p2)).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate and(Predicate... ps) {
        PredicateImpl impl = YES;
        for (int i = ps.length - 1; i >= 0; i--) {
            impl = impl == YES ? unproxy(ps[i]) : new AndImpl(unproxy(ps[i]), impl);
        }
        return impl.proxy();
    }

    private static final class AndImpl extends PredicateImpl {
        private static final long serialVersionUID = -7248491569810098948L;

        private AndImpl(PredicateImpl pred1, PredicateImpl pred2) {
            super(AND_FUNCTOR, pred1, pred2);
        }

        private AndImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Predicate proxy() {
            return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected AndImpl struct(Object[] array) {
            return new AndImpl(array);
        }

        private List<int[]> idxList;

        @SuppressWarnings("rawtypes")
        private List<int[]> idxList() {
            if (idxList == null) {
                List<int[]> l = List.of();
                PredicateImpl p1 = pred1();
                if (p1 instanceof AndImpl) {
                    l = l.prependList(((AndImpl) p1).idxList().replaceAll(ADD_ONE));
                } else {
                    l = l.append(ONE_ARRAY);
                }
                PredicateImpl p2 = pred2();
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
        protected final PredicateImpl pred1() {
            return (PredicateImpl) get(1);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl pred2() {
            return (PredicateImpl) get(2);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            Set<PredicateImpl> out = Set.of();
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
                        Set<PredicateImpl> ic = Set.of();
                        for (int ii = 0; ii < idxl.size(); ii++) {
                            int[] i = idxl.get(ii);
                            PredicateImpl g = goal.getPred(i);
                            Set<PredicateImpl> ts = and.getPred(i).match(g, der, rec, database);
                            Set<PredicateImpl> in = ts.retainAll(PredicateImpl::isIncomplete);
                            if (in.isEmpty()) {
                                List<int[]> iil = idxl.removeIndex(ii);
                                ands = ands.addAll(ts.replaceAll(m -> {
                                    AndImpl a = (AndImpl) goal.setBinding(and, g.getBinding(m, Map.of()));
                                    a.idxList = iil;
                                    return a;
                                }));
                                continue outer;
                            } else if (in.anyMatch(PredicateImpl::isToDepthIcomplete)) {
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
        public AndImpl set(int i, Object... a) {
            return (AndImpl) super.set(i, a);
        }
    }

    // Rules

    public interface Rule extends Structure {
    }

    private static final FunctorImpl<Rule> RULE_FUNCTOR       = Logic.<Rule, Relation, Predicate> functImpl(Logic::rule);

    private static final Functor<Rule>     RULE_FUNCTOR_PROXY = RULE_FUNCTOR.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Rule rule(Relation consequence, Predicate condition) {
        RuleImpl ruleImpl = new RuleImpl(consequence, condition);
        PredicateImpl consImpl = Logic.<Predicate, PredicateImpl> unproxy(consequence);
        PredicateImpl signature = consImpl.signature();
        Map<Class, Set<Class>> specs = SPECS.get();
        Database database = DATABASE.get();
        database.rules.updateAndGet(m -> signature.addRule(ruleImpl, m, specs));
        return ruleImpl.proxy();
    }

    private static final class RuleImpl extends StructureImpl<Rule> {
        private static final long serialVersionUID = -4602043866952049391L;

        private RuleImpl(Relation pred, Predicate goal) {
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
        protected RuleImpl struct(Object[] array) {
            return new RuleImpl(array);
        }

        @SuppressWarnings("rawtypes")
        private Map<VariableImpl, Object> variables;

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Map<VariableImpl, Object> variables() {
            if (variables == null) {
                variables = super.variables();
            }
            return variables;
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl cons() {
            return (PredicateImpl) get(1);
        }

        @SuppressWarnings("rawtypes")
        protected final PredicateImpl cond() {
            return (PredicateImpl) get(2);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Set<PredicateImpl> eval(PredicateImpl pred, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
            PredicateImpl cons = cons();
            Map<VariableImpl, Object> binding = cons.getBinding(pred, Map.of());
            if (binding == null) {
                return Set.of();
            }
            if (TRACE_LOGIC) {
                System.err.println("LOGIC " + "  ".repeat(der.size()) + this + " " + binding.toString().substring(3));
            }
            PredicateImpl cond = cond();
            Set<PredicateImpl> match = cond.setBinding(cond, cond.variables().putAll(binding)).match(cond, der, rec, database);
            return match.replaceAll(t -> t.isIncomplete() ? t : cons.setBinding(pred, cond.getBinding(t, Map.of())));
        }

        protected int rulePrio() {
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

    // Incomplete

    public interface Incomplete extends Predicate {
    }

    @SuppressWarnings("rawtypes")
    private static final FunctorImpl<Incomplete>  INCOMPLETE_FUNCTOR       = Logic.<Incomplete, List<Predicate>> functImpl(Logic::incomplete);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static PredicateImpl incompleteImpl(List<PredicateImpl> der) {
        return predImpl(INCOMPLETE_FUNCTOR, der);
    }

    // Facts

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void fact(Relation pred) {
        Logic.<Predicate, PredicateImpl> unproxy(pred).makeFact(DATABASE.get());
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

    public interface Atomic<T extends Structure> extends Structure {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> eq = Logic.<Predicate, Atomic, Atomic> functor(Logic::eq, (LogicLambda) t -> {
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
    public static <T extends Structure> Predicate eq(Atomic<T> a, Atomic<T> b) {
        return pred(eq, a, b);
    }

    // Is

    public interface Function<T extends Structure> extends Structure {
    }

    @SuppressWarnings("rawtypes")
    private static final Functor<Relation> is = Logic.<Relation, Structure, Structure> functor(Logic::is);

    private static <T extends Structure> Relation is(T a, T b) {
        return pred(is, a, b);
    }

    // Use this one for function definitions
    public static <T extends Structure> Relation is(T a, Atomic<T> b) {
        return pred(is, a, b);
    }

    // Implied by the above using the generic rules here
    public static <T extends Structure> Relation is(Atomic<T> a, T b) {
        return pred(is, a, b);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void isRules() {
        Atomic A1 = var(Atomic.class, "A1");
        Atomic A2 = var(Atomic.class, "A2");
        Function F1 = var(Function.class, "F1");
        Function F2 = var(Function.class, "F2");

        rule(is((Structure) A1, (Structure) A2), eq(A1, A2));
        rule(is(F1, F2), and(is(F2, A2), is(F1, A2)));
        rule(is(A1, F1), is(F1, A1));
    }

}

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

import static org.modelingvalue.logic.Logic.*;

import java.math.BigInteger;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.SerializableBiFunction;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.SerializableTriFunction;
import org.modelingvalue.logic.Logic.*;

public final class Integers {

    private Integers() {
    }

    public static interface Int extends Term {
    }

    public static interface IntAtom extends Int, Atom<Int> {
    }

    public static interface IntFunc extends Int, Func<Int> {
    }

    private static Functor<IntAtom> i = functor((SerializableFunction<BigInteger, IntAtom>) Integers::i);

    public static IntAtom i(BigInteger x) {
        return term(i, x);
    }

    public static IntAtom i(String val, int radix) {
        return i(new BigInteger(val, radix));
    }

    public static IntAtom i(long x) {
        return i(BigInteger.valueOf(x));
    }

    public static IntAtom iav(String name) {
        return var(IntAtom.class, name);
    }

    public static Int iv(String name) {
        return var(Int.class, name);
    }

    // Operators

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> compare = functor((SerializableTriFunction<IntAtom, IntAtom, IntAtom, Pred>) Integers::compare, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null) {
            BigInteger r = BigInteger.valueOf(ai.compareTo(bi));
            if (ci != null) {
                return ci.equals(r) ? Set.of(t) : Set.of();
            } else {
                return Set.of(t.set(3, at.set(1, r)));
            }
        } else if (BigInteger.ZERO.equals(ci)) {
            if (ai != null) {
                return Set.of(t.set(2, at));
            } else if (bi != null) {
                return Set.of(t.set(1, bt));
            }
        }
        return t.incomplete();
    });

    public static Pred compare(IntAtom a, IntAtom b, IntAtom c) {
        return term(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> plusPred = functor((SerializableTriFunction<IntAtom, IntAtom, IntAtom, Pred>) Integers::plus, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null && ci != null) {
            return ai.add(bi).equals(ci) ? Set.of(t) : Set.of();
        } else if (ai != null && bi != null && ci == null) {
            return Set.of(t.set(3, at.set(1, ai.add(bi))));
        } else if (ai != null && bi == null && ci != null) {
            return Set.of(t.set(2, at.set(1, ci.subtract(ai))));
        } else if (ai == null && bi != null && ci != null) {
            return Set.of(t.set(1, bt.set(1, ci.subtract(bi))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred plus(IntAtom a, IntAtom b, IntAtom r) {
        return term(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> multiplyPred = functor((SerializableTriFunction<IntAtom, IntAtom, IntAtom, Pred>) Integers::multiply, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null && ci != null) {
            return ai.multiply(bi).equals(ci) ? Set.of(t) : Set.of();
        } else if (ai != null && bi != null && ci == null) {
            return Set.of(t.set(3, at.set(1, ai.multiply(bi))));
        } else if (ai != null && bi == null && ci != null) {
            return Set.of(t.set(2, at.set(1, ci.divide(ai))));
        } else if (ai == null && bi != null && ci != null) {
            return Set.of(t.set(1, bt.set(1, ci.divide(bi))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred multiply(IntAtom a, IntAtom b, IntAtom r) {
        return term(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> powerPred = functor((SerializableBiFunction<IntAtom, IntAtom, Pred>) Integers::power, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        if (ai != null && bi != null) {
            return ai.multiply(ai).equals(bi) ? Set.of(t) : Set.of();
        } else if (ai != null && bi == null) {
            return Set.of(t.set(2, at.set(1, ai.multiply(ai))));
        } else if (ai == null && bi != null) {
            BigInteger sqrt = bi.sqrt();
            return Set.of(t.set(1, bt.set(1, sqrt)), t.set(1, bt.set(1, sqrt.negate())));
        } else {
            return t.incomplete();
        }
    });

    public static Pred power(IntAtom a, IntAtom r) {
        return term(powerPred, a, r);
    }

    // Functions

    private static Functor<AtomPred> gt = functor(Integers::gt);

    public static AtomPred gt(Int a, Int b) {
        return term(gt, a, b);
    }

    private static Functor<AtomPred> lt = functor(Integers::lt);

    public static AtomPred lt(Int a, Int b) {
        return term(lt, a, b);
    }

    private static Functor<AtomPred> ge = functor(Integers::ge);

    public static AtomPred ge(Int a, Int b) {
        return term(ge, a, b);
    }

    private static Functor<AtomPred> le = functor(Integers::le);

    public static AtomPred le(Int a, Int b) {
        return term(le, a, b);
    }

    private static Functor<IntFunc> plusFunc = functor((SerializableBiFunction<Int, Int, IntFunc>) Integers::plus);

    public static IntFunc plus(Int a, Int b) {
        return term(plusFunc, a, b);
    }

    private static Functor<IntFunc> minusFunc = functor((SerializableBiFunction<Int, Int, IntFunc>) Integers::minus);

    public static IntFunc minus(Int a, Int b) {
        return term(minusFunc, a, b);
    }

    private static Functor<IntFunc> multiplyFunc = functor((SerializableBiFunction<Int, Int, IntFunc>) Integers::multiply);

    public static IntFunc multiply(Int a, Int b) {
        return term(multiplyFunc, a, b);
    }

    private static Functor<IntFunc> divideFunc = functor((SerializableBiFunction<Int, Int, IntFunc>) Integers::divide);

    public static IntFunc divide(Int a, Int b) {
        return term(divideFunc, a, b);
    }

    private static Functor<IntFunc> powerFunc = functor((SerializableFunction<Int, IntFunc>) Integers::power);

    public static IntFunc power(Int a) {
        return term(powerFunc, a);
    }

    private static Functor<IntFunc> sqrtFunc = functor((SerializableFunction<Int, IntFunc>) Integers::sqrt);

    public static IntFunc sqrt(Int a) {
        return term(sqrtFunc, a);
    }

    // Rules

    public static void integerRules() {
        isRules();

        IntAtom P = iav("PL");
        IntAtom Q = iav("QL");
        IntAtom R = iav("RL");

        Int X = iv("X");
        Int Y = iv("Y");

        rule(is(plus(X, Y), R), and(is(X, P), is(Y, Q), plus(P, Q, R)));
        rule(is(minus(X, Y), R), and(is(X, P), is(Y, Q), plus(R, Q, P)));
        rule(is(multiply(X, Y), R), and(is(X, P), is(Y, Q), multiply(P, Q, R)));
        rule(is(divide(X, Y), R), and(is(X, P), is(Y, Q), multiply(R, Q, P)));
        rule(is(power(X), R), and(is(X, P), power(P, R)));
        rule(is(sqrt(X), R), and(is(X, P), power(R, P)));
        rule(gt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(1))));
        rule(lt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(-1))));
        rule(ge(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, R), or(eq(R, i(1)), eq(R, i(0)))));
        rule(le(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, R), or(eq(R, i(-1)), eq(R, i(0)))));
    }

}

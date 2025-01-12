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
import org.modelingvalue.logic.Logic.*;

public final class Integers {

    private Integers() {
    }

    public interface Integer extends Structure {
    }

    public interface IntCons extends Integer, Constant<Integer> {
    }

    public interface IntFunc extends Integer, Function<Integer> {
    }

    private static Functor<IntCons> i = Logic.<IntCons, BigInteger> functor(Integers::i);

    public static IntCons i(BigInteger x) {
        return constant(i, x);
    }

    public static IntCons i(String val, int radix) {
        return i(new BigInteger(val, radix));
    }

    public static IntCons i(long x) {
        return i(BigInteger.valueOf(x));
    }

    public static IntCons iav(String name) {
        return var(IntCons.class, name);
    }

    public static Integer iv(String name) {
        return var(Integer.class, name);
    }

    // Operators

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> compare = Logic.<Predicate, IntCons, IntCons, IntCons> functor(Integers::compare, (LogicLambda) t -> {
        StructureImpl<IntCons> at = t.getStruct(1);
        StructureImpl<IntCons> bt = t.getStruct(2);
        StructureImpl<IntCons> ct = t.getStruct(3);
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

    public static Predicate compare(IntCons a, IntCons b, IntCons c) {
        return pred(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> plusPred = Logic.<Predicate, IntCons, IntCons, IntCons> functor(Integers::plus, (LogicLambda) t -> {
        StructureImpl<IntCons> at = t.getStruct(1);
        StructureImpl<IntCons> bt = t.getStruct(2);
        StructureImpl<IntCons> ct = t.getStruct(3);
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

    public static Predicate plus(IntCons a, IntCons b, IntCons r) {
        return pred(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> multiplyPred = Logic.<Predicate, IntCons, IntCons, IntCons> functor(Integers::multiply, (LogicLambda) t -> {
        StructureImpl<IntCons> at = t.getStruct(1);
        StructureImpl<IntCons> bt = t.getStruct(2);
        StructureImpl<IntCons> ct = t.getStruct(3);
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

    public static Predicate multiply(IntCons a, IntCons b, IntCons r) {
        return pred(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> powerPred = Logic.<Predicate, IntCons, IntCons> functor(Integers::power, (LogicLambda) t -> {
        StructureImpl<IntCons> at = t.getStruct(1);
        StructureImpl<IntCons> bt = t.getStruct(2);
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

    public static Predicate power(IntCons a, IntCons r) {
        return pred(powerPred, a, r);
    }

    // Functions

    private static Functor<Relation> gt = functor(Integers::gt);

    public static Relation gt(Integer a, Integer b) {
        return pred(gt, a, b);
    }

    private static Functor<Relation> lt = functor(Integers::lt);

    public static Relation lt(Integer a, Integer b) {
        return pred(lt, a, b);
    }

    private static Functor<Relation> ge = functor(Integers::ge);

    public static Relation ge(Integer a, Integer b) {
        return pred(ge, a, b);
    }

    private static Functor<Relation> le = functor(Integers::le);

    public static Relation le(Integer a, Integer b) {
        return pred(le, a, b);
    }

    private static Functor<IntFunc> plusFunc = Logic.<IntFunc, Integer, Integer> functor(Integers::plus);

    public static IntFunc plus(Integer a, Integer b) {
        return function(plusFunc, a, b);
    }

    private static Functor<IntFunc> minusFunc = Logic.<IntFunc, Integer, Integer> functor(Integers::minus);

    public static IntFunc minus(Integer a, Integer b) {
        return function(minusFunc, a, b);
    }

    private static Functor<IntFunc> multiplyFunc = Logic.<IntFunc, Integer, Integer> functor(Integers::multiply);

    public static IntFunc multiply(Integer a, Integer b) {
        return function(multiplyFunc, a, b);
    }

    private static Functor<IntFunc> divideFunc = Logic.<IntFunc, Integer, Integer> functor(Integers::divide);

    public static IntFunc divide(Integer a, Integer b) {
        return function(divideFunc, a, b);
    }

    private static Functor<IntFunc> powerFunc = Logic.<IntFunc, Integer> functor(Integers::power);

    public static IntFunc power(Integer a) {
        return function(powerFunc, a);
    }

    private static Functor<IntFunc> sqrtFunc = Logic.<IntFunc, Integer> functor(Integers::sqrt);

    public static IntFunc sqrt(Integer a) {
        return function(sqrtFunc, a);
    }

    // Rules

    public static void integerRules() {
        isRules();

        IntCons P = iav("PL");
        IntCons Q = iav("QL");
        IntCons R = iav("RL");

        Integer X = iv("X");
        Integer Y = iv("Y");

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

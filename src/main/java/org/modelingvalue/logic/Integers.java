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
import org.modelingvalue.logic.Logic.Constant;
import org.modelingvalue.logic.Logic.Function;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;
import org.modelingvalue.logic.Logic.Relation;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.impl.Conclusion;
import org.modelingvalue.logic.impl.StructureImpl;

public final class Integers {

    private Integers() {
    }

    public interface Integer extends Structure {
    }

    public interface IntegerCons extends Integer, Constant<Integer> {
    }

    public interface IntegerFunc extends Integer, Function<Integer> {
    }

    private static Functor<IntegerCons> i = Logic.<IntegerCons, BigInteger> functor(Integers::i);

    public static IntegerCons i(BigInteger x) {
        return constant(i, x);
    }

    public static IntegerCons i(String val, int radix) {
        return i(new BigInteger(val, radix));
    }

    public static IntegerCons i(long x) {
        return i(BigInteger.valueOf(x));
    }

    public static IntegerCons iav(String name) {
        return var(IntegerCons.class, name);
    }

    public static Integer iv(String name) {
        return var(Integer.class, name);
    }

    // Predicates

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> compare = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::compare, (LogicLambda) predicate -> {
        StructureImpl<IntegerCons> at = predicate.getStruct(1);
        StructureImpl<IntegerCons> bt = predicate.getStruct(2);
        StructureImpl<IntegerCons> ct = predicate.getStruct(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null) {
            BigInteger r = BigInteger.valueOf(ai.compareTo(bi));
            if (ci != null) {
                return Conclusion.of(ci.equals(r) ? Set.of(predicate) : Set.of());
            } else {
                return Conclusion.of(Set.of(predicate.set(3, at.set(1, r))));
            }
        } else if (BigInteger.ZERO.equals(ci)) {
            if (ai != null) {
                return Conclusion.of(Set.of(predicate.set(2, at)));
            } else if (bi != null) {
                return Conclusion.of(Set.of(predicate.set(1, bt)));
            }
        }
        return predicate.incomplete();
    });

    public static Predicate compare(IntegerCons a, IntegerCons b, IntegerCons c) {
        return pred(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> plusPred = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::plus, (LogicLambda) predicate -> {
        StructureImpl<IntegerCons> at = predicate.getStruct(1);
        StructureImpl<IntegerCons> bt = predicate.getStruct(2);
        StructureImpl<IntegerCons> ct = predicate.getStruct(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null && ci != null) {
            return Conclusion.of(ai.add(bi).equals(ci) ? Set.of(predicate) : Set.of());
        } else if (ai != null && bi != null && ci == null) {
            return Conclusion.of(Set.of(predicate.set(3, at.set(1, ai.add(bi)))));
        } else if (ai != null && bi == null && ci != null) {
            return Conclusion.of(Set.of(predicate.set(2, at.set(1, ci.subtract(ai)))));
        } else if (ai == null && bi != null && ci != null) {
            return Conclusion.of(Set.of(predicate.set(1, bt.set(1, ci.subtract(bi)))));
        } else {
            return predicate.incomplete();
        }
    });

    public static Predicate plus(IntegerCons a, IntegerCons b, IntegerCons r) {
        return pred(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> multiplyPred = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::multiply, (LogicLambda) predicate -> {
        StructureImpl<IntegerCons> at = predicate.getStruct(1);
        StructureImpl<IntegerCons> bt = predicate.getStruct(2);
        StructureImpl<IntegerCons> ct = predicate.getStruct(3);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (ai != null && bi != null && ci != null) {
            return Conclusion.of(ai.multiply(bi).equals(ci) ? Set.of(predicate) : Set.of());
        } else if (ai != null && bi != null && ci == null) {
            return Conclusion.of(Set.of(predicate.set(3, at.set(1, ai.multiply(bi)))));
        } else if (ai != null && bi == null && ci != null) {
            return Conclusion.of(Set.of(predicate.set(2, at.set(1, ci.divide(ai)))));
        } else if (ai == null && bi != null && ci != null) {
            return Conclusion.of(Set.of(predicate.set(1, bt.set(1, ci.divide(bi)))));
        } else {
            return predicate.incomplete();
        }
    });

    public static Predicate multiply(IntegerCons a, IntegerCons b, IntegerCons r) {
        return pred(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> squarePred = Logic.<Predicate, IntegerCons, IntegerCons> functor(Integers::square, (LogicLambda) predicate -> {
        StructureImpl<IntegerCons> at = predicate.getStruct(1);
        StructureImpl<IntegerCons> bt = predicate.getStruct(2);
        BigInteger ai = at != null ? at.getVal(1) : null;
        BigInteger bi = bt != null ? bt.getVal(1) : null;
        if (ai != null && bi != null) {
            return Conclusion.of(ai.multiply(ai).equals(bi) ? Set.of(predicate) : Set.of());
        } else if (ai != null && bi == null) {
            return Conclusion.of(Set.of(predicate.set(2, at.set(1, ai.multiply(ai)))));
        } else if (ai == null && bi != null) {
            BigInteger sqrt = bi.sqrt();
            return Conclusion.of(Set.of(predicate.set(1, bt.set(1, sqrt)), predicate.set(1, bt.set(1, sqrt.negate()))));
        } else {
            return predicate.incomplete();
        }
    });

    public static Predicate square(IntegerCons a, IntegerCons r) {
        return pred(squarePred, a, r);
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

    private static Functor<IntegerFunc> plusFunc = Logic.<IntegerFunc, Integer, Integer> functor(Integers::plus);

    public static IntegerFunc plus(Integer a, Integer b) {
        return function(plusFunc, a, b);
    }

    private static Functor<IntegerFunc> minusFunc = Logic.<IntegerFunc, Integer, Integer> functor(Integers::minus);

    public static IntegerFunc minus(Integer a, Integer b) {
        return function(minusFunc, a, b);
    }

    private static Functor<IntegerFunc> multiplyFunc = Logic.<IntegerFunc, Integer, Integer> functor(Integers::multiply);

    public static IntegerFunc multiply(Integer a, Integer b) {
        return function(multiplyFunc, a, b);
    }

    private static Functor<IntegerFunc> divideFunc = Logic.<IntegerFunc, Integer, Integer> functor(Integers::divide);

    public static IntegerFunc divide(Integer a, Integer b) {
        return function(divideFunc, a, b);
    }

    private static Functor<IntegerFunc> squareFunc = Logic.<IntegerFunc, Integer> functor(Integers::square);

    public static IntegerFunc square(Integer a) {
        return function(squareFunc, a);
    }

    private static Functor<IntegerFunc> sqrtFunc = Logic.<IntegerFunc, Integer> functor(Integers::sqrt);

    public static IntegerFunc sqrt(Integer a) {
        return function(sqrtFunc, a);
    }

    // Rules

    public static void integerRules() {
        isRules();

        IntegerCons P = iav("PL");
        IntegerCons Q = iav("QL");
        IntegerCons R = iav("RL");

        Integer X = iv("X");
        Integer Y = iv("Y");

        rule(gt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(1))));
        rule(lt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(-1))));
        rule(ge(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, R), or(eq(R, i(1)), eq(R, i(0)))));
        rule(le(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, R), or(eq(R, i(-1)), eq(R, i(0)))));

        rule(is(plus(X, Y), R), and(is(X, P), is(Y, Q), plus(P, Q, R)));
        rule(is(minus(X, Y), R), and(is(X, P), is(Y, Q), plus(R, Q, P)));
        rule(is(multiply(X, Y), R), and(is(X, P), is(Y, Q), multiply(P, Q, R)));
        rule(is(divide(X, Y), R), and(is(X, P), is(Y, Q), multiply(R, Q, P)));
        rule(is(square(X), R), and(is(X, P), square(P, R)));
        rule(is(sqrt(X), R), and(is(X, P), square(R, P)));
    }

}

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
import org.modelingvalue.logic.impl.FunctorImpl;
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

    private static FunctorImpl<IntegerCons> iImpl = FunctorImpl.<IntegerCons, BigInteger> of(Integers::i);
    private static Functor<IntegerCons>     i     = iImpl.proxy();

    public static IntegerCons i(BigInteger val) {
        return constant(i, val);
    }

    public static IntegerCons i(String val, int radix) {
        return i(new BigInteger(val, radix));
    }

    public static IntegerCons i(long val) {
        return i(BigInteger.valueOf(val));
    }

    public static IntegerCons iVarCons(String name) {
        return variable(IntegerCons.class, name);
    }

    public static Integer iVar(String name) {
        return variable(Integer.class, name);
    }

    // Predicates

    private static final StructureImpl<IntegerCons> ZERO_INT      = StructureImpl.unproxy(i(0));
    private static final StructureImpl<IntegerCons> ONE_INT       = StructureImpl.unproxy(i(1));
    private static final StructureImpl<IntegerCons> MINUS_ONE_INT = StructureImpl.unproxy(i(-1));

    private static StructureImpl<IntegerCons> struct(BigInteger i) {
        return ZERO_INT.set(1, i);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> compare = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::compare, (LogicLambda) predicate -> {
        BigInteger compared1 = predicate.getVal(1, 1);
        BigInteger compared2 = predicate.getVal(2, 1);
        BigInteger result = predicate.getVal(3, 1);
        if (compared1 != null && compared2 != null) {
            int r = compared1.compareTo(compared2);
            if (result != null) {
                return Conclusion.of(r == result.intValue() ? Set.of(predicate) : Set.of());
            } else {
                return Conclusion.of(Set.of(predicate.set(3, r == 0 ? ZERO_INT : r == 1 ? ONE_INT : MINUS_ONE_INT)));
            }
        } else if (BigInteger.ZERO.equals(result)) {
            if (compared1 != null) {
                return Conclusion.of(Set.of(predicate.set(2, predicate.getVal(1))));
            } else if (compared2 != null) {
                return Conclusion.of(Set.of(predicate.set(1, predicate.getVal(2))));
            }
        }
        return predicate.incomplete();
    });

    public static Predicate compare(IntegerCons a, IntegerCons b, IntegerCons c) {
        return pred(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> plusPred = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::plus, (LogicLambda) predicate -> {
        BigInteger addend1 = predicate.getVal(1, 1);
        BigInteger addend2 = predicate.getVal(2, 1);
        BigInteger sum = predicate.getVal(3, 1);
        if (addend1 != null && addend2 != null) {
            BigInteger s = addend1.add(addend2);
            if (sum != null) {
                return Conclusion.of(s.equals(sum) ? Set.of(predicate) : Set.of());
            } else {
                return Conclusion.of(Set.of(predicate.set(3, struct(s))));
            }
        } else if (addend1 != null && sum != null) {
            return Conclusion.of(Set.of(predicate.set(2, struct(sum.subtract(addend1)))));
        } else if (addend2 != null && sum != null) {
            return Conclusion.of(Set.of(predicate.set(1, struct(sum.subtract(addend2)))));
        } else {
            return predicate.incomplete();
        }
    });

    public static Predicate plus(IntegerCons a, IntegerCons b, IntegerCons r) {
        return pred(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> multiplyPred = Logic.<Predicate, IntegerCons, IntegerCons, IntegerCons> functor(Integers::multiply, (LogicLambda) predicate -> {
        BigInteger factor1 = predicate.getVal(1, 1);
        BigInteger factor2 = predicate.getVal(2, 1);
        BigInteger product = predicate.getVal(3, 1);
        if (factor1 != null && factor2 != null) {
            BigInteger p = factor1.multiply(factor2);
            if (product != null) {
                return Conclusion.of(p.equals(product) ? Set.of(predicate) : Set.of());
            } else {
                return Conclusion.of(Set.of(predicate.set(3, struct(p))));
            }
        } else if (factor1 != null && product != null) {
            return Conclusion.of(Set.of(predicate.set(2, struct(product.divide(factor1)))));
        } else if (factor2 != null && product != null) {
            return Conclusion.of(Set.of(predicate.set(1, struct(product.divide(factor2)))));
        } else {
            return predicate.incomplete();
        }
    });

    public static Predicate multiply(IntegerCons a, IntegerCons b, IntegerCons r) {
        return pred(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Predicate> squarePred = Logic.<Predicate, IntegerCons, IntegerCons> functor(Integers::square, (LogicLambda) predicate -> {
        BigInteger root = predicate.getVal(1, 1);
        BigInteger square = predicate.getVal(2, 1);
        if (root != null && square != null) {
            return Conclusion.of(root.multiply(root).equals(square) ? Set.of(predicate) : Set.of());
        } else if (root != null && square == null) {
            return Conclusion.of(Set.of(predicate.set(2, struct(root.multiply(root)))));
        } else if (root == null && square != null) {
            BigInteger sqrt = square.sqrt();
            return Conclusion.of(Set.of(predicate.set(1, struct(sqrt)), predicate.set(1, struct(sqrt.negate()))));
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

        IntegerCons P = iVarCons("PL");
        IntegerCons Q = iVarCons("QL");
        IntegerCons R = iVarCons("RL");

        Integer X = iVar("X");
        Integer Y = iVar("Y");

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

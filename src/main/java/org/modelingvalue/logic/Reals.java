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

import static org.modelingvalue.logic.Integers.i;
import static org.modelingvalue.logic.Integers.iav;
import static org.modelingvalue.logic.Logic.*;

import java.math.BigInteger;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.SerializableBiFunction;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.SerializableTriFunction;
import org.modelingvalue.logic.Integers.IntAtom;
import org.modelingvalue.logic.Logic.*;

public final class Reals {

    private Reals() {
    }

    public static interface Real extends Type<Real> {
    }

    public static interface RealAtom extends Real, Atom<Real> {
    }

    public static interface RealFunc extends Real, Func<Real> {
    }

    private static Functor<RealAtom> r = functor((SerializableBiFunction<BigInteger, BigInteger, RealAtom>) Reals::r, (EqualsLambda) (at, bt) -> {
        BigInteger ax = at.getVal(1);
        BigInteger ay = at.getVal(2);
        BigInteger bx = bt.getVal(1);
        BigInteger by = bt.getVal(2);
        BigInteger a = ax.multiply(by);
        BigInteger b = bx.multiply(ay);
        return a.equals(b) ? at : null;
    });

    public static RealAtom r(BigInteger x, BigInteger y) {
        return term(r, x, y);
    }

    public static RealAtom r(String x, String y, int radix) {
        return r(new BigInteger(x, radix), new BigInteger(y, radix));
    }

    public static RealAtom r(long x, long y) {
        return r(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    public static RealAtom r(BigInteger x) {
        return term(r, x, BigInteger.ONE);
    }

    public static RealAtom r(String x, int radix) {
        return r(new BigInteger(x, radix));
    }

    public static RealAtom r(long x) {
        return r(BigInteger.valueOf(x));
    }

    public static RealAtom rav(String name) {
        return var(RealAtom.class, name);
    }

    public static Real rv(String name) {
        return var(Real.class, name);
    }

    // Operators

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> compare = functor((SerializableTriFunction<RealAtom, RealAtom, IntAtom, Pred>) Reals::compare, (LogicLambda) t -> {
        TermImpl<RealAtom> at = t.getTerm(1);
        TermImpl<RealAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ci = ct != null ? ct.getVal(1) : null;
        if (at != null && bt != null) {
            BigInteger ax = at.getVal(1);
            BigInteger ay = at.getVal(2);
            BigInteger bx = bt.getVal(1);
            BigInteger by = bt.getVal(2);
            BigInteger a = ax.multiply(by);
            BigInteger b = bx.multiply(ay);
            BigInteger r = BigInteger.valueOf(a.compareTo(b));
            if (ci != null) {
                return ci.equals(r) ? Set.of(t) : Set.of();
            } else {
                return Set.of(t.set(3, at.set(1, r)));
            }
        } else if (BigInteger.ZERO.equals(ci)) {
            if (at != null) {
                return Set.of(t.set(2, at));
            } else if (bt != null) {
                return Set.of(t.set(1, bt));
            }
        }
        return t.incomplete();
    });

    public static Pred compare(RealAtom a, RealAtom b, IntAtom c) {
        return term(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> plusPred = functor((SerializableTriFunction<RealAtom, RealAtom, RealAtom, Pred>) Reals::plus, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger bx = bt != null ? bt.getVal(1) : null;
        BigInteger cx = ct != null ? ct.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        BigInteger by = bt != null ? bt.getVal(2) : null;
        BigInteger cy = ct != null ? ct.getVal(2) : null;
        if (at != null && bt != null && ct != null) {
            BigInteger a = ax.multiply(by).multiply(cy);
            BigInteger b = bx.multiply(ay).multiply(cy);
            BigInteger c = cx.multiply(ay).multiply(by);
            return a.add(b).equals(c) ? Set.of(t) : Set.of();
        } else if (at != null && bt != null && ct == null) {
            BigInteger a = ax.multiply(by);
            BigInteger b = bx.multiply(ay);
            return Set.of(t.set(3, at.set(1, a.add(b)).set(2, by.multiply(ay))));
        } else if (at != null && bt == null && ct != null) {
            BigInteger a = ax.multiply(cy);
            BigInteger c = cx.multiply(ay);
            return Set.of(t.set(2, at.set(1, c.subtract(a)).set(2, cy.multiply(ay))));
        } else if (at == null && bt != null && ct != null) {
            BigInteger b = bx.multiply(cy);
            BigInteger c = cx.multiply(by);
            return Set.of(t.set(1, bt.set(1, c.subtract(b)).set(2, cy.multiply(by))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred plus(RealAtom a, RealAtom b, RealAtom r) {
        return term(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> multiplyPred = functor((SerializableTriFunction<RealAtom, RealAtom, RealAtom, Pred>) Reals::multiply, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        TermImpl<IntAtom> ct = t.getTerm(3);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger bx = bt != null ? bt.getVal(1) : null;
        BigInteger cx = ct != null ? ct.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        BigInteger by = bt != null ? bt.getVal(2) : null;
        BigInteger cy = ct != null ? ct.getVal(2) : null;
        if (at != null && bt != null && ct != null) {
            BigInteger a = ax.multiply(by).multiply(cy);
            BigInteger b = bx.multiply(ay).multiply(cy);
            BigInteger c = cx.multiply(ay).multiply(by);
            return a.multiply(b).equals(c) ? Set.of(t) : Set.of();
        } else if (at != null && bt != null && ct == null) {
            return Set.of(t.set(3, at.set(1, ax.multiply(bx)).set(2, ay.multiply(by))));
        } else if (at != null && bt == null && ct != null) {
            return Set.of(t.set(2, at.set(1, cx.multiply(ay)).set(2, cy.multiply(ax))));
        } else if (at == null && bt != null && ct != null) {
            return Set.of(t.set(1, bt.set(1, cx.multiply(by)).set(2, cy.multiply(bx))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred multiply(RealAtom a, RealAtom b, RealAtom r) {
        return term(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> powerPred = functor((SerializableBiFunction<RealAtom, RealAtom, Pred>) Reals::power, (LogicLambda) t -> {
        TermImpl<IntAtom> at = t.getTerm(1);
        TermImpl<IntAtom> bt = t.getTerm(2);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger bx = bt != null ? bt.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        BigInteger by = bt != null ? bt.getVal(2) : null;
        if (at != null && bt != null) {
            BigInteger a = ax.multiply(by);
            BigInteger b = bx.multiply(ay);
            return a.multiply(a).equals(b) ? Set.of(t) : Set.of();
        } else if (at != null && bt == null) {
            return Set.of(t.set(2, at.set(1, ax.multiply(ax))));
        } else if (at == null && bt != null) {
            BigInteger sqrt = bx.multiply(by).sqrt();
            return Set.of(t.set(1, bt.set(1, sqrt)), t.set(1, bt.set(1, sqrt.negate())));
        } else {
            return t.incomplete();
        }
    });

    public static Pred power(RealAtom a, RealAtom r) {
        return term(powerPred, a, r);
    }

    // Functions

    private static Functor<AtomPred> gt = functor(Reals::gt);

    public static AtomPred gt(Real a, Real b) {
        return term(gt, a, b);
    }

    private static Functor<AtomPred> lt = functor(Reals::lt);

    public static AtomPred lt(Real a, Real b) {
        return term(lt, a, b);
    }

    private static Functor<AtomPred> ge = functor(Reals::ge);

    public static AtomPred ge(Real a, Real b) {
        return term(ge, a, b);
    }

    private static Functor<AtomPred> le = functor(Reals::le);

    public static AtomPred le(Real a, Real b) {
        return term(le, a, b);
    }

    private static Functor<RealFunc> plusFunc = functor((SerializableBiFunction<Real, Real, RealFunc>) Reals::plus);

    public static RealFunc plus(Real a, Real b) {
        return term(plusFunc, a, b);
    }

    private static Functor<RealFunc> minusFunc = functor((SerializableBiFunction<Real, Real, RealFunc>) Reals::minus);

    public static RealFunc minus(Real a, Real b) {
        return term(minusFunc, a, b);
    }

    private static Functor<RealFunc> multiplyFunc = functor((SerializableBiFunction<Real, Real, RealFunc>) Reals::multiply);

    public static RealFunc multiply(Real a, Real b) {
        return term(multiplyFunc, a, b);
    }

    private static Functor<RealFunc> divideFunc = functor((SerializableBiFunction<Real, Real, RealFunc>) Reals::divide);

    public static RealFunc divide(Real a, Real b) {
        return term(divideFunc, a, b);
    }

    private static Functor<RealFunc> powerFunc = functor((SerializableFunction<Real, RealFunc>) Reals::power);

    public static RealFunc power(Real a) {
        return term(powerFunc, a);
    }

    private static Functor<RealFunc> sqrtFunc = functor((SerializableFunction<Real, RealFunc>) Reals::sqrt);

    public static RealFunc sqrt(Real a) {
        return term(sqrtFunc, a);
    }

    // Rules

    public static void realRules() {
        isRules();

        RealAtom P = rav("PL");
        RealAtom Q = rav("QL");
        RealAtom R = rav("RL");

        IntAtom I = iav("IL");

        Real X = rv("X");
        Real Y = rv("Y");

        rule(is(plus(X, Y), R), and(is(X, P), is(Y, Q), plus(P, Q, R)));
        rule(is(minus(X, Y), R), and(is(X, P), is(Y, Q), plus(R, Q, P)));
        rule(is(multiply(X, Y), R), and(is(X, P), is(Y, Q), multiply(P, Q, R)));
        rule(is(divide(X, Y), R), and(is(X, P), is(Y, Q), multiply(R, Q, P)));
        rule(is(power(X), R), and(is(X, P), power(P, R)));
        rule(is(sqrt(X), R), and(is(X, P), power(R, P)));
        rule(gt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(1))));
        rule(lt(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, i(-1))));
        rule(ge(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, I), or(eq(I, i(1)), eq(I, i(0)))));
        rule(le(X, Y), and(is(X, P), is(Y, Q), compare(P, Q, I), or(eq(I, i(-1)), eq(I, i(0)))));
    }

}

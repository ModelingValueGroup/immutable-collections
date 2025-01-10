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
import org.modelingvalue.logic.Integers.IntAtom;
import org.modelingvalue.logic.Logic.*;

public final class Rationals {

    private Rationals() {
    }

    public interface Rational extends Term {
    }

    public interface RationalAtom extends Rational, Atom<Rational> {
    }

    public interface RationalFunc extends Rational, Func<Rational> {
    }

    private static Functor<RationalAtom> r = Logic.<RationalAtom, BigInteger, BigInteger> functor(Rationals::r, (NormalizeLambda) t -> {
        BigInteger ax = t.getVal(1);
        BigInteger ay = t.getVal(2);
        BigInteger gcd = ax.gcd(ay);
        return t.set(1, ax.divide(gcd), ay.divide(gcd));
    });

    public static RationalAtom r(BigInteger x, BigInteger y) {
        return term(r, x, y);
    }

    public static RationalAtom r(String x, String y, int radix) {
        return r(new BigInteger(x, radix), new BigInteger(y, radix));
    }

    public static RationalAtom r(long x, long y) {
        return r(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    public static RationalAtom r(BigInteger x) {
        return term(r, x, BigInteger.ONE);
    }

    public static RationalAtom r(String x, int radix) {
        return r(new BigInteger(x, radix));
    }

    public static RationalAtom r(long x) {
        return r(BigInteger.valueOf(x));
    }

    public static RationalAtom rav(String name) {
        return var(RationalAtom.class, name);
    }

    public static Rational rv(String name) {
        return var(Rational.class, name);
    }

    // Operators

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> compare = Logic.<Pred, RationalAtom, RationalAtom, IntAtom> functor(Rationals::compare, (LogicLambda) t -> {
        TermImpl<RationalAtom> at = t.getTerm(1);
        TermImpl<RationalAtom> bt = t.getTerm(2);
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

    public static Pred compare(RationalAtom a, RationalAtom b, IntAtom c) {
        return term(compare, a, b, c);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> plusPred = Logic.<Pred, RationalAtom, RationalAtom, RationalAtom> functor(Rationals::plus, (LogicLambda) t -> {
        TermImpl<RationalAtom> at = t.getTerm(1);
        TermImpl<RationalAtom> bt = t.getTerm(2);
        TermImpl<RationalAtom> ct = t.getTerm(3);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger bx = bt != null ? bt.getVal(1) : null;
        BigInteger cx = ct != null ? ct.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        BigInteger by = bt != null ? bt.getVal(2) : null;
        BigInteger cy = ct != null ? ct.getVal(2) : null;
        if (at != null && bt != null && ct != null) {
            BigInteger a = ax.multiply(by);
            BigInteger b = bx.multiply(ay);
            TermImpl<RationalAtom> pt = at.set(1, a.add(b), by.multiply(ay));
            return pt.equals(ct) ? Set.of(t) : Set.of();
        } else if (at != null && bt != null && ct == null) {
            BigInteger a = ax.multiply(by);
            BigInteger b = bx.multiply(ay);
            return Set.of(t.set(3, at.set(1, a.add(b), by.multiply(ay))));
        } else if (at != null && bt == null && ct != null) {
            BigInteger a = ax.multiply(cy);
            BigInteger c = cx.multiply(ay);
            return Set.of(t.set(2, at.set(1, c.subtract(a), cy.multiply(ay))));
        } else if (at == null && bt != null && ct != null) {
            BigInteger b = bx.multiply(cy);
            BigInteger c = cx.multiply(by);
            return Set.of(t.set(1, bt.set(1, c.subtract(b), cy.multiply(by))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred plus(RationalAtom a, RationalAtom b, RationalAtom r) {
        return term(plusPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> multiplyPred = Logic.<Pred, RationalAtom, RationalAtom, RationalAtom> functor(Rationals::multiply, (LogicLambda) t -> {
        TermImpl<RationalAtom> at = t.getTerm(1);
        TermImpl<RationalAtom> bt = t.getTerm(2);
        TermImpl<RationalAtom> ct = t.getTerm(3);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger bx = bt != null ? bt.getVal(1) : null;
        BigInteger cx = ct != null ? ct.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        BigInteger by = bt != null ? bt.getVal(2) : null;
        BigInteger cy = ct != null ? ct.getVal(2) : null;
        if (at != null && bt != null && ct != null) {
            TermImpl<RationalAtom> mt = at.set(1, ax.multiply(bx), ay.multiply(by));
            return mt.equals(ct) ? Set.of(t) : Set.of();
        } else if (at != null && bt != null && ct == null) {
            return Set.of(t.set(3, at.set(1, ax.multiply(bx), ay.multiply(by))));
        } else if (at != null && bt == null && ct != null) {
            return Set.of(t.set(2, at.set(1, cx.multiply(ay), cy.multiply(ax))));
        } else if (at == null && bt != null && ct != null) {
            return Set.of(t.set(1, bt.set(1, cx.multiply(by), cy.multiply(bx))));
        } else {
            return t.incomplete();
        }
    });

    public static Pred multiply(RationalAtom a, RationalAtom b, RationalAtom r) {
        return term(multiplyPred, a, b, r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Functor<Pred> powerPred = Logic.<Pred, RationalAtom, RationalAtom> functor(Rationals::power, (LogicLambda) t -> {
        TermImpl<RationalAtom> at = t.getTerm(1);
        TermImpl<RationalAtom> bt = t.getTerm(2);
        BigInteger ax = at != null ? at.getVal(1) : null;
        BigInteger ay = at != null ? at.getVal(2) : null;
        if (at != null && bt != null) {
            TermImpl<RationalAtom> mt = at.set(1, ax.multiply(ax), ay.multiply(ay));
            return mt.equals(bt) ? Set.of(t) : Set.of();
        } else if (at != null && bt == null) {
            return Set.of(t.set(2, at.set(1, ax.multiply(ax), ay.multiply(ay))));
        } else if (at == null && bt != null) {
            BigInteger bx = bt.getVal(1);
            BigInteger by = bt.getVal(2);
            BigInteger sqrt = bx.multiply(by).sqrt();
            bt = bt.set(2, by.abs());
            return Set.of(t.set(1, bt.set(1, sqrt)), t.set(1, bt.set(1, sqrt.negate())));
        } else {
            return t.incomplete();
        }
    });

    public static Pred power(RationalAtom a, RationalAtom r) {
        return term(powerPred, a, r);
    }

    // Functions

    private static Functor<AtomPred> gt = functor(Rationals::gt);

    public static AtomPred gt(Rational a, Rational b) {
        return term(gt, a, b);
    }

    private static Functor<AtomPred> lt = functor(Rationals::lt);

    public static AtomPred lt(Rational a, Rational b) {
        return term(lt, a, b);
    }

    private static Functor<AtomPred> ge = functor(Rationals::ge);

    public static AtomPred ge(Rational a, Rational b) {
        return term(ge, a, b);
    }

    private static Functor<AtomPred> le = functor(Rationals::le);

    public static AtomPred le(Rational a, Rational b) {
        return term(le, a, b);
    }

    private static Functor<RationalFunc> plusFunc = Logic.<RationalFunc, Rational, Rational> functor(Rationals::plus);

    public static RationalFunc plus(Rational a, Rational b) {
        return term(plusFunc, a, b);
    }

    private static Functor<RationalFunc> minusFunc = Logic.<RationalFunc, Rational, Rational> functor(Rationals::minus);

    public static RationalFunc minus(Rational a, Rational b) {
        return term(minusFunc, a, b);
    }

    private static Functor<RationalFunc> multiplyFunc = Logic.<RationalFunc, Rational, Rational> functor(Rationals::multiply);

    public static RationalFunc multiply(Rational a, Rational b) {
        return term(multiplyFunc, a, b);
    }

    private static Functor<RationalFunc> divideFunc = Logic.<RationalFunc, Rational, Rational> functor(Rationals::divide);

    public static RationalFunc divide(Rational a, Rational b) {
        return term(divideFunc, a, b);
    }

    private static Functor<RationalFunc> powerFunc = Logic.<RationalFunc, Rational> functor(Rationals::power);

    public static RationalFunc power(Rational a) {
        return term(powerFunc, a);
    }

    private static Functor<RationalFunc> sqrtFunc = Logic.<RationalFunc, Rational> functor(Rationals::sqrt);

    public static RationalFunc sqrt(Rational a) {
        return term(sqrtFunc, a);
    }

    // Rules

    public static void rationalRules() {
        isRules();

        RationalAtom P = rav("PL");
        RationalAtom Q = rav("QL");
        RationalAtom R = rav("RL");

        IntAtom I = iav("IL");

        Rational X = rv("X");
        Rational Y = rv("Y");

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

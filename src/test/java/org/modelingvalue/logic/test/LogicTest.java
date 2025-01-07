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

package org.modelingvalue.logic.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.modelingvalue.logic.Integers.*;
import static org.modelingvalue.logic.Lists.add;
import static org.modelingvalue.logic.Lists.l;
import static org.modelingvalue.logic.Logic.*;

import org.junit.jupiter.api.RepeatedTest;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.SerializableBiFunction;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.logic.Integers.Int;
import org.modelingvalue.logic.Integers.IntAtom;
import org.modelingvalue.logic.Integers.IntFunc;
import org.modelingvalue.logic.Lists.L;
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.*;

public class LogicTest {

    // Utilities

    Database run(Runnable test) {
        return Logic.run(test);
    }

    Database run(Runnable test, Database init) {
        return Logic.run(test, init);
    }

    static void isTrue(Pred goal) {
        assertTrue(Logic.isTrue(goal));
    }

    static void isFalse(Pred goal) {
        assertTrue(Logic.isFalse(goal));
    }

    static void isIncomplete(Pred goal) {
        assertTrue(Logic.isIncomplete(goal));
    }

    @SafeVarargs
    static void hasBindings(Pred goal, Map<Variable, Object>... bindings) {
        assertEquals(Set.of(bindings), getBindings(goal));
    }

    // Root

    interface Root extends Term {
    }

    interface RootAtom extends Root, Atom<Root> {
    }

    interface RootFunc extends Root, Func<Root> {
    }

    static Functor<RootAtom> rootAtom = functor((SerializableFunction<String, RootAtom>) LogicTest::root);

    static RootAtom root(String name) {
        return term(rootAtom, name);
    }

    static RootAtom rootAtomVar(String name) {
        return var(RootAtom.class, name);
    }

    static Root rootVar(String name) {
        return var(Root.class, name);
    }

    static Functor<AtomPred> rootPerson = functor(LogicTest::rootPerson);

    static AtomPred rootPerson(RootAtom root, PersonAtom person) {
        return term(rootPerson, root, person);
    }

    static Functor<RootFunc> rootFunc = functor((SerializableFunction<Person, RootFunc>) LogicTest::root);

    static RootFunc root(Person person) {
        return term(rootFunc, person);
    }

    // Family Tree

    interface Person extends Term {
    }

    interface PersonAtom extends Person, Atom<Person> {
    }

    interface PersonFunc extends Person, Func<Person> {
    }

    static Functor<PersonAtom> strPerson = functor((SerializableFunction<String, PersonAtom>) LogicTest::person);

    static PersonAtom person(String name) {
        return term(strPerson, name);
    }

    static Functor<PersonAtom> intPerson = functor((SerializableFunction<IntAtom, PersonAtom>) LogicTest::person);

    static PersonAtom person(IntAtom i) {
        return term(intPerson, i);
    }

    static PersonAtom person(int i) {
        return person(i(i));
    }

    static PersonAtom personAtomVar(String name) {
        return var(PersonAtom.class, name);
    }

    static Person personVar(String name) {
        return var(Person.class, name);
    }

    static Functor<Rel> parentChild = functor(LogicTest::parentChild);

    static Rel parentChild(PersonAtom parent, PersonAtom child) {
        return term(parentChild, parent, child);
    }

    static Functor<PersonFunc> parent = functor(LogicTest::parent);

    static PersonFunc parent(Person child) {
        return term(parent, child);
    }

    static Functor<PersonFunc> child = functor(LogicTest::child);

    static PersonFunc child(Person parent) {
        return term(child, parent);
    }

    static Functor<AtomPred> ancestorDescendent = functor(LogicTest::ancestorDescendent);

    static AtomPred ancestorDescendent(PersonAtom ancestor, PersonAtom descendent) {
        return term(ancestorDescendent, ancestor, descendent);
    }

    static Functor<PersonFunc> ancestor = functor(LogicTest::ancestor);

    static PersonFunc ancestor(Person descendent) {
        return term(ancestor, descendent);
    }

    static Functor<PersonFunc> descendent = functor(LogicTest::descendent);

    static PersonFunc descendent(Person ancestor) {
        return term(descendent, ancestor);
    }

    // Variables

    IntAtom                  P      = iav("P");
    IntAtom                  Q      = iav("Q");

    Int                      R      = iv("R");
    Int                      S      = iv("S");

    PersonAtom               A      = personAtomVar("A");
    PersonAtom               B      = personAtomVar("B");
    PersonAtom               C      = personAtomVar("C");

    Person                   X      = personVar("X");
    Person                   Y      = personVar("Y");
    Person                   Z      = personVar("Z");

    RootAtom                 U      = rootAtomVar("U");
    Root                     V      = rootVar("V");

    @SuppressWarnings("unchecked")
    L<Person>                PL     = var(L.class, "PL");

    // Terms

    PersonAtom               Carel  = person("Carel");
    PersonAtom               Jan    = person("Jan");
    PersonAtom               Elske  = person("Elske");
    PersonAtom               Wim    = person("Wim");
    PersonAtom               Joppe  = person("Joppe");
    PersonAtom               Heleen = person("Heleen");
    PersonAtom               Marijn = person("Marijn");

    RootAtom                 Root   = root("Root");

    // Fibonacci

    static Functor<AtomPred> fib2   = functor((SerializableBiFunction<IntAtom, IntAtom, AtomPred>) LogicTest::fib);

    static AtomPred fib(IntAtom i, IntAtom f) {
        return term(fib2, i, f);
    }

    static Functor<IntFunc> fib1 = functor((SerializableFunction<Int, IntFunc>) LogicTest::fib);

    static IntFunc fib(Int i) {
        return term(fib1, i);
    }

    private void fibonacciRules() {
        integerRules();

        rule(is(fib(R), Q), and(is(R, P), fib(P, Q)));

        rule(fib(P, Q), and(le(P, i(1)), eq(Q, P)));
        rule(fib(P, Q), and(gt(P, i(1)), is(plus(fib(minus(P, i(1))), fib(minus(P, i(2)))), Q)));
    }

    // Root Rules

    private void rootRules() {
        integerRules();

        rule(is(parent(X), A), and(is(X, B), parentChild(A, B)));
        rule(is(child(X), A), and(is(X, B), parentChild(B, A)));

        rule(is(root(X), U), and(is(X, B), rootPerson(U, B)));

        rule(parentChild(person(Q), person(P)), and(lt(Q, i(4)), is(plus(Q, i(1)), P)));
        rule(rootPerson(U, person(0)), yes());
        rule(rootPerson(U, C), and(rootPerson(U, A), parentChild(A, C)));
    }

    // Family Rules

    private void familyRules() {
        isRules();

        rule(is(parent(X), A), and(is(X, B), parentChild(A, B)));
        rule(is(child(X), A), and(is(X, B), parentChild(B, A)));

        rule(is(ancestor(X), A), and(is(X, B), ancestorDescendent(A, B)));
        rule(is(descendent(X), A), and(is(X, B), ancestorDescendent(B, A)));

        rule(ancestorDescendent(A, C), parentChild(A, C));
        rule(ancestorDescendent(A, C), and(ancestorDescendent(A, B), parentChild(B, C)));
    }

    @RepeatedTest(100)
    public void famTest0() {
        run(() -> {
            familyRules();

            fact(parentChild(Carel, Jan));
            fact(parentChild(Jan, Wim));
            fact(parentChild(Elske, Wim));
            fact(parentChild(Wim, Joppe));
            fact(parentChild(Heleen, Joppe));
            fact(parentChild(Wim, Marijn));
            fact(parentChild(Heleen, Marijn));

            isTrue(is(parent(Joppe), Heleen));
            isTrue(is(Wim, child(Jan)));

            isFalse(is(Marijn, parent(Wim)));
            isFalse(is(parent(Wim), Heleen));
            isFalse(is(child(Wim), Wim));

            isTrue(is(ancestor(Marijn), Wim));
            isTrue(is(descendent(Carel), Marijn));

            isFalse(is(descendent(Marijn), Wim));
            isFalse(is(descendent(Heleen), Wim));
            isFalse(is(descendent(Joppe), Carel));
            isFalse(is(descendent(Carel), Carel));
        });
    }

    @RepeatedTest(100)
    public void famTest1() {
        run(() -> {
            familyRules();

            fact(parentChild(Carel, Jan));
            fact(parentChild(Jan, Wim));
            fact(parentChild(Elske, Wim));
            fact(parentChild(Wim, Joppe));
            fact(parentChild(Heleen, Joppe));
            fact(parentChild(Wim, Marijn));
            fact(parentChild(Heleen, Marijn));

            isTrue(parentChild(Heleen, Joppe));
            isTrue(parentChild(Jan, Wim));

            isFalse(parentChild(Marijn, Wim));
            isFalse(parentChild(Heleen, Wim));
            isFalse(parentChild(Wim, Wim));

            isTrue(ancestorDescendent(Wim, Marijn));
            isTrue(ancestorDescendent(Carel, Marijn));

            isFalse(ancestorDescendent(Marijn, Wim));
            isFalse(ancestorDescendent(Heleen, Wim));
            isFalse(ancestorDescendent(Joppe, Carel));
            isFalse(ancestorDescendent(Carel, Carel));

            hasBindings(collect(parentChild(Wim, C), add(C, l(), PL)), binding(PL, l(Joppe, Marijn)));
        });
    }

    @RepeatedTest(100)
    public void famTest2() {
        run(() -> {
            familyRules();

            fact(parentChild(Carel, Jan));
            fact(parentChild(Jan, Wim));

            hasBindings(ancestorDescendent(A, Wim), binding(A, Jan), binding(A, Carel));
            hasBindings(ancestorDescendent(Carel, C), binding(C, Jan), binding(C, Wim));
        });
    }

    @RepeatedTest(100)
    public void famTest3() {
        run(() -> {
            rule(parentChild(B, C), parentChild(B, C));

            hasBindings(parentChild(Wim, Jan), incomplete(parentChild(Wim, Jan), parentChild(Wim, Jan)));
            isIncomplete(parentChild(Wim, Jan));
        });
    }

    @RepeatedTest(100)
    public void rootTest() {
        run(() -> {
            rootRules();

            isTrue(is(child(person(0)), person(1)));
            isTrue(is(child(person(3)), person(4)));
            isFalse(is(child(person(4)), person(5)));

            isTrue(is(root(person(0)), Root));
            isTrue(is(root(person(1)), Root));
            isTrue(is(root(person(4)), Root));
            isTrue(is(root(person(3)), Root));
            isTrue(is(root(person(2)), Root));

            hasBindings(is(root(C), Root), binding(C, person(0)), binding(C, person(1)), //
                    binding(C, person(2)), binding(C, person(3)), binding(C, person(4)));
        });
    }

    @RepeatedTest(100)
    public void intTest() {
        run(() -> {
            integerRules();

            hasBindings(plus(i(7), i(3), P), binding(P, i(10)));
            hasBindings(plus(i(7), P, i(10)), binding(P, i(3)));
            hasBindings(plus(P, i(3), i(10)), binding(P, i(7)));
        });
    }

    @RepeatedTest(100)
    public void isTest() {
        run(() -> {
            integerRules();

            isTrue(is(plus(i(11), i(22)), i(33)));
            isTrue(is(minus(i(33), i(22)), i(11)));
            isTrue(is(plus(i(11), plus(plus(i(22), i(33)), i(44))), i(110)));

            isTrue(is(plus(i(11), divide(multiply(i(44), i(33)), i(22))), i(77)));

            isTrue(is(sqrt(i(49)), i(7)));
            isTrue(is(sqrt(i(49)), i(-7)));

            hasBindings(is(plus(i(11), plus(plus(i(22), i(33)), i(44))), P), binding(P, i(110)));
            hasBindings(is(plus(i(11), plus(plus(i(22), P), i(44))), i(110)), binding(P, i(33)));
            hasBindings(is(plus(i(7), i(3)), P), binding(P, i(10)));
            hasBindings(is(plus(i(7), P), i(10)), binding(P, i(3)));
            hasBindings(is(plus(P, i(3)), i(10)), binding(P, i(7)));

            hasBindings(is(sqrt(i(49)), P), binding(P, i(7)), binding(P, i(-7)));

            hasBindings(and(is(sqrt(i(49)), P), not(lt(P, i(0)))), binding(P, i(7)));

            hasBindings(collect(is(sqrt(i(49)), P), plus(P, i(0), Q)), binding(Q, i(0)));
        });
    }

    @RepeatedTest(50)
    public void fibonacci() {
        run(() -> {
            fibonacciRules();

            hasBindings(is(fib(i(21)), P), binding(P, i(10946)));
            hasBindings(is(fib(i(1000)), P), binding(P, i("18nrvsuayughau0blk8aylvbyaqwiaqba77rdsgscn5hzwgbgaws8i8svp4xdmoo82plxiyogd5iaj1cspez8zfeio92a76t9n1frssxklr92wyyxm8r903o1ofgncikuggcwnf", Character.MAX_RADIX)));
        });
    }

}

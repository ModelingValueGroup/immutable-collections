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

import static org.modelingvalue.logic.Logic.functImpl;
import static org.modelingvalue.logic.Logic.term;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.SerializableBiFunction;
import org.modelingvalue.collections.util.SerializableSupplier;
import org.modelingvalue.collections.util.SerializableTriFunction;
import org.modelingvalue.logic.Logic.FunctImpl;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Pred;
import org.modelingvalue.logic.Logic.Term;
import org.modelingvalue.logic.Logic.TermImpl;
import org.modelingvalue.logic.Logic.Typed;

public final class Lists {

    private Lists() {
    }

    // Lists

    public interface L<E extends Term> extends Typed<L<E>> {
    }

    @SuppressWarnings("rawtypes")
    private static final FunctImpl<L> LIST_FUNCTOR_0   = functImpl((SerializableSupplier<L>) Lists::l);
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final ListImpl     EMPTY_LIST       = new ListImpl(LIST_FUNCTOR_0);
    @SuppressWarnings("rawtypes")
    private static final L            EMPTY_LIST_PROXY = EMPTY_LIST.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Term> L<E> l() {
        return EMPTY_LIST_PROXY;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final FunctImpl<L> LIST_FUNCTOR_2       = functImpl((SerializableBiFunction<Term, L, L>) Lists::l);
    @SuppressWarnings("rawtypes")
    private static final Functor<L>   LIST_FUNCTOR_2_PROXY = LIST_FUNCTOR_2.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Term> L<E> l(E head, L<E> tail) {
        return new ListImpl(LIST_FUNCTOR_2_PROXY, head, tail).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <E extends Term> ListImpl<E> lImpl(TermImpl<E> head, ListImpl<E> tail) {
        return new ListImpl(LIST_FUNCTOR_2, head, tail);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Term> L<E> l(E... es) {
        return l(List.of(es));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Term> L<E> l(List<E> es) {
        ListImpl<E> l = EMPTY_LIST;
        for (E e : es.reverse()) {
            l = lImpl(Logic.<E, TermImpl<E>> unproxy(e), l);
        }
        return l.proxy();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Term> ListImpl<E> lImpl(List<TermImpl<E>> es) {
        ListImpl<E> l = EMPTY_LIST;
        for (TermImpl<E> e : es.reverse()) {
            l = lImpl(e, l);
        }
        l.list = es;
        return l;
    }

    private static final class ListImpl<E extends Term> extends TermImpl<L<E>> {
        private static final long serialVersionUID = -916406585584150604L;

        private List<TermImpl<E>> list;

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(FunctImpl functor) {
            super(functor);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(Functor functor, E head, L<E> tail) {
            super(functor, head, tail);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(FunctImpl functor, TermImpl<E> head, ListImpl<E> tail) {
            super(functor, head, tail);
        }

        private ListImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final L<E> proxy() {
            return (L<E>) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{L.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected ListImpl<E> term(Object[] array) {
            return new ListImpl(array);
        }

        @SuppressWarnings("unchecked")
        protected TermImpl<E> head() {
            return (TermImpl<E>) get(1);
        }

        @SuppressWarnings("unchecked")
        protected ListImpl<E> tail() {
            return (ListImpl<E>) get(2);
        }

        @Override
        public String toString() {
            return list().toString().substring(4);
        }

        protected List<TermImpl<E>> list() {
            if (list == null) {
                list = length() == 3 ? tail().list().prepend(head()) : List.of();
            }
            return list;
        }

        @Override
        public ListImpl<E> set(int i, Object... a) {
            return (ListImpl<E>) super.set(i, a);
        }
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

    private static <E extends Term> Set<List<TermImpl<E>>> permRemove(List<TermImpl<E>> l, TermImpl<E> e) {
        Set<List<TermImpl<E>>> ls = Set.of();
        for (int i = l.firstIndexOf(e); i >= 0; i = l.firstIndexOf(i, l.size(), e)) {
            ls = ls.add(l.removeIndex(i));
        }
        return ls;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final FunctImpl<Pred> ADD_FUNCTOR       = functImpl((SerializableTriFunction<Term, L, L, Pred>) Lists::add, (LogicLambda) t -> {
                                                               TermImpl<Term> e = t.getTerm(1);
                                                               ListImpl<Term> i = t.getTerm(2);
                                                               ListImpl<Term> o = t.getTerm(3);
                                                               List<TermImpl<Term>> il = i != null ? i.list() : null;
                                                               List<TermImpl<Term>> ol = o != null ? o.list() : null;
                                                               if (e != null && il != null && ol != null) {
                                                                   return addOrdered(il, e).equals(ol) ? Set.of(t) : Set.of();
                                                               } else if (e != null && il != null && ol == null) {
                                                                   return Set.of(t.set(3, lImpl(addOrdered(il, e))));
                                                               } else if (e != null && il == null && ol != null) {
                                                                   return permRemove(ol, e).replaceAll(l -> (TermImpl) t.set(2, lImpl(l)));
                                                               } else if (e == null && il != null && ol != null) {
                                                                   if (il.anyMatch(ol::notContains)) {
                                                                       return Set.of();
                                                                   }
                                                                   return ol.asSet().removeAll(il).replaceAll(r -> (TermImpl) t.set(1, r));
                                                               } else {
                                                                   return t.incomplete();
                                                               }
                                                           });
    @SuppressWarnings("rawtypes")
    private static final Functor<Pred>   ADD_FUNCTOR_PROXY = ADD_FUNCTOR.proxy();

    public static <E extends Term> Pred add(E e, L<E> i, L<E> o) {
        return term(ADD_FUNCTOR_PROXY, e, i, o);
    }

}

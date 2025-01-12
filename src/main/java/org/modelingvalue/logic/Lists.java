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

import static org.modelingvalue.logic.Logic.pred;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.FunctorImpl;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.Logic.StructureImpl;

public final class Lists {

    private Lists() {
    }

    // Lists

    public interface List<E extends Structure> extends Structure {
    }

    @SuppressWarnings("rawtypes")
    private static final FunctorImpl<List> LIST_FUNCTOR_0   = Logic.<List> functImpl(Lists::l);
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final ListImpl          EMPTY_LIST       = new ListImpl(LIST_FUNCTOR_0);
    @SuppressWarnings("rawtypes")
    private static final List              EMPTY_LIST_PROXY = EMPTY_LIST.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> List<E> l() {
        return EMPTY_LIST_PROXY;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final FunctorImpl<List> LIST_FUNCTOR_2       = Logic.<List, Structure, List> functImpl(Lists::l);
    @SuppressWarnings("rawtypes")
    private static final Functor<List>     LIST_FUNCTOR_2_PROXY = LIST_FUNCTOR_2.proxy();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> List<E> l(E head, List<E> tail) {
        return new ListImpl(LIST_FUNCTOR_2_PROXY, head, tail).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <E extends Structure> ListImpl<E> lImpl(StructureImpl<E> head, ListImpl<E> tail) {
        return new ListImpl(LIST_FUNCTOR_2, head, tail);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> List<E> l(E... es) {
        return l(org.modelingvalue.collections.List.of(es));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Structure> List<E> l(org.modelingvalue.collections.List<E> es) {
        ListImpl<E> l = EMPTY_LIST;
        for (E e : es.reverse()) {
            l = lImpl(Logic.<E, StructureImpl<E>> unproxy(e), l);
        }
        return l.proxy();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Structure> ListImpl<E> lImpl(org.modelingvalue.collections.List<StructureImpl<E>> es) {
        ListImpl<E> l = EMPTY_LIST;
        for (StructureImpl<E> e : es.reverse()) {
            l = lImpl(e, l);
        }
        l.list = es;
        return l;
    }

    private static final class ListImpl<E extends Structure> extends StructureImpl<List<E>> {
        private static final long                                    serialVersionUID = -916406585584150604L;

        private org.modelingvalue.collections.List<StructureImpl<E>> list;

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(FunctorImpl functor) {
            super(functor);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(Functor functor, E head, List<E> tail) {
            super(functor, head, tail);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ListImpl(FunctorImpl functor, StructureImpl<E> head, ListImpl<E> tail) {
            super(functor, head, tail);
        }

        private ListImpl(Object[] args) {
            super(args);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected final List<E> proxy() {
            return (List<E>) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{List.class}, this);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected ListImpl<E> struct(Object[] array) {
            return new ListImpl(array);
        }

        @SuppressWarnings("unchecked")
        protected StructureImpl<E> head() {
            return (StructureImpl<E>) get(1);
        }

        @SuppressWarnings("unchecked")
        protected ListImpl<E> tail() {
            return (ListImpl<E>) get(2);
        }

        @Override
        public String toString() {
            return list().toString().substring(4);
        }

        protected org.modelingvalue.collections.List<StructureImpl<E>> list() {
            if (list == null) {
                list = length() == 3 ? tail().list().prepend(head()) : org.modelingvalue.collections.List.of();
            }
            return list;
        }

        @Override
        public ListImpl<E> set(int i, Object... a) {
            return (ListImpl<E>) super.set(i, a);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Class<List<E>> type() {
            return (Class) List.class;
        }
    }

    // Add

    private static <E extends Structure> org.modelingvalue.collections.List<StructureImpl<E>> addOrdered(org.modelingvalue.collections.List<StructureImpl<E>> l, StructureImpl<E> e) {
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).compareTo(e) > 0) {
                return l.insert(i, e);
            }
        }
        return l.append(e);
    }

    private static <E extends Structure> Set<org.modelingvalue.collections.List<StructureImpl<E>>> permRemove(org.modelingvalue.collections.List<StructureImpl<E>> l, StructureImpl<E> e) {
        Set<org.modelingvalue.collections.List<StructureImpl<E>>> ls = Set.of();
        for (int i = l.firstIndexOf(e); i >= 0; i = l.firstIndexOf(i, l.size(), e)) {
            ls = ls.add(l.removeIndex(i));
        }
        return ls;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final FunctorImpl<Predicate> ADD_FUNCTOR       = Logic.<Predicate, Structure, List, List> functImpl(Lists::add, (LogicLambda) t -> {
                                                                      StructureImpl<Structure> e = t.getStruct(1);
                                                                      ListImpl<Structure> i = t.getStruct(2);
                                                                      ListImpl<Structure> o = t.getStruct(3);
                                                                      org.modelingvalue.collections.List<StructureImpl<Structure>> il = i != null ? i.list() : null;
                                                                      org.modelingvalue.collections.List<StructureImpl<Structure>> ol = o != null ? o.list() : null;
                                                                      if (e != null && il != null && ol != null) {
                                                                          return addOrdered(il, e).equals(ol) ? Set.of(t) : Set.of();
                                                                      } else if (e != null && il != null && ol == null) {
                                                                          return Set.of(t.set(3, lImpl(addOrdered(il, e))));
                                                                      } else if (e != null && il == null && ol != null) {
                                                                          return Set.of(t.set(2, permRemove(ol, e).replaceAll(l -> (StructureImpl) t.set(2, lImpl(l)))));
                                                                      } else if (e == null && il != null && ol != null) {
                                                                          if (il.anyMatch(ol::notContains)) {
                                                                              return Set.of();
                                                                          }
                                                                          return Set.of(t.set(1, ol.asSet().removeAll(il).replaceAll(r -> (StructureImpl) t.set(1, r))));
                                                                      } else {
                                                                          return t.incomplete();
                                                                      }
                                                                  });
    @SuppressWarnings("rawtypes")
    private static final Functor<Predicate>     ADD_FUNCTOR_PROXY = ADD_FUNCTOR.proxy();

    public static <E extends Structure> Predicate add(E e, List<E> i, List<E> o) {
        return pred(ADD_FUNCTOR_PROXY, e, i, o);
    }

}

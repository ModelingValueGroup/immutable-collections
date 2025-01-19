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

import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Constant;
import org.modelingvalue.logic.Logic.Function;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.LogicLambda;
import org.modelingvalue.logic.Logic.Predicate;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.impl.Conclusion;
import org.modelingvalue.logic.impl.FunctorImpl;
import org.modelingvalue.logic.impl.ListImpl;
import org.modelingvalue.logic.impl.StructureImpl;

public final class Lists {

    private Lists() {
    }

    public interface List<E extends Structure> extends Structure {
    }

    public interface ListCons<E extends Structure> extends List<E>, Constant<List<E>> {
    }

    public interface ListFunc<E extends Structure> extends List<E>, Function<List<E>> {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> ListCons<E> l() {
        return (ListCons) ListImpl.EMPTY_LIST_PROXY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> ListCons<E> l(E head, ListCons<E> tail) {
        return new ListImpl<E>(head, tail).proxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Structure> ListCons<E> l(E... es) {
        return l(org.modelingvalue.collections.List.of(es));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Structure> ListCons<E> l(org.modelingvalue.collections.List<E> es) {
        ListImpl<E> l = (ListImpl) ListImpl.EMPTY_LIST;
        for (E e : es.reverse()) {
            l = ListImpl.of(StructureImpl.<E, StructureImpl<E>> unproxy(e), l);
        }
        return l.proxy();
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
    private static final FunctorImpl<Predicate> ADD_FUNCTOR       = FunctorImpl.<Predicate, Structure, ListCons, ListCons> of(Lists::add, (LogicLambda) predicate -> {
                                                                      StructureImpl<Structure> element = predicate.getStruct(1);
                                                                      ListImpl<Structure> sub = predicate.getStruct(2);
                                                                      ListImpl<Structure> sup = predicate.getStruct(3);
                                                                      org.modelingvalue.collections.List<StructureImpl<Structure>> sublist = sub != null ? sub.list() : null;
                                                                      org.modelingvalue.collections.List<StructureImpl<Structure>> superlist = sup != null ? sup.list() : null;
                                                                      if (element != null && sublist != null && superlist != null) {
                                                                          return Conclusion.EMPTY.positive(addOrdered(sublist, element).equals(superlist) ? Set.of(predicate) : Set.of());
                                                                      } else if (element != null && sublist != null && superlist == null) {
                                                                          return Conclusion.EMPTY.positive(Set.of(predicate.set(3, ListImpl.of(addOrdered(sublist, element)))));
                                                                      } else if (element != null && sublist == null && superlist != null) {
                                                                          return Conclusion.EMPTY.positive(Set.of(predicate.set(2, permRemove(superlist, element).replaceAll(l -> (StructureImpl) predicate.set(2, ListImpl.of(l))))));
                                                                      } else if (element == null && sublist != null && superlist != null) {
                                                                          if (sublist.anyMatch(superlist::notContains)) {
                                                                              return Conclusion.EMPTY;
                                                                          }
                                                                          return Conclusion.EMPTY.positive(Set.of(predicate.set(1, superlist.asSet().removeAll(sublist).replaceAll(r -> (StructureImpl) predicate.set(1, r)))));
                                                                      } else {
                                                                          return predicate.incomplete();
                                                                      }
                                                                  });
    @SuppressWarnings("rawtypes")
    private static final Functor<Predicate>     ADD_FUNCTOR_PROXY = ADD_FUNCTOR.proxy();

    public static <E extends Structure> Predicate add(E e, ListCons<E> i, ListCons<E> o) {
        return pred(ADD_FUNCTOR_PROXY, e, i, o);
    }

}

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

package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic.Predicate;

public final class OrImpl extends PredicateImpl {
    private static final long                   serialVersionUID = -1732549494864415986L;

    private static final FunctorImpl<Predicate> OR_FUNCTOR       = FunctorImpl.<Predicate, Predicate, Predicate> of(OrImpl::or);

    private List<int[]>                         idxList;

    private static Predicate or(Predicate p1, Predicate p2) {
        return new OrImpl(StructureImpl.unproxy(p1), StructureImpl.unproxy(p2)).proxy();
    }

    public OrImpl(PredicateImpl pred1, PredicateImpl pred2) {
        super(OR_FUNCTOR, pred1, pred2);
    }

    private OrImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate proxy() {
        return (Predicate) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{Predicate.class}, this);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected OrImpl struct(Object[] array) {
        return new OrImpl(array);
    }

    @SuppressWarnings("rawtypes")
    private List<int[]> idxList() {
        if (idxList == null) {
            List<int[]> l = List.of();
            PredicateImpl p1 = pred1();
            if (p1 instanceof OrImpl) {
                l = l.prependList(((OrImpl) p1).idxList().replaceAll(ADD_ONE));
            } else {
                l = l.append(ONE_ARRAY);
            }
            PredicateImpl p2 = pred2();
            if (p2 instanceof OrImpl) {
                l = l.appendList(((OrImpl) p2).idxList().replaceAll(ADD_TWO));
            } else {
                l = l.append(TWO_ARRAY);
            }
            idxList = l;
        }
        return idxList;
    }

    @SuppressWarnings("rawtypes")
    protected final PredicateImpl pred1() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    protected final PredicateImpl pred2() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl goal, List<PredicateImpl> der, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Set<PredicateImpl> r = Set.of();
        for (int[] i : ((OrImpl) goal).idxList()) {
            PredicateImpl g = goal.getPred(i);
            Set<PredicateImpl> m = getPred(i).match(g, der, rec, database);
            if (m.anyMatch(PredicateImpl::isToDepthIcomplete)) {
                return m;
            } else {
                r = r.addAll(m.replaceAll(t -> t.isIncomplete() ? t : goal.setBinding(this, g.getBinding(t, Map.of()))));
            }
        }
        return r;
    }

    @Override
    public OrImpl set(int i, Object... a) {
        return (OrImpl) super.set(i, a);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean contains(PredicateImpl cond) {
        return super.contains(cond) || pred1().contains(cond) || pred2().contains(cond);
    }
}

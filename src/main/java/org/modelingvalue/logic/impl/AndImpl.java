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

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Database;
import org.modelingvalue.logic.Logic.Predicate;

public final class AndImpl extends PredicateImpl {
    private static final long                   serialVersionUID = -7248491569810098948L;

    private static final FunctorImpl<Predicate> AND_FUNCTOR      = FunctorImpl.<Predicate, Predicate, Predicate> of(AndImpl::and);

    private static Predicate and(Predicate p1, Predicate p2) {
        return new AndImpl(StructureImpl.unproxy(p1), StructureImpl.unproxy(p2)).proxy();
    }

    private List<int[]> idxList;

    public AndImpl(PredicateImpl pred1, PredicateImpl pred2) {
        super(AND_FUNCTOR, pred1, pred2);
    }

    private AndImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AndImpl struct(Object[] array) {
        return new AndImpl(array);
    }

    @SuppressWarnings("rawtypes")
    private List<int[]> idxList() {
        if (idxList == null) {
            List<int[]> l = List.of();
            PredicateImpl p1 = pred1();
            if (p1 instanceof AndImpl) {
                l = l.prependList(((AndImpl) p1).idxList().replaceAll(ADD_ONE));
            } else {
                l = l.append(ONE_ARRAY);
            }
            PredicateImpl p2 = pred2();
            if (p2 instanceof AndImpl) {
                l = l.appendList(((AndImpl) p2).idxList().replaceAll(ADD_TWO));
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
        Set<PredicateImpl> out = Set.of();
        Set<AndImpl> ands = Set.of(this);
        idxList = ((AndImpl) goal).idxList();
        do {
            Set<AndImpl> ands2 = ands;
            ands = Set.of();
            outer:
            for (AndImpl and : ands2) {
                List<int[]> idxl = and.idxList;
                if (idxl.isEmpty()) {
                    out = out.add(and);
                } else {
                    Set<PredicateImpl> ic = Set.of();
                    for (int ii = 0; ii < idxl.size(); ii++) {
                        int[] i = idxl.get(ii);
                        PredicateImpl g = goal.getPred(i);
                        Set<PredicateImpl> ts = and.getPred(i).match(g, der, rec, database);
                        Set<PredicateImpl> in = ts.retainAll(PredicateImpl::isIncomplete);
                        if (in.isEmpty()) {
                            List<int[]> iil = idxl.removeIndex(ii);
                            ands = ands.addAll(ts.replaceAll(m -> {
                                AndImpl a = (AndImpl) goal.setBinding(and, g.getBinding(m, Map.of()));
                                a.idxList = iil;
                                return a;
                            }));
                            continue outer;
                        } else if (in.anyMatch(PredicateImpl::isToDepthIcomplete)) {
                            return in;
                        } else {
                            ic = ic.addAll(in);
                        }
                    }
                    out = out.addAll(ic);
                }

            }
        } while (!ands.isEmpty());
        return out;
    }

    @Override
    public AndImpl set(int i, Object... a) {
        return (AndImpl) super.set(i, a);
    }
}

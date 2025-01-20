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
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Predicate;

public final class AndImpl extends PredicateImpl {
    private static final long                   serialVersionUID = -7248491569810098948L;

    private static final FunctorImpl<Predicate> AND_FUNCTOR      = FunctorImpl.<Predicate, Predicate, Predicate> of(AndImpl::and);

    private static Predicate and(Predicate p1, Predicate p2) {
        return new AndImpl(StructureImpl.unproxy(p1), StructureImpl.unproxy(p2)).proxy();
    }

    private List<int[]> idxList;

    public AndImpl(PredicateImpl predicate1, PredicateImpl predicate2) {
        super(AND_FUNCTOR, predicate1, predicate2);
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
            PredicateImpl predicate1 = predicate1();
            if (predicate1 instanceof AndImpl) {
                l = l.prependList(((AndImpl) predicate1).idxList().replaceAll(ADD_ONE));
            } else {
                l = l.append(ONE_ARRAY);
            }
            PredicateImpl predicate2 = predicate2();
            if (predicate2 instanceof AndImpl) {
                l = l.appendList(((AndImpl) predicate2).idxList().replaceAll(ADD_TWO));
            } else {
                l = l.append(TWO_ARRAY);
            }
            idxList = l;
        }
        return idxList;
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl predicate1() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl predicate2() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Conclusion infer(PredicateImpl declaration, InferContext context) {
        idxList = ((AndImpl) declaration).idxList();
        Set<PredicateImpl> facts = Set.of();
        Conclusion result = Conclusion.EMPTY, tmpResult;
        Set<AndImpl> ands1 = Set.of(this), ands2;
        do {
            ands2 = ands1;
            ands1 = Set.of();
            outer:
            for (AndImpl and : ands2) {
                List<int[]> idxl = and.idxList;
                if (idxl.isEmpty()) {
                    facts = facts.add(and);
                } else {
                    tmpResult = Conclusion.EMPTY;
                    for (int ii = 0; ii < idxl.size(); ii++) {
                        int[] i = idxl.get(ii);
                        PredicateImpl declPred = declaration.getVal(i);
                        PredicateImpl pred = and.getVal(i);
                        Conclusion conclusion = pred.infer(declPred, context);
                        if (conclusion.hasStackOverflow()) {
                            return conclusion;
                        }
                        conclusion = conclusion.bind(declPred, this, declaration);
                        if (conclusion.incomplete().isEmpty()) {
                            List<int[]> iil = idxl.removeIndex(ii);
                            conclusion.facts().forEach(f -> ((AndImpl) f).idxList = iil);
                            ands1 = ands1.addAll((Set) conclusion.facts());
                            result = result.add(Conclusion.of(Set.of(), conclusion.falsehoods()));
                            continue outer;
                        } else {
                            tmpResult = tmpResult.add(conclusion);
                        }
                    }
                    result = result.add(tmpResult);
                }
            }
        } while (!ands1.isEmpty());
        return Conclusion.of(facts, result.falsehoods(), result.incomplete(), result.falseIncomplete());

    }

    @Override
    public AndImpl set(int i, Object... a) {
        return (AndImpl) super.set(i, a);
    }
}

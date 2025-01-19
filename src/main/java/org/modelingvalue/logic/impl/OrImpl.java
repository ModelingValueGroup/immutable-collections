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
import org.modelingvalue.logic.Logic.Predicate;

public final class OrImpl extends PredicateImpl {
    private static final long                   serialVersionUID = -1732549494864415986L;

    private static final FunctorImpl<Predicate> OR_FUNCTOR       = FunctorImpl.<Predicate, Predicate, Predicate> of(OrImpl::or);

    private List<int[]>                         idxList;

    private static Predicate or(Predicate predicate1, Predicate predicate2) {
        return new OrImpl(StructureImpl.unproxy(predicate1), StructureImpl.unproxy(predicate2)).proxy();
    }

    public OrImpl(PredicateImpl predicate1, PredicateImpl predicate2) {
        super(OR_FUNCTOR, predicate1, predicate2);
    }

    private OrImpl(Object[] args) {
        super(args);
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
            PredicateImpl predicate1 = predicate1();
            if (predicate1 instanceof OrImpl) {
                l = l.prependList(((OrImpl) predicate1).idxList().replaceAll(ADD_ONE));
            } else {
                l = l.append(ONE_ARRAY);
            }
            PredicateImpl predicate2 = predicate2();
            if (predicate2 instanceof OrImpl) {
                l = l.appendList(((OrImpl) predicate2).idxList().replaceAll(ADD_TWO));
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
        Set<PredicateImpl> positive = Set.of();
        Set<List<PredicateImpl>> incomplete = Set.of();
        for (int[] i : ((OrImpl) declaration).idxList()) {
            PredicateImpl declPred = declaration.getVal(i);
            PredicateImpl pred = getVal(i);
            Conclusion conclusion = pred.infer(declPred, context);
            if (conclusion.hasStackOverflow()) {
                return conclusion;
            } else {
                positive = positive.addAll(conclusion.positive().replaceAll(p -> declaration.setBinding(this, declPred.getBinding(p, Map.of()))));
                incomplete = incomplete.addAll(conclusion.incomplete());
            }
        }
        return Conclusion.of(positive, incomplete);
    }

    @Override
    public OrImpl set(int i, Object... a) {
        return (OrImpl) super.set(i, a);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean contains(PredicateImpl cond) {
        return super.contains(cond) || predicate1().contains(cond) || predicate2().contains(cond);
    }
}

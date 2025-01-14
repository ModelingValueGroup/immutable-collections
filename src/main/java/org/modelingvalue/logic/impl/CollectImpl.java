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
import org.modelingvalue.logic.Logic;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.Predicate;

public final class CollectImpl extends PredicateImpl {
    private static final long                   serialVersionUID      = -2799691054715131197L;

    private static final FunctorImpl<Predicate> COLLECT_FUNCTOR       = FunctorImpl.<Predicate, Predicate, Predicate> of(Logic::collect);
    private static final Functor<Predicate>     COLLECT_FUNCTOR_PROXY = COLLECT_FUNCTOR.proxy();

    private int                                 resultIndex           = -1;

    public CollectImpl(Predicate pred, Predicate accum) {
        super(COLLECT_FUNCTOR_PROXY, pred, accum);
    }

    private CollectImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected CollectImpl struct(Object[] array) {
        return new CollectImpl(array);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl pred() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl accum() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings("rawtypes")
    private Map<VariableImpl, Object> localVariables;

    @SuppressWarnings("rawtypes")
    protected Map<VariableImpl, Object> localVariables() {
        if (localVariables == null) {
            Map<VariableImpl, Object> predVars = pred().variables();
            Map<VariableImpl, Object> accumVars = accum().variables();
            localVariables = predVars.retainAll(accumVars::contains);
        }
        return localVariables;
    }

    @SuppressWarnings("rawtypes")
    private Map<VariableImpl, Object> variables;

    @SuppressWarnings("rawtypes")
    @Override
    public Map<VariableImpl, Object> variables() {
        if (variables == null) {
            Map<VariableImpl, Object> predVars = pred().variables();
            Map<VariableImpl, Object> accumVars = accum().variables();
            variables = predVars.removeAll(accumVars::contains).addAll(accumVars.removeAll(predVars::contains));
        }
        return variables;
    }

    private int identityIndex = -1;

    @SuppressWarnings("rawtypes")
    private int identityIndex() {
        if (identityIndex < 0) {
            PredicateImpl accum = accum();
            for (int i = 1; i < accum.length(); i++) {
                Object v = accum.get(i);
                if (!(v instanceof VariableImpl) && v instanceof StructureImpl) {
                    Class<?> rt = ((VariableImpl) accum.get(resultIndex())).type();
                    Class<?> at = ((StructureImpl) v).type();
                    if (rt.isAssignableFrom(at)) {
                        identityIndex = i;
                        break;
                    }
                }
            }
        }
        return identityIndex;
    }

    @SuppressWarnings("rawtypes")
    private int resultIndex() {
        if (resultIndex < 0) {
            PredicateImpl accum = accum();
            for (int i = 1; i < accum.length(); i++) {
                Object v = accum.get(i);
                if (v instanceof VariableImpl && !localVariables().containsKey((VariableImpl) v)) {
                    resultIndex = i;
                    break;
                }
            }
        }
        return resultIndex;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Set<PredicateImpl> match(PredicateImpl decl, List<PredicateImpl> stack, Map<PredicateImpl, Set<PredicateImpl>> rec, Database database) {
        Map<VariableImpl, Object> localVars = ((CollectImpl) decl).localVariables();
        int ii = ((CollectImpl) decl).identityIndex();
        int ri = ((CollectImpl) decl).resultIndex();
        PredicateImpl goalPred = ((CollectImpl) decl).pred();
        PredicateImpl goalAccum = ((CollectImpl) decl).accum();
        PredicateImpl accum = accum();
        StructureImpl id = accum.getStruct(ii);
        Set<StructureImpl> rs = Set.of(id);
        Set<PredicateImpl> inc = Set.of();
        for (PredicateImpl pm : goalPred.setBinding(pred(), localVars).match(goalPred, stack, rec, database)) {
            if (pm.isIncomplete()) {
                inc = inc.add(pm);
            } else {
                Map<VariableImpl, Object> b = goalPred.getBinding(pm, Map.of());
                Set<StructureImpl> irs = Set.of();
                for (StructureImpl r : rs) {
                    PredicateImpl s = goalAccum.setBinding(accum, b).set(ii, r);
                    for (PredicateImpl am : s.match(goalAccum, stack, rec, database)) {
                        if (am.isIncomplete()) {
                            inc = inc.add(am);
                        } else {
                            irs = irs.add(am.getStruct(ri));
                        }
                    }
                }
                rs = irs;
            }
        }
        for (StructureImpl t : rs) {
            inc = inc.add(set(2, accum.set(ri, t)));
        }
        return inc;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<VariableImpl, Object> getBinding(StructureImpl<Predicate> pred, Map<VariableImpl, Object> vars) {
        Map<VariableImpl, Object> localVars = localVariables();
        return super.getBinding(pred, vars).removeAll(e -> localVars.containsKey(e.getKey()));
    }

    @Override
    public CollectImpl set(int i, Object... a) {
        return (CollectImpl) super.set(i, a);
    }
}

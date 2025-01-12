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

import java.util.concurrent.atomic.AtomicReference;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.struct.impl.Struct2Impl;
import org.modelingvalue.logic.Logic.Rule;
import org.modelingvalue.logic.Logic.Structure;
import org.modelingvalue.logic.impl.PredicateImpl;
import org.modelingvalue.logic.impl.RuleImpl;
import org.modelingvalue.logic.impl.StructureImpl;

@SuppressWarnings("rawtypes")
public final class Database {

    @SuppressWarnings("rawtypes")
    public static final QualifiedSet<PredicateImpl, Memoiz> EMPTY_MEMOIZ = QualifiedSet.of(Memoiz::pred);

    @SuppressWarnings("rawtypes")
    public static class Memoiz extends Struct2Impl<PredicateImpl, Set<PredicateImpl>> {
        private static final long serialVersionUID = 1531759272582548244L;

        public int                count            = Logic.INITIAL_USAGE_COUNT;

        public Memoiz(PredicateImpl t, Set<PredicateImpl> s) {
            super(t, s);
        }

        private PredicateImpl pred() {
            return get0();
        }

        public Set<PredicateImpl> set() {
            return get1();
        }

        protected boolean keep() {
            return count-- > 0;
        }
    }

    public final AtomicReference<Map<PredicateImpl, Set<PredicateImpl>>> facts;
    public final AtomicReference<Map<PredicateImpl, List<RuleImpl>>>     rules;
    public final AtomicReference<QualifiedSet<PredicateImpl, Memoiz>[]>  memoiz;

    boolean                                                              stopped;

    @SuppressWarnings("unchecked")
    Database(Database init) {
        facts = new AtomicReference<>(init != null ? init.facts.get() : Map.of());
        rules = new AtomicReference<>(init != null ? init.rules.get() : Map.of());
        memoiz = new AtomicReference<>(init != null ? init.memoiz.get() : new QualifiedSet[]{EMPTY_MEMOIZ, EMPTY_MEMOIZ, EMPTY_MEMOIZ});
    }

    public Map<Structure, List<Rule>> rules() {
        return rules.get().replaceAll(e -> {
            Structure k = e.getKey().proxy();
            List<Rule> v = e.getValue().replaceAll(RuleImpl::proxy);
            return Entry.of(k, v);
        });
    }

    public Map<Structure, Set<Structure>> facts() {
        return facts.get().replaceAll(e -> {
            Structure k = e.getKey().proxy();
            Set<Structure> v = e.getValue().replaceAll(StructureImpl::proxy);
            return Entry.of(k, v);
        });
    }

    public void cleanup() {
        QualifiedSet<PredicateImpl, Memoiz>[] mem = memoiz.get();
        while (mem[2].size() > Logic.MAX_LOGIC_MEMOIZ) {
            for (int i = 0; i < mem[2].size(); i++) {
                if (stopped) {
                    return;
                }
                Memoiz m = mem[2].get(i);
                if (!m.keep()) {
                    mem = memoiz.updateAndGet(a -> {
                        a = a.clone();
                        a[2] = a[2].removeKey(m.pred());
                        return a;
                    });
                    i--;
                }
            }
        }
    }
}

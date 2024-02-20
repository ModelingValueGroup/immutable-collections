//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

package org.modelingvalue.collections.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class MutationWrapper<C> {
    private final AtomicReference<C> ref;

    public MutationWrapper() {
        this(null);
    }

    public MutationWrapper(C wrapped) {
        ref = new AtomicReference<>(wrapped);
    }

    public C get() {
        return ref.get();
    }

    public void clear() {
        ref.set(null);
    }

    public void update(UnaryOperator<C> f) {
        ref.updateAndGet(f);
    }

    public void set(C val) {
        ref.set(val);
    }

    public <E> void update(BiFunction<C, E, C> f, E e) {
        ref.updateAndGet(prev -> f.apply(prev, e));
    }

    public C updateAndGet(UnaryOperator<C> f) {
        return ref.updateAndGet(f);
    }

    public <E> C updateAndGet(BiFunction<C, E, C> f, E e) {
        return ref.updateAndGet(prev -> f.apply(prev, e));
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2023 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.util;

import java.util.function.BiFunction;

@FunctionalInterface
public interface SerializableBiFunction<A, B, C> extends BiFunction<A, B, C>, LambdaReflection {

    @Override
    default SerializableBiFunctionImpl<A, B, C> of() {
        return this instanceof SerializableBiFunctionImpl ? (SerializableBiFunctionImpl<A, B, C>) this : new SerializableBiFunctionImpl<>(this);
    }

    class SerializableBiFunctionImpl<A, B, C> extends LambdaImpl<SerializableBiFunction<A, B, C>> implements SerializableBiFunction<A, B, C> {

        private static final long serialVersionUID = 3679004512278552829L;

        public SerializableBiFunctionImpl(SerializableBiFunction<A, B, C> f) {
            super(f);
        }

        @Override
        public C apply(A t, B u) {
            return f.apply(t, u);
        }

    }

}

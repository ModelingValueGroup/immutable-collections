//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2021 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import java.util.function.Function;

@FunctionalInterface
public interface SerializableFunction<U, V> extends Function<U, V>, LambdaReflection {

    @Override
    default SerializableFunctionImpl<U, V> of() {
        return this instanceof SerializableFunctionImpl ? (SerializableFunctionImpl<U, V>) this : new SerializableFunctionImpl<>(this);
    }

    class SerializableFunctionImpl<U, V> extends LambdaImpl<SerializableFunction<U, V>> implements SerializableFunction<U, V> {

        private static final long serialVersionUID = 5814783501752526565L;

        public SerializableFunctionImpl(SerializableFunction<U, V> f) {
            super(f);
        }

        @Override
        public final V apply(U t) {
            return f.apply(t);
        }

    }

}

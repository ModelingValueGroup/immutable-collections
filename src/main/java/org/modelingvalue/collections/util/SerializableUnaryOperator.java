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

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface SerializableUnaryOperator<T> extends UnaryOperator<T>, LambdaReflection {

    @Override
    default SerializableUnaryOperatorImpl<T> of() {
        return this instanceof SerializableUnaryOperatorImpl ? (SerializableUnaryOperatorImpl<T>) this : new SerializableUnaryOperatorImpl<>(this);
    }

    class SerializableUnaryOperatorImpl<T> extends LambdaImpl<SerializableUnaryOperator<T>> implements SerializableUnaryOperator<T> {

        private static final long serialVersionUID = -7330574302748660603L;

        public SerializableUnaryOperatorImpl(SerializableUnaryOperator<T> f) {
            super(f);
        }

        @Override
        public T apply(T t) {
            return f.apply(t);
        }

    }

}

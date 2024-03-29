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

import java.util.function.BinaryOperator;

@FunctionalInterface
public interface SerializableBinaryOperator<T> extends BinaryOperator<T>, LambdaReflection {

    @Override
    default SerializableBinaryOperatorImpl<T> of() {
        return this instanceof SerializableBinaryOperatorImpl ? (SerializableBinaryOperatorImpl<T>) this : new SerializableBinaryOperatorImpl<>(this);
    }

    class SerializableBinaryOperatorImpl<T> extends LambdaImpl<SerializableBinaryOperator<T>> implements SerializableBinaryOperator<T> {

        private static final long serialVersionUID = 7465116047167434408L;

        public SerializableBinaryOperatorImpl(SerializableBinaryOperator<T> f) {
            super(f);
        }

        @Override
        public T apply(T t1, T t2) {
            return f.apply(t1, t2);
        }

    }

}

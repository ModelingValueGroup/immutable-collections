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

import java.util.function.BiConsumer;

@FunctionalInterface
public interface SerializableBiConsumer<A, B> extends BiConsumer<A, B>, LambdaReflection {

    @Override
    default SerializableBiConsumerImpl<A, B> of() {
        return this instanceof SerializableBiConsumerImpl ? (SerializableBiConsumerImpl<A, B>) this : new SerializableBiConsumerImpl<>(this);
    }

    class SerializableBiConsumerImpl<A, B> extends LambdaImpl<SerializableBiConsumer<A, B>> implements SerializableBiConsumer<A, B> {

        private static final long serialVersionUID = 4203724319598910043L;

        public SerializableBiConsumerImpl(SerializableBiConsumer<A, B> f) {
            super(f);
        }

        @Override
        public void accept(A t, B u) {
            f.accept(t, u);
        }

    }

}

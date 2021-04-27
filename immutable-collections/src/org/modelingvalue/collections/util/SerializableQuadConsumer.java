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

@FunctionalInterface
public interface SerializableQuadConsumer<A, B, C, D> extends QuadConsumer<A, B, C, D>, LambdaReflection {

    @Override
    default SerializableQuadConsumerImpl<A, B, C, D> of() {
        return this instanceof SerializableQuadConsumerImpl ? (SerializableQuadConsumerImpl<A, B, C, D>) this : new SerializableQuadConsumerImpl<>(this);
    }

    class SerializableQuadConsumerImpl<A, B, C, D> extends LambdaImpl<SerializableQuadConsumer<A, B, C, D>> implements SerializableQuadConsumer<A, B, C, D> {

        private static final long serialVersionUID = -5270335229106305734L;

        public SerializableQuadConsumerImpl(SerializableQuadConsumer<A, B, C, D> f) {
            super(f);
        }

        @Override
        public void accept(A s, B t, C u, D v) {
            f.accept(s, t, u, v);
        }

    }

}

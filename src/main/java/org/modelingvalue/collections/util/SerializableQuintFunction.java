//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2026 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

@FunctionalInterface
public interface SerializableQuintFunction<A, B, C, D, E, F> extends QuintFunction<A, B, C, D, E, F>, LambdaReflection {

    @Override
    default SerializableQuintFunctionImpl<A, B, C, D, E, F> of() {
        return this instanceof SerializableQuintFunctionImpl ? (SerializableQuintFunctionImpl<A, B, C, D, E, F>) this : new SerializableQuintFunctionImpl<>(this);
    }

    class SerializableQuintFunctionImpl<A, B, C, D, E, F> extends LambdaImpl<SerializableQuintFunction<A, B, C, D, E, F>> implements SerializableQuintFunction<A, B, C, D, E, F> {
        private static final long serialVersionUID = 7903500913155308463L;

        public SerializableQuintFunctionImpl(SerializableQuintFunction<A, B, C, D, E, F> f) {
            super(f);
        }

        @Override
        public F apply(A s, B t, C u, D v, E e) {
            return f.apply(s, t, u, v, e);
        }

    }

}

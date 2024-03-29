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

import java.util.function.Predicate;

@FunctionalInterface
public interface SerializablePredicate<U> extends Predicate<U>, LambdaReflection {

    @Override
    default SerializablePredicateImpl<U> of() {
        return this instanceof SerializablePredicateImpl ? (SerializablePredicateImpl<U>) this : new SerializablePredicateImpl<>(this);
    }

    class SerializablePredicateImpl<U> extends LambdaImpl<SerializablePredicate<U>> implements SerializablePredicate<U> {

        private static final long serialVersionUID = -8866812297241618996L;

        public SerializablePredicateImpl(SerializablePredicate<U> f) {
            super(f);
        }

        @Override
        public final boolean test(U t) {
            return f.test(t);
        }

    }

}

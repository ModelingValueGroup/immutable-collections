//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2019 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Carel Bast, Tom Brus                                                                                  ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Ronald Krijgsheld                                                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.util;

@FunctionalInterface
public interface SerializableRunnable extends Runnable, LambdaReflection {

    @Override
    default SerializableRunnableImpl of() {
        return this instanceof SerializableRunnableImpl ? (SerializableRunnableImpl) this : new SerializableRunnableImpl(this);
    }

    static class SerializableRunnableImpl extends LambdaImpl<SerializableRunnable> implements SerializableRunnable {

        private static final long serialVersionUID = -930587277518158938L;

        public SerializableRunnableImpl(SerializableRunnable f) {
            super(f);
        }

        @Override
        public void run() {
            f.run();
        }

    }

}
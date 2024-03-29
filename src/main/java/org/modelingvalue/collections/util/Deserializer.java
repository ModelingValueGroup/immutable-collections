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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.function.Consumer;

public interface Deserializer {
    <X> X readObject();

    int readInt();

    @SuppressWarnings("unchecked")
    default <T> T[] readArray() {
        //noinspection unchecked
        return readArray((T[]) new Object[]{});
    }

    default <T> T[] readArray(T[] arg) {
        int n = readInt();
        if (arg.length == 0) {
            @SuppressWarnings("unchecked")
            T[] a = (T[]) Arrays.copyOf(arg, n, arg.getClass());
            for (int i = 0; i < n; i++) {
                a[i] = readObject();
            }
            return a;
        } else if (arg.length == n) {
            for (int i = 0; i < n; i++) {
                arg[i] = readObject();
            }
            return arg;
        } else {
            throw new NotDeserializableError("was expecting array of size " + arg.length + " but found array of size " + n);
        }
    }

    /**
     * Just a wrapper for ObjectInputStream to act as a Deserializer
     */
    class WrapObjectInputStream implements Deserializer {
        private final ObjectInputStream s;

        public WrapObjectInputStream(ObjectInputStream s) {
            this.s = s;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <X> X readObject() {
            try {
                //noinspection unchecked
                return (X) s.readObject();
            } catch (IOException e) {
                throw new WrappedIOException(e);
            } catch (ClassNotFoundException e) {
                throw new WrappedClassNotFoundException(e);
            }
        }

        @Override
        public int readInt() {
            try {
                return s.readInt();
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }
    }

    static void wrap(ObjectInputStream s, Consumer<Deserializer> readObject) throws IOException, ClassNotFoundException {
        try {
            readObject.accept(new WrapObjectInputStream(s));
        } catch (WrappedIOException e) {
            e.throwOriginal();
        } catch (WrappedClassNotFoundException e) {
            e.throwOriginal();
        }
    }
}

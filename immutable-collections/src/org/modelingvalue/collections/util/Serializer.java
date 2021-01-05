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

import java.io.*;
import java.util.function.*;

public interface Serializer {
    void writeObject(Object o);

    void writeInt(int i);

    default void writeArray(Object[] a) {
        writeInt(a.length);
        for (Object o : a) {
            writeObject(o);
        }
    }

    /**
     * Just a wrapper for ObjectOutputStream to act as a Serializer
     */
    class WrapObjectOutputStream implements Serializer {
        private final ObjectOutputStream s;

        public WrapObjectOutputStream(ObjectOutputStream s) {
            this.s = s;
        }

        @Override
        public void writeObject(Object o) {
            try {
                s.writeObject(o);
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }

        @Override
        public void writeInt(int i) {
            try {
                s.writeInt(i);
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }
    }

    static void wrap(ObjectOutputStream s, Consumer<Serializer> writeObject) throws IOException {
        try {
            writeObject.accept(new WrapObjectOutputStream(s));
        } catch (WrappedIOException e) {
            e.throwOriginal();
        }
    }
}

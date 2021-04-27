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

import java.util.ArrayList;

public class Reusable<U, D, C, T, P> {

    private static final int                           CHUNCK_SIZE = 4;

    private final ArrayList<T>                         list        = new ArrayList<>(0);
    private final U                                    init;
    private final SerializableBiFunction<C, U, T>      construct;
    private final SerializableQuadConsumer<T, D, C, P> start;
    private final SerializableConsumer<T>              stop;
    private final SerializableFunction<T, Boolean>     isOpen;

    private int                                        level       = -1;

    public Reusable(U init, SerializableBiFunction<C, U, T> construct, SerializableQuadConsumer<T, D, C, P> start, SerializableConsumer<T> stop, SerializableFunction<T, Boolean> isOpen) {
        this.init = init;
        this.construct = construct;
        this.start = start;
        this.stop = stop;
        this.isOpen = isOpen;
    }

    public T open(D dir, C cls, P parent) {
        if (ContextThread.getNr() < 0) {
            synchronized (list) {
                return doOpen(dir, cls, parent);
            }
        } else {
            return doOpen(dir, cls, parent);
        }
    }

    public void close(T tx) {
        if (ContextThread.getNr() < 0) {
            synchronized (list) {
                doClose(tx);
            }
        } else {
            doClose(tx);
        }
    }

    private T doOpen(D dir, C cls, P parent) {
        if (++level >= list.size()) {
            list.ensureCapacity(list.size() + CHUNCK_SIZE);
            for (int i = 0; i < CHUNCK_SIZE; i++) {
                list.add(construct.apply(cls, init));
            }
        }
        T tx = list.get(level);
        start.accept(tx, dir, cls, parent);
        return tx;
    }

    private void doClose(T tx) {
        stop.accept(tx);
        while (level >= 0 && !isOpen.apply(list.get(level))) {
            level--;
        }
    }

}

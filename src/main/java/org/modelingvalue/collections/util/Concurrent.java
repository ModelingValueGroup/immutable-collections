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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Collection;

public class Concurrent<T> {

    public static <V> Concurrent<V> of(V value) {
        return new Concurrent<>(value);
    }

    public static <V> Concurrent<V> of(Supplier<V> value) {
        return new Concurrent<>(value);
    }

    public static <V> Concurrent<V> of() {
        return new Concurrent<>();
    }

    private T   pre;
    private T[] states;

    private Concurrent(T value) {
        init(value);
    }

    private Concurrent(Supplier<T> value) {
        init(value);
    }

    protected Concurrent() {
    }

    public boolean isInitialized() {
        return pre != null;
    }

    public T pre() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        return pre;
    }

    public <E> boolean set(BiFunction<T, E, T> function, E e) {
        return change(t -> function.apply(t, e));
    }

    public boolean change(UnaryOperator<T> oper) {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        if (i < 0) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (states) {
                T t = states[ContextThread.POOL_SIZE];
                T value = Collection.getSequential(() -> oper.apply(t)); // TODO @Wim: come up with an alternative
                if (t != value) {
                    states[ContextThread.POOL_SIZE] = value;
                    return true;
                } else {
                    return false;
                }
            }
        } else {

            T value = Collection.getSequential(() -> oper.apply(states[i])); // TODO @Wim: come up with an alternative
            if (states[i] != value) {
                states[i] = value;
                return true;
            } else {
                return false;
            }
        }
    }

    public T get() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        return i < 0 ? states[ContextThread.POOL_SIZE] : states[i];
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean set(T value) {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        if (i < 0) {
            // this is a synchronize on a non-final field and this is on purpose
            // the states field is only assigned once in one of the init methods
            //noinspection SynchronizeOnNonFinalField
            synchronized (states) {
                if (states[ContextThread.POOL_SIZE] != value) {
                    if (pre == states[ContextThread.POOL_SIZE]) {
                        states[ContextThread.POOL_SIZE] = value;
                    } else if (pre instanceof Mergeable) {
                        states[ContextThread.POOL_SIZE] = (T) Collection.getSequential(() -> ((Mergeable) pre).merge(states[ContextThread.POOL_SIZE], value));
                    } else {
                        throw new ConcurrentModificationException();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (states[i] != value) {
                states[i] = value;
                return true;
            } else {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void init(T value) {
        if (pre != null) {
            throw new ConcurrentModificationException();
        }
        pre = value;
        if (states == null) {
            states = (T[]) Array.newInstance(pre.getClass(), ContextThread.POOL_SIZE + 1);
        }
        Arrays.fill(states, value);
    }

    @SuppressWarnings("unchecked")
    public void init(Supplier<T> value) {
        if (pre != null) {
            throw new ConcurrentModificationException();
        }
        pre = value.get();
        if (states == null) {
            states = (T[]) Array.newInstance(pre.getClass(), ContextThread.POOL_SIZE + 1);
        }
        for (int i = 0; i < states.length; i++) {
            states[i] = value.get();
        }
    }

    public T merge() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int l = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i] != pre) {
                states[l++] = states[i];
            }
        }
        T result = Mergeables.merge(pre, this::merge, states, l);
        Arrays.fill(states, result);
        pre = result;
        return result;
    }

    public T result() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int l = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i] != pre) {
                states[l++] = states[i];
            }
        }
        T result = Mergeables.merge(pre, this::merge, states, l);
        Arrays.fill(states, null);
        pre = null;
        return result;
    }

    public void clear() {
        if (pre != null) {
            Arrays.fill(states, null);
            pre = null;
        }
    }

    @SuppressWarnings("unchecked")
    protected T merge(T base, T[] branches, int l) {
        if (base instanceof Mergeable) {
            return ((Mergeable<T>) base).merge(branches, l);
        } else {
            throw new ConcurrentModificationException();
        }
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2022 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Context<T> {

    private static final int           MAX_NR_OF_CONTEXT = Integer.getInteger("MAX_NR_OF_CONTEXT", 64);

    private final static AtomicInteger COUNT             = new AtomicInteger(0);

    private static final Object[]      DEFAULTS          = new Object[MAX_NR_OF_CONTEXT];

    public static <V> Context<V> of() {
        return new Context<>(null);
    }

    public static <V> Context<V> of(V def) {
        return new Context<>(def);
    }

    private final int nr;

    private Context(T def) {
        nr = COUNT.getAndIncrement();
        DEFAULTS[nr] = def;
    }

    public <V> V get(T v, Supplier<V> s) {
        Object[] c = ContextThread.getContext();
        if (set(c, v)) {
            try {
                return s.get();
            } finally {
                ContextThread.setContext(c);
            }
        } else {
            return s.get();
        }
    }

    public void run(T v, Runnable r) {
        Object[] c = ContextThread.getContext();
        if (set(c, v)) {
            try {
                r.run();
            } finally {
                ContextThread.setContext(c);
            }
        } else {
            r.run();
        }
    }

    public void setOnThread(T v) {
        set(ContextThread.getContext(), v);
    }

    @SuppressWarnings("unchecked")
    private boolean set(Object[] c, T v) {
        if (v != (c != null && c.length > nr ? (T) c[nr] : (T) DEFAULTS[nr])) {
            Object[] r = Arrays.copyOf(DEFAULTS, c != null ? Math.max(nr + 1, c.length) : nr + 1);
            if (c != null) {
                System.arraycopy(c, 0, r, 0, c.length);
            }
            r[nr] = v;
            ContextThread.setContext(r);
            return true;
        } else {
            return false;
        }
    }

    public T get() {
        return get(ContextThread.getContext());
    }

    @SuppressWarnings("unchecked")
    private T get(Object[] c) {
        return c == null || c.length <= nr ? (T) DEFAULTS[nr] : (T) c[nr];
    }

}

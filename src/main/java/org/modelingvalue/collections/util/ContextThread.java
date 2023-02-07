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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.modelingvalue.collections.Collection;

@SuppressWarnings("unused")
public final class ContextThread extends ForkJoinWorkerThread {
    public static final String                WORKER_THREAD_NAME_TEMPLATE = "dclare-p%02d-w%02d";
    public static final int                   POOL_SIZE                   = Integer.getInteger("POOL_SIZE", Collection.PARALLELISM * 2 + 2);
    //
    private static final ContextThreadFactory FACTORY                     = new ContextThreadFactory();
    private static final AtomicInteger        POOL_COUNTER                = new AtomicInteger();

    public static ContextPool createPool() {
        return new ContextPool(Collection.PARALLELISM, FACTORY, null, false);
    }

    public static ContextPool createPool(int parallelism) {
        return new ContextPool(parallelism, FACTORY, null, false);
    }

    public static ContextPool createPool(int parallelism, UncaughtExceptionHandler handler) {
        return new ContextPool(parallelism, FACTORY, handler, false);
    }

    public static ContextPool createPool(UncaughtExceptionHandler handler) {
        return new ContextPool(Collection.PARALLELISM, FACTORY, handler, false);
    }

    private final static ThreadLocal<Object[]> CONTEXT = new ThreadLocal<>();

    public static Object[] getContext() {
        Thread currentThread = Thread.currentThread();
        return currentThread instanceof ContextThread ? ((ContextThread) currentThread).getCtx() : CONTEXT.get();
    }

    public static Object[] setIncrement(Object[] context) {
        return setContext(context, +1);
    }

    public static Object[] setDecrement(Object[] context) {
        return setContext(context, -1);
    }

    public static Object[] setContext(Object[] context) {
        return setContext(context, 0);
    }

    private static Object[] setContext(Object[] context, int delta) {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ContextThread) {
            ContextThread contextThread = (ContextThread) currentThread;
            Object[] pre = contextThread.getCtx();
            contextThread.setCtx(context, delta);
            return pre;
        } else {
            Object[] pre = CONTEXT.get();
            CONTEXT.set(context);
            return pre;
        }
    }

    public static int getNr() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ContextThread) {
            return ((ContextThread) currentThread).nr;
        } else {
            return -1;
        }
    }

    public int nrOfRunningThreads() {
        return getPool().runningThreads();
    }

    private final int nr;
    private Object[]  context;

    private ContextThread(ForkJoinPool pool, int nr) {
        super(pool);
        this.nr = nr;
    }

    private Object[] getCtx() {
        return context;
    }

    private void setCtx(Object[] context, int delta) {
        this.context = context;
        if (delta != 0) {
            ContextPool pool = getPool();
            pool.activity[nr] += delta;
            if (pool.activity[nr] == 0 || pool.activity[nr] == 1) {
                pool.running = -1;
            }
        }
    }

    @Override
    public ContextPool getPool() {
        return (ContextPool) super.getPool();
    }

    @Override
    protected void onTermination(Throwable exception) {
        getPool().counter.set(nr, 0);
        context = null;
        if (exception != null) {
            UncaughtExceptionHandler handler = getPool().getUncaughtExceptionHandler();
            if (handler != null) {
                handler.uncaughtException(this, exception);
            } else {
                exception.printStackTrace();
            }
        }
        super.onTermination(exception);
    }

    public static final class ContextPool extends ForkJoinPool {
        private final AtomicIntegerArray counter       = new AtomicIntegerArray(POOL_SIZE);
        private final int                poolNr;
        private final AtomicInteger      numInOverflow = new AtomicInteger();
        private final int[]              activity      = new int[POOL_SIZE];
        private int                      running       = -1;

        private ContextPool(int parallelism, ForkJoinWorkerThreadFactory factory, UncaughtExceptionHandler handler, boolean asyncMode) {
            super(parallelism, factory, handler, asyncMode);
            poolNr = POOL_COUNTER.getAndIncrement();
        }

        public int runningThreads() {
            int nr = running;
            if (nr < 0) {
                for (int value : activity) {
                    if (value > 0) {
                        nr++;
                    }
                }
                running = nr;
            }
            return nr;
        }

        public int poolNr() {
            return poolNr;
        }

        public int getNumInOverflow() {
            return numInOverflow.get();
        }

        public int incrementAndGetNumInOverflow() {
            return numInOverflow.incrementAndGet();
        }

        @Override
        public String toString() {
            return String.format("pool%02d-%s", poolNr, super.toString().replaceFirst(".*\\[", "").replaceFirst("].*", ""));
        }
    }

    private static final class ContextThreadFactory implements ForkJoinWorkerThreadFactory {
        private int numInOverflow;

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ContextPool contextPool = (ContextPool) pool;
            for (int i = 0; i < POOL_SIZE; i++) {
                if (contextPool.counter.compareAndSet(i, 0, 1)) {
                    ContextThread thread = new ContextThread(pool, i);
                    thread.setName(String.format(WORKER_THREAD_NAME_TEMPLATE, contextPool.poolNr(), i));
                    return thread;
                }
            }
            int numInOverflow = contextPool.incrementAndGetNumInOverflow();
            System.err.println("WARNING: Overflow ForkJoinWorkerThread created, consider increasing POOL_SIZE (=" + POOL_SIZE + ") to at least " + (POOL_SIZE + numInOverflow));
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName(String.format(WORKER_THREAD_NAME_TEMPLATE, contextPool.poolNr(), POOL_SIZE + numInOverflow) + "-OVERFLOW");
            return thread;
        }
    }
}

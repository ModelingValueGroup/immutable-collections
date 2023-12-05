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

import org.modelingvalue.collections.Collection;

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

@SuppressWarnings("unused")
public final class ContextThread extends DclareWorkerThread {
    public static final  String                      WORKER_THREAD_NAME_TEMPLATE          = "dclare-p%02d-w%02d";
    public static final  int                         POOL_SIZE                            = Integer.getInteger("POOL_SIZE", Collection.PARALLELISM * 2 + 2);
    //
    private static final ForkJoinWorkerThreadFactory FACTORY                              = pool -> ((ContextPool) pool).newThread();

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

    @SuppressWarnings("UnusedReturnValue")
    public static Object[] setDecrement(Object[] context) {
        return setContext(context, -1);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Object[] setContext(Object[] context) {
        return setContext(context, 0);
    }

    private static Object[] setContext(Object[] context, int delta) {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ContextThread contextThread) {
            Object[] pre = contextThread.getCtx();
            contextThread.setCtx(context, delta);
            return pre;
        } else {
            Object[] pre = CONTEXT.get();
            CONTEXT.set(context);
            return pre;
        }
    }

    public static int getCurrentNr() {
        if (Thread.currentThread() instanceof ContextThread ctxThread) {
            return ctxThread.getNr();
        } else {
            return -1;
        }
    }

    public static boolean isCurrentAContextThread() {
        return Thread.currentThread() instanceof ContextThread;
    }

    private Object[] context;

    ContextThread(ContextPool pool, int nr) {
        super(pool, nr, WORKER_THREAD_NAME_TEMPLATE);
    }

    private Object[] getCtx() {
        return context;
    }

    private void setCtx(Object[] context, int delta) {
        this.context = context;
        if (delta != 0) {
            getPool().adjustDelta(nr, delta);
        }
    }

    @Override
    protected void onTermination(Throwable exception) {
        getPool().workerTerminated(nr);
        context = null;
        if (exception != null) {
            UncaughtExceptionHandler handler = getPool().getUncaughtExceptionHandler();
            if (handler != null) {
                handler.uncaughtException(this, exception);
            } else {
                System.err.println("Uncaught exception in thread " + this.getName());
                exception.printStackTrace(System.err);
            }
        }
        super.onTermination(exception);
    }
}

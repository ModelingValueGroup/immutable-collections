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
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class ContextPool extends ForkJoinPool {
    private static final AtomicInteger      POOL_COUNTER  = new AtomicInteger();
    private final        AtomicIntegerArray workerCreated = new AtomicIntegerArray(ContextThread.POOL_SIZE);
    private final        int                poolNr;
    private final        AtomicInteger      numInOverflow = new AtomicInteger();
    private final        int[]              activity      = new int[ContextThread.POOL_SIZE];
    private              int                running       = -1;

    static {
        ContextPoolMonitor.init();
    }

    ContextPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
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

    @SuppressWarnings("unused")
    public int getNumInOverflow() {
        return numInOverflow.get();
    }

    public int incrementAndGetNumInOverflow() {
        return numInOverflow.incrementAndGet();
    }

    private boolean isVacantContextThreadSlot(int i) {
        return workerCreated.compareAndSet(i, 0, 1);
    }

    public void workerTerminated(int i) {
        workerCreated.set(i, 0);
    }

    public void adjustDelta(int nr, int delta) {
        activity[nr] += delta;
        if (activity[nr] == 0 || activity[nr] == 1) {
            running = -1;
        }
    }

    public ForkJoinWorkerThread newThread() {
        for (int i = 0; i < ContextThread.POOL_SIZE; i++) {
            if (isVacantContextThreadSlot(i)) {
                return new ContextThread(this, i);
            }
        }
        return new OverflowWorkerThread(this);
    }

    @Override
    public String toString() {
        return String.format("pool%02d-%s", poolNr, super.toString().replaceFirst(".*\\[", "").replaceFirst("].*", ""));
    }
}

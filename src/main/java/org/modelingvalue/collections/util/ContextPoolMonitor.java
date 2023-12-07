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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContextPoolMonitor extends Thread {
    public static final String  NAME      = "CONTEXT_POOL_MONITOR";
    public static final boolean ON        = Boolean.getBoolean("CONTEXT_POOL_MONITOR");
    public static final int     INTERVAL  = Integer.getInteger("CONTEXT_POOL_MONITOR_INTERVAL", 30_000);
    public static final boolean ALWAYS_ON = Boolean.getBoolean("CONTEXT_POOL_MONITOR_ALWAYS_ON");

    public static void init() {
        if (ON) {
            new ContextPoolMonitor();
        }
    }

    private ContextPoolMonitor() {
        super(NAME);
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        System.out.println(NAME + ": started, interval=" + INTERVAL + "ms");
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                //noinspection BusyWait
                Thread.sleep(INTERVAL);
                new Monitor().monitor();
            } catch (InterruptedException e) {
                System.out.println(NAME + ": stopped");
            }
        }
    }

    private static class Monitor {
        List<PoolInfo> pools = determinePools();

        private void monitor() {
            if (ALWAYS_ON || hasStuckThread()) {
                System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                pools.forEach(pi -> {
                    System.err.printf("POOL-%02d: %2d c-threads, %2d o-threads, [%s]\n", pi.poolNr, pi.numContextThreads(), pi.numOverflowThreads(), pi.state());
                    pi.workerInfos.forEach(wi -> {
                        if (wi.state == Thread.State.RUNNABLE) {
                            System.err.printf("   - %s\n", wi.stack[0]);
                        }
                    });
                });
            }
        }

        private boolean hasStuckThread() {
            return pools.stream().anyMatch(PoolInfo::hasStuckThread);
        }

        private List<PoolInfo> determinePools() {
            return Thread.getAllStackTraces()
                         .entrySet()
                         .stream()
                         .map(WorkerInfo::of)
                         .filter(Objects::nonNull)
                         .collect(Collectors.groupingBy(wi -> wi.poolNr))
                         .values()
                         .stream()
                         .map(l -> new PoolInfo(l.get(0).poolNr, l))
                         .sorted(Comparator.comparing(p -> p.poolNr))
                         .toList();
        }

        private record PoolInfo(
                int poolNr,
                List<WorkerInfo> workerInfos
        ) {
            public String state() {
                return workerInfos.stream().map(WorkerInfo::describeState).sorted().collect(Collectors.joining(" "));
            }

            public boolean hasStuckThread() {
                return workerInfos.stream().anyMatch(wi -> wi.stuck);
            }

            public int numContextThreads() {
                return (int) workerInfos.stream().filter(wi -> wi.thread instanceof ContextThread).count();
            }

            public int numOverflowThreads() {
                return (int) workerInfos.stream().filter(wi -> wi.thread instanceof OverflowWorkerThread).count();
            }
        }

        private record WorkerInfo(
                int poolNr,
                int workerNr,
                State state,
                boolean stuck,
                Thread thread,
                StackTraceElement[] stack
        ) {
            private static WorkerInfo of(Map.Entry<Thread, StackTraceElement[]> e) {
                if (e.getKey() instanceof DclareWorkerThread dwt) {
                    State               state = dwt.getState();
                    StackTraceElement[] stack = e.getValue();
                    boolean             stuck = state == State.RUNNABLE && stack[0].toString().contains("java.util.concurrent.ForkJoinPool$WorkQueue.helpComplete(");
                    return new WorkerInfo(dwt.getPool().poolNr(), dwt.getNr(), state, stuck, e.getKey(), stack);
                } else {
                    return null;
                }
            }

            private String describeState() {
                return (thread instanceof ContextThread ct ? "" : "+") + switch (state) {
                    case RUNNABLE -> stuck ? "!" : "R";
                    case WAITING, TIMED_WAITING -> ".";
                    case BLOCKED, TERMINATED, NEW -> "a";
                };
            }
        }
    }
}

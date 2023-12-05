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

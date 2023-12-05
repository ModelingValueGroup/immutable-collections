package org.modelingvalue.collections.util;

public class OverflowWorkerThread extends DclareWorkerThread {
    public static final String OVERFLOW_WORKER_THREAD_NAME_TEMPLATE = ContextThread.WORKER_THREAD_NAME_TEMPLATE + "-OVERFLOW";

    public OverflowWorkerThread(ContextPool pool) {
        super(pool, ContextThread.POOL_SIZE + pool.incrementAndGetNumInOverflow(), OVERFLOW_WORKER_THREAD_NAME_TEMPLATE);
        setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.err.println("WARNING: Overflow ForkJoinWorkerThread created, consider increasing POOL_SIZE (=" + ContextThread.POOL_SIZE + ") to at least " + getNr());
    }
}

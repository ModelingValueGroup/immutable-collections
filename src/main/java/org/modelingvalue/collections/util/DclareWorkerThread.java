package org.modelingvalue.collections.util;

import java.util.concurrent.ForkJoinWorkerThread;

public class DclareWorkerThread extends ForkJoinWorkerThread {
    protected final int nr;

    protected DclareWorkerThread(ContextPool pool, int nr, String nameTemplate) {
        super(pool);
        this.nr = nr;
        setName(String.format(nameTemplate, pool.poolNr(), nr));
    }

    public int getNr() {
        return nr;
    }

    public int nrOfRunningThreads() {
        return getPool().runningThreads();
    }

    @Override
    public ContextPool getPool() {
        return (ContextPool) super.getPool();
    }
}

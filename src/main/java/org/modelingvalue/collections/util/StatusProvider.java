package org.modelingvalue.collections.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class StatusProvider<S extends StatusProvider.AbstractStatus> implements Iterable<S> {

    public static abstract class AbstractStatus {

        protected final CompletableFuture<AbstractStatus> next = new CompletableFuture<>();

        public abstract boolean isStopped();

        protected void handleException(Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private final AtomicReference<S> status;

    public StatusProvider(S start) {
        this.status = new AtomicReference<>(start);
    }

    public void setNext(UnaryOperator<S> nextFunction) {
        for (S pre = status.get(), post = nextFunction.apply(pre);; pre = status.get(), post = nextFunction.apply(pre)) {
            if (status.weakCompareAndSetVolatile(pre, post)) {
                pre.next.complete(post);
                return;
            }
        }
    }

    public S getStatus() {
        return status.get();
    }

    @Override
    public StatusIterator<S> iterator() {
        return new StatusIterator<>(getStatus());
    }

    public static final class StatusIterator<M extends StatusProvider.AbstractStatus> implements Iterator<M> {

        private M       status;
        private boolean firstDone;

        private StatusIterator(M status) {
            this.status = status;
        }

        @Override
        public boolean hasNext() {
            return !firstDone || !status.isStopped();
        }

        @SuppressWarnings("unchecked")
        @Override
        public M next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (!firstDone) {
                firstDone = true;
                return status;
            } else {
                try {
                    status = (M) status.next.get();
                    return status;
                } catch (InterruptedException | ExecutionException e) {
                    status.handleException(e);
                    return null;
                }
            }
        }

        public M getFirst(Predicate<M> pred) {
            M s;
            while (hasNext()) {
                s = next();
                if (pred.test(s)) {
                    return s;
                }
            }
            throw new IllegalStateException();
        }
    }

}

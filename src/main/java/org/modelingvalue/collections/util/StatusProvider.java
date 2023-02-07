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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class StatusProvider<S extends StatusProvider.AbstractStatus> implements Iterable<S> {

    private static final boolean TRACE_STATUS = Boolean.getBoolean("TRACE_STATUS");

    public static abstract class AbstractStatus {

        protected final CompletableFuture<AbstractStatus> next = new CompletableFuture<>();

        public abstract boolean isStopped();

        protected void handleException(Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private final AtomicReference<S> status;

    public StatusProvider(Object context, S start) {
        this.status = new AtomicReference<>(start);
        if (TRACE_STATUS) {
            Thread tread = new Thread(() -> new StatusIterator<>(start).forEachRemaining(s -> System.err.println("Status of " + context + " changed: " + s)), "StatusProvider.traceThread");
            tread.setDaemon(true);
            tread.start();
        }
    }

    public void setNext(UnaryOperator<S> nextFunction) {
        for (S pre = status.get(), post = nextFunction.apply(pre);; pre = status.get(), post = nextFunction.apply(pre)) {
            if (post == pre) {
                return;
            } else if (status.weakCompareAndSetVolatile(pre, post)) {
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
        return StatusIterator.of(getStatus());
    }

    @SuppressWarnings("unused")
    public static final class StatusIterator<M extends StatusProvider.AbstractStatus> implements Iterator<M> {
        private M                   status;
        private boolean             firstDone;
        private Consumer<Exception> interruptedHandler;

        public static <X extends StatusProvider.AbstractStatus> StatusIterator<X> of(X status) {
            return new StatusIterator<>(status);
        }

        private StatusIterator(M status) {
            this.status = status;
        }

        @Override
        public boolean hasNext() {
            return !firstDone || !status.isStopped();
        }

        public void setInterruptedHandler(Consumer<Exception> h) {
            interruptedHandler = h;
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
                    if (interruptedHandler != null) {
                        interruptedHandler.accept(e);
                    } else {
                        status.handleException(e);
                    }
                    return null;
                }
            }
        }

        public M waitFor(Predicate<M> pred) {
            M s;
            while (hasNext()) {
                s = next();
                if (pred.test(s)) {
                    firstDone = false;
                    return s;
                }
            }
            firstDone = false;
            throw new IllegalStateException();
        }

        public M waitForStoppedOr(Predicate<M> pred) {
            M s = status;
            while (hasNext()) {
                s = next();
                if (pred.test(s)) {
                    firstDone = false;
                    return s;
                }
            }
            firstDone = false;
            return s;
        }
    }

}

package org.modelingvalue.collections.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ContextSpliterator<A> implements Spliterator<A> {
    private final Spliterator<A> sp;
    private final Object[]       ctx;

    public static <T> Spliterator<T> of(Spliterator<T> sp) {
        return new ContextSpliterator<T>(sp, ContextThread.getContext());
    }

    private ContextSpliterator(Spliterator<A> sp, Object[] ctx) {
        this.sp = sp;
        this.ctx = ctx;
    }

    @Override
    public void forEachRemaining(Consumer<? super A> action) {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            sp.forEachRemaining(action);
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super A> action) {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            return sp.tryAdvance(action);
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public Spliterator<A> trySplit() {
        Object[] old = ContextThread.setIncrement(ctx);
        try {
            Spliterator<A> tsp = sp.trySplit();
            return tsp != null ? new ContextSpliterator<A>(tsp, ctx) : null;
        } finally {
            ContextThread.setDecrement(old);
        }
    }

    @Override
    public long getExactSizeIfKnown() {
        return sp.getExactSizeIfKnown();
    }

    @Override
    public long estimateSize() {
        return sp.estimateSize();
    }

    @Override
    public int characteristics() {
        return sp.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return sp.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super A> getComparator() {
        return sp.getComparator();
    }

}

package org.modelingvalue.logic.impl;

import java.util.Optional;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;

public interface Conclusion {
    Conclusion EMPTY = new Conclusion() {
        @Override
        public Set<PredicateImpl> positive() {
            return Set.of();
        }

        @Override
        public Set<List<PredicateImpl>> incomplete() {
            return Set.of();
        }
    };

    Set<PredicateImpl> positive();

    Set<List<PredicateImpl>> incomplete();

    static Conclusion of(Set<PredicateImpl> positive, Set<List<PredicateImpl>> incomplete) {
        return new Conclusion() {
            @Override
            public Set<PredicateImpl> positive() {
                return positive;
            }

            @Override
            public Set<List<PredicateImpl>> incomplete() {
                return incomplete;
            }
        };
    }

    default Conclusion positive(Set<PredicateImpl> positive) {
        return of(positive, incomplete());
    }

    default Conclusion incomplete(Set<List<PredicateImpl>> incomplete) {
        return of(positive(), incomplete);
    }

    default Conclusion add(Conclusion conclusion) {
        return of(positive().addAll(conclusion.positive()), incomplete().addAll(conclusion.incomplete()));
    }

    default boolean hasCycleWith(PredicateImpl predicate) {
        return incomplete().anyMatch(l -> l.last().equals(predicate));
    }

    default Optional<List<PredicateImpl>> stackOverflow() {
        return incomplete().findAny(l -> l.size() >= PredicateImpl.MAX_LOGIC_DEPTH);
    }

    default boolean hasStackOverflow() {
        return incomplete().anyMatch(l -> l.size() >= PredicateImpl.MAX_LOGIC_DEPTH);
    }
}

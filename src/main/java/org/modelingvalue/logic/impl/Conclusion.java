package org.modelingvalue.logic.impl;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;

public interface Conclusion {

    Set<PredicateImpl> positive();

    Set<List<PredicateImpl>> incomplete();

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

    static Conclusion of(Set<PredicateImpl> positive) {
        return new Conclusion() {
            @Override
            public Set<PredicateImpl> positive() {
                return positive;
            }

            @Override
            public Set<List<PredicateImpl>> incomplete() {
                return Set.of();
            }
        };
    }

    default Conclusion add(Conclusion conclusion) {
        return of(positive().addAll(conclusion.positive()), incomplete().addAll(conclusion.incomplete()));
    }

    default boolean hasCycleWith(PredicateImpl predicate) {
        return incomplete().anyMatch(l -> l.last().equals(predicate));
    }

    default List<PredicateImpl> stackOverflow() {
        return incomplete().findAny(l -> l.size() >= PredicateImpl.MAX_LOGIC_DEPTH).orElse(null);
    }

    default boolean hasStackOverflow() {
        return incomplete().anyMatch(l -> l.size() >= PredicateImpl.MAX_LOGIC_DEPTH);
    }
}

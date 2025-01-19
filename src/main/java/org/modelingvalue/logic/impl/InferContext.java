package org.modelingvalue.logic.impl;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;

public interface InferContext {
    KnowledgeBaseImpl knowledgebase();

    List<PredicateImpl> stack();

    Map<PredicateImpl, Conclusion> cyclic();

    static InferContext of(KnowledgeBaseImpl knowledgebase, List<PredicateImpl> stack, Map<PredicateImpl, Conclusion> cyclic) {
        return new InferContext() {
            @Override
            public KnowledgeBaseImpl knowledgebase() {
                return knowledgebase;
            }

            @Override
            public List<PredicateImpl> stack() {
                return stack;
            }

            @Override
            public Map<PredicateImpl, Conclusion> cyclic() {
                return cyclic;
            }
        };
    }

    default InferContext stack(PredicateImpl predicate) {
        return of(knowledgebase(), stack().append(predicate), cyclic());
    }

    default InferContext cycle(PredicateImpl predicate, Set<PredicateImpl> facts) {
        return of(knowledgebase(), stack(), cyclic().put(predicate, Conclusion.of(facts)));
    }
}

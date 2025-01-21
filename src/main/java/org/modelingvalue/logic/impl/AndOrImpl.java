package org.modelingvalue.logic.impl;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Predicate;

public abstract class AndOrImpl extends PredicateImpl {
    private static final long serialVersionUID = -928776822979604743L;

    private List<int[]>       idxList;

    protected AndOrImpl(FunctorImpl<Predicate> functor, PredicateImpl predicate1, PredicateImpl predicate2) {
        super(functor, predicate1, predicate2);
    }

    protected AndOrImpl(Object[] args) {
        super(args);
    }

    @SuppressWarnings("rawtypes")
    private List<int[]> idxList() {
        if (idxList == null) {
            List<int[]> l = List.of();
            PredicateImpl predicate1 = predicate1();
            if (equalClass(predicate1)) {
                l = l.prependList(((AndOrImpl) predicate1).idxList().replaceAll(ADD_ONE));
            } else {
                l = l.append(ONE_ARRAY);
            }
            PredicateImpl predicate2 = predicate2();
            if (equalClass(predicate2)) {
                l = l.appendList(((AndOrImpl) predicate2).idxList().replaceAll(ADD_TWO));
            } else {
                l = l.append(TWO_ARRAY);
            }
            idxList = l;
        }
        return idxList;
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl predicate1() {
        return (PredicateImpl) get(1);
    }

    @SuppressWarnings("rawtypes")
    public final PredicateImpl predicate2() {
        return (PredicateImpl) get(2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public final InferResult infer(PredicateImpl declaration, InferContext context) {
        idxList = ((AndOrImpl) declaration).idxList();
        Set<PredicateImpl> facts = Set.of();
        InferResult result = InferResult.EMPTY, tmpResult, andResult;
        Set<AndOrImpl> nextAnds = Set.of(this), prevAnds;
        do {
            prevAnds = nextAnds;
            nextAnds = Set.of();
            outer:
            for (AndOrImpl and : prevAnds) {
                List<int[]> idxl = and.idxList;
                if (idxl.isEmpty()) {
                    facts = facts.add(and);
                } else {
                    tmpResult = InferResult.EMPTY;
                    for (int ii = 0; ii < idxl.size(); ii++) {
                        int[] i = idxl.get(ii);
                        PredicateImpl declPred = declaration.getVal(i);
                        PredicateImpl pred = and.getVal(i);
                        InferResult predResult = flip(pred.infer(declPred, context));
                        if (predResult.hasStackOverflow()) {
                            return predResult;
                        }
                        andResult = predResult.bind(declPred, and, declaration);
                        if (andResult.incomplete().isEmpty()) {
                            List<int[]> iil = idxl.removeIndex(ii);
                            andResult.facts().forEach(f -> ((AndOrImpl) f).idxList = iil);
                            nextAnds = nextAnds.addAll((Set) andResult.facts());
                            result = result.add(andResult);
                            continue outer;
                        } else {
                            tmpResult = tmpResult.add(andResult);
                        }
                    }
                    result = result.add(tmpResult);
                }
            }
        } while (!nextAnds.isEmpty());
        return flip(InferResult.of(facts, result.falsehoods(), result.incomplete(), result.falseIncomplete()));
    }

    protected abstract boolean equalClass(PredicateImpl predicate);

    protected abstract InferResult flip(InferResult result);
}

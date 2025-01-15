package org.modelingvalue.logic;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.logic.Logic.Relation;
import org.modelingvalue.logic.Logic.Rule;

public interface Database {

    Map<Relation, List<Rule>> rules();

    Map<Relation, Set<Relation>> facts();

}

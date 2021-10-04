package org.modelingvalue.collections.mutable;

import org.modelingvalue.collections.Collection;

public interface Mutable<T> {

    Collection<T> toImmutable();

}

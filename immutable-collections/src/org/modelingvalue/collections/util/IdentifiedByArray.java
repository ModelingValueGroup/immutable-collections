package org.modelingvalue.collections.util;

import java.util.Arrays;

public class IdentifiedByArray {

    private Object[] array;

    public IdentifiedByArray(Object[] array) {
        this.array = array;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            IdentifiedByArray other = (IdentifiedByArray) obj;
            if (other.array == array) {
                return true;
            } else if (!Arrays.deepEquals(array, other.array)) {
                return false;
            } else {
                if (Age.age(array) > Age.age(other.array)) {
                    other.array = array;
                } else {
                    array = other.array;
                }
                return true;
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(array);
    }

    public Object[] array() {
        return array;
    }

}

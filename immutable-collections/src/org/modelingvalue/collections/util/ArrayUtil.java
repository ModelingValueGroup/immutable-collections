package org.modelingvalue.collections.util;

import java.lang.reflect.Array;

public class ArrayUtil {

    @SuppressWarnings("unchecked")
    public static <T> T[] set(T[] a, int i, T v, int l) {
        if (a == null && v != null) {
            a = (T[]) Array.newInstance(v.getClass(), l);
            a[i] = v;
            return a;
        } else {
            while (true) {
                try {
                    a[i] = v;
                    return a;
                } catch (ArrayStoreException ase) {
                    Class<?> s = a.getClass().getComponentType().getSuperclass();
                    if (s == null) {
                        throw ase;
                    }
                    Object old = a;
                    a = (T[]) Array.newInstance(s, l);
                    System.arraycopy(old, 0, a, 0, l);
                }
            }
        }
    }

}

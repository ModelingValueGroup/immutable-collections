package org.modelingvalue.collections.util;

public class WrappedClassNotFoundException extends Error {
    public WrappedClassNotFoundException(ClassNotFoundException cause) {
        super(cause);
    }

    public void throwOriginal() throws ClassNotFoundException {
        throw (ClassNotFoundException) getCause();
    }
}

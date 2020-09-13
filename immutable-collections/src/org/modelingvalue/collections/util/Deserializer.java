package org.modelingvalue.collections.util;

import java.io.*;
import java.util.function.*;

public interface Deserializer {
    Object readObject();

    int readInt();

    /**
     * Just a wrapper for ObjectInputStream to act as a Deserializer
     */
    class WrapObjectInputStream implements Deserializer {
        private final ObjectInputStream s;

        public WrapObjectInputStream(ObjectInputStream s) {
            this.s = s;
        }

        @Override
        public Object readObject() {
            try {
                return s.readObject();
            } catch (IOException e) {
                throw new WrappedIOException(e);
            } catch (ClassNotFoundException e) {
                throw new WrappedClassNotFoundException(e);
            }
        }

        @Override
        public int readInt() {
            try {
                return s.readInt();
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }
    }

    static void wrap(ObjectInputStream s, Consumer<Deserializer> readObject) throws IOException, ClassNotFoundException {
        try {
            readObject.accept(new WrapObjectInputStream(s));
        } catch (WrappedIOException e) {
            e.throwOriginal();
        } catch (WrappedClassNotFoundException e) {
            e.throwOriginal();
        }
    }
}

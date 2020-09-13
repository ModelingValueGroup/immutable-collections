package org.modelingvalue.collections.util;

import java.io.*;
import java.util.function.*;

public interface Serializer {
    void writeObject(Object o);

    void writeInt(int i);

    /**
     * Just a wrapper for ObjectOutputStream to act as a Serializer
     */
    class WrapObjectOutputStream implements Serializer {
        private final ObjectOutputStream s;

        public WrapObjectOutputStream(ObjectOutputStream s) {
            this.s = s;
        }

        @Override
        public void writeObject(Object o) {
            try {
                s.writeObject(o);
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }

        @Override
        public void writeInt(int i) {
            try {
                s.writeInt(i);
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
        }
    }

    static void wrap(ObjectOutputStream s, Consumer<Serializer> writeObject) throws IOException {
        try {
            writeObject.accept(new WrapObjectOutputStream(s));
        } catch (WrappedIOException e) {
            e.throwOriginal();
        }
    }
}

package org.modelingvalue.collections.util;

import java.io.*;

public class WrappedIOException extends Error {
    public WrappedIOException(IOException cause) {
        super(cause);
    }

    public void throwOriginal() throws IOException {
        throw (IOException) getCause();
    }
}

package org.modelingvalue.collections.util;

@SuppressWarnings("unused")
public class NotSerializableError extends RuntimeException {
    public NotSerializableError() {
    }

    public NotSerializableError(String message) {
        super(message);
    }

    public NotSerializableError(Throwable cause) {
        super(cause);
    }

    public NotSerializableError(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.github.miracelwhipp.typewriter;

public class WrappedCheckedException extends RuntimeException {

    public WrappedCheckedException(Throwable cause) {
        super(cause);
    }
}

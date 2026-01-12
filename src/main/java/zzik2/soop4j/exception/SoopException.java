package zzik2.soop4j.exception;

public class SoopException extends RuntimeException {

    public SoopException(String message) {
        super(message);
    }

    public SoopException(String message, Throwable cause) {
        super(message, cause);
    }
}

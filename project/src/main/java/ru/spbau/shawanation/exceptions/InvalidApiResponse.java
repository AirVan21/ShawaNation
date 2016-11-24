package ru.spbau.shawanation.exceptions;

public class InvalidApiResponse extends Exception {
    public InvalidApiResponse() {
        super();
    }

    public InvalidApiResponse(String message) {
        super(message);
    }

    public InvalidApiResponse(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidApiResponse(Throwable cause) {
        super(cause);
    }

    protected InvalidApiResponse(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

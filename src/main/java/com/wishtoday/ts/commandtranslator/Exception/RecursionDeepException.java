package com.wishtoday.ts.commandtranslator.Exception;

public class RecursionDeepException extends RuntimeException {
    public RecursionDeepException(String message) {
        super(message);
    }
    public RecursionDeepException() {
        super();
    }
}

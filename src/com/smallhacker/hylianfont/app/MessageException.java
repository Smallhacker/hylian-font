package com.smallhacker.hylianfont.app;

public final class MessageException extends RuntimeException {
    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}

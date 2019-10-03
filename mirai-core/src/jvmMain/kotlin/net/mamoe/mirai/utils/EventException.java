package net.mamoe.mirai.utils;

/**
 * @author NaturalHG
 */
public class EventException extends RuntimeException {
    private final Throwable cause;

    public EventException(Throwable throwable) {
        cause = throwable;
    }


    public EventException() {
        cause = null;
    }


    public EventException(Throwable cause, String message) {
        super(message);
        this.cause = cause;
    }


    public EventException(String message) {
        super(message);
        cause = null;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}


package ru.practicum.exception;

public class StatsException extends RuntimeException {
    public StatsException(String message) {
        super(message);
    }

    public StatsException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatsException(String message, Object... parameters) {
        super(String.format(message, parameters));
    }
}

package ru.practicum.ewm.exceptions;

public class UncorrectedRequestException extends RuntimeException {

    public UncorrectedRequestException(String message) {
        super(message);
    }

    public UncorrectedRequestException(String message, Throwable cause) {
        super(message, cause);
    }


}

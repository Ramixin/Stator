package net.ramixin.stator.metadata;

public class StatorMetaFileException extends Exception {

    public StatorMetaFileException(String message, Object... args) {
        super(String.format(message, args));
    }

    public StatorMetaFileException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }
}

package io.github.trae.hytale.framework.event.exceptions;

/**
 * Unchecked exception thrown when an error occurs during event handling.
 *
 * <p>Wraps checked exceptions surfaced by reflective handler invocation
 * (e.g. via {@link io.github.trae.hytale.framework.helper.EventHelper}) into
 * an unchecked form suitable for propagation through lambda and
 * {@link java.util.function.Consumer} boundaries.</p>
 *
 * @see io.github.trae.hytale.framework.helper.EventHelper
 */
public class EventException extends RuntimeException {

    /**
     * Constructs an {@link EventException} with no message or cause.
     */
    public EventException() {
    }

    /**
     * Constructs an {@link EventException} with the given message and cause.
     *
     * @param message a description of the error
     * @param cause   the underlying exception that triggered this error
     */
    public EventException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@link EventException} with the given message and no cause.
     *
     * @param message a description of the error
     */
    public EventException(final String message) {
        super(message);
    }

    /**
     * Constructs an {@link EventException} wrapping the given cause.
     *
     * @param cause the underlying exception that triggered this error
     */
    public EventException(final Throwable cause) {
        super(cause);
    }
}
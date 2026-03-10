package org.mycore.mets.validator.validators;

import java.io.Serial;

/**
 * Exception thrown when a METS document fails validation.
 */
public class ValidationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The line number where the validation error occurred, or -1 if unknown. */
    private int lineNumber = -1;

    /**
     * Creates a new ValidationException with the given message.
     *
     * @param message description of the validation error
     */
    public ValidationException(String message) {
        this(message, -1);
    }

    /**
     * Creates a new ValidationException wrapping the given cause.
     *
     * @param cause the underlying cause of this exception
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new ValidationException with the given message and line number.
     *
     * @param message description of the validation error
     * @param lineNumber the line number in the document where the error occurred
     */
    public ValidationException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    /**
     * Creates a new ValidationException with the given message and cause.
     *
     * @param message description of the validation error
     * @param cause the underlying cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the line number where the validation error occurred.
     *
     * @return the line number, or -1 if unknown
     */
    public int getLineNumber() {
        return lineNumber;
    }

}

package org.mycore.mets.validator.validators;

import java.io.Serial;

public class ValidationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    private int lineNumber = -1;

    public ValidationException(String message) {
        this(message, -1);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getLineNumber() {
        return lineNumber;
    }

}

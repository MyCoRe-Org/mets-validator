package org.mycore.mets.validator.validators;

import com.google.gson.JsonObject;

public class ValidationException extends Exception {

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

    public JsonObject toJSON() {
        JsonObject o = new JsonObject();
        o.addProperty("message", getMessage());
        if (lineNumber != -1) {
            o.addProperty("lineNumber", lineNumber);
        }
        if (getCause() != null) {
            o.addProperty("cause", getCause().getMessage());
        }
        return o;
    }

}

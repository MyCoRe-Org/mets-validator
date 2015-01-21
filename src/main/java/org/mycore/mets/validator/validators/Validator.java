package org.mycore.mets.validator.validators;

import org.jdom2.Document;

/**
 * Base interface for any mets validation based classes.
 * 
 * @author Matthias Eichner
 */
public interface Validator {

    /**
     * Validates the given document. Throws a {@link ValidationException} when the
     * validation fails.
     * 
     * @param document the document to validate
     * @throws ValidationException is thrown when the document is invalid
     */
    public void validate(Document document) throws ValidationException;

}

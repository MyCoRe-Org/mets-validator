package org.mycore.mets.validator.validators;

import org.jdom2.Document;

public interface Validator {

    public void validate(Document document) throws ValidationException;

}

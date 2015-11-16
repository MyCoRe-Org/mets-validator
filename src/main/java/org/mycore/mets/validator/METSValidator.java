package org.mycore.mets.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.transform.JDOMSource;
import org.mycore.mets.validator.validators.FileSectionValidator;
import org.mycore.mets.validator.validators.LogicalStructMapValidator;
import org.mycore.mets.validator.validators.PhysicalStructureValidator;
import org.mycore.mets.validator.validators.SchemaValidator;
import org.mycore.mets.validator.validators.StructLinkValidator;
import org.mycore.mets.validator.validators.ValidationException;
import org.mycore.mets.validator.validators.Validator;

/**
 * Base validation class.
 * 
 * @author Matthias Eichner
 */
public class METSValidator {

    private Document document;

    private List<Validator> validatorList;

    /**
     * Creates a new mets validator with the document to validate.
     * 
     * @param doc the document to validate
     */
    public METSValidator(Document doc) throws JDOMException, IOException, TransformerException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Source xmlSource = new JDOMSource(doc);
        Result outputTarget = new StreamResult(os);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        this.document = buildDocument(is);
        this.validatorList = new ArrayList<>();
        this.addDefaultValidators();
        is.close();
    }

    /**
     * Creates a new mets validator with the input stream to validate.
     * 
     * @param in validate this input
     */
    public METSValidator(InputStream in) throws JDOMException, IOException {
        this.document = buildDocument(in);
        this.validatorList = new ArrayList<>();
        this.addDefaultValidators();
    }

    public void addDefaultValidators() {
        validatorList.add(new SchemaValidator());
        validatorList.add(new FileSectionValidator());
        validatorList.add(new PhysicalStructureValidator());
        validatorList.add(new LogicalStructMapValidator());
        validatorList.add(new StructLinkValidator());
    }

    /**
     * List of all validators which should be used in the validation process.
     * 
     * @return list of validators
     */
    public List<Validator> getValidators() {
        return this.validatorList;
    }

    /**
     * Does the validation.
     * 
     * @return A list of validation exceptions. This list is empty when everything is fine.
     */
    public List<ValidationException> validate() {
        List<ValidationException> errorList = new ArrayList<ValidationException>();
        for (Validator validator : getValidators()) {
            validate(validator, document, errorList);
        }
        return errorList;
    }

    /**
     * Builds a jdom document from the given input stream. Uses the {@link LocatedJDOMFactory}
     * for line number information.
     * 
     * @param in input stream to parse
     * @return jdom document
     * @throws JDOMException
     * @throws IOException
     */
    protected Document buildDocument(InputStream in) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        builder.setJDOMFactory(new LocatedJDOMFactory());
        return builder.build(in);
    }

    /**
     * Validates the given document with the validator. All errors are append to
     * the errorList.
     * 
     * @param validator
     * @param document
     * @param errorList
     */
    protected void validate(Validator validator, Document document, List<ValidationException> errorList) {
        try {
            validator.validate(document);
        } catch (ValidationException validationException) {
            errorList.add(validationException);
        }
    }

}

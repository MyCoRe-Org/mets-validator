package org.mycore.mets.validator.validators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.jdom2.transform.JDOMSource;

/**
 * Validates against the mets schema.
 * 
 * @author Matthias Eichner
 */
public class SchemaValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        try {
            URL xsd = getClass().getResource("/mets_1.10.xsd");
            if (xsd == null) {
                throw new RuntimeException("Unable to read mets.xsd!");
            }
            XMLReaderJDOMFactory readerFactory = new XMLReaderXSDFactory(xsd);
            SAXBuilder builder = new SAXBuilder(readerFactory);
            builder.build(toInputStream(document));
        } catch (Exception exc) {
            throw new ValidationException(exc);
        }
    }

    protected InputStream toInputStream(Document doc) throws TransformerConfigurationException, TransformerException,
        TransformerFactoryConfigurationError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new JDOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}

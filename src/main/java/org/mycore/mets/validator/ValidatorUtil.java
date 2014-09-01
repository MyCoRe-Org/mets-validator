package org.mycore.mets.validator;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedElement;
import org.mycore.mets.validator.validators.ValidationException;

/**
 * Some utility methods required for the validation.
 * 
 * @author Matthias Eichner
 */
public abstract class ValidatorUtil {

    public static Namespace METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

    public static Namespace XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    /**
     * Throws a {@link ValidationException} with the given message.
     * 
     * @param e
     * @param message
     * @throws ValidationException
     */
    public static void throwException(Element e, String message) throws ValidationException {
        if (e instanceof LocatedElement) {
            LocatedElement le = (LocatedElement) e;
            throw new ValidationException(message, le.getLine());
        }
        throw new ValidationException(message);
    }

    /**
     * Checks if the element contains an attribute with the given name.
     * Throws an exception if there is no such attribute.
     * 
     * @param element
     * @param attributeName
     * @throws ValidationException
     */
    public static void checkNullAttribute(Element element, String attributeName) throws ValidationException {
        if (element.getAttributeValue(attributeName) == null) {
            ValidatorUtil.throwException(element, "Missing @" + attributeName + " attribute.");
        }
    }

    /**
     * Checks if the element contains an attribute with the given name and it should not be empty ("").
     * Throws an exception if there is no such attribute.
     * 
     * @param element
     * @param attributeName
     * @return
     * @throws ValidationException
     */
    public static String checkNullAndEmptyAttribute(Element element, String attributeName) throws ValidationException {
        return checkNullAndEmptyAttribute(element, attributeName, Namespace.NO_NAMESPACE);
    }

    /**
     * Same as {@link #checkNullAndEmptyAttribute(Element, String)} with a namespace.
     * 
     * @param element
     * @param attributeName
     * @param namespace
     * @return
     * @throws ValidationException
     */
    public static String checkNullAndEmptyAttribute(Element element, String attributeName, Namespace namespace)
        throws ValidationException {
        String attributeValue = element.getAttributeValue(attributeName, namespace);
        if (attributeValue == null || attributeValue.equals("")) {
            ValidatorUtil.throwException(element, "Missing or empty @" + attributeName + " attribute.");
        }
        return attributeValue;
    }

    /**
     * Checks if the parent element has a child with the given name. Throws a {@link ValidationException}
     * if there is no such element.
     * 
     * @param parent
     * @param elementName
     * @return return the first element with the elementName
     * @throws ValidationException
     */
    public static Element checkElement(Element parent, String elementName) throws ValidationException {
        Element fptr = parent.getChild(elementName, ValidatorUtil.METS);
        if (fptr == null) {
            ValidatorUtil.throwException(parent, "Missing '" + elementName + "' element.");
        }
        return fptr;
    }

    /**
     * Checks if the parent element has a child with the given name. Throws a {@link ValidationException}
     * if there is no such element.
     * 
     * @param parent
     * @param elementName
     * @return a list of all elements with the elementName
     * @throws ValidationException
     */
    public static List<Element> checkElements(Element parent, String elementName) throws ValidationException {
        List<Element> elements = parent.getChildren(elementName, ValidatorUtil.METS);
        if (elements.isEmpty()) {
            ValidatorUtil.throwException(parent, parent.getName() + " should contain at least one '" + elementName
                + "' element.");
        }
        return elements;
    }

    /**
     * Helper method to get the structMap[@TYPE='LOGICAL'] element of the mets document.
     * 
     * @param mets
     * @return the element or null
     */
    public static Element getLogicalStructMap(Element mets) {
        return getStructMap(mets, "LOGICAL");
    }

    /**
     * Helper method to get the structMap[@TYPE='LOGICAL'] element of the mets document.
     * 
     * @param mets
     * @return the element or null
     */
    public static Element getPhysicalStructMap(Element mets) {
        return getStructMap(mets, "PHYSICAL");
    }

    public static Element getStructMap(Element mets, String type) {
        List<Element> structMaps = mets.getChildren("structMap", ValidatorUtil.METS);
        for (Element sm : structMaps) {
            if (type.equals(sm.getAttributeValue("TYPE"))) {
                return sm;
            }
        }
        return null;
    }

}

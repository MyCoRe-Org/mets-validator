package org.mycore.mets.validator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.validator.validators.ValidationException;

import java.util.List;

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
     * Checks if the given xpath is valid.
     * 
     * @param parent
     * @param xpath
     * @param filter
     * @return
     * @throws ValidationException
     */
    public static <T> T checkXPath(Element parent, String xpath, Filter<T> filter) throws ValidationException {
        XPathExpression<T> compile = XPathFactory.instance().compile(xpath, filter, null, ValidatorUtil.METS);
        try {
            T firstValue = compile.evaluateFirst(parent);
            if (firstValue == null) {
                ValidatorUtil.throwException(parent, "Invalid xpath " + xpath);
            }
            return firstValue;
        } catch (Exception exc) {
            ValidatorUtil.throwException(parent, "Invalid xpath " + xpath);
        }
        return null;
    }

    /**
     * Checks if the element contains an attribute with the given name.
     * Throws an exception if there is no such attribute.
     * 
     * @param element the elment to check
     * @param attributeName name of the attribute
     * @throws ValidationException the attribute does not exists
     * @return the value of the attribute
     */
    public static String checkNullAttribute(Element element, String attributeName) throws ValidationException {
        String attributeValue = element.getAttributeValue(attributeName);
        if (attributeValue == null) {
            ValidatorUtil.throwException(element, "Missing @" + attributeName + " attribute.");
        }
        return attributeValue;
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

    public static Boolean hasLinkedChildren(Element mets, String logicalId) throws ValidationException {
        Element logicalDiv = getDivByLogicalId(mets, logicalId);
        List<Element> children = logicalDiv.getChildren();

        if (hasLinkedChildren(mets, children, getSmLinks(mets.getChild("structLink", METS)))) {
            return true;
        }

        return false;
    }

    private static boolean hasLinkedChildren(Element mets, List<Element> children, Multimap<String, String> smLinks)
        throws ValidationException {
        // First check if any child is direct linked
        for (Element child : children) {
            String id = child.getAttributeValue("ID");
            if (smLinks.containsKey(id)) {
                return true;
            }
        }

        for (Element child : children) {
            if (hasLinkedChildren(mets, child.getChildren(), smLinks)) {
                return true;
            }
        }
        return false;
    }

    private static Element getDivByLogicalId(Element mets, String logicalId) {
        XPathExpression<Element> elementXPathExpression;
        String xpathString = "mets:structMap[@TYPE='LOGICAL']//mets:div[@ID='" + logicalId + "']";
        elementXPathExpression = XPathFactory.instance().compile(xpathString, Filters.element(), null, METS);
        return elementXPathExpression.evaluateFirst(mets);
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

    public static Multimap<String, String> getSmLinks(Element structLink) throws ValidationException {
        HashMultimap<String, String> map = HashMultimap.create();
        List<Element> smLinks = checkElements(structLink, "smLink");
        for (Element smLink : smLinks) {
            String from = checkNullAndEmptyAttribute(smLink, "from", XLINK);
            String to = checkNullAndEmptyAttribute(smLink, "to", XLINK);
            map.put(from, to);
        }
        return map;
    }
}

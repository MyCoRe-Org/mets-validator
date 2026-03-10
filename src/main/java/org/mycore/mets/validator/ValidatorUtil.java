package org.mycore.mets.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.validator.validators.ValidationException;

/**
 * Some utility methods required for the validation.
 * 
 * @author Matthias Eichner
 */
public abstract class ValidatorUtil {

    /** The METS namespace. */
    public static Namespace METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

    /** The XLink namespace. */
    public static Namespace XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ValidatorUtil() {
    }

    /**
     * Throws a {@link ValidationException} with the given message.
     * 
     * @param e element where the exception appeared
     * @param message message of what went wrong
     * @throws ValidationException the validation exception
     */
    public static void throwException(Element e, String message) throws ValidationException {
        if (e instanceof LocatedElement le) {
            throw new ValidationException(message, le.getLine());
        }
        throw new ValidationException(message);
    }

    /**
     * Checks if the given xpath is valid.
     *
     * @param <T> the type of the result
     * @param parent the parent element to evaluate the xpath against
     * @param xpath the xpath expression to evaluate
     * @param filter the filter to apply to the xpath result
     * @return the first matching result
     * @throws ValidationException if the xpath yields no result or evaluation fails
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
     * @param element the element to check
     * @param attributeName name of the attribute
     * @return the value of the attribute
     * @throws ValidationException if the attribute is missing or empty
     */
    public static String checkNullAndEmptyAttribute(Element element, String attributeName) throws ValidationException {
        return checkNullAndEmptyAttribute(element, attributeName, Namespace.NO_NAMESPACE);
    }

    /**
     * Same as {@link #checkNullAndEmptyAttribute(Element, String)} with a namespace.
     *
     * @param element the element to check
     * @param attributeName name of the attribute
     * @param namespace the namespace of the attribute
     * @return the value of the attribute
     * @throws ValidationException if the attribute is missing or empty
     */
    public static String checkNullAndEmptyAttribute(Element element, String attributeName, Namespace namespace)
        throws ValidationException {
        String attributeValue = element.getAttributeValue(attributeName, namespace);
        if (attributeValue == null || attributeValue.isEmpty()) {
            ValidatorUtil.throwException(element, "Missing or empty @" + attributeName + " attribute.");
        }
        return attributeValue;
    }

    /**
     * Checks if a present attribute with the given name is not empty.
     *
     * @param element the element to check
     * @param attrName name of the attribute
     * @return the value of the attribute, or null if the attribute is absent
     * @throws ValidationException if the attribute is present but empty
     */
    public static String checkEmptyAttribute(Element element, String attrName) throws ValidationException {
        String attributeValue = element.getAttributeValue(attrName);
        if (element.getAttribute(attrName) != null && attributeValue.isEmpty()) {
            ValidatorUtil.throwException(element, element.getName() + " " + attrName + " is present but empty!");
        }
        return attributeValue;
    }

    /**
     * Checks if the parent element has a child with the given name. Throws a {@link ValidationException}
     * if there is no such element.
     *
     * @param parent the parent element
     * @param elementName name of the element
     * @return return the first element with the elementName
     * @throws ValidationException if there is no element with given name
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
     * @param parent the parent element
     * @param elementName name of the element
     * @return a list of all elements with the elementName
     * @throws ValidationException if there is no element with given name
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
     * @param mets the root mets element
     * @return the element or null
     */
    public static Element getLogicalStructMap(Element mets) {
        return getStructMap(mets, "LOGICAL");
    }

    /**
     * Returns whether the logical div with the given ID has any children that are linked via smLink.
     *
     * @param mets the root mets element
     * @param logicalId the ID of the logical div to check
     * @return true if any descendant is linked, false otherwise
     * @throws ValidationException if the structLink section is invalid
     */
    public static Boolean hasLinkedChildren(Element mets, String logicalId) throws ValidationException {
        Element logicalDiv = getDivByLogicalId(mets, logicalId);
        List<Element> children = logicalDiv.getChildren();
        return hasLinkedChildren(children, getSmLinks(mets.getChild("structLink", METS)));
    }

    private static boolean hasLinkedChildren(List<Element> children, Map<String, Set<String>> smLinks) {
        // First check if any child is direct linked
        for (Element child : children) {
            String id = child.getAttributeValue("ID");
            if (smLinks.containsKey(id)) {
                return true;
            }
        }
        for (Element child : children) {
            if (hasLinkedChildren(child.getChildren(), smLinks)) {
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
     * Helper method to get the structMap[@TYPE='PHYSICAL'] element of the mets document.
     *
     * @param mets the root mets element
     * @return the element or null
     */
    public static Element getPhysicalStructMap(Element mets) {
        return getStructMap(mets, "PHYSICAL");
    }

    /**
     * Returns the structMap element with the given TYPE attribute.
     *
     * @param mets the root mets element
     * @param type the value of the TYPE attribute to look for
     * @return the matching structMap element, or null if not found
     */
    public static Element getStructMap(Element mets, String type) {
        List<Element> structMaps = mets.getChildren("structMap", ValidatorUtil.METS);
        for (Element sm : structMaps) {
            if (type.equals(sm.getAttributeValue("TYPE"))) {
                return sm;
            }
        }
        return null;
    }

    /**
     * Parses all smLink elements and returns a map from logical ID (xlink:from) to a set of physical IDs (xlink:to).
     *
     * @param structLink the mets:structLink element
     * @return a map of logical IDs to sets of linked physical IDs
     * @throws ValidationException if any smLink is missing required attributes
     */
    public static Map<String, Set<String>> getSmLinks(Element structLink) throws ValidationException {
        Map<String, Set<String>> map = new HashMap<>();
        List<Element> smLinks = checkElements(structLink, "smLink");
        for (Element smLink : smLinks) {
            String from = checkNullAndEmptyAttribute(smLink, "from", XLINK);
            String to = checkNullAndEmptyAttribute(smLink, "to", XLINK);
            map.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        }
        return map;
    }

}

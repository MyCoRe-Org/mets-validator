package org.mycore.mets.validator.validators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import org.mycore.mets.validator.ValidatorUtil;

/**
 * Validates the struct link section of a METS document.
 *
 * @author Matthias Eichner
 */
public class StructLinkValidator implements Validator {

    /**
     * Creates a new StructLinkValidator.
     */
    public StructLinkValidator() {
    }

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element structLink = ValidatorUtil.checkElement(mets, "structLink");

        List<String> physicalIds = getPhysicalIds(mets);
        List<String> logicalIds = getLogicalIds(mets);

        Map<String, Set<String>> smLinks = ValidatorUtil.getSmLinks(structLink);

        // flatten "to" side (all linked physical IDs)
        Set<String> linkedPhysicalIds = new HashSet<>();
        for (Set<String> tos : smLinks.values()) {
            linkedPhysicalIds.addAll(tos);
        }

        // check missing
        ArrayList<String> missingLogicalDivs = new ArrayList<>(logicalIds);
        missingLogicalDivs.removeAll(smLinks.keySet());

        ArrayList<String> missingPhysicalDivs = new ArrayList<>(physicalIds);
        missingPhysicalDivs.removeAll(linkedPhysicalIds);

        // check if children are linked
        List<String> foundLogicalDivs = new ArrayList<>();
        for (String missingLogicalDiv : missingLogicalDivs) {
            if (ValidatorUtil.hasLinkedChildren(mets, missingLogicalDiv)) {
                foundLogicalDivs.add(missingLogicalDiv);
            }
        }
        missingLogicalDivs.removeAll(foundLogicalDivs);

        if (!missingLogicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some logical elements are not linked: " + missingLogicalDivs);
        }
        if (!missingPhysicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some physical elements are not linked: " + missingPhysicalDivs);
        }

        // check not existing
        ArrayList<String> notExistingLogicalDivs = new ArrayList<>(smLinks.keySet());
        notExistingLogicalDivs.removeAll(logicalIds);
        if (!notExistingLogicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some linked logical elements does not exist: " + notExistingLogicalDivs);
        }

        ArrayList<String> notExistingPhysicalDivs = new ArrayList<>(linkedPhysicalIds);
        notExistingPhysicalDivs.removeAll(physicalIds);
        if (!notExistingPhysicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some linked physical elements does not exist: " + notExistingPhysicalDivs);
        }
    }

    private List<String> getLogicalIds(Element mets) {
        Element logicalStructMap = ValidatorUtil.getLogicalStructMap(mets);
        return fillIds(logicalStructMap);
    }

    private List<String> getPhysicalIds(Element mets) throws ValidationException {
        Element physicalStructMap = ValidatorUtil.getPhysicalStructMap(mets);
        Element physSequence = ValidatorUtil.checkElement(physicalStructMap, "div");
        if (physSequence != null) {
            return fillIds(physSequence);
        }
        return new ArrayList<>();
    }

    private List<String> fillIds(Element logicalStructMap) {
        List<String> ids = new ArrayList<>();
        IteratorIterable<Element> divsIterator = logicalStructMap.getDescendants(
            new ElementFilter("div", ValidatorUtil.METS));
        while (divsIterator.hasNext()) {
            Element div = divsIterator.next();
            String id = div.getAttributeValue("ID");
            if (id != null && !id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

}

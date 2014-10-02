package org.mycore.mets.validator.validators;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import org.mycore.mets.validator.ValidatorUtil;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class StructLinkValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element structLink = ValidatorUtil.checkElement(mets, "structLink");

        List<String> physicalIds = getPhysicalIds(mets);
        List<String> logicalIds = getLogicalIds(mets);

        Multimap<String, String> smLinks = getSmLinks(structLink);

        // check missing
        ArrayList<String> missingLogicalDivs = new ArrayList<String>(logicalIds);
        missingLogicalDivs.removeAll(smLinks.keys());

        ArrayList<String> missingPhysicallDivs = new ArrayList<String>(physicalIds);
        missingPhysicallDivs.removeAll(smLinks.values());

        if (!missingLogicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some logical elements are not linked: " + missingLogicalDivs.toString());
        }
        if (!missingPhysicallDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink,
                "Some physical elements are not linked: " + missingPhysicallDivs.toString());
        }

        // check not existing
        ArrayList<String> notExistingLogicalDivs = new ArrayList<String>(smLinks.keys());
        notExistingLogicalDivs.removeAll(logicalIds);
        if (!notExistingLogicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink, "Some linked logical elements does not exist: "
                + notExistingLogicalDivs.toString());
        }

        ArrayList<String> notExistingPhysicalDivs = new ArrayList<String>(smLinks.values());
        notExistingPhysicalDivs.removeAll(physicalIds);
        if (!notExistingPhysicalDivs.isEmpty()) {
            ValidatorUtil.throwException(structLink, "Some linked physical elements does not exist: "
                + notExistingPhysicalDivs.toString());
        }
    }

    private Multimap<String, String> getSmLinks(Element structLink) throws ValidationException {
        HashMultimap<String, String> map = HashMultimap.create();
        List<Element> smLinks = ValidatorUtil.checkElements(structLink, "smLink");
        for (Element smLink : smLinks) {
            String from = ValidatorUtil.checkNullAndEmptyAttribute(smLink, "from", ValidatorUtil.XLINK);
            String to = ValidatorUtil.checkNullAndEmptyAttribute(smLink, "to", ValidatorUtil.XLINK);
            map.put(from, to);
        }
        return map;
    }

    private List<String> getLogicalIds(Element mets) {
        List<String> ids = new ArrayList<String>();
        Element logicalStructMap = ValidatorUtil.getLogicalStructMap(mets);
        IteratorIterable<Element> divsIterator = logicalStructMap.getDescendants(new ElementFilter("div",
            ValidatorUtil.METS));
        while (divsIterator.hasNext()) {
            Element div = divsIterator.next();
            String id = div.getAttributeValue("ID");
            if (id != null && !id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private List<String> getPhysicalIds(Element mets) throws ValidationException {
        List<String> ids = new ArrayList<String>();
        Element physicalStructMap = ValidatorUtil.getPhysicalStructMap(mets);
        Element physSequence = ValidatorUtil.checkElement(physicalStructMap, "div");
        if (physSequence != null) {
            IteratorIterable<Element> divsIterator = physSequence.getDescendants(new ElementFilter("div",
                ValidatorUtil.METS));
            while (divsIterator.hasNext()) {
                Element div = divsIterator.next();
                String id = div.getAttributeValue("ID");
                if (id != null && !id.isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

}

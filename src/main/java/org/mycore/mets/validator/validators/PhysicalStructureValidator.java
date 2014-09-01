package org.mycore.mets.validator.validators;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.mets.validator.ValidatorUtil;

public class PhysicalStructureValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element physicalStructMap = ValidatorUtil.getPhysicalStructMap(mets);
        if (physicalStructMap == null) {
            ValidatorUtil.throwException(mets, "Missing mets:structMap TYPE='PHYSICAL' element.");
        }
        Element mainDiv = physicalStructMap.getChild("div", ValidatorUtil.METS);
        if (mainDiv == null) {
            ValidatorUtil.throwException(physicalStructMap,
                "Physical struct map should contain sourrounding mets:div TYPE='physSequence' element.");
        }
        if (!"physSequence".equals(mainDiv.getAttributeValue("TYPE"))) {
            ValidatorUtil
                .throwException(mainDiv, "Missing or invalid @TYPE attribute. Should be @TYPE='physSequence'.");
        }

        List<Element> children = mainDiv.getChildren("div", ValidatorUtil.METS);
        if (children.isEmpty()) {
            ValidatorUtil.throwException(mainDiv, "mets:div TYPE='physSequence' should have at least one page.");
        }
        for (Element div : children) {
            if (!"page".equals(div.getAttributeValue("TYPE"))) {
                ValidatorUtil.throwException(div, "Invalid @TYPE attribute. Should be @TYPE='page'.");
            }
            if (div.getAttributeValue("ID") == null) {
                ValidatorUtil.throwException(div, "div requires @ID attribute.");
            }
            if (div.getAttributeValue("ORDER") == null) {
                ValidatorUtil.throwException(div, "div requires @ORDER attribute.");
            }
            List<Element> fptrs = div.getChildren("fptr", ValidatorUtil.METS);
            if (fptrs.isEmpty()) {
                ValidatorUtil.throwException(div, "div should contain at least one mets:fptr @FILEID element.");
            }
            for (Element fptr : fptrs) {
                if (fptr.getAttributeValue("FILEID") == null) {
                    ValidatorUtil.throwException(fptr, "fptr requires @FILEID attribute.");
                }
            }
        }
    }

}

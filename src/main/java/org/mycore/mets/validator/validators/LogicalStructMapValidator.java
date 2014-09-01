package org.mycore.mets.validator.validators;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import org.mycore.mets.validator.ValidatorUtil;

public class LogicalStructMapValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element logicalStructMap = ValidatorUtil.getLogicalStructMap(mets);
        if (logicalStructMap == null) {
            ValidatorUtil.throwException(mets, "Missing mets:structMap TYPE='LOGICAL' element.");
        }

        // check surrounding div
        Element surroundingDiv = ValidatorUtil.checkElement(logicalStructMap, "div");
        ValidatorUtil.checkNullAttribute(surroundingDiv, "TYPE");
        ValidatorUtil.checkNullAndEmptyAttribute(surroundingDiv, "LABEL");

        // check all div
        IteratorIterable<Element> divsIterator = surroundingDiv.getDescendants(new ElementFilter("div",
            ValidatorUtil.METS));
        while (divsIterator.hasNext()) {
            Element div = divsIterator.next();
            ValidatorUtil.checkNullAttribute(div, "TYPE");
            ValidatorUtil.checkNullAndEmptyAttribute(div, "LABEL");
            ValidatorUtil.checkNullAttribute(div, "ORDER");
            Element fptr = div.getChild("fptr", ValidatorUtil.METS);
            if (fptr != null) {
                Element seq = ValidatorUtil.checkElement(fptr, "seq");
                List<Element> areas = ValidatorUtil.checkElements(seq, "area");
                for (Element area : areas) {
                    ValidatorUtil.checkNullAttribute(area, "FILEID");
                    ValidatorUtil.checkNullAttribute(area, "BEGIN");
                    ValidatorUtil.checkNullAttribute(area, "END");
                    if (area.getAttributeValue("BETYPE") == null || !area.getAttributeValue("BETYPE").equals("IDREF")) {
                        ValidatorUtil.throwException(area, "@BETYPE attribute required. Should be @BETYPE='IDREF'");
                    }
                }
            }
        }
    }

}

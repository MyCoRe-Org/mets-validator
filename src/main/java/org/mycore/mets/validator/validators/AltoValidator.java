package org.mycore.mets.validator.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.ValidatorUtil;

/**
 * Validates the alto parts of a mets xml. This validator is optional. You can add
 * it to the validation process by calling {@link METSValidator#getValidators()}
 * 
 * @author Matthias Eichner
 */
public class AltoValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element altoFileGroup = ValidatorUtil.checkXPath(mets, "./mets:fileSec/mets:fileGrp[@USE='ALTO']",
            Filters.element());
        List<Element> files = ValidatorUtil.checkElements(altoFileGroup, "file");
        // get all alto file id's
        List<String> altoFileIds = new ArrayList<>();
        for (Element file : files) {
            altoFileIds.add(ValidatorUtil.checkNullAndEmptyAttribute(file, "ID"));
        }
        // check if they are referenced in the logical structure
        Element logicalStructMap = ValidatorUtil.getLogicalStructMap(mets);
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", null);
        XPathExpression<Element> idExp = XPathFactory.instance().compile(
            "./mets:div//mets:fptr/mets:seq/mets:area[@FILEID=$id]", Filters.element(), variables, ValidatorUtil.METS);
        for (String id : altoFileIds) {
            idExp.setVariable("id", id);
            if (idExp.evaluateFirst(logicalStructMap) == null) {
                throw new ValidationException("Unable to find the alto id '" + id + "' in the logical struct map.");
            }
        }
    }

}

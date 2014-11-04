package org.mycore.mets.validator.validators;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.mets.validator.ValidatorUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Validates the file section.
 * 
 * @author Matthias Eichner
 */
public class FileSectionValidator implements Validator {

    @Override
    public void validate(Document document) throws ValidationException {
        Element mets = document.getRootElement();
        Element fileSec = mets.getChild("fileSec", ValidatorUtil.METS);
        if (fileSec == null) {
            throw new ValidationException("No mets:fileSec element.");
        }
        Element masterFileGrp = getFileGroup(fileSec, "MASTER");
        if (masterFileGrp == null) {
            throw new ValidationException("No fileGrp with @USE='MASTER'.");
        }
        validateMasterFileGroup(masterFileGrp);
        Element altoFileGrp = getFileGroup(fileSec, "ALTO");
        if (altoFileGrp != null) {
            validateAltoFileGroup(altoFileGrp);
        }
    }

    private void validateAltoFileGroup(Element altoFileGrp) throws ValidationException {
        List<Element> files = altoFileGrp.getChildren("file", ValidatorUtil.METS);
        if (files.isEmpty()) {
            throw new ValidationException("FileGrp[@USE='ALTO'] does not contain any 'mets:file' elements.");
        }
        for (Element file : files) {
            validateFileId(file);
            validateMimeType(file, "/xml");
            String href = validateFLocat(file);
            if (!href.startsWith("alto/")) {
                ValidatorUtil.throwException(file, "alto file is not placed in 'alto/' directory.");
            }
        }
    }

    private void validateMasterFileGroup(Element masterFileGrp) throws ValidationException {
        List<Element> files = masterFileGrp.getChildren("file", ValidatorUtil.METS);
        if (files.isEmpty()) {
            throw new ValidationException("FileGrp[@USE='MASTER'] does not contain any 'mets:file' elements.");
        }
        for (Element file : files) {
            validateFileId(file);
            validateMimeType(file, "image/");
            validateFLocat(file);
        }
    }

    private String validateFLocat(Element file) throws ValidationException {
        Element flocat = file.getChild("FLocat", ValidatorUtil.METS);
        if (flocat == null) {
            ValidatorUtil.throwException(file, "mets:file has no mets:FLocat element.");
        }
        String href = flocat.getAttributeValue("href", ValidatorUtil.XLINK);
        URI uri = null;
        try {
            uri = new URI(href);
        } catch (URISyntaxException uriSyntaxException) {
            ValidatorUtil.throwException(flocat, "invalid @xlink:href uri.");
        }
        if (uri.isAbsolute()) {
            ValidatorUtil.throwException(flocat, "mets:FLocat should be a relative uri.");
        }
        return href;
    }

    private void validateMimeType(Element file, String expectedMimeType) throws ValidationException {
        String mimeType = file.getAttributeValue("MIMETYPE");
        if (mimeType == null) {
            ValidatorUtil.throwException(file, "mets:file has no @MIMETYPE.");
        }
        if (!mimeType.startsWith(expectedMimeType) && !mimeType.endsWith(expectedMimeType)) {
            ValidatorUtil.throwException(file, "mets:file has an invalid mimetype. Should start with '"
                + expectedMimeType + "' but is '" + mimeType + "'");
        }
    }

    private void validateFileId(Element file) throws ValidationException {
        String id = file.getAttributeValue("ID");
        if (id == null) {
            ValidatorUtil.throwException(file, "mets:file has no @ID.");
        }
    }

    private Element getFileGroup(Element fileSec, String use) {
        for (Element fileGrp : fileSec.getChildren("fileGrp", ValidatorUtil.METS)) {
            if (use.equals(fileGrp.getAttributeValue("USE"))) {
                return fileGrp;
            }
        }
        return null;
    }

}

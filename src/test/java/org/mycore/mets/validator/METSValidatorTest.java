/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.mets.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.mets.validator.validators.ValidationException;

public class METSValidatorTest {

    @Test
    public void testJVBMets() throws IOException, JDOMException {
        InputStream jvbMetsInputStream = METSValidatorTest.class.getResourceAsStream("/jvb_mets.xml");
        METSValidator validator = new METSValidator(jvbMetsInputStream);
        List<ValidationException> errors = validator.validate();
        if (!errors.isEmpty()) {
            RuntimeException combined = new RuntimeException(
                "METS validation failed with " + errors.size() + " error(s)"
            );
            errors.forEach(combined::addSuppressed);
            Assertions.fail(combined);
        }
    }

}

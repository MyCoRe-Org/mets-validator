# METS Validator

A Java library for validating [METS (Metadata Encoding and Transmission Standard)](https://www.loc.gov/standards/mets/) documents.

## Features

Validates METS documents against multiple criteria:

- **Schema validation** — validates against METS schema 1.12.1
- **File section validation** — checks the `fileSec` structure
- **Physical structure validation** — validates the `structMap[@TYPE='PHYSICAL']`
- **Logical struct map validation** — validates the `structMap[@TYPE='LOGICAL']`
- **Struct link validation** — validates `structLink` / `smLink` references between logical and physical structures
- **ALTO validation** — validates linked ALTO files

Line numbers are reported in validation errors where available.

## Requirements

- Java 17+
- Maven

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.mycore.mets</groupId>
    <artifactId>mets-validator</artifactId>
    <version>1.4</version>
</dependency>
```

## Usage

### Validate from an InputStream

```java
try (InputStream is = new FileInputStream("my-mets.xml")) {
    METSValidator validator = new METSValidator(is);
    List<ValidationException> errors = validator.validate();

    if (errors.isEmpty()) {
        System.out.println("METS document is valid.");
    } else {
        errors.forEach(e -> System.err.println(e.getMessage()));
    }
}
```

### Validate from a JDOM Document

```java
Document doc = /* ... your JDOM document ... */;
METSValidator validator = new METSValidator(doc);
List<ValidationException> errors = validator.validate();
```

### Custom validators

You can add or replace validators:

```java
METSValidator validator = new METSValidator(is);
validator.getValidators().add(new MyCustomValidator());
List<ValidationException> errors = validator.validate();
```

All validators implement the `Validator` interface:

```java
public interface Validator {
    void validate(Document document) throws ValidationException;
}
```

## Building

```bash
mvn clean install
```

## License

This project is part of MyCoRe and is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).

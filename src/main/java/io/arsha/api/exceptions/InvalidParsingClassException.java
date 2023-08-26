package io.arsha.api.exceptions;

public class InvalidParsingClassException extends AbstractException {
    public InvalidParsingClassException(String message) {
        super(500, ExceptionCode.INVALID_PARSING_CLASS, message);
    }
}

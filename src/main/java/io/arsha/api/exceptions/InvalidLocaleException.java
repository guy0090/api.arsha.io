package io.arsha.api.exceptions;

public class InvalidLocaleException extends AbstractException {

    public InvalidLocaleException(String locale) {
        super(400, ExceptionCode.INVALID_LOCALE);
        this.message = String.format("Invalid locale: %s", locale);
    }

}

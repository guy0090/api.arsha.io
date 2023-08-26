package io.arsha.api.exceptions;

import java.util.List;

public class IdAndSidMismatchException extends AbstractException {

    public IdAndSidMismatchException(List<Long> id, List<Long> sid) {
        super(400, ExceptionCode.ID_SID_MISMATCH,
                String.format("The number of IDs (%s) and SIDs (%s) do not match", id.size(), sid.size()));
    }

}

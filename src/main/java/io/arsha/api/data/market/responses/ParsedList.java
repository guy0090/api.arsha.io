package io.arsha.api.data.market.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.arsha.api.data.market.IMarketResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class ParsedList extends ArrayList<ParsedList.ParsedItem> implements IMarketResponse {

    public ParsedList(ParsedItem item) {
        super(1);
        add(item);
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class ParsedItem {
        String name;
        Long id;
        Long sid;
    }
}
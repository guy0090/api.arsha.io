package io.arsha.api.data.scraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
public class ScrapedItem {
    Long id;
    String name;
    Integer grade;

    public String stringId() {
        return String.valueOf(id);
    }
}

package io.arsha.api.data.scraper;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.util.List;

@Builder
@Jacksonized
public record CodexData(List<List<String>> aaData) {

    public static ScrapedItem buildItem(List<String> fields) {
        var id = Long.valueOf(fields.get(0));
        var name = Jsoup.parse(fields.get(2)).text();
        var grade = Integer.valueOf(fields.get(fields.size() - 2));

        return ScrapedItem.builder()
                .id(id)
                .name(Parser.unescapeEntities(name, true))
                .grade(grade)
                .build();
    }
}

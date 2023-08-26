package io.arsha.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MarketCategoriesConfiguration {

    @Bean
    @SneakyThrows
    public MarketCategories marketCategories(@Value("classpath:categories.json") Resource categoriesFile) {
        var mapper = new ObjectMapper();
        return mapper.readValue(categoriesFile.getInputStream(), MarketCategories.class);
    }

    public static class MarketCategories extends ArrayList<MarketCategory> {
    }

    public record MarketCategory(String name, Long id, List<MarketSubCategory> subCategories) {
    }

    public record MarketSubCategory(String name, Long id) {
    }
}

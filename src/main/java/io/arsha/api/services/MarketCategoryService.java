package io.arsha.api.services;

import io.arsha.api.config.MarketCategoriesConfiguration.*;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MarketCategoryService {

    private final MarketCategories marketCategories;

    public List<Long> getMainCategories() {
        return marketCategories.stream()
                .map(MarketCategory::id)
                .toList();
    }

    public List<Long> getSubCategories(Long mainCategory) {
        var category = marketCategories.stream()
                .filter(c -> Objects.equals(c.id(), mainCategory))
                .findFirst()
                .orElseThrow();

        return category.subCategories().stream()
                .map(MarketSubCategory::id)
                .toList();
    }

    public boolean isValidCategory(Long category) {
        return marketCategories.stream()
                .anyMatch(c -> Objects.equals(c.id(), category));
    }

    public boolean isValidCombination(Long mainCategory, @Nullable Long subCategory) {
        if (!isValidCategory(mainCategory)) return false;

        return subCategory == null || getSubCategories(mainCategory).stream()
                .anyMatch(s -> s.equals(subCategory));
    }

    public SortedMap<Long, List<Long>> getCombinations() {
        return marketCategories.stream()
                .collect(Collectors.toMap(MarketCategory::id, c -> c.subCategories().stream()
                        .map(MarketSubCategory::id)
                        .toList(), (a, b) -> b, TreeMap::new));
    }
}

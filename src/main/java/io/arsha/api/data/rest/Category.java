package io.arsha.api.data.rest;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode
public class Category {
    @NotNull
    Long mainCategory;
    @Nullable @Min(1)
    Long subCategory;

    public Category(@NonNull Long mainCategory) {
        this.mainCategory = mainCategory;
    }
}

package io.arsha.api.data.market.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.stream.Collectors;

@ToString
@AllArgsConstructor
public enum GameRegion {

    NORTH_AMERICA("na"),
    EUROPE("eu"),
    SOUTH_EAST_ASIA("sea"),
    MIDDLE_EAST_NORTHERN_AFRICA("mena"),
    KOREA("kr"),
    RUSSIA("ru"),
    JAPAN("jp"),
    THAILAND("th"),
    TAIWAN("tw"),
    SOUTH_AMERICA("sa"),
    GLOBAL_LAB("gl"),
    CONSOLE_NA("console_na"),
    CONSOLE_EU("console_eu"),
    CONSOLE_ASIA("console_asia");

    @Getter
    final String region;

    public static final String REGIONS = Arrays.stream(GameRegion.values())
            .map(GameRegion::getRegion)
            .collect(Collectors.joining(", "));

    public static GameRegion fromValue(String region) {
        for (GameRegion gameRegion : GameRegion.values()) {
            if (gameRegion.region.equals(region.toLowerCase())) {
                return gameRegion;
            }
        }
        return null;
    }
}

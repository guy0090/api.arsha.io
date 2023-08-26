package io.arsha.api.config.services;

import io.arsha.api.data.market.common.GameRegion;
import io.arsha.api.config.properties.MarketProperties;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MarketConfigurationService {

    private final MarketProperties marketProperties;

    public MarketProperties.Market getMarketRegion(GameRegion region) {
        return switch (region) {
            case NORTH_AMERICA -> marketProperties.na;
            case EUROPE -> marketProperties.eu;
            case SOUTH_EAST_ASIA -> marketProperties.sea;
            case MIDDLE_EAST_NORTHERN_AFRICA -> marketProperties.mena;
            case KOREA -> marketProperties.kr;
            case RUSSIA -> marketProperties.ru;
            case JAPAN -> marketProperties.jp;
            case THAILAND -> marketProperties.th;
            case TAIWAN -> marketProperties.tw;
            case SOUTH_AMERICA -> marketProperties.sa;
            case GLOBAL_LAB -> marketProperties.gl;
            case CONSOLE_NA -> marketProperties.consoleNa;
            case CONSOLE_EU -> marketProperties.consoleEu;
            case CONSOLE_ASIA -> marketProperties.consoleAsia;
        };
    }

    public boolean isEnabled(GameRegion region) {
        return getMarketRegion(region).isEnabled();
    }

    public void disableMarketRegion(GameRegion region) {
        getMarketRegion(region).setEnabled(false);
    }

    public void enableMarketRegion(GameRegion region) {
        getMarketRegion(region).setEnabled(true);
    }

}

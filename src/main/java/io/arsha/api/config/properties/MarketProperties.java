package io.arsha.api.config.properties;

import io.arsha.api.data.market.common.MarketConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "marketplace")
public class MarketProperties {

    public final Market na = new Market(MarketConstants.NA_URL);
    public final Market eu = new Market(MarketConstants.EU_URL);
    public final Market sea = new Market(MarketConstants.SEA_URL);
    public final Market mena = new Market(MarketConstants.MENA_URL);
    public final Market kr = new Market(MarketConstants.KR_URL);
    public final Market ru = new Market(MarketConstants.RU_URL);
    public final Market jp = new Market(MarketConstants.JP_URL);
    public final Market th = new Market(MarketConstants.TH_URL);
    public final Market tw = new Market(MarketConstants.TW_URL);
    public final Market sa = new Market(MarketConstants.SA_URL);
    public final Market gl = new Market(MarketConstants.GL_URL);
    public final Market consoleNa = new Market(MarketConstants.CONSOLE_NA_URL);
    public final Market consoleEu = new Market(MarketConstants.CONSOLE_EU_URL);
    public final Market consoleAsia = new Market(MarketConstants.CONSOLE_ASIA_URL);

    @AllArgsConstructor
    public static class Market {

        @Getter @Setter
        boolean enabled;
        @Getter
        final String url;

        public Market(String url) {
            this(true, url);
        }
    }
}

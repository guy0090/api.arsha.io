package io.arsha.api.data.market;

import io.arsha.api.data.market.common.GameRegion;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.requests.MarketRequestBody;

public record MarketRequest(GameRegion region, MarketEndpoint endpoint, MarketRequestBody body) {}

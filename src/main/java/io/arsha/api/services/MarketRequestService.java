package io.arsha.api.services;

import io.arsha.api.config.services.MarketConfigurationService;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketRequest;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.common.MarketConstants;
import io.arsha.api.data.market.requests.GetBiddingInfoListBody;
import io.arsha.api.data.market.requests.GetMarketPriceInfoBody;
import io.arsha.api.data.market.requests.GetWorldMarketHotListBody;
import io.arsha.api.data.market.requests.GetWorldMarketListBody;
import io.arsha.api.data.market.requests.GetWorldMarketSearchListBody;
import io.arsha.api.data.market.requests.GetWorldMarketSubListBody;
import io.arsha.api.data.market.requests.GetWorldMarketWaitListBody;
import io.arsha.api.data.market.requests.MarketRequestBody;
import io.arsha.api.exceptions.CannotBeRegisteredException;
import io.arsha.api.lib.HuffmanDecoder;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Service for sending requests to the market API and returning responses as JSON strings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRequestService {

    private final RestTemplate client;
    private final MarketConfigurationService marketConfigurationService;
    private final HuffmanDecoder huffmanDecoder;

    private Optional<MarketResponse> sendRequest(MarketRequest marketRequest) {
        var region = marketConfigurationService.getMarketRegion(marketRequest.region());
        var endpoint = marketRequest.endpoint();
        var url = String.format("https://%s/%s/%s", region.getUrl(), MarketConstants.BASE_PATH,
            endpoint.getPath());

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, MarketConstants.USER_AGENT);
        var request = new HttpEntity<>(marketRequest.body(), headers);

        try {
            log.debug("Sending request to {}", url);
            MarketResponse marketResponse;
            if (endpoint.isCompressed()) {
                var bytes = client.postForObject(url, request, byte[].class);
                var decoded = huffmanDecode(bytes);
                marketResponse = new MarketResponse(decoded);
            } else {
                marketResponse = client.postForObject(url, request, MarketResponse.class);
            }
            return Optional.ofNullable(marketResponse);
        } catch (CannotBeRegisteredException e) {
            return Optional.of(e.getResponse());
        } catch (Exception e) {
            log.error("Error sending request to {}", url, e);
            return Optional.empty();
        }
    }

    @Async("asyncExecutor")
    public CompletableFuture<Tuple2<CacheCompositeKey, Optional<MarketResponse>>> request(
        CacheCompositeKey key) {
        var primary = key.getPrimary();
        var secondary = key.getSecondary();
        var region = key.getRegion();
        var endpoint = key.getEndpoint();
        MarketRequestBody body = switch (endpoint) {
            case MARKET_LIST -> new GetWorldMarketListBody(primary, secondary);
            case MARKET_SUB_LIST -> new GetWorldMarketSubListBody(primary);
            case MARKET_HOT_LIST -> new GetWorldMarketHotListBody();
            case MARKET_WAIT_LIST -> new GetWorldMarketWaitListBody();
            case MARKET_PRICE_INFO -> new GetMarketPriceInfoBody(primary, secondary);
            case BIDDING_INFO_LIST -> new GetBiddingInfoListBody(primary, secondary);
            case MARKET_SEARCH_LIST -> new GetWorldMarketSearchListBody(key.getSearch());
        };

        var request = new MarketRequest(region, endpoint, body);
        var response = sendRequest(request);

        return CompletableFuture.completedFuture(Tuples.of(key, response));
    }

    public String huffmanDecode(byte[] data) throws CannotBeRegisteredException, IOException {
        var testString = new String(data);
        if (testString.contains("resultMsg")) {
            throw new CannotBeRegisteredException(testString);
        }
        return huffmanDecoder.decode(data);
    }
}

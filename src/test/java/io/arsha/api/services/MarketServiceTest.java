package io.arsha.api.services;

import io.arsha.api.MarketResponseFixtures;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.exceptions.MarketRequestException;
import io.arsha.api.lib.AppTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.util.function.Tuples;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AppTest
class MarketServiceTest {

    @Mock
    MarketRequestService marketRequestService;
    @Autowired
    MarketRedisService marketRedisService;
    @Autowired
    ScraperService scraperService;
    MarketService marketService;

    @BeforeEach
    public void setUp() {
        marketRedisService.flushDb();
        marketRequestService = mock(MarketRequestService.class);
    }

    @AfterEach
    public void tearDown() {
        marketRedisService.flushDb();
    }

    @Test
    void requestOne_shouldEqualFixture() throws MarketRequestException {
        var queue = MarketResponseFixtures.buildNoArgKey(MarketEndpoint.MARKET_WAIT_LIST);
        var response = MarketResponseFixtures.GetWorldMarketWaitListItem;
        var tuple = Tuples.of(queue, Optional.ofNullable(response));
        var future = CompletableFuture.completedFuture(tuple);

        when(marketRequestService.request(queue)).thenReturn(future);
        marketService = new MarketService(marketRequestService, marketRedisService, scraperService);

        var item = marketService.requestOne(queue);
        assertThat(item).isNotEmpty().hasSize(1);
        assertThat(item.get(0)).isEqualTo(response);
    }

    @Test
    void requestMany_resultSizeShouldEqualProvidedKeysSize() {
        var subItem = MarketResponseFixtures.buildIdKey(MarketResponseFixtures.item_10007, MarketEndpoint.MARKET_SUB_LIST);
        var response = MarketResponseFixtures.GetWorldMarketSearchListItem_10007;
        var tuple = Tuples.of(subItem, Optional.ofNullable(response));
        var future = CompletableFuture.completedFuture(tuple);

        when(marketRequestService.request(any())).thenReturn(future);
        marketService = new MarketService(marketRequestService, marketRedisService, scraperService);

        var keys = MarketResponseFixtures.buildIdKeys(MarketEndpoint.MARKET_SUB_LIST);
        var items = marketService.requestMany(keys);

        assertThat(items).isNotEmpty().hasSize(keys.size());
    }

    @Test
    void requestSearchResult_shouldReturnCombinedItem() throws MarketRequestException {
        // Precache one of the results
        var cachedItemKey = MarketResponseFixtures.buildSearchKey(MarketResponseFixtures.item_10006);
        marketRedisService.setEternal(cachedItemKey, MarketResponseFixtures.GetWorldMarketSearchListItem_10006);

        // Request the combined item (10007 and 10006)
        var missingItem = MarketResponseFixtures.buildSearchKey(MarketResponseFixtures.item_10007);
        var response = MarketResponseFixtures.GetWorldMarketSearchListItem_10007;

        var tuple = Tuples.of(missingItem, Optional.of(response));
        var future = CompletableFuture.completedFuture(tuple);

        when(marketRequestService.request(missingItem)).thenReturn(future);
        marketService = new MarketService(marketRequestService, marketRedisService, scraperService);

        // The cached key should be loaded from redis while the missing key should be requested (mocked)
        var combinedKey = MarketResponseFixtures.buildSearchKey();
        var items = marketService.requestSearchResult(combinedKey);

        assertThat(items).hasSize(1);

        var combinedResultMsg = MarketResponseFixtures.GetWorldMarketSearchListItem_10007.getResultMessage() + "|" +
                MarketResponseFixtures.GetWorldMarketSearchListItem_10006.getResultMessage();

        assertThat(items.get(0).getResultMessage()).isEqualTo(combinedResultMsg);
    }
}

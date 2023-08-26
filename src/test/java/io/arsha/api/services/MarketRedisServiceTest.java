package io.arsha.api.services;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.common.GameRegion;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.util.AppTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@AppTest
class MarketRedisServiceTest {

    @Autowired
    private MarketRedisService marketRedisService;

    @BeforeEach
    void setup() {
        marketRedisService.flushDb();
    }

    @AfterEach
    void teardown() {
        marketRedisService.flushDb();
    }

    @Test
    void putEternal_ttlShouldBeNegative() {
        var key = makeCacheKey(1L);
        var value = MarketResponse.builder().resultMessage("1").build();

        marketRedisService.setEternal(key, value);
        var ttl = marketRedisService.ttl(key);

        assertThat(ttl).isEqualTo(-1);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 15, 100})
    void testBulkGet_shouldRetainOrderWhileRetainingNullValues(int count) {
        var items = makeDummyInput(count);
        var keys = new ArrayList<>(items.keySet());
        marketRedisService.setEternal(keys.get(0), items.get(keys.get(0)));

        var res = marketRedisService.getMany(keys);
        assertThat(res).hasSize(count);

        var key = keys.get(0);
        var resultItem = res.get(key);
        assertThat(resultItem).isNotNull();

        var resultMsg = Long.valueOf(resultItem.getResultMessage());
        // Check that the ID of the resulting item (primary) is equal to the result message (which contains the ID)
        assertThat(key.getPrimary()).isEqualTo(resultMsg);

        for (var i = 1; i < count; i++) {
            key = keys.get(i);
            resultItem = res.get(key);
            assertThat(resultItem).isNull();
        }
    }

    private LinkedHashMap<CacheCompositeKey, MarketResponse> makeDummyInput(int count) {
        LinkedHashMap<CacheCompositeKey, MarketResponse> res = new LinkedHashMap<>(count);
        for (var i = 0; i < count; i++) {
            var key = makeCacheKey((long) i);
            var value = MarketResponse.builder().resultMessage(String.valueOf(i)).build();

            res.put(key, value);
        }
        return res;
    }

    private CacheCompositeKey makeCacheKey(Long primary) {
        return makeCacheKey(primary, 0L, GameRegion.NORTH_AMERICA, MarketEndpoint.MARKET_SUB_LIST);
    }

    private CacheCompositeKey makeCacheKey(Long primary, Long secondary, GameRegion region, MarketEndpoint endpoint) {
        return CacheCompositeKey.builder().primary(primary).secondary(secondary).region(region).endpoint(endpoint).build();
    }
}

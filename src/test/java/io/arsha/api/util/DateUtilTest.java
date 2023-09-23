package io.arsha.api.util;


import io.arsha.api.lib.AppTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AppTest
class DateUtilTest {

    private final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void testStartOfDay() {
        var days = 30;
        var startOfDay = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(DateUtils.startOfDay(0)), UTC);

        assertEquals(0, startOfDay.getHour());
        assertEquals(0, startOfDay.getMinute());
        assertEquals(0, startOfDay.getSecond());

        var startOfX = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(DateUtils.startOfDay(days)), UTC);

        assertEquals(0, startOfDay.minusDays(days).compareTo(startOfX));
    }

}

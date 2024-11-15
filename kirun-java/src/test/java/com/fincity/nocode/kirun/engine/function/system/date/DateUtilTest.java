package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DateUtilTest {

    @Test
    void testFromLuxonFormatToDateTimeFormat() {
        assertEquals("yyyy-MM-dd", DateUtil.toDateTimeFormat("yyyy-MM-dd"));
        // 1:07:04 PM
        assertEquals("h:mm:ss a", DateUtil.toDateTimeFormat("tt"));
    }

    @Test
    void testToRelative() {

        assertEquals("1 yr ago", DateUtil.toRelative(
                ZonedDateTime.parse("2024-11-13T10:00:00Z"), ZonedDateTime.parse("2025-11-13T09:00:00Z"),
                List.of(), true, "short"));

        assertEquals("in 1 year", DateUtil.toRelative(ZonedDateTime.parse("2025-11-13T11:00:00Z"),
                ZonedDateTime.parse("2024-11-13T10:00:00Z"), Arrays.asList(ChronoUnit.YEARS), true, "long"));

        assertEquals("in 365 days", DateUtil.toRelative(ZonedDateTime.parse("2025-11-13T11:00:00Z"),
                ZonedDateTime.parse("2024-11-13T10:00:00Z"), Arrays.asList(ChronoUnit.DAYS), true, "long"));

        assertEquals("in 365 days", DateUtil.toRelative(ZonedDateTime.parse("2025-11-13T11:00:00Z"),
                ZonedDateTime.parse("2024-11-13T10:00:00Z"), Arrays.asList(ChronoUnit.MINUTES, ChronoUnit.DAYS), true,
                "long"));

        assertEquals("23 minutes ago", DateUtil.toRelative(ZonedDateTime.parse("2025-11-13T09:37:00Z"),
                ZonedDateTime.parse("2025-11-13T10:00:00Z"), List.of(), true, "long"));

        assertEquals("0.38 hrs ago", DateUtil.toRelative(ZonedDateTime.parse("2025-11-13T09:37:00Z"),
                ZonedDateTime.parse("2025-11-13T10:00:00Z"), List.of(ChronoUnit.HOURS), false, "short"));

    }
}

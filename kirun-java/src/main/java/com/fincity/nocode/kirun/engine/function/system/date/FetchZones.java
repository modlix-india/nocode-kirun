package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.TimeZone;

public class FetchZones {
    public static void main(String[] args) {

        String[] availableTimezones = TimeZone.getAvailableIDs();

        System.out.println(availableTimezones.length);

        for (String timezoneId : availableTimezones) {
            TimeZone timezone = TimeZone.getTimeZone(timezoneId);
            int rawOffsetInMillis = timezone.getRawOffset();
            String offset = String.format(
                    "%02d:%02d",
                    Math.abs(rawOffsetInMillis / 3600000), // Hours
                    Math.abs((rawOffsetInMillis / 60000) % 60) // Minutes
            );
            String offsetString = (rawOffsetInMillis >= 0 ? "+" : "-") + offset;

            System.out.println("Timezone ID: " + timezoneId);
            System.out.println("Offset: " + offsetString);
            System.out.println();
        }

        Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();

        System.out.println(availableZoneIds.size());

        for (String zoneId : availableZoneIds) {
            ZoneId zone = ZoneId.of(zoneId);
            ZoneOffset offset = zone.getRules().getOffset(java.time.Instant.now());
            System.out.println("Time Zone ID: " + zoneId + ", Offset: " + offset);
        }

    }
}

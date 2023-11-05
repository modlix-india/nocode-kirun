package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.TimeZone;

public class GetAvailableTimeZones {

    public static void main(String[] args) {

        String[] availableIds = TimeZone.getAvailableIDs();

        for (int i = 0; i < 10; i++) {
            System.out.println(availableIds[i]);
            System.out.println(TimeZone.getTimeZone(availableIds[i]).getRawOffset() / 1000);
        }
    }

}

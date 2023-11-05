package com.fincity.nocode.kirun.engine.util.date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IsValidISODateUtilTest {

    @Test
    void simpleDate() {
        String date = "2023-12-10T10:02:54.959Z";
        Assertions.assertTrue(IsValidISODateUtil.checkValidity(date));

        date = "2023-10-10T10:02:54.959-12:12";
        Assertions.assertTrue(IsValidISODateUtil.checkValidity(date));

        date = "2023-10-4T14:10:30.700+56:70";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));

        date = "2023-10-34T14:10:30.700+56:70";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));
    }

    @Test
    void leapYearTest() {

        String date = "2023-02-10T10:02:54.959Z";
        Assertions.assertTrue(IsValidISODateUtil.checkValidity(date));

        date = "2023-02-28T10:02:54.959-12:12";
        Assertions.assertTrue(IsValidISODateUtil.checkValidity(date));

        date = "2020-02-29T14:10:30.700+12:21";
        Assertions.assertTrue(IsValidISODateUtil.checkValidity(date));

        date = "2021-02-29T14:10:30.700+12:21";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));
    }

    @Test
    void wrongDateTest() {

        String date = "2023-12-87T10:02:54.959Z";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));

        date = "2023-10-36T10:02:54.959-12:12";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));

        date = "2023-16-14T14:10:30.700+12:21";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));

        date = "2023-17-34T14:10:30.700+12:21";
        Assertions.assertFalse(IsValidISODateUtil.checkValidity(date));
    }

}

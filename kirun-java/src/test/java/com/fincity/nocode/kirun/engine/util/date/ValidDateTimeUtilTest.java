package com.fincity.nocode.kirun.engine.util.date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidDateTimeUtilTest {

    @Test
    void validPosTest() {

        String date = "+122023-12-08T10:02:54.959Z";

        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "+12023-12-08T10:02:54.959+12:11";

        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

        date = "+120123-12-08T10:02:54.959-12:11";

        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "+120123-12-34T10:02:54.959-12:11";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

    }

    @Test
    void validnegTest() {
        String date = "-122023-12-08T10:02:54.959Z";

        Assertions.assertTrue(ValidDateTimeUtil.validate(date));
    }

    @Test
    void validpTest() {
        String date = "2023-12-08T10:02:54.959Z";

        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "2023-12-08T10:02:54.959-12:11";

        Assertions.assertTrue(ValidDateTimeUtil.validate(date));
    }

    @Test
    void simpleTest() {

        String date = "202-02-31T11:45:38.939Z";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));
    }

    @Test
    void failTest() {

        String date = "2012-02-35T11:45:38.939Z";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

        date = "surendhar";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

        date = "++++++++++++++++++++++++++++";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

        date = "+122012-02-23T11:45:38.939p";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

        date = "+122012+02-23T11:45:38.939+05:30";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));

    }

    @Test
    void withoutMillisTest() {
        String date = "2012-02-24T11:45:38Z";
        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "+122012-02-24T11:45:38Z";
        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "2012-02-24T11:45:38-12:12";
        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

        date = "+122012-02-24T11:45:38+12:54";
        Assertions.assertTrue(ValidDateTimeUtil.validate(date));

    }

    @Test
    void validate() {
        String date = "-0100-11-06T11:38:15.118+05:30";
        Assertions.assertFalse(ValidDateTimeUtil.validate(date));
    }
}

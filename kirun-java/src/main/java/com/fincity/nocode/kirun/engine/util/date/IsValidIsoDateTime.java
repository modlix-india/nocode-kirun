package com.fincity.nocode.kirun.engine.util.date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsValidIsoDateTime {

    private IsValidIsoDateTime() {

    }

    public static final Pattern dateTimePattern = Pattern.compile(
            "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T"
                    + "([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)(\\.\\d+)?"
                    + "(Z|([+-]([01]\\d|2[0-3]):([0-5]\\d)))?$"); // NO SONAR // Required for evaluating date time
                                                                  // object

    public static boolean checkValidity(String input) {

        Pattern isoPattern = dateTimePattern;

        Matcher matcher = isoPattern.matcher(input);

        return matcher.find();
    }

}

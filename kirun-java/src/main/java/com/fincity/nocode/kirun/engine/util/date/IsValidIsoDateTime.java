package com.fincity.nocode.kirun.engine.util.date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsValidIsoDateTime {

	private IsValidIsoDateTime() {

	}

	// Required for evaluating date time object

	public static final Pattern dateTimePattern = Pattern.compile("^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T"
	        + "([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)(Z|\\.\\d{3})" + "(Z|([+-]([01]\\d|2[0-3]):([0-5]\\d)))?$"); // NO
	                                                                                                               // SONAR

	public static boolean checkValidity(String input) {

		return getMatcher(input).find();
	}

	public static Matcher getMatcher(String input) {

		return dateTimePattern.matcher(input);
	}
}

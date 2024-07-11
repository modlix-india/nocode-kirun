package com.fincity.nocode.kirun.engine.util.date;

public class ValidDateTimeUtil {

	private ValidDateTimeUtil() {

	}

	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}

	public static boolean validYearRange(int year) {
		return year < 275761 && year > -271821;
	}

	public static boolean validMonthRange(int month) {
		return month <= 12 && month > 0;
	}

	public static boolean validDate(int year, int month, int date) {

		if (date <= 0 || month <= 0)
			return false;

		switch (month) {
		case 2:
			return validYearRange(year) && validMonthRange(month) && (isLeapYear(year) && date < 30)
			        || (!isLeapYear(year) && date < 29);

		case 4, 6, 9, 11:
			return validYearRange(year) && validMonthRange(month) && date <= 30;

		default:
			return validYearRange(year) && validMonthRange(month) && date <= 31;
		}

	}

	public static boolean validHourRange(int hour) {
		return hour < 24 && hour >= 0;
	}

	public static boolean validMinuteSecondRange(int minute) {
		return minute < 60 && minute >= 0;
	}

	public static boolean validMilliSecondRange(int millis) {
		return millis < 1000 && millis >= 0;
	}

	public static boolean validateWithOptionalMillis(String portion) {

		return validMilliSecondRange(convertToInt(portion.substring(1, 4))) && validateLocalTime(portion.substring(4));
	}

	public static boolean validateLocalTime(String offset) {

		if (offset.charAt(0) == 'Z')
			return true;

		if (offset.length() != 6)
			return false;

		return (offset.charAt(0) == '+' || offset.charAt(0) == '-')
		        && (validHourRange(convertToInt(offset.substring(1, 3))) && offset.charAt(3) == ':'
		                && validMinuteSecondRange(convertToInt(offset.substring(4, offset.length() - 1))));
	}

	public int getYear(String date) {

		char first = date.charAt(0);
		if (first == '+' || first == '-') {
			int year = convertToInt(date.substring(1, 7));
			return first == '-' ? year * -1 : year;
		}

		return convertToInt(date.substring(0, 4));
	}

	public static boolean validate(String date) {

		if (date.length() < 20 || date.length() > 32)
			return false; // as date time should have minimum of 20 characters and maximum of 32 characters

		char first = date.charAt(0); // checking first index whether '+' or '-' or any digit

		if (first == '+' || first == '-') {

			boolean a = date.charAt(7) == '-' && date.charAt(10) == '-' && date.charAt(13) == 'T'
			        && date.charAt(16) == ':' && date.charAt(19) == ':' && date.charAt(22) == '.';

			return a && validDate(convertToInt(date.substring(1, 7)), convertToInt(date.substring(8, 10)),
			        convertToInt(date.substring(11, 13))) && validHourRange(convertToInt(date.substring(14, 16)))
			        && validMinuteSecondRange(convertToInt(date.substring(17, 19)))
			        && validMinuteSecondRange(convertToInt(date.substring(20, 22)))
			        && validateWithOptionalMillis(date.substring(22));
		} else {

			boolean a = date.charAt(4) == '-' && date.charAt(7) == '-' && date.charAt(10) == 'T'
			        && date.charAt(13) == ':' && date.charAt(16) == ':' && date.charAt(19) == '.';

			return a && validDate(convertToInt(date.substring(0, 4)), convertToInt(date.substring(5, 7)),
			        convertToInt(date.substring(8, 10))) && validHourRange(convertToInt(date.substring(11, 13)))
			        && validMinuteSecondRange(convertToInt(date.substring(14, 16)))
			        && validMinuteSecondRange(convertToInt(date.substring(17, 19)))
			        && validateWithOptionalMillis(date.substring(19));
		}

	}

	private static int convertToInt(String num) {

		try {
			return Integer.valueOf(num);
		} catch (NumberFormatException nfe) {
			return Integer.MIN_VALUE; // not able to parse return
		}
	}
}

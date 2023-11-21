package com.fincity.nocode.kirun.engine.util.date;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class DurationUtil {

	private DurationUtil() {

	}

	private static final String[] stringArray = { "years", "months", "days", "hours", "minutes", "seconds" };

	public static String getDifference(int[] helperArray, String prefix, String suffix) {
		int i;
		for (i = 0; i < helperArray.length; i++) {
			if (helperArray[i] != 0) {
				break;
			}
		}

		if (i == 0) {
			return formatResult(helperArray[i], prefix, suffix, "year", "years");
		}
		if (i == 1) {
			return formatResult(helperArray[i], prefix, suffix, "a month", "months");
		}
		if (i == 2) {
			return formatResult(helperArray[i], prefix, suffix, "a day", "days");
		}
		if (i == 3) {

			return formatResult2(helperArray[i], prefix, suffix, "hour", "hours", 1, 3);
		}
		if (i == 4) {
			return formatResult2(helperArray[i], prefix, suffix, "minute", "minutes", 1, 15);
		}
		if (i == 5) {
			return (helperArray[i] <= 44) ? (helperArray[i] <= 2) ? "now" : prefix + "few seconds" + suffix
			        : prefix + helperArray[i] + " seconds" + suffix;

		}

		return "now";
	}

	private static String formatResult(int value, String prefix, String suffix, String string, String string2) {
		return (value == 1) ? prefix + string + suffix : prefix + value + string2 + suffix;
	}

	private static String formatResult2(int value, String prefix, String suffix, String string, String string2, int lb,
	        int ub) {
		String timeUnit = (value >= lb && value <= ub) ? "few " + string2
		        : value + " " + (value == 1 ? string : string2);
		return prefix + timeUnit + suffix;
	}

	public static String getExactDifference(int[] helperArray, String key, String prefix, String suffix) {
		int i;
		for (i = 0; i < helperArray.length; i++) {
			if (helperArray[i] != 0) {
				break;
			}
		}

		int end = key.substring(2)
		        .equals("A") ? helperArray.length : i + Integer.parseInt(key.substring(2));
		StringBuilder finalString = new StringBuilder(helperArray[i] + " "
		        + (helperArray[i] == 1 ? stringArray[i].substring(0, stringArray[i].length() - 1) : stringArray[i]));

		if (end <= helperArray.length) {
			while (i + 1 < end) {
				finalString.append(" ")
				        .append(helperArray[i + 1])
				        .append(" ")
				        .append(helperArray[i + 1] == 1
				                ? stringArray[i + 1].substring(0, stringArray[i + 1].length() - 1)
				                : stringArray[i + 1]);
				i++;
			}
			return prefix + finalString + suffix;
		}
		throw new KIRuntimeException("Please provide a valid key.");

	}

	private static String getUnit(int diff, String suffix) {
		StringBuilder sb = new StringBuilder(diff + " " + suffix);
		return sb.append(diff == 1 ? "" : "s")
		        .toString();
	}

	public static String getPrefix(long diff, String prefix) {
		return diff < 0 ? prefix : "";
	}

	public static String getSuffix(long diff) {
		return (diff > 0) ? " ago" : "";
	}

	public static String getDuration(LocalDateTime firstDate, LocalDateTime secondDate, String key) {
		String prefix = "";
		String suffix = "";

		long diffInMilli = ChronoUnit.MILLIS.between(firstDate, secondDate);
		long weeks = Math.abs(diffInMilli / (1000 * 60 * 60 * 24 * 7));

		int[] helperArray = new int[6];
		helperArray[0] = Math.abs(firstDate.getYear() - secondDate.getYear());
		helperArray[1] = Math.abs(firstDate.getMonthValue() - secondDate.getMonthValue());
		helperArray[2] = Math.abs(firstDate.getDayOfMonth() - secondDate.getDayOfMonth());
		helperArray[3] = Math.abs(firstDate.getHour() - secondDate.getHour());
		helperArray[4] = Math.abs(firstDate.getMinute() - secondDate.getMinute());
		helperArray[5] = Math.abs(firstDate.getSecond() - secondDate.getSecond());

		switch (key) {
		case "EY":
			return getUnit(helperArray[0], "year");
		case "EM":
			return getUnit(helperArray[1], "month");
		case "EW":
			return weeks == 1L ? "1 week" : weeks + " weeks";
		case "ED":
			return getUnit(helperArray[2], "day");
		case "EH":
			return getUnit(helperArray[3], "hour");
		case "ES":
			return getUnit(helperArray[4], "second");
		}
		if (key.equals("A") || (key.length() > 1 && key.charAt(1) == 'A')) {
			prefix = getPrefix(diffInMilli, "after ");
			suffix = getSuffix(diffInMilli);
		}

		if (key.equals("I") || (key.length() > 1 && key.charAt(1) == 'I')) {
			prefix = getPrefix(diffInMilli, "In ");
			suffix = getSuffix(diffInMilli);
		}

		if (key.equals("EN") || key.equals("EA") || key.equals("EI")) {
			key = key + "1";
		}

		if (key.length() <= 2) {
			return getDifference(helperArray, prefix, suffix);
		}

		return getExactDifference(helperArray, key, prefix, suffix);

	}
}
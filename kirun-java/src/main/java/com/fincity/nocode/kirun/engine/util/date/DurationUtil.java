package com.fincity.nocode.kirun.engine.util.date;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DurationUtil {

	private DurationUtil() {

	}

	private static int weeks = 0;
	private static final String[] stringArray = { "years", "months", "days", "hours", "minutes", "seconds" };

	public static String getDifference(int[] helperArray, String key, String prefix, String suffix) {
		int i;
		for (i = 0; i < helperArray.length; i++) {
			if (helperArray[i] != 0) {
				break;
			}
		}

		if (i == 0) {
			return helperArray[i] == 1 ? prefix + "a year" + suffix : prefix + helperArray[i] + " years" + suffix;
		}
		if (i == 1) {
			return helperArray[i] == 1 ? prefix + "a month" + suffix : prefix + helperArray[i] + " months" + suffix;
		}
		if (i == 2) {
			return helperArray[i] == 1 ? prefix + "a day" + suffix : prefix + helperArray[i] + " days" + suffix;
		}
		if (i == 3) {
			return (helperArray[i] >= 1 && helperArray[i] <= 3) ? prefix + "few hours" + suffix
			        : prefix + helperArray[i] + " hours" + suffix;
		}
		if (i == 4) {
			return (helperArray[i] >= 1 && helperArray[i] <= 15) ? prefix + "few minutes" + suffix
			        : prefix + helperArray[i] + " minutes" + suffix;
		}
		if (i == 5) {
			return (helperArray[i] <= 44) ? (helperArray[i] <= 2) ? "now" : prefix + "few seconds" + suffix
			        : prefix + helperArray[i] + " seconds" + suffix;
		}

		return "now";
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
		} else {
			throw new RuntimeException("Please provide a valid key.");
		}
	}

	public static String getDuration(LocalDateTime firstDate, LocalDateTime secondDate, String key) {
		String prefix = "";
		String suffix = "";

		long diffInMilli = ChronoUnit.MILLIS.between(firstDate, secondDate);
		weeks = (int) Math.floor(Math.abs(diffInMilli / (1000 * 60 * 60 * 24 * 7)));

		int[] helperArray = new int[6];
		helperArray[0] = Math.abs(firstDate.getYear() - secondDate.getYear());
		helperArray[1] = Math.abs(firstDate.getMonthValue() - secondDate.getMonthValue());
		helperArray[2] = Math.abs(firstDate.getDayOfMonth() - secondDate.getDayOfMonth());
		helperArray[3] = Math.abs(firstDate.getHour() - secondDate.getHour());
		helperArray[4] = Math.abs(firstDate.getMinute() - secondDate.getMinute());
		helperArray[5] = Math.abs(firstDate.getSecond() - secondDate.getSecond());

		if ("EY".equals(key)) {
			return helperArray[0] == 1 ? "1 year" : helperArray[0] + " years";
		}

		if ("EM".equals(key)) {
			return helperArray[1] == 1 ? "1 month" : helperArray[1] + " months";
		}

		if ("EW".equals(key)) {
			return weeks == 1 ? "1 week" : weeks + " weeks";
		}

		if ("ED".equals(key)) {
			return helperArray[2] == 1 ? "1 day" : helperArray[2] + " days";
		}
		if ("EH".equals(key)) {
			return helperArray[3] == 1 ? "1 hour" : helperArray[3] + " hours";
		}
		if ("ES".equals(key)) {
			return helperArray[4] == 1 ? "1 second" : helperArray[4] + " seconds";
		}
		if (key.equals("A") || (key.length() > 1 && key.charAt(1) == 'A')) {
			prefix = (diffInMilli < 0) ? "after " : "";
			suffix = (diffInMilli > 0) ? " ago" : "";
		}

		if (key.equals("I") || (key.length() > 1 && key.charAt(1) == 'I')) {
			prefix = (diffInMilli < 0) ? "In " : "";
			suffix = (diffInMilli > 0) ? " ago" : "";
		}

		if (key.equals("EN") || key.equals("EA") || key.equals("EI")) {
			key = key + "1";
		}

		if (key.length() <= 2) {
			return getDifference(helperArray, key, prefix, suffix);
		} else {
			return getExactDifference(helperArray, key, prefix, suffix);
		}

	}
}
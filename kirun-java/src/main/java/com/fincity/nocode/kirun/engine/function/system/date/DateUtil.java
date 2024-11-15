package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class DateUtil {

    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendFraction(MILLI_OF_SECOND, 3, 3, true)
            .appendOffsetId()
            .toFormatter();

    public static final DateTimeFormatter ISO_DATE_READING_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart() // time made optional
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart() // zone and offset made optional
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId()
            .appendLiteral(']')
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .toFormatter();

    private static final Map<String, String> LUXON_DATEFORMATTER_MAP = new HashMap<>();

    static {
        // Milliseconds and fractions
        LUXON_DATEFORMATTER_MAP.put("S", "S");
        LUXON_DATEFORMATTER_MAP.put("SSS", "SSS");
        LUXON_DATEFORMATTER_MAP.put("u", "SSS");
        LUXON_DATEFORMATTER_MAP.put("uu", "SS");
        LUXON_DATEFORMATTER_MAP.put("uuu", "S");

        // Seconds and minutes
        LUXON_DATEFORMATTER_MAP.put("s", "s");
        LUXON_DATEFORMATTER_MAP.put("ss", "ss");
        LUXON_DATEFORMATTER_MAP.put("m", "m");
        LUXON_DATEFORMATTER_MAP.put("mm", "mm");

        // Hours (12-hour and 24-hour)
        LUXON_DATEFORMATTER_MAP.put("h", "h");
        LUXON_DATEFORMATTER_MAP.put("hh", "hh");
        LUXON_DATEFORMATTER_MAP.put("H", "H");
        LUXON_DATEFORMATTER_MAP.put("HH", "HH");

        // Timezone offsets
        LUXON_DATEFORMATTER_MAP.put("Z", "X");
        LUXON_DATEFORMATTER_MAP.put("ZZ", "XX");
        LUXON_DATEFORMATTER_MAP.put("ZZZ", "XXX");
        LUXON_DATEFORMATTER_MAP.put("ZZZZ", "z");
        LUXON_DATEFORMATTER_MAP.put("ZZZZZ", "VV");
        LUXON_DATEFORMATTER_MAP.put("z", "VV");

        // AM/PM
        LUXON_DATEFORMATTER_MAP.put("a", "a");

        // Days
        LUXON_DATEFORMATTER_MAP.put("d", "d");
        LUXON_DATEFORMATTER_MAP.put("dd", "dd");

        // Day of the week
        LUXON_DATEFORMATTER_MAP.put("c", "e");
        LUXON_DATEFORMATTER_MAP.put("ccc", "EEE");
        LUXON_DATEFORMATTER_MAP.put("cccc", "EEEE");
        LUXON_DATEFORMATTER_MAP.put("ccccc", "EEEEE");

        // Months
        LUXON_DATEFORMATTER_MAP.put("L", "M");
        LUXON_DATEFORMATTER_MAP.put("LL", "MM");
        LUXON_DATEFORMATTER_MAP.put("LLL", "MMM");
        LUXON_DATEFORMATTER_MAP.put("LLLL", "MMMM");
        LUXON_DATEFORMATTER_MAP.put("LLLLL", "MMMMM");

        // Years and Eras
        LUXON_DATEFORMATTER_MAP.put("y", "y");
        LUXON_DATEFORMATTER_MAP.put("yy", "yy");
        LUXON_DATEFORMATTER_MAP.put("yyyy", "yyyy");
        LUXON_DATEFORMATTER_MAP.put("G", "G");
        LUXON_DATEFORMATTER_MAP.put("GG", "GG");
        LUXON_DATEFORMATTER_MAP.put("GGGGG", "G");

        // Quarters
        LUXON_DATEFORMATTER_MAP.put("q", "Q");
        LUXON_DATEFORMATTER_MAP.put("qq", "QQ");

        // ISO Week year
        LUXON_DATEFORMATTER_MAP.put("kk", "u");
        LUXON_DATEFORMATTER_MAP.put("kkkk", "uuuu");

        // Week-based fields
        LUXON_DATEFORMATTER_MAP.put("W", "w");
        LUXON_DATEFORMATTER_MAP.put("WW", "ww");

        // Ordinal day of the year
        LUXON_DATEFORMATTER_MAP.put("o", "D");
        LUXON_DATEFORMATTER_MAP.put("ooo", "DDD");

        // Localized date/time styles
        LUXON_DATEFORMATTER_MAP.put("D", "M/d/yyyy");
        LUXON_DATEFORMATTER_MAP.put("DD", "MMM d, yyyy");
        LUXON_DATEFORMATTER_MAP.put("DDD", "MMMM d, yyyy");
        LUXON_DATEFORMATTER_MAP.put("DDDD", "EEEE, MMMM d, yyyy");
        LUXON_DATEFORMATTER_MAP.put("t", "h:mm a");
        LUXON_DATEFORMATTER_MAP.put("tt", "h:mm:ss a");
        LUXON_DATEFORMATTER_MAP.put("T", "HH:mm");
        LUXON_DATEFORMATTER_MAP.put("TT", "HH:mm:ss");
    }

    public static ZonedDateTime getDateTime(String isoTimestamp) {
        try {
            TemporalAccessor temporalAccessor = ISO_DATE_READING_FORMATTER.parseBest(isoTimestamp, ZonedDateTime::from,
                    LocalDateTime::from,
                    LocalDate::from);

            if (temporalAccessor == null) {
                throw new IllegalArgumentException("Invalid timestamp: " + isoTimestamp);
            }

            if (temporalAccessor instanceof ZonedDateTime zonedDateTime) {
                return zonedDateTime;
            }

            if (temporalAccessor instanceof LocalDateTime localDateTime) {

                return localDateTime.atZone(ZoneId.systemDefault());
            }
            return ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault());
        } catch (IllegalArgumentException e) {
            throw new KIRuntimeException("Invalid ISO timestamp : " + isoTimestamp);
        }

    }

    public static String toDateTimeFormat(String luxonFormat) {
        StringBuilder javaFormat = new StringBuilder();
        int i = 0;
        while (i < luxonFormat.length()) {

            String token = luxonFormat.substring(i, Math.min(i + 5, luxonFormat.length()));

            while (!LUXON_DATEFORMATTER_MAP.containsKey(token) && token.length() > 1) {
                token = token.substring(0, token.length() - 1);
            }

            if (LUXON_DATEFORMATTER_MAP.containsKey(token)) {
                javaFormat.append(LUXON_DATEFORMATTER_MAP.get(token));
                i += token.length();
            } else {
                javaFormat.append(token.charAt(0));
                i++;
            }
        }

        return javaFormat.toString();
    }

    public static String toRelative(
            ZonedDateTime from,
            ZonedDateTime now,
            List<ChronoUnit> units,
            boolean round,
            String format) {
        if (from.equals(now)) {
            return "now";
        }

        long diff = from.toEpochSecond() - now.toEpochSecond();
        final boolean isFuture = diff > 0;
        diff = isFuture ? diff : -diff;
        final ChronoUnit bestUnit = bestUnit(diff, units);

        Double timeMagnitude = ((double) diff) / bestUnit.getDuration().toSeconds();
        String unitString = getUnitString(bestUnit, timeMagnitude, format);

        String timeMagnitudeString;

        if (round)
            timeMagnitudeString = "" + Math.round(timeMagnitude);
        else {
            Double value = (Math.round(timeMagnitude * 100) / 100.0);
            Long longValue = value.longValue();
            if (value.equals(longValue.doubleValue())) {
                timeMagnitudeString = "" + longValue;
            } else {
                timeMagnitudeString = "" + value;
            }
        }

        return formatResult(timeMagnitudeString, unitString, isFuture);
    }

    private static ChronoUnit bestUnit(long seconds, List<ChronoUnit> units) {

        ChronoUnit betterUnit = units.stream()
                .sorted((a, b) -> Long.compare(b.getDuration().toMillis(), a.getDuration().toMillis()))
                .filter(unit -> seconds <= unit.getDuration().toMillis())
                .findFirst().orElse(null);

        if (betterUnit != null)
            return betterUnit;

        if (isLessThan(seconds, 50, ChronoUnit.SECONDS))
            return ChronoUnit.SECONDS;
        if (isLessThan(seconds, 50, ChronoUnit.MINUTES))
            return ChronoUnit.MINUTES;
        if (isLessThan(seconds, 23, ChronoUnit.HOURS))
            return ChronoUnit.HOURS;
        if (isLessThan(seconds, 21, ChronoUnit.DAYS))
            return ChronoUnit.WEEKS;
        if (isLessThan(seconds, 29, ChronoUnit.DAYS))
            return ChronoUnit.DAYS;
        if (isLessThan(seconds, 11, ChronoUnit.MONTHS))
            return ChronoUnit.MONTHS;
        return ChronoUnit.YEARS;
    }

    private static boolean isLessThan(long seconds, long end, ChronoUnit unit) {
        return seconds <= (end * unit.getDuration().toSeconds());
    }

    private static String formatResult(String timeString, String unitString, boolean isFuture) {
        if (isFuture) {
            return "in " + timeString + " " + unitString;
        } else {
            return timeString + " " + unitString + " ago";
        }
    }

    private static String getUnitString(ChronoUnit unit, double value, String format) {
        boolean isPlural = Math.abs(value - 1.0d) > 0.001d;
        String unitKey = unit.toString().toLowerCase();

        return switch (format) {
            case "long" -> getLongUnit(unitKey, isPlural);
            case "short" -> getShortUnit(unitKey, isPlural);
            case "narrow" -> getNarrowUnit(unitKey);
            default -> getLongUnit(unitKey, isPlural);
        };
    }

    private static String getLongUnit(String unit, boolean isPlural) {
        return isPlural ? unit : unit.substring(0, unit.length() - 1);
    }

    private static String getShortUnit(String unit, boolean isPlural) {
        return switch (unit) {
            case "years" -> isPlural ? "yrs" : "yr";
            case "hours" -> isPlural ? "hrs" : "hr";
            case "weeks" -> isPlural ? "wks" : "wk";
            case "months" -> "mo";
            default -> unit.substring(0, 3);
        };
    }

    private static String getNarrowUnit(String unit) {
        return unit.substring(0, 1);
    }

    private DateUtil() {
    }
}
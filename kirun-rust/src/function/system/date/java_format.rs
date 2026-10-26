//! `java.time.format.DateTimeFormatter` as kirun-java's date functions use it: `ofPattern`
//! patterns (compiled by Java's `parsePattern` rules), formatting a `ZonedDateTime` in the
//! default locale, and parsing with `parseDefaulting` fields and the SMART resolver
//! (`FromDateString`). Also kirun-java's `DateUtil.toDateTimeFormat` (its luxon-to-Java token
//! map, which knows nothing of quotes) and `DateUtil.toRelative`.
//!
//! Locale data covers English: `en_US` (and any locale without its own entry), `en_IN` and
//! `en_GB`, which print `am`/`pm` and `Sept`; `en_GB` starts weeks on Monday. Zone names come
//! from a table of common zones, with Java's fallbacks for the rest.

use std::collections::HashMap;

use chrono::{Datelike, NaiveDate, NaiveDateTime, NaiveTime, Timelike};

use super::java_time::{
    default_locale, invalid_value, java_error, java_zone_ids, offset_id, offset_of_total, parse_error_at,
    parse_zone_id, resolve_error, resolve_time, resolve_ymd, system_default_zone, unparsed_error, ZoneId,
    ZonedDateTime,
};
use crate::{KIRunError, Result};

// --------------------------------------------------------------------------- locale data

struct LocaleData {
    lower_am_pm: bool,
    sept: bool,
    /// ISO day number (1 = Monday) weeks start on.
    first_day: u32,
    min_days: u32,
    /// en_US keeps US zone abbreviations (`EST`, `PT`); other English locales print `GMT-05:00`.
    us_abbreviations: bool,
}

fn locale() -> LocaleData {
    let l = default_locale();
    let mut parts = l.split('_');
    let lang = parts.next().unwrap_or("");
    let country = parts.next().unwrap_or("");
    match (lang, country) {
        ("en", "IN") => LocaleData { lower_am_pm: true, sept: true, first_day: 7, min_days: 1, us_abbreviations: false },
        ("en", "GB") | ("en", "IE") => {
            LocaleData { lower_am_pm: true, sept: true, first_day: 1, min_days: 4, us_abbreviations: false }
        }
        _ => LocaleData { lower_am_pm: false, sept: false, first_day: 7, min_days: 1, us_abbreviations: true },
    }
}

const MONTHS: [&str; 12] = [
    "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December",
];
const DAYS: [&str; 7] = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Style {
    Short,
    Full,
    Narrow,
}

fn month_text(m: u32, style: Style, ld: &LocaleData) -> String {
    let full = MONTHS[(m as usize).clamp(1, 12) - 1];
    match style {
        Style::Full => full.into(),
        Style::Narrow => full[..1].into(),
        Style::Short if ld.sept && m == 9 => "Sept".into(),
        Style::Short => full[..3].into(),
    }
}

fn day_text(iso_dow: u32, style: Style) -> String {
    let full = DAYS[(iso_dow as usize).clamp(1, 7) - 1];
    match style {
        Style::Full => full.into(),
        Style::Narrow => full[..1].into(),
        Style::Short => full[..3].into(),
    }
}

fn am_pm_text(pm: bool, ld: &LocaleData) -> &'static str {
    match (pm, ld.lower_am_pm) {
        (false, false) => "AM",
        (true, false) => "PM",
        (false, true) => "am",
        (true, true) => "pm",
    }
}

fn era_text(ad: bool, style: Style) -> &'static str {
    match (ad, style) {
        (true, Style::Full) => "Anno Domini",
        (false, Style::Full) => "Before Christ",
        (true, Style::Narrow) => "A",
        (false, Style::Narrow) => "B",
        (true, Style::Short) => "AD",
        (false, Style::Short) => "BC",
    }
}

fn quarter_text(q: u32, style: Style) -> String {
    match style {
        Style::Short => format!("Q{q}"),
        Style::Narrow => q.to_string(),
        Style::Full => format!("{q}{} quarter", ["st", "nd", "rd", "th"][(q as usize).clamp(1, 4) - 1]),
    }
}

/// Java's flexible day periods (`B`) in English.
fn day_period_text(minute_of_day: u32, style: Style) -> &'static str {
    match minute_of_day {
        0 => {
            if style == Style::Narrow {
                "mi"
            } else {
                "midnight"
            }
        }
        720 => {
            if style == Style::Narrow {
                "n"
            } else {
                "noon"
            }
        }
        1..=719 => "in the morning",
        721..=1079 => "in the afternoon",
        1080..=1259 => "in the evening",
        _ => "at night",
    }
}

// ----------------------------------------------------------------------------- zone names

/// (zone, generic full, generic short (en_US), specific short standard/daylight (en_US),
/// specific long standard/daylight, generic short / specific short outside en_US).
struct ZoneNames {
    generic: &'static str,
    generic_short_us: &'static str,
    short_us: (&'static str, &'static str),
    long: (&'static str, &'static str),
    short_other: Option<(&'static str, &'static str)>,
    generic_short_other: Option<&'static str>,
}

fn zone_names(id: &str) -> Option<ZoneNames> {
    let n = |generic, gs, ss, sd, ls, ld| ZoneNames {
        generic,
        generic_short_us: gs,
        short_us: (ss, sd),
        long: (ls, ld),
        short_other: None,
        generic_short_other: None,
    };
    Some(match id {
        "UTC" | "Etc/UTC" | "Etc/UCT" | "UCT" | "Etc/Universal" | "Universal" | "Etc/Zulu" | "Zulu" => n(
            "Coordinated Universal Time",
            "UTC",
            "UTC",
            "UTC",
            "Coordinated Universal Time",
            "Coordinated Universal Time",
        ),
        "GMT" | "Etc/GMT" | "Etc/Greenwich" | "Greenwich" | "GMT0" | "Etc/GMT0" => {
            n("Greenwich Mean Time", "GMT", "GMT", "GMT", "Greenwich Mean Time", "Greenwich Mean Time")
        }
        "Asia/Kolkata" | "Asia/Calcutta" => {
            n("Kolkata Time", "IST", "IST", "IST", "India Standard Time", "India Standard Time")
        }
        "America/New_York" | "US/Eastern" => ZoneNames {
            short_other: Some(("GMT-05:00", "GMT-04:00")),
            generic_short_other: Some("GMT-05:00"),
            ..n("Eastern Time", "ET", "EST", "EDT", "Eastern Standard Time", "Eastern Daylight Time")
        },
        "America/Los_Angeles" | "US/Pacific" => ZoneNames {
            short_other: Some(("GMT-08:00", "GMT-07:00")),
            generic_short_other: Some("GMT-08:00"),
            ..n("Pacific Time", "PT", "PST", "PDT", "Pacific Standard Time", "Pacific Daylight Time")
        },
        "Europe/London" => n("London Time", "GMT", "GMT", "BST", "Greenwich Mean Time", "British Summer Time"),
        "Asia/Dubai" => ZoneNames {
            short_other: Some(("GST", "GST")),
            ..n("Dubai Time", "GMT+04:00", "GMT+04:00", "GMT+04:00", "Gulf Standard Time", "Gulf Standard Time")
        },
        "Asia/Singapore" | "Singapore" => n(
            "Singapore Time",
            "GMT+08:00",
            "GMT+08:00",
            "GMT+08:00",
            "Singapore Standard Time",
            "Singapore Standard Time",
        ),
        "Australia/Sydney" => n(
            "Australian Eastern Time",
            "AET",
            "AEST",
            "AEDT",
            "Australian Eastern Standard Time",
            "Australian Eastern Daylight Time",
        ),
        "Europe/Paris" => n(
            "Central European Time",
            "CET",
            "CET",
            "CEST",
            "Central European Standard Time",
            "Central European Summer Time",
        ),
        "Asia/Tokyo" => n("Japan Time", "JT", "JST", "JST", "Japan Standard Time", "Japan Standard Time"),
        _ => return None,
    })
}

/// Java's localized offset (`O`, `ZZZZ`): `GMT`, `GMT+5:30` (short) or `GMT+05:30` (full).
fn localized_offset(total: i32, full: bool) -> String {
    if total == 0 {
        return "GMT".into();
    }
    let a = total.unsigned_abs();
    let (h, m, s) = (a / 3600, (a / 60) % 60, a % 60);
    let sign = if total < 0 { '-' } else { '+' };
    let mut out = if full { format!("GMT{sign}{h:02}:{m:02}") } else { format!("GMT{sign}{h}") };
    if !full && (m != 0 || s != 0) {
        out.push_str(&format!(":{m:02}"));
    }
    if s != 0 {
        out.push_str(&format!(":{s:02}"));
    }
    out
}

/// The city of a tzdb ID (`America/New_York` → `New York`).
fn city(id: &str) -> String {
    id.rsplit('/').next().unwrap_or(id).replace('_', " ")
}

/// Java `ZoneId.getDisplayName(style, locale)`: an offset prints its ID; a region its generic
/// name (`FULL`, `FULL_STANDALONE`) or generic short name (`SHORT_STANDALONE`).
pub fn zone_display_name(zone: &ZoneId, short: bool) -> String {
    let ZoneId::Region { id, .. } = zone else { return zone.id() };
    let ld = locale();
    match zone_names(id) {
        Some(n) if short => {
            if ld.us_abbreviations { n.generic_short_us } else { n.generic_short_other.unwrap_or(n.generic_short_us) }.into()
        }
        Some(n) => n.generic.into(),
        None if short => localized_offset(zone.standard_offset_at(0), true),
        None if id.starts_with("UTC") || id.starts_with("GMT") || id.starts_with("UT") => id.clone(),
        None => format!("{} Time", city(id)),
    }
}

/// `z` / `zzzz`: the specific (standard or daylight) zone name at the instant.
fn zone_text(z: &ZonedDateTime, full: bool) -> String {
    let ZoneId::Region { id, .. } = &z.zone else { return z.zone.id() };
    let daylight = z.is_daylight_savings();
    let pick = |pair: (&'static str, &'static str)| if daylight { pair.1 } else { pair.0 };
    match zone_names(id) {
        Some(n) if full => pick(n.long).into(),
        Some(n) => {
            let ld = locale();
            pick(if ld.us_abbreviations { n.short_us } else { n.short_other.unwrap_or(n.short_us) }).into()
        }
        None => localized_offset(z.offset, true),
    }
}

// ----------------------------------------------------------------------- compiled pattern

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
enum Field {
    Era,
    YearOfEra,
    Year,
    Quarter,
    Month,
    DayOfMonth,
    DayOfYear,
    DayOfWeek,
    AlignedWeekOfMonth,
    AmPm,
    ClockHourOfAmPm,
    HourOfAmPm,
    ClockHourOfDay,
    HourOfDay,
    Minute,
    Second,
    NanoOfSecond,
    MilliOfDay,
    NanoOfDay,
    // WeekFields (locale): localized day-of-week, week-of-month, week-of-week-based-year,
    // week-based-year.
    LocalDayOfWeek,
    WeekOfMonth,
    WeekOfWeekBasedYear,
    WeekBasedYear,
    // Set by offset and zone parsers.
    OffsetSeconds,
}

impl Field {
    /// Java `TemporalField.toString()`.
    fn java_name(self, ld: &LocaleData) -> String {
        let wf = || format!("WeekFields[{},{}]", DAYS[(ld.first_day as usize) - 1].to_uppercase(), ld.min_days);
        match self {
            Field::Era => "Era".into(),
            Field::YearOfEra => "YearOfEra".into(),
            Field::Year => "Year".into(),
            Field::Quarter => "QuarterOfYear".into(),
            Field::Month => "MonthOfYear".into(),
            Field::DayOfMonth => "DayOfMonth".into(),
            Field::DayOfYear => "DayOfYear".into(),
            Field::DayOfWeek => "DayOfWeek".into(),
            Field::AlignedWeekOfMonth => "AlignedWeekOfMonth".into(),
            Field::AmPm => "AmPmOfDay".into(),
            Field::ClockHourOfAmPm => "ClockHourOfAmPm".into(),
            Field::HourOfAmPm => "HourOfAmPm".into(),
            Field::ClockHourOfDay => "ClockHourOfDay".into(),
            Field::HourOfDay => "HourOfDay".into(),
            Field::Minute => "MinuteOfHour".into(),
            Field::Second => "SecondOfMinute".into(),
            Field::NanoOfSecond => "NanoOfSecond".into(),
            Field::MilliOfDay => "MilliOfDay".into(),
            Field::NanoOfDay => "NanoOfDay".into(),
            Field::LocalDayOfWeek => format!("DayOfWeek[{}]", wf()),
            Field::WeekOfMonth => format!("WeekOfMonth[{}]", wf()),
            Field::WeekOfWeekBasedYear => format!("WeekOfWeekBasedYear[{}]", wf()),
            Field::WeekBasedYear => format!("WeekBasedYear[{}]", wf()),
            Field::OffsetSeconds => "OffsetSeconds".into(),
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
enum Sign {
    Normal,
    NotNegative,
    ExceedsPad,
}

#[derive(Clone, Debug)]
enum Item {
    Literal(String),
    /// `appendValue`: the field, min and max width, sign style, and the widths of the fixed
    /// value fields that follow it with nothing in between (Java's adjacent value parsing).
    Number { field: Field, min: usize, max: usize, sign: Sign, subsequent: usize },
    /// `appendValueReduced(field, 2, 2, 2000)`.
    Reduced { field: Field },
    /// `appendFraction(NANO_OF_SECOND, n, n, false)`.
    Fraction { width: usize },
    Text { field: Field, style: Style },
    /// `appendOffset(pattern, noOffsetText)`.
    Offset { pattern: &'static str, zero: &'static str },
    LocalizedOffset { full: bool },
    ZoneId,
    ZoneText { full: bool },
    GenericZone { full: bool },
    DayPeriod { style: Style },
    Optional(Vec<Item>),
    Pad { width: usize, item: Box<Item> },
}

const OFFSET_PATTERNS: [&str; 7] = ["+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss"];

fn illegal(message: impl Into<String>) -> KIRunError {
    java_error(message)
}

/// Java `DateTimeFormatterBuilder.appendPattern` (`ofPattern`).
fn compile(pattern: &str) -> Result<Vec<Item>> {
    let chars: Vec<char> = pattern.chars().collect();
    // A stack of open optional sections; the last is where items go.
    let mut stack: Vec<Vec<Item>> = vec![Vec::new()];
    // Index in the current section of the value parser further fixed-width values attach to.
    let mut value_parser: Vec<Option<usize>> = vec![None];
    let mut pad_next: Option<usize> = None;
    let mut pos = 0;

    fn push(stack: &mut [Vec<Item>], vp: &mut [Option<usize>], pad: &mut Option<usize>, item: Item) {
        let item = match pad.take() {
            Some(width) => Item::Pad { width, item: Box::new(item) },
            None => item,
        };
        let top = stack.len() - 1;
        let is_value = matches!(item, Item::Number { .. } | Item::Reduced { .. } | Item::Fraction { .. });
        let fixed_not_negative = match &item {
            Item::Number { min, max, sign, .. } => min == max && *sign == Sign::NotNegative,
            Item::Fraction { .. } => true,
            _ => false,
        };
        if is_value {
            if let Some(base) = vp[top] {
                if fixed_not_negative {
                    let width = match &item {
                        Item::Number { max, .. } => *max,
                        Item::Fraction { width } => *width,
                        _ => 0,
                    };
                    if let Item::Number { subsequent, .. } = &mut stack[top][base] {
                        *subsequent += width;
                    }
                    stack[top].push(item);
                    return;
                }
            }
            stack[top].push(item);
            vp[top] = Some(stack[top].len() - 1);
            return;
        }
        stack[top].push(item);
        vp[top] = None;
    }

    while pos < chars.len() {
        let mut cur = chars[pos];
        if cur.is_ascii_alphabetic() {
            let start = pos;
            pos += 1;
            while pos < chars.len() && chars[pos] == cur {
                pos += 1;
            }
            let mut count = pos - start;
            if cur == 'p' {
                let mut pad = 0;
                if pos < chars.len() && chars[pos].is_ascii_alphabetic() {
                    pad = count;
                    cur = chars[pos];
                    let s = pos;
                    pos += 1;
                    while pos < chars.len() && chars[pos] == cur {
                        pos += 1;
                    }
                    count = pos - s;
                }
                if pad == 0 {
                    return Err(illegal(format!("Pad letter 'p' must be followed by valid pad pattern: {pattern}")));
                }
                pad_next = Some(pad);
            }
            let item = letter(cur, count)?;
            push(&mut stack, &mut value_parser, &mut pad_next, item);
        } else if cur == '\'' {
            let start = pos;
            pos += 1;
            while pos < chars.len() {
                if chars[pos] == '\'' {
                    if pos + 1 < chars.len() && chars[pos + 1] == '\'' {
                        pos += 1;
                    } else {
                        break;
                    }
                }
                pos += 1;
            }
            if pos >= chars.len() {
                return Err(illegal(format!("Pattern ends with an incomplete string literal: {pattern}")));
            }
            let s: String = chars[start + 1..pos].iter().collect();
            let lit = if s.is_empty() { "'".to_string() } else { s.replace("''", "'") };
            push(&mut stack, &mut value_parser, &mut pad_next, Item::Literal(lit));
            pos += 1;
        } else if cur == '[' {
            stack.push(Vec::new());
            value_parser.push(None);
            pos += 1;
        } else if cur == ']' {
            if stack.len() == 1 {
                return Err(illegal("Pattern invalid as it contains ] without previous ["));
            }
            let section = stack.pop().unwrap_or_default();
            value_parser.pop();
            push(&mut stack, &mut value_parser, &mut pad_next, Item::Optional(section));
            pos += 1;
        } else if cur == '{' || cur == '}' || cur == '#' {
            return Err(illegal(format!("Pattern includes reserved character: '{cur}'")));
        } else {
            push(&mut stack, &mut value_parser, &mut pad_next, Item::Literal(cur.to_string()));
            pos += 1;
        }
    }
    // Java closes sections left open at the end.
    while stack.len() > 1 {
        let section = stack.pop().unwrap_or_default();
        value_parser.pop();
        push(&mut stack, &mut value_parser, &mut pad_next, Item::Optional(section));
    }
    Ok(stack.pop().unwrap_or_default())
}

fn too_many(c: char) -> KIRunError {
    illegal(format!("Too many pattern letters: {c}"))
}

/// Java `parsePattern`'s letter rules (and `parseField`).
fn letter(c: char, count: usize) -> Result<Item> {
    let num = |field, min, max, sign| Item::Number { field, min, max, sign, subsequent: 0 };
    let value = |field| num(field, 1, 19, Sign::Normal);
    let fixed = |field, w| num(field, w, w, Sign::NotNegative);
    let text_style = |count: usize, c: char| -> Result<Style> {
        Ok(match count {
            3 => Style::Short,
            4 => Style::Full,
            5 => Style::Narrow,
            _ => return Err(too_many(c)),
        })
    };
    Ok(match c {
        'G' => Item::Text {
            field: Field::Era,
            style: match count {
                1..=3 => Style::Short,
                4 => Style::Full,
                5 => Style::Narrow,
                _ => return Err(too_many(c)),
            },
        },
        'y' | 'u' => {
            let field = if c == 'y' { Field::YearOfEra } else { Field::Year };
            match count {
                2 => Item::Reduced { field },
                1..=3 => num(field, count, 19, Sign::Normal),
                _ => num(field, count, 19, Sign::ExceedsPad),
            }
        }
        'c' if count == 1 => fixed(Field::LocalDayOfWeek, 1),
        'c' if count == 2 => return Err(illegal("Invalid pattern \"cc\"")),
        'e' if count <= 2 => fixed(Field::LocalDayOfWeek, count),
        'E' if count <= 2 => Item::Text { field: Field::DayOfWeek, style: Style::Short },
        'M' | 'L' | 'Q' | 'q' if count == 1 => value(if matches!(c, 'M' | 'L') { Field::Month } else { Field::Quarter }),
        'M' | 'L' | 'Q' | 'q' if count == 2 => fixed(if matches!(c, 'M' | 'L') { Field::Month } else { Field::Quarter }, 2),
        'M' | 'L' => Item::Text { field: Field::Month, style: text_style(count, c)? },
        'Q' | 'q' => Item::Text { field: Field::Quarter, style: text_style(count, c)? },
        'E' | 'e' | 'c' => Item::Text { field: Field::DayOfWeek, style: text_style(count, c)? },
        'a' if count == 1 => Item::Text { field: Field::AmPm, style: Style::Short },
        'a' => return Err(too_many(c)),
        'S' => Item::Fraction { width: count },
        'F' if count == 1 => value(Field::AlignedWeekOfMonth),
        'F' => return Err(too_many(c)),
        'd' | 'h' | 'H' | 'k' | 'K' | 'm' | 's' => {
            let field = match c {
                'd' => Field::DayOfMonth,
                'h' => Field::ClockHourOfAmPm,
                'H' => Field::HourOfDay,
                'k' => Field::ClockHourOfDay,
                'K' => Field::HourOfAmPm,
                'm' => Field::Minute,
                _ => Field::Second,
            };
            match count {
                1 => value(field),
                2 => fixed(field, 2),
                _ => return Err(too_many(c)),
            }
        }
        'D' => match count {
            1 => value(Field::DayOfYear),
            2 | 3 => num(Field::DayOfYear, count, 3, Sign::NotNegative),
            _ => return Err(too_many(c)),
        },
        'A' => num(Field::MilliOfDay, count, 19, Sign::NotNegative),
        'n' => num(Field::NanoOfSecond, count, 19, Sign::NotNegative),
        'N' => num(Field::NanoOfDay, count, 19, Sign::NotNegative),
        'z' => match count {
            1..=3 => Item::ZoneText { full: false },
            4 => Item::ZoneText { full: true },
            _ => return Err(too_many(c)),
        },
        'V' if count == 2 => Item::ZoneId,
        'V' => return Err(illegal(format!("Pattern letter count must be 2: {c}"))),
        'v' => match count {
            1 => Item::GenericZone { full: false },
            4 => Item::GenericZone { full: true },
            _ => return Err(illegal(format!("Wrong number of  pattern letters: {c}"))),
        },
        'Z' => match count {
            1..=3 => Item::Offset { pattern: "+HHMM", zero: "+0000" },
            4 => Item::LocalizedOffset { full: true },
            5 => Item::Offset { pattern: "+HH:MM:ss", zero: "Z" },
            _ => return Err(too_many(c)),
        },
        'O' => match count {
            1 => Item::LocalizedOffset { full: false },
            4 => Item::LocalizedOffset { full: true },
            _ => return Err(illegal(format!("Pattern letter count must be 1 or 4: {c}"))),
        },
        'X' | 'x' => {
            if count > 5 {
                return Err(too_many(c));
            }
            let pattern = OFFSET_PATTERNS[count + usize::from(count != 1)];
            let zero = match (c, count) {
                ('X', _) => "Z",
                (_, 1) => "+00",
                (_, n) if n % 2 == 0 => "+0000",
                _ => "+00:00",
            };
            Item::Offset { pattern, zero }
        }
        'W' if count == 1 => fixed(Field::WeekOfMonth, 1),
        'W' => return Err(too_many(c)),
        'w' if count <= 2 => num(Field::WeekOfWeekBasedYear, count, 2, Sign::NotNegative),
        'w' => return Err(too_many(c)),
        'Y' if count == 2 => Item::Reduced { field: Field::WeekBasedYear },
        'Y' => num(Field::WeekBasedYear, count, 19, if count < 4 { Sign::Normal } else { Sign::ExceedsPad }),
        'B' => Item::DayPeriod {
            style: match count {
                1 => Style::Short,
                4 => Style::Full,
                5 => Style::Narrow,
                _ => return Err(illegal(format!("Wrong number of pattern letters: {c}"))),
            },
        },
        _ => return Err(illegal(format!("Unknown pattern letter: {c}"))),
    })
}

// ------------------------------------------------------------------------------- printing

/// WeekFields arithmetic (Java `ComputedDayOfField`).
fn localized_dow(date: NaiveDate, ld: &LocaleData) -> i64 {
    let iso = date.weekday().number_from_monday() as i64;
    (iso - ld.first_day as i64).rem_euclid(7) + 1
}

fn start_of_week_offset(day: i64, dow: i64, ld: &LocaleData) -> i64 {
    let week_start = (day - dow).rem_euclid(7);
    if week_start + 1 > ld.min_days as i64 {
        7 - week_start
    } else {
        -week_start
    }
}

fn compute_week(offset: i64, day: i64) -> i64 {
    (7 + offset + (day - 1)).div_euclid(7)
}

fn week_of_month(date: NaiveDate, ld: &LocaleData) -> i64 {
    let dow = localized_dow(date, ld);
    let dom = date.day() as i64;
    compute_week(start_of_week_offset(dom, dow, ld), dom)
}

fn year_length(y: i32) -> i64 {
    if super::java_time::is_leap(y) {
        366
    } else {
        365
    }
}

fn week_of_week_based_year(date: NaiveDate, ld: &LocaleData) -> i64 {
    let dow = localized_dow(date, ld);
    let doy = date.ordinal() as i64;
    let offset = start_of_week_offset(doy, dow, ld);
    let week = compute_week(offset, doy);
    if week == 0 {
        return week_of_week_based_year(date - chrono::Duration::days(doy), ld);
    }
    if week > 50 {
        let new_year_week = compute_week(offset, year_length(date.year()) + ld.min_days as i64);
        if week >= new_year_week {
            return week - new_year_week + 1;
        }
    }
    week
}

fn week_based_year(date: NaiveDate, ld: &LocaleData) -> i64 {
    let dow = localized_dow(date, ld);
    let year = date.year() as i64;
    let doy = date.ordinal() as i64;
    let offset = start_of_week_offset(doy, dow, ld);
    let week = compute_week(offset, doy);
    if week == 0 {
        return year - 1;
    }
    let new_year_week = compute_week(offset, year_length(date.year()) + ld.min_days as i64);
    if week >= new_year_week {
        year + 1
    } else {
        year
    }
}

fn field_value(z: &ZonedDateTime, field: Field, ld: &LocaleData) -> i64 {
    let l = z.local;
    let d = l.date();
    match field {
        Field::Era => i64::from(l.year() > 0),
        Field::YearOfEra => {
            let y = l.year() as i64;
            if y > 0 {
                y
            } else {
                1 - y
            }
        }
        Field::Year => l.year() as i64,
        Field::Quarter => ((l.month() - 1) / 3 + 1) as i64,
        Field::Month => l.month() as i64,
        Field::DayOfMonth => l.day() as i64,
        Field::DayOfYear => l.ordinal() as i64,
        Field::DayOfWeek => l.weekday().number_from_monday() as i64,
        Field::AlignedWeekOfMonth => ((l.day() - 1) / 7 + 1) as i64,
        Field::AmPm => i64::from(l.hour() >= 12),
        Field::ClockHourOfAmPm => {
            let h = (l.hour() % 12) as i64;
            if h == 0 {
                12
            } else {
                h
            }
        }
        Field::HourOfAmPm => (l.hour() % 12) as i64,
        Field::ClockHourOfDay => {
            if l.hour() == 0 {
                24
            } else {
                l.hour() as i64
            }
        }
        Field::HourOfDay => l.hour() as i64,
        Field::Minute => l.minute() as i64,
        Field::Second => l.second() as i64,
        Field::NanoOfSecond => z.nano() as i64,
        Field::MilliOfDay => l.num_seconds_from_midnight() as i64 * 1000 + (z.nano() / 1_000_000) as i64,
        Field::NanoOfDay => l.num_seconds_from_midnight() as i64 * 1_000_000_000 + z.nano() as i64,
        Field::LocalDayOfWeek => localized_dow(d, ld),
        Field::WeekOfMonth => week_of_month(d, ld),
        Field::WeekOfWeekBasedYear => week_of_week_based_year(d, ld),
        Field::WeekBasedYear => week_based_year(d, ld),
        Field::OffsetSeconds => z.offset as i64,
    }
}

const EXCEED_POINTS: [i64; 11] =
    [0, 10, 100, 1000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000, 10_000_000_000];

fn format_number(field: Field, value: i64, min: usize, max: usize, sign: Sign, ld: &LocaleData) -> Result<String> {
    let digits = value.unsigned_abs().to_string();
    if digits.len() > max {
        return Err(java_error(format!(
            "Field {} cannot be printed as the value {value} exceeds the maximum print width of {max}",
            field.java_name(ld)
        )));
    }
    let mut out = String::new();
    if value >= 0 {
        if sign == Sign::ExceedsPad && min < 11 && value >= EXCEED_POINTS[min] {
            out.push('+');
        }
    } else if sign == Sign::NotNegative {
        return Err(java_error(format!(
            "Field {} cannot be printed as the value {value} cannot be negative according to the SignStyle",
            field.java_name(ld)
        )));
    } else {
        out.push('-');
    }
    for _ in digits.len()..min {
        out.push('0');
    }
    out.push_str(&digits);
    Ok(out)
}

/// Java `OffsetIdPrinterParser.format`.
fn format_offset(total: i32, pattern: &str, zero: &str) -> String {
    if total == 0 {
        return zero.into();
    }
    let ty = OFFSET_PATTERNS.iter().position(|p| *p == pattern).unwrap_or(0);
    let colon = pattern.contains(':');
    let a = total.unsigned_abs();
    let (h, m, s) = ((a / 3600) % 100, (a / 60) % 60, a % 60);
    let mut out = format!("{}{h:02}", if total < 0 { '-' } else { '+' });
    let mut output = h;
    if (3..=8).contains(&ty) || (ty >= 9 && s > 0) || (ty >= 1 && m > 0) {
        if colon {
            out.push(':');
        }
        out.push_str(&format!("{m:02}"));
        output += m;
        if ty == 7 || ty == 8 || (ty >= 5 && s > 0) {
            if colon {
                out.push(':');
            }
            out.push_str(&format!("{s:02}"));
            output += s;
        }
    }
    if output == 0 {
        return zero.into();
    }
    out
}

fn print(items: &[Item], z: &ZonedDateTime, ld: &LocaleData, out: &mut String) -> Result<()> {
    for item in items {
        print_item(item, z, ld, out)?;
    }
    Ok(())
}

fn print_item(item: &Item, z: &ZonedDateTime, ld: &LocaleData, out: &mut String) -> Result<()> {
    match item {
        Item::Literal(s) => out.push_str(s),
        Item::Number { field, min, max, sign, .. } => {
            out.push_str(&format_number(*field, field_value(z, *field, ld), *min, *max, *sign, ld)?)
        }
        Item::Reduced { field } => out.push_str(&format!("{:02}", field_value(z, *field, ld).rem_euclid(100))),
        Item::Fraction { width } => {
            let nanos = format!("{:09}", z.nano());
            let w = (*width).min(9);
            out.push_str(&nanos[..w]);
            for _ in 9..*width {
                out.push('0');
            }
        }
        Item::Text { field, style } => {
            let v = field_value(z, *field, ld);
            out.push_str(&match field {
                Field::Month => month_text(v as u32, *style, ld),
                Field::DayOfWeek => day_text(v as u32, *style),
                Field::AmPm => am_pm_text(v == 1, ld).into(),
                Field::Era => era_text(v == 1, *style).into(),
                Field::Quarter => quarter_text(v as u32, *style),
                _ => v.to_string(),
            })
        }
        Item::Offset { pattern, zero } => out.push_str(&format_offset(z.offset, pattern, zero)),
        Item::LocalizedOffset { full } => out.push_str(&localized_offset(z.offset, *full)),
        Item::ZoneId => out.push_str(&z.zone.id()),
        Item::ZoneText { full } => out.push_str(&zone_text(z, *full)),
        Item::GenericZone { full } => out.push_str(&zone_display_name(&z.zone, !*full)),
        Item::DayPeriod { style } => {
            out.push_str(day_period_text(z.local.hour() * 60 + z.local.minute(), *style));
        }
        Item::Optional(items) => print(items, z, ld, out)?,
        Item::Pad { width, item } => {
            let mut inner = String::new();
            print_item(item, z, ld, &mut inner)?;
            let len = inner.chars().count();
            if len > *width {
                return Err(java_error(format!(
                    "Cannot print as output of {len} characters exceeds pad width of {width}"
                )));
            }
            for _ in len..*width {
                out.push(' ');
            }
            out.push_str(&inner);
        }
    }
    Ok(())
}

/// Java `zdt.format(DateTimeFormatter.ofPattern(pattern))` in the default locale.
pub fn format_pattern(z: &ZonedDateTime, pattern: &str) -> Result<String> {
    let items = compile(pattern)?;
    let mut out = String::new();
    print(&items, z, &locale(), &mut out)?;
    Ok(out)
}

// -------------------------------------------------------------------------------- parsing

#[derive(Clone, Default)]
struct Parsed {
    fields: HashMap<Field, i64>,
    zone: Option<ZoneId>,
}

/// `Err(position)`: where the parse failed (Java `~position`).
type Step = std::result::Result<usize, usize>;

impl Parsed {
    /// Java `setParsedField`: the same field twice with different values fails at `error`.
    fn set(&mut self, field: Field, value: i64, error: usize, success: usize) -> Step {
        match self.fields.insert(field, value) {
            Some(old) if old != value => Err(error),
            _ => Ok(success),
        }
    }
}

fn parse_items(items: &[Item], text: &[char], mut pos: usize, p: &mut Parsed, ld: &LocaleData) -> Step {
    for item in items {
        pos = parse_item(item, text, pos, p, ld)?;
    }
    Ok(pos)
}

fn digit(c: Option<&char>) -> Option<i64> {
    c.and_then(|c| c.to_digit(10)).map(|d| d as i64)
}

fn parse_item(item: &Item, text: &[char], pos: usize, p: &mut Parsed, ld: &LocaleData) -> Step {
    match item {
        Item::Literal(s) => {
            let lit: Vec<char> = s.chars().collect();
            if text.len() >= pos + lit.len() && text[pos..pos + lit.len()] == lit[..] {
                Ok(pos + lit.len())
            } else {
                Err(pos)
            }
        }
        Item::Number { field, min, max, sign, subsequent } => {
            parse_number(text, pos, *field, *min, *max, *sign, *subsequent, p)
        }
        Item::Reduced { field } => {
            let (Some(a), Some(b)) = (digit(text.get(pos)), digit(text.get(pos + 1))) else { return Err(pos) };
            // Base 2000: two digits read as 2000..2099.
            p.set(*field, 2000 + a * 10 + b, pos, pos + 2)
        }
        Item::Fraction { width } => {
            let mut v = 0i64;
            for i in 0..*width {
                match digit(text.get(pos + i)) {
                    Some(d) => v = v * 10 + d,
                    None => return Err(pos),
                }
            }
            let nanos = if *width <= 9 { v * 10i64.pow(9 - *width as u32) } else { v / 10i64.pow(*width as u32 - 9) };
            p.set(Field::NanoOfSecond, nanos, pos, pos + width)
        }
        Item::Text { field, style } => {
            let mut options: Vec<(String, i64)> = match field {
                Field::Month => (1..=12).map(|m| (month_text(m, *style, ld), m as i64)).collect(),
                Field::DayOfWeek => (1..=7).map(|d| (day_text(d, *style), d as i64)).collect(),
                Field::AmPm => vec![(am_pm_text(false, ld).into(), 0), (am_pm_text(true, ld).into(), 1)],
                Field::Era => vec![(era_text(true, *style).into(), 1), (era_text(false, *style).into(), 0)],
                Field::Quarter => (1..=4).map(|q| (quarter_text(q, *style), q as i64)).collect(),
                _ => vec![],
            };
            options.sort_by(|a, b| b.0.chars().count().cmp(&a.0.chars().count()));
            for (t, v) in options {
                let t: Vec<char> = t.chars().collect();
                if text.len() >= pos + t.len() && text[pos..pos + t.len()] == t[..] {
                    return p.set(*field, v, pos, pos + t.len());
                }
            }
            Err(pos)
        }
        Item::Offset { pattern, zero } => parse_offset(text, pos, pattern, zero, p),
        Item::ZoneId => {
            let s: String = text[pos.min(text.len())..].iter().collect();
            match parse_zone_id(s.as_bytes(), 0) {
                Some((zone, end)) => {
                    p.zone = Some(zone);
                    Ok(pos + s[..end].chars().count())
                }
                None => Err(pos),
            }
        }
        // kirun-java's patterns never parse zone names or day periods in stored functions;
        // refuse them rather than guess.
        Item::ZoneText { .. } | Item::GenericZone { .. } | Item::LocalizedOffset { .. } | Item::DayPeriod { .. } => {
            Err(pos)
        }
        Item::Optional(items) => {
            let mut trial = p.clone();
            match parse_items(items, text, pos, &mut trial, ld) {
                Ok(end) => {
                    *p = trial;
                    Ok(end)
                }
                Err(_) => Ok(pos),
            }
        }
        Item::Pad { width, item } => {
            let end = (pos + width).min(text.len());
            let mut start = pos;
            while start < end && text[start] == ' ' {
                start += 1;
            }
            let r = parse_item(item, &text[..end], start, p, ld)?;
            if r != end {
                return Err(pos);
            }
            Ok(r)
        }
    }
}

/// Java `NumberPrinterParser.parse` in strict mode, with adjacent value parsing.
#[allow(clippy::too_many_arguments)]
fn parse_number(
    text: &[char],
    position: usize,
    field: Field,
    min: usize,
    max: usize,
    sign: Sign,
    subsequent: usize,
    p: &mut Parsed,
) -> Step {
    if position >= text.len() {
        return Err(position);
    }
    let fixed = min == max && sign == Sign::NotNegative;
    let mut pos = position;
    let (mut negative, mut positive) = (false, false);
    match text[pos] {
        '+' => {
            // NORMAL refuses '+' when strict; NOT_NEGATIVE refuses any sign.
            if sign != Sign::ExceedsPad {
                return Err(position);
            }
            positive = true;
            pos += 1;
        }
        '-' => {
            if sign == Sign::NotNegative {
                return Err(position);
            }
            negative = true;
            pos += 1;
        }
        _ => {}
    }
    let start = pos;
    if start + min > text.len() {
        return Err(start);
    }
    let mut eff_max = max + subsequent;
    let mut total: i64;
    let mut end;
    let mut pass = 0;
    loop {
        total = 0;
        end = start;
        let max_end = (start + eff_max).min(text.len());
        while end < max_end {
            match digit(text.get(end)) {
                Some(d) => {
                    total = total.saturating_mul(10).saturating_add(d);
                    end += 1;
                }
                None => break,
            }
        }
        if end < start + min {
            return Err(start);
        }
        if subsequent > 0 && pass == 0 && !fixed {
            eff_max = min.max((end - start).saturating_sub(subsequent));
            pass = 1;
            continue;
        }
        break;
    }
    if negative {
        if total == 0 {
            return Err(start - 1);
        }
        total = -total;
    } else if sign == Sign::ExceedsPad {
        let len = end - start;
        if positive {
            if len <= min {
                return Err(start - 1);
            }
        } else if len > min {
            return Err(start);
        }
    }
    p.set(field, total, position, end)
}

/// Java `OffsetIdPrinterParser.parse` (strict) for `X`, `x` and `Z`.
fn parse_offset(text: &[char], pos: usize, pattern: &str, zero: &str, p: &mut Parsed) -> Step {
    let zero_chars: Vec<char> = zero.chars().collect();
    if text.len() >= pos + zero_chars.len() && text[pos..pos + zero_chars.len()] == zero_chars[..] {
        return p.set(Field::OffsetSeconds, 0, pos, pos + zero_chars.len());
    }
    let sign = match text.get(pos) {
        Some('+') => 1,
        Some('-') => -1,
        _ => return Err(pos),
    };
    let two = |at: usize| -> Option<i64> { Some(digit(text.get(at))? * 10 + digit(text.get(at + 1))?) };
    let colon = pattern.contains(':');
    let Some(h) = two(pos + 1) else { return Err(pos) };
    let mut end = pos + 3;
    let mut m = 0;
    let mut s = 0;
    let minutes_required = pattern.contains("MM");
    let at_colon = |at: usize| text.get(at) == Some(&':');
    if pattern.len() > 3 {
        let mstart = if colon { end + 1 } else { end };
        let have = (!colon || at_colon(end)) && two(mstart).is_some();
        if have {
            m = two(mstart).unwrap_or(0);
            end = mstart + 2;
            if pattern.ends_with("ss") || pattern.ends_with("SS") {
                let sstart = if colon { end + 1 } else { end };
                if (!colon || at_colon(end)) && two(sstart).is_some() {
                    s = two(sstart).unwrap_or(0);
                    end = sstart + 2;
                }
            }
        } else if minutes_required {
            return Err(pos);
        }
    }
    if h > 23 || m > 59 || s > 59 {
        return Err(pos);
    }
    p.set(Field::OffsetSeconds, sign * (h * 3600 + m * 60 + s), pos, end)
}

/// `Parsed.updateCheckConflict(target, change, value)` during time resolution.
fn change(fields: &mut HashMap<Field, i64>, target: Field, field: Field, value: i64, ld: &LocaleData) -> Result<()> {
    if let Some(old) = fields.insert(field, value) {
        if old != value {
            return Err(java_error(format!(
                "Conflict found: {} {old} differs from {} {value} while resolving  {}",
                field.java_name(ld),
                field.java_name(ld),
                target.java_name(ld)
            )));
        }
    }
    Ok(())
}

fn check(field: Field, value: i64, lo: i64, hi: i64, range: &str, ld: &LocaleData) -> Result<i64> {
    if value < lo || value > hi {
        return Err(invalid_value(&field.java_name(ld), range, value));
    }
    Ok(value)
}

/// kirun-java `FromDateString`: `appendPattern(pattern)` plus `parseDefaulting` of year-of-era,
/// month and day (from now), and zero time, read with the SMART resolver in the system zone.
pub fn parse_with_defaults(text: &str, pattern: &str, now: &ZonedDateTime) -> Result<ZonedDateTime> {
    let items = compile(pattern)?;
    let ld = locale();
    let chars: Vec<char> = text.chars().collect();
    let mut p = Parsed::default();
    let end = parse_items(&items, &chars, 0, &mut p, &ld).map_err(|at| parse_error_at(text, at))?;
    for (field, value) in [
        (Field::YearOfEra, now.local.year() as i64),
        (Field::Month, now.local.month() as i64),
        (Field::DayOfMonth, now.local.day() as i64),
        (Field::HourOfDay, 0),
        (Field::Minute, 0),
        (Field::Second, 0),
        (Field::NanoOfSecond, 0),
    ] {
        p.fields.entry(field).or_insert(value);
    }
    if end < chars.len() {
        return Err(unparsed_error(text, end));
    }
    resolve(p, &ld).map_err(|e| resolve_error(text, &e))
}

fn resolve(p: Parsed, ld: &LocaleData) -> Result<ZonedDateTime> {
    let mut f = p.fields;

    // Localized day-of-week becomes ISO day-of-week.
    if let Some(v) = f.remove(&Field::LocalDayOfWeek) {
        let v = check(Field::LocalDayOfWeek, v, 1, 7, "1 - 7", ld)?;
        let iso = (v - 1 + ld.first_day as i64 - 1).rem_euclid(7) + 1;
        if let Some(old) = f.insert(Field::DayOfWeek, iso) {
            if old != iso {
                return Err(java_error(format!(
                    "Conflict found: {} {old} differs from {} {iso}",
                    Field::DayOfWeek.java_name(ld),
                    Field::DayOfWeek.java_name(ld)
                )));
            }
        }
    }

    // Date: year-of-era (with era) to year, then year-month-day.
    if let Some(yoe) = f.remove(&Field::YearOfEra) {
        check(Field::YearOfEra, yoe, 1, 1_000_000_000, "1 - 999999999/1000000000", ld)?;
        let year = match f.remove(&Field::Era) {
            None => match f.get(&Field::Year) {
                Some(y) if *y <= 0 => 1 - yoe,
                _ => yoe,
            },
            Some(1) => yoe,
            Some(0) => 1 - yoe,
            Some(e) => return Err(java_error(format!("Invalid value for era: {e}"))),
        };
        if let Some(old) = f.insert(Field::Year, year) {
            if old != year {
                return Err(java_error(format!("Conflict found: Year {old} differs from Year {year}")));
            }
        }
    }
    let y = f.remove(&Field::Year).unwrap_or(0);
    let m = f.remove(&Field::Month).unwrap_or(1);
    let d = f.remove(&Field::DayOfMonth).unwrap_or(1);
    let mut date = resolve_ymd(y, m, d)?;

    // Time: clock hours, am/pm, milli/nano of day, then the four fields.
    if let Some(ch) = f.remove(&Field::ClockHourOfDay) {
        if ch != 0 {
            check(Field::ClockHourOfDay, ch, 1, 24, "1 - 24", ld)?;
        }
        change(&mut f, Field::ClockHourOfDay, Field::HourOfDay, if ch == 24 { 0 } else { ch }, ld)?;
    }
    if let Some(ch) = f.remove(&Field::ClockHourOfAmPm) {
        if ch != 0 {
            check(Field::ClockHourOfAmPm, ch, 1, 12, "1 - 12", ld)?;
        }
        change(&mut f, Field::ClockHourOfAmPm, Field::HourOfAmPm, if ch == 12 { 0 } else { ch }, ld)?;
    }
    if f.contains_key(&Field::AmPm) && f.contains_key(&Field::HourOfAmPm) {
        let ap = f.remove(&Field::AmPm).unwrap_or(0);
        let hap = f.remove(&Field::HourOfAmPm).unwrap_or(0);
        check(Field::AmPm, ap, 0, 1, "0 - 1", ld)?;
        check(Field::HourOfAmPm, hap, 0, 11, "0 - 11", ld)?;
        change(&mut f, Field::AmPm, Field::HourOfDay, ap * 12 + hap, ld)?;
    }
    if let Some(nod) = f.remove(&Field::NanoOfDay) {
        check(Field::NanoOfDay, nod, 0, 86_399_999_999_999, "0 - 86399999999999", ld)?;
        change(&mut f, Field::NanoOfDay, Field::HourOfDay, nod / 3_600_000_000_000, ld)?;
        change(&mut f, Field::NanoOfDay, Field::Minute, (nod / 60_000_000_000) % 60, ld)?;
        change(&mut f, Field::NanoOfDay, Field::Second, (nod / 1_000_000_000) % 60, ld)?;
        change(&mut f, Field::NanoOfDay, Field::NanoOfSecond, nod % 1_000_000_000, ld)?;
    }
    if let Some(lod) = f.remove(&Field::MilliOfDay) {
        check(Field::MilliOfDay, lod, 0, 86_399_999, "0 - 86399999", ld)?;
        let sod = lod / 1000;
        change(&mut f, Field::MilliOfDay, Field::HourOfDay, sod / 3600, ld)?;
        change(&mut f, Field::MilliOfDay, Field::Minute, (sod / 60) % 60, ld)?;
        change(&mut f, Field::MilliOfDay, Field::Second, sod % 60, ld)?;
        let nos = f.get(&Field::NanoOfSecond).copied().unwrap_or(0);
        change(&mut f, Field::MilliOfDay, Field::NanoOfSecond, (lod % 1000) * 1_000_000 + nos % 1_000_000, ld)?;
    }
    let h = f.remove(&Field::HourOfDay).unwrap_or(0);
    let mi = f.remove(&Field::Minute).unwrap_or(0);
    let s = f.remove(&Field::Second).unwrap_or(0);
    let nano = f.remove(&Field::NanoOfSecond).unwrap_or(0);
    let (time, extra) = resolve_time(h, mi, s, nano)?;
    date += chrono::Duration::days(extra);

    // What is left is checked against the date and the time (Java `crossCheck`).
    let offset = f.remove(&Field::OffsetSeconds);
    let zdt_for = |z: ZoneId| ZonedDateTime::of_local(NaiveDateTime::new(date, time), z, None);
    let probe = zdt_for(ZoneId::Offset(0));
    let mut remaining: Vec<(Field, i64)> = f.into_iter().collect();
    remaining.sort_by_key(|(field, _)| *field as u32);
    for (field, parsed) in remaining {
        let actual = field_value(&probe, field, ld);
        if actual != parsed {
            let target = if is_time_field(field) { java_time_string(time) } else { date.format("%Y-%m-%d").to_string() };
            return Err(java_error(format!(
                "Conflict found: Field {} {actual} differs from {} {parsed} derived from {target}",
                field.java_name(ld),
                field.java_name(ld)
            )));
        }
    }

    let zone = p.zone.unwrap_or_else(system_default_zone);
    let local = NaiveDateTime::new(date, time);
    match offset {
        Some(o) => {
            let o = offset_of_total(o)?;
            let epoch = local.and_utc().timestamp() - o as i64;
            ZonedDateTime::of_instant(epoch, time.nanosecond(), zone)
        }
        None => Ok(ZonedDateTime::of_local(local, zone, None)),
    }
}

fn is_time_field(f: Field) -> bool {
    matches!(f, Field::AmPm | Field::HourOfAmPm | Field::ClockHourOfAmPm | Field::ClockHourOfDay | Field::HourOfDay)
}

/// Java `LocalTime.toString()`: `HH:mm`, then `:ss` and the fraction when not zero.
fn java_time_string(t: NaiveTime) -> String {
    let mut s = format!("{:02}:{:02}", t.hour(), t.minute());
    let (sec, nano) = (t.second(), t.nanosecond());
    if sec > 0 || nano > 0 {
        s.push_str(&format!(":{sec:02}"));
        if nano > 0 {
            if nano % 1_000_000 == 0 {
                s.push_str(&format!(".{:03}", nano / 1_000_000));
            } else if nano % 1000 == 0 {
                s.push_str(&format!(".{:06}", nano / 1000));
            } else {
                s.push_str(&format!(".{nano:09}"));
            }
        }
    }
    s
}

// --------------------------------------------------------------- kirun-java's DateUtil

/// kirun-java `DateUtil.LUXON_DATEFORMATTER_MAP`.
fn luxon_token(token: &str) -> Option<&'static str> {
    Some(match token {
        "S" => "S",
        "SSS" => "SSS",
        "u" => "SSS",
        "uu" => "SS",
        "uuu" => "S",
        "s" => "s",
        "ss" => "ss",
        "m" => "m",
        "mm" => "mm",
        "h" => "h",
        "hh" => "hh",
        "H" => "H",
        "HH" => "HH",
        "Z" => "X",
        "ZZ" => "XX",
        "ZZZ" => "XXX",
        "ZZZZ" => "z",
        "ZZZZZ" => "VV",
        "z" => "VV",
        "a" => "a",
        "d" => "d",
        "dd" => "dd",
        "c" => "e",
        "ccc" => "EEE",
        "cccc" => "EEEE",
        "ccccc" => "EEEEE",
        "L" => "M",
        "LL" => "MM",
        "LLL" => "MMM",
        "LLLL" => "MMMM",
        "LLLLL" => "MMMMM",
        "y" => "y",
        "yy" => "yy",
        "yyyy" => "yyyy",
        "G" => "G",
        "GG" => "GG",
        "GGGGG" => "G",
        "q" => "Q",
        "qq" => "QQ",
        "kk" => "u",
        "kkkk" => "uuuu",
        "W" => "w",
        "WW" => "ww",
        "o" => "D",
        "ooo" => "DDD",
        "D" => "M/d/yyyy",
        "DD" => "MMM d, yyyy",
        "DDD" => "MMMM d, yyyy",
        "DDDD" => "EEEE, MMMM d, yyyy",
        "t" => "h:mm a",
        "tt" => "h:mm:ss a",
        "T" => "HH:mm",
        "TT" => "HH:mm:ss",
        _ => return None,
    })
}

/// kirun-java `DateUtil.toDateTimeFormat`: the longest luxon token (up to five characters) at
/// each position becomes its Java pattern; other characters stay. Quotes are not special, so a
/// quoted `T` becomes `HH:mm`, as in kirun-java.
pub fn to_date_time_format(luxon: &str) -> String {
    let chars: Vec<char> = luxon.chars().collect();
    let mut out = String::new();
    let mut i = 0;
    while i < chars.len() {
        let mut len = 5.min(chars.len() - i);
        let mut matched = None;
        while len >= 1 {
            let token: String = chars[i..i + len].iter().collect();
            if let Some(java) = luxon_token(&token) {
                matched = Some((java, len));
                break;
            }
            len -= 1;
        }
        match matched {
            Some((java, len)) => {
                out.push_str(java);
                i += len;
            }
            None => {
                out.push(chars[i]);
                i += 1;
            }
        }
    }
    out
}

/// kirun-java `DateUtil.toRelative(from, now, units, round, format)` with the `long` unit names
/// (`FromNow` passes the format with its JSON quotes, so it never matches `short`/`narrow`).
pub fn to_relative(from: &ZonedDateTime, now: &ZonedDateTime, units: &[super::java_time::Unit], round: bool) -> String {
    use super::java_time::Unit;
    if from.java_equals(now) {
        return "now".into();
    }
    let diff = from.epoch_second() - now.epoch_second();
    let future = diff > 0;
    let diff = diff.abs();

    let mut sorted: Vec<Unit> = units.to_vec();
    sorted.sort_by_key(|u| std::cmp::Reverse(u.duration_millis()));
    let best = sorted.into_iter().find(|u| diff <= u.duration_millis()).unwrap_or_else(|| {
        let within = |end: i64, unit: Unit| diff <= end * unit.duration_seconds();
        if within(50, Unit::Seconds) {
            Unit::Seconds
        } else if within(50, Unit::Minutes) {
            Unit::Minutes
        } else if within(23, Unit::Hours) {
            Unit::Hours
        } else if within(21, Unit::Days) {
            Unit::Weeks
        } else if within(29, Unit::Days) {
            Unit::Days
        } else if within(11, Unit::Months) {
            Unit::Months
        } else {
            Unit::Years
        }
    });

    let magnitude = diff as f64 / best.duration_seconds() as f64;
    let plural = (magnitude - 1.0).abs() > 0.001;
    let name = best.name().to_lowercase();
    let unit = if plural { name } else { name[..name.len() - 1].to_string() };
    let amount = if round {
        java_round(magnitude).to_string()
    } else {
        let v = java_round(magnitude * 100.0) as f64 / 100.0;
        if v == (v as i64) as f64 {
            (v as i64).to_string()
        } else {
            java_double_string(v)
        }
    };
    if future {
        format!("in {amount} {unit}")
    } else {
        format!("{amount} {unit} ago")
    }
}

/// Java `Math.round(double)`: halves up, `NaN` to 0, saturating.
fn java_round(v: f64) -> i64 {
    if v.is_nan() {
        return 0;
    }
    (v + 0.5).floor() as i64
}

/// Java `Double.toString` for the values `toRelative` prints (two decimals at most).
fn java_double_string(v: f64) -> String {
    if v.is_infinite() {
        return if v > 0.0 { "Infinity".into() } else { "-Infinity".into() };
    }
    let a = v.abs();
    if (1e-3..1e7).contains(&a) {
        let s = format!("{v}");
        if s.contains('.') {
            s
        } else {
            format!("{s}.0")
        }
    } else {
        let exp = a.log10().floor() as i32;
        let mantissa = v / 10f64.powi(exp);
        let m = format!("{mantissa}");
        format!("{}E{exp}", if m.contains('.') { m } else { format!("{m}.0") })
    }
}

/// `ZoneId.getAvailableZoneIds()` display names (`GetNames TIMEZONES`): kirun-java lists them
/// in its `HashSet`'s order, which cannot be reproduced; kirun-rust lists them by ID.
pub fn zone_names_list() -> Vec<String> {
    java_zone_ids()
        .iter()
        .filter_map(|id| ZoneId::region(id).ok())
        .map(|z| zone_display_name(&z, false))
        .collect()
}

/// Java `ZonedDateTime.now().withMonth(m).format(ofPattern("MMMM"))` for every month.
pub fn month_names() -> Vec<String> {
    let ld = locale();
    (1..=12).map(|m| month_text(m, Style::Full, &ld)).collect()
}

/// `with(DAY_OF_WEEK, d).format(ofPattern("EEEE"))`, Monday first.
pub fn weekday_names() -> Vec<String> {
    (1..=7).map(|d| day_text(d, Style::Full)).collect()
}

/// The ISO day (1 = Monday) weeks start on in the default locale (`WeekFields.of(locale)`).
pub fn first_day_of_week() -> u32 {
    locale().first_day
}

/// Java's offset ID, for callers outside this module.
pub fn zone_offset_id(total: i32) -> String {
    offset_id(total)
}

#[cfg(test)]
mod tests {
    use super::super::java_time::{set_thread_locale, set_thread_zone};
    use super::*;

    fn zdt(s: &str) -> ZonedDateTime {
        super::super::java_time::get_date_time(s).unwrap()
    }

    #[test]
    fn formats_like_java() {
        set_thread_locale(Some("en_IN"));
        let z = zdt("2024-09-05T14:07:09.045+05:30");
        let f = |p: &str| format_pattern(&z, &to_date_time_format(p)).unwrap_or_else(|e| format!("ERR {e}"));
        assert_eq!(f("MM/dd/yyyy hh:mm a"), "09/05/2024 02:07 pm");
        assert_eq!(f("DD"), "Sept 5, 2024");
        assert_eq!(f("DDDD"), "Thursday, September 5, 2024");
        assert_eq!(f("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"), "2024-09-05HH:mm14:07:09.045+0530");
        assert_eq!(f("u"), "045");
        assert_eq!(f("ZZZZ"), "+05:30");
        assert_eq!(f("QQQQ"), "3rd quarter");
        assert_eq!(f("aa"), "ERR Too many pattern letters: a");
        assert_eq!(f("zzzz"), "ERR Pattern letter count must be 2: V");
        assert_eq!(f("B"), "in the afternoon");
        set_thread_locale(Some("en_US"));
        assert_eq!(f("t"), "2:07 PM");
        assert_eq!(f("LLL"), "Sep");
        set_thread_locale(None);
    }

    #[test]
    fn parses_like_java() {
        set_thread_locale(Some("en_IN"));
        set_thread_zone(Some(ZoneId::of("Asia/Kolkata").unwrap()));
        let now = zdt("2026-10-17T13:00:00+05:30");
        let p = |s: &str, f: &str| {
            parse_with_defaults(s, &to_date_time_format(f), &now).map(|z| z.format_iso()).unwrap_or_else(|e| format!("ERR {e}"))
        };
        assert_eq!(p("2024-03", "yyyy-dd"), "2024-10-03T00:00:00.000+05:30");
        assert_eq!(p("20240115", "yyyyMMdd"), "2024-01-15T00:00:00.000+05:30");
        assert_eq!(p("2024-01-15", "uuuu-MM-dd"), "2026-01-15T00:00:00.202+05:30");
        assert_eq!(p("10:30 AM", "hh:mm a"), "ERR Text '10:30 AM' could not be parsed at index 6");
        assert_eq!(
            p("10:30 am", "hh:mm a"),
            "ERR Text '10:30 am' could not be parsed: Conflict found: HourOfDay 0 differs from HourOfDay 10 while resolving  AmPmOfDay"
        );
        assert_eq!(
            p("2024-01-15 Tue", "yyyy-MM-dd EEE"),
            "ERR Text '2024-01-15 Tue' could not be parsed: Conflict found: Field DayOfWeek 1 differs from DayOfWeek 2 derived from 2024-01-15"
        );
        assert_eq!(p("2024-01-15 Asia/Tokyo", "yyyy-MM-dd z"), "2024-01-15T00:00:00.000+09:00");
        assert_eq!(p("1/2/3", "d/M/y"), "0003-02-01T00:00:00.000+05:53:28");
        set_thread_locale(None);
        set_thread_zone(None);
    }
}

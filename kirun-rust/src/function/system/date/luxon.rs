//! The parts of luxon 3.7 (the date library behind kirun-js) that the System.Date functions use,
//! ported so results match kirun-js: ISO parsing and printing, zones (fixed offsets, IANA, the
//! system zone), `set`/`plus` with luxon's offset fixing, `startOf`/`endOf`, durations with
//! luxon's casual conversion matrix, calendar `diff`, `toFormat`/`fromFormat` and `toRelative`.
//!
//! Locale text (month/weekday names, localized dates, relative time) covers English and French;
//! other locales fall back to English. Algorithms follow `node_modules/luxon/src`.

use std::cell::RefCell;
use std::str::FromStr;

use chrono::{Offset, TimeZone};
use chrono_tz::Tz;

use crate::util::primitive::number_value;
use crate::{Map, Value};

// ---------------------------------------------------------------- calendar arithmetic

const MS_PER_DAY: i64 = 86_400_000;

/// Days since 1970-01-01 of a proleptic Gregorian date (Howard Hinnant's algorithm).
pub fn days_from_civil(y: i64, m: i64, d: i64) -> i64 {
    let y = if m <= 2 { y - 1 } else { y };
    let era = y.div_euclid(400);
    let yoe = y - era * 400;
    let mp = (m + 9) % 12;
    let doy = (153 * mp + 2) / 5 + d - 1;
    let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    era * 146_097 + doe - 719_468
}

pub fn civil_from_days(z: i64) -> (i64, i64, i64) {
    let z = z + 719_468;
    let era = z.div_euclid(146_097);
    let doe = z - era * 146_097;
    let yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365;
    let y = yoe + era * 400;
    let doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    let mp = (5 * doy + 2) / 153;
    let d = doy - (153 * mp + 2) / 5 + 1;
    let m = if mp < 10 { mp + 3 } else { mp - 9 };
    (if m <= 2 { y + 1 } else { y }, m, d)
}

pub fn is_leap_year(y: i64) -> bool {
    (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
}

/// luxon `daysInMonth`, accepting an out-of-range month.
pub fn days_in_month(year: i64, month: i64) -> i64 {
    let mod_month = (month - 1).rem_euclid(12) + 1;
    let mod_year = year + (month - mod_month) / 12;
    match mod_month {
        2 => {
            if is_leap_year(mod_year) {
                29
            } else {
                28
            }
        }
        4 | 6 | 9 | 11 => 30,
        _ => 31,
    }
}

/// Local calendar fields (luxon `c`). Fields may be out of range before normalisation.
#[derive(Debug, Clone, Copy, PartialEq)]
pub struct Fields {
    pub year: i64,
    pub month: i64,
    pub day: i64,
    pub hour: i64,
    pub minute: i64,
    pub second: i64,
    pub millisecond: i64,
}

/// luxon `objToLocalTS`: like `Date.UTC`, overflowing fields roll over.
fn obj_to_local_ts(c: &Fields) -> i64 {
    let month0 = c.month - 1;
    let year = c.year + month0.div_euclid(12);
    let month = month0.rem_euclid(12) + 1;
    let days = days_from_civil(year, month, 1) + (c.day - 1);
    days * MS_PER_DAY + c.hour * 3_600_000 + c.minute * 60_000 + c.second * 1000 + c.millisecond
}

/// luxon `tsToObj`.
fn ts_to_obj(ts: i64, offset_minutes: i32) -> Fields {
    let local = ts + offset_minutes as i64 * 60_000;
    let days = local.div_euclid(MS_PER_DAY);
    let ms_of_day = local.rem_euclid(MS_PER_DAY);
    let (year, month, day) = civil_from_days(days);
    Fields {
        year,
        month,
        day,
        hour: ms_of_day / 3_600_000,
        minute: ms_of_day / 60_000 % 60,
        second: ms_of_day / 1000 % 60,
        millisecond: ms_of_day % 1000,
    }
}

// ---------------------------------------------------------------- zones

#[derive(Debug, Clone, PartialEq)]
pub enum Zone {
    /// luxon `FixedOffsetZone`, offset in minutes. `Fixed(0)` is luxon's UTC instance.
    Fixed(i32),
    Iana(Tz),
}

thread_local! {
    static DEFAULT_ZONE: RefCell<Option<Zone>> = const { RefCell::new(None) };
}

/// The machine's zone (luxon `SystemZone`): `TZ`, else `/etc/localtime`, else the local offset.
fn system_zone() -> Zone {
    if let Ok(tz) = std::env::var("TZ") {
        if let Ok(z) = Tz::from_str(tz.trim_start_matches(':')) {
            return Zone::Iana(z);
        }
    }
    if let Ok(target) = std::fs::read_link("/etc/localtime") {
        let s = target.to_string_lossy();
        if let Some(idx) = s.find("zoneinfo/") {
            if let Ok(z) = Tz::from_str(&s[idx + "zoneinfo/".len()..]) {
                return Zone::Iana(z);
            }
        }
    }
    Zone::Fixed(chrono::Local::now().offset().local_minus_utc() / 60)
}

impl Zone {
    /// luxon `normalizeZone` for strings: `utc`, `gmt`, `system`, `local`, `UTC+5:30`, IANA.
    pub fn parse(name: &str) -> Option<Zone> {
        let lower = name.trim().to_lowercase();
        match lower.as_str() {
            "local" | "system" => return Some(Zone::default_zone()),
            "utc" | "gmt" | "z" => return Some(Zone::Fixed(0)),
            _ => {}
        }
        if let Some(rest) = lower.strip_prefix("utc").or_else(|| lower.strip_prefix("gmt")) {
            return parse_offset(rest).map(Zone::Fixed);
        }
        let name = name.trim();
        Tz::from_str(name)
            .ok()
            .or_else(|| chrono_tz::TZ_VARIANTS.iter().copied().find(|tz| tz.name().eq_ignore_ascii_case(name)))
            .map(Zone::Iana)
    }

    /// luxon `Settings.defaultZone` for this thread (the system zone until set).
    pub fn default_zone() -> Zone {
        DEFAULT_ZONE.with(|z| z.borrow().clone()).unwrap_or_else(system_zone)
    }

    pub fn set_default_zone(zone: Option<Zone>) {
        DEFAULT_ZONE.with(|z| *z.borrow_mut() = zone);
    }

    /// Offset from UTC in minutes at instant `ts` (ms).
    pub fn offset(&self, ts: i64) -> i32 {
        match self {
            Zone::Fixed(o) => *o,
            Zone::Iana(tz) => match chrono::DateTime::from_timestamp_millis(ts) {
                Some(dt) => tz.offset_from_utc_datetime(&dt.naive_utc()).fix().local_minus_utc() / 60,
                None => 0,
            },
        }
    }

    pub fn is_universal(&self) -> bool {
        matches!(self, Zone::Fixed(_))
    }

    /// luxon `zone.name`.
    pub fn name(&self) -> String {
        match self {
            Zone::Fixed(0) => "UTC".to_string(),
            Zone::Fixed(o) => format!("UTC{}", format_offset(*o, OffsetStyle::Narrow)),
            Zone::Iana(tz) => tz.name().to_string(),
        }
    }
}

/// `+05:30`, `+0530`, `+05`, `+5:30`, `-6` → minutes.
fn parse_offset(s: &str) -> Option<i32> {
    let s = s.trim();
    let (sign, rest) = match s.as_bytes().first()? {
        b'+' => (1, &s[1..]),
        b'-' => (-1, &s[1..]),
        _ => return None,
    };
    let (h, m) = match rest.split_once(':') {
        Some((h, m)) => (h, m),
        None if rest.len() > 2 => (&rest[..rest.len() - 2], &rest[rest.len() - 2..]),
        None => (rest, "0"),
    };
    let h: i32 = h.parse().ok()?;
    let m: i32 = m.parse().ok()?;
    if h > 23 || m > 59 {
        return None;
    }
    Some(sign * (h * 60 + m))
}

#[derive(Clone, Copy)]
enum OffsetStyle {
    /// `+5:30`, `-6`
    Narrow,
    /// `+05:30`
    Short,
    /// `+0530`
    Techie,
}

/// luxon `formatOffset`.
fn format_offset(offset: i32, style: OffsetStyle) -> String {
    let sign = if offset >= 0 { "+" } else { "-" };
    let (h, m) = ((offset.abs()) / 60, offset.abs() % 60);
    match style {
        OffsetStyle::Narrow if m > 0 => format!("{sign}{h}:{m:02}"),
        OffsetStyle::Narrow => format!("{sign}{h}"),
        OffsetStyle::Short => format!("{sign}{h:02}:{m:02}"),
        OffsetStyle::Techie => format!("{sign}{h:02}{m:02}"),
    }
}

/// luxon `fixOffset`: the instant for local time `local_ts` in `zone`, starting from offset `o`.
fn fix_offset(local_ts: i64, o: i32, zone: &Zone) -> (i64, i32) {
    let mut utc_guess = local_ts - o as i64 * 60_000;
    let o2 = zone.offset(utc_guess);
    if o == o2 {
        return (utc_guess, o);
    }
    utc_guess -= (o2 - o) as i64 * 60_000;
    let o3 = zone.offset(utc_guess);
    if o2 == o3 {
        return (utc_guess, o2);
    }
    (local_ts - o2.min(o3) as i64 * 60_000, o2.max(o3))
}

// ---------------------------------------------------------------- DateTime

/// A valid luxon `DateTime`: an instant, its zone, the offset at that instant, local fields.
#[derive(Debug, Clone, PartialEq)]
pub struct DateTime {
    pub ts: i64,
    pub zone: Zone,
    pub o: i32,
    pub c: Fields,
}

impl DateTime {
    pub fn from_millis(ts: i64, zone: Zone) -> DateTime {
        let o = zone.offset(ts);
        DateTime { ts, c: ts_to_obj(ts, o), zone, o }
    }

    pub fn now(zone: Zone) -> DateTime {
        DateTime::from_millis(chrono::Utc::now().timestamp_millis(), zone)
    }

    /// Local fields in `zone` (luxon `objToTS` with a provisional offset of "now").
    fn from_local_fields(c: &Fields, zone: Zone) -> DateTime {
        let provisional = zone.offset(chrono::Utc::now().timestamp_millis());
        let (ts, _) = fix_offset(obj_to_local_ts(c), provisional, &zone);
        DateTime::from_millis(ts, zone)
    }

    /// luxon `DateTime.fromISO(text, { setZone })`. `None` when invalid.
    pub fn from_iso(text: &str, set_zone: bool) -> Option<DateTime> {
        let parsed = parse_iso(text)?;
        Some(DateTime::from_parsed(parsed, set_zone))
    }

    /// luxon `parseDataToDateTime`.
    fn from_parsed(p: Parsed, set_zone: bool) -> DateTime {
        let default_zone = Zone::default_zone();
        match p.offset {
            Some(offset) => {
                let parsed_zone = Zone::Fixed(offset);
                let dt = DateTime::from_local_fields(&p.fields, parsed_zone.clone());
                if set_zone {
                    dt
                } else {
                    dt.set_zone(default_zone)
                }
            }
            None => DateTime::from_local_fields(&p.fields, default_zone),
        }
    }

    pub fn set_zone(&self, zone: Zone) -> DateTime {
        DateTime::from_millis(self.ts, zone)
    }

    pub fn to_millis(&self) -> i64 {
        self.ts
    }

    pub fn weekday(&self) -> i64 {
        // 1970-01-01 was a Thursday (ISO 4).
        (days_from_civil(self.c.year, self.c.month, self.c.day) + 3).rem_euclid(7) + 1
    }

    pub fn ordinal(&self) -> i64 {
        days_from_civil(self.c.year, self.c.month, self.c.day) - days_from_civil(self.c.year, 1, 1) + 1
    }

    pub fn quarter(&self) -> i64 {
        (self.c.month + 2) / 3
    }

    pub fn days_in_month(&self) -> i64 {
        days_in_month(self.c.year, self.c.month)
    }

    pub fn days_in_year(&self) -> i64 {
        if is_leap_year(self.c.year) {
            366
        } else {
            365
        }
    }

    pub fn is_in_leap_year(&self) -> bool {
        is_leap_year(self.c.year)
    }

    /// ISO week number and week year.
    pub fn iso_week(&self) -> (i64, i64) {
        let ordinal = self.ordinal();
        let weekday = self.weekday();
        let week = (ordinal - weekday + 10) / 7;
        let weeks_in = |y: i64| {
            let p = |y: i64| (y + y.div_euclid(4) - y.div_euclid(100) + y.div_euclid(400)).rem_euclid(7);
            if p(y) == 4 || p(y - 1) == 3 {
                53
            } else {
                52
            }
        };
        if week < 1 {
            (self.c.year - 1, weeks_in(self.c.year - 1))
        } else if week > weeks_in(self.c.year) {
            (self.c.year + 1, 1)
        } else {
            (self.c.year, week)
        }
    }

    /// luxon `isInDST`.
    pub fn is_in_dst(&self) -> bool {
        if self.zone.is_universal() {
            return false;
        }
        let jan = self.set(&[("month", 1), ("day", 1)]);
        let may = self.set(&[("month", 5)]);
        self.o > jan.o || self.o > may.o
    }

    /// luxon `set({...})` with gregorian units (and `weekday`).
    pub fn set(&self, values: &[(&str, i64)]) -> DateTime {
        let mut c = self.c;
        let mut has_day = false;
        let mut weekday = None;
        for (unit, v) in values {
            match *unit {
                "year" => c.year = *v,
                "month" => c.month = *v,
                "day" => {
                    c.day = *v;
                    has_day = true;
                }
                "hour" => c.hour = *v,
                "minute" => c.minute = *v,
                "second" => c.second = *v,
                "millisecond" => c.millisecond = *v,
                "weekday" => weekday = Some(*v),
                _ => {}
            }
        }
        if let Some(wd) = weekday {
            // Same ISO week, requested weekday.
            c.day += wd - self.weekday();
            has_day = true;
        }
        if !has_day {
            c.day = c.day.min(days_in_month(c.year, c.month));
        }
        let (ts, _) = fix_offset(obj_to_local_ts(&c), self.o, &self.zone);
        DateTime::from_millis(ts, self.zone.clone())
    }

    /// luxon `plus(duration)` (`adjustTime`).
    pub fn plus(&self, dur: &Duration) -> DateTime {
        let g = |u: &str| dur.get(u);
        let year = self.c.year + g("years").trunc() as i64;
        let month = self.c.month + g("months").trunc() as i64 + g("quarters").trunc() as i64 * 3;
        let c = Fields {
            year,
            month,
            day: self.c.day.min(days_in_month(year, month)) + g("days").trunc() as i64 + g("weeks").trunc() as i64 * 7,
            ..self.c
        };
        let fractional = Duration::from_pairs(&[
            ("years", g("years").fract()),
            ("quarters", g("quarters").fract()),
            ("months", g("months").fract()),
            ("weeks", g("weeks").fract()),
            ("days", g("days").fract()),
            ("hours", g("hours")),
            ("minutes", g("minutes")),
            ("seconds", g("seconds")),
            ("milliseconds", g("milliseconds")),
        ]);
        let millis_to_add = fractional.as_milliseconds();
        let (mut ts, _) = fix_offset(obj_to_local_ts(&c), self.o, &self.zone);
        if millis_to_add != 0.0 {
            ts += millis_to_add as i64;
        }
        DateTime::from_millis(ts, self.zone.clone())
    }

    pub fn minus(&self, dur: &Duration) -> DateTime {
        self.plus(&dur.negate())
    }

    /// luxon `startOf(unit)`; `unit` in any luxon spelling (`year`, `months`, ...).
    pub fn start_of(&self, unit: &str) -> Option<DateTime> {
        let unit = normalize_unit(unit)?;
        let mut o: Vec<(&str, i64)> = Vec::new();
        let order = ["years", "quarters", "months", "weeks", "days", "hours", "minutes", "seconds"];
        let idx = order.iter().position(|u| *u == unit)?;
        // The JS switch falls through from the matched unit to every smaller one.
        for u in &order[idx..] {
            match *u {
                "years" => o.push(("month", 1)),
                "months" => o.push(("day", 1)),
                "days" => o.push(("hour", 0)),
                "hours" => o.push(("minute", 0)),
                "minutes" => o.push(("second", 0)),
                "seconds" => o.push(("millisecond", 0)),
                _ => {}
            }
        }
        if unit == "weeks" {
            o.push(("weekday", 1));
        }
        if unit == "quarters" {
            let q = (self.c.month + 2) / 3;
            o.push(("month", (q - 1) * 3 + 1));
        }
        Some(self.set(&o))
    }

    /// luxon `endOf(unit)`: start of the next unit minus 1ms.
    pub fn end_of(&self, unit: &str) -> Option<DateTime> {
        let n = normalize_unit(unit)?;
        Some(
            self.plus(&Duration::from_pairs(&[(n, 1.0)]))
                .start_of(n)?
                .minus(&Duration::from_pairs(&[("milliseconds", 1.0)])),
        )
    }

    /// luxon `toISO()`.
    pub fn to_iso(&self) -> String {
        let year = if self.c.year > 9999 {
            format!("+{:06}", self.c.year)
        } else if self.c.year < 0 {
            format!("-{:06}", -self.c.year)
        } else {
            format!("{:04}", self.c.year)
        };
        format!(
            "{year}-{:02}-{:02}T{:02}:{:02}:{:02}.{:03}{}",
            self.c.month,
            self.c.day,
            self.c.hour,
            self.c.minute,
            self.c.second,
            self.c.millisecond,
            self.iso_offset()
        )
    }

    fn iso_offset(&self) -> String {
        if self.zone.is_universal() && self.o == 0 {
            "Z".to_string()
        } else {
            format_offset(self.o, OffsetStyle::Short)
        }
    }

    /// luxon `toObject()`.
    pub fn to_object(&self) -> Value {
        let mut m = serde_json::Map::new();
        for (k, v) in [
            ("year", self.c.year),
            ("month", self.c.month),
            ("day", self.c.day),
            ("hour", self.c.hour),
            ("minute", self.c.minute),
            ("second", self.c.second),
            ("millisecond", self.c.millisecond),
        ] {
            m.insert(k.to_string(), Value::from(v));
        }
        Value::Object(m)
    }

    /// luxon `offsetNameShort` (en-US `Intl` short zone name).
    pub fn offset_name_short(&self) -> String {
        match &self.zone {
            Zone::Fixed(_) => self.zone.name(),
            Zone::Iana(tz) => zone_abbreviation(tz, self.o, false).unwrap_or_else(|| gmt_name(self.o)),
        }
    }

    /// luxon `offsetNameLong` (en-US `Intl` long zone name).
    pub fn offset_name_long(&self) -> String {
        match &self.zone {
            Zone::Fixed(_) => self.zone.name(),
            Zone::Iana(tz) => zone_abbreviation(tz, self.o, true).unwrap_or_else(|| {
                if self.o == 0 {
                    "GMT".to_string()
                } else {
                    format!("GMT{}", format_offset(self.o, OffsetStyle::Short))
                }
            }),
        }
    }

    /// luxon `diff(other, units)`: calendar-aware for years..days, then exact milliseconds.
    pub fn diff(&self, other: &DateTime, units: &[&'static str]) -> Duration {
        let other_is_later = other.ts > self.ts;
        let (earlier, later) = if other_is_later { (self, other) } else { (other, self) };
        let d = diff_impl(earlier, later, units);
        if other_is_later {
            d.negate()
        } else {
            d
        }
    }
}

fn gmt_name(o: i32) -> String {
    if o == 0 {
        "GMT".to_string()
    } else {
        format!("GMT{}", format_offset(o, OffsetStyle::Narrow))
    }
}

/// en-US `Intl` names for the zones that have them; `None` → the GMT±h form.
fn zone_abbreviation(tz: &Tz, offset: i32, long: bool) -> Option<String> {
    let name = tz.name();
    let us = |std_short: &str, dst_short: &str, std_long: &str, dst_long: &str, std_offset: i32| {
        let dst = offset != std_offset;
        Some(if long { if dst { dst_long } else { std_long } } else if dst { dst_short } else { std_short }.to_string())
    };
    match name {
        "UTC" | "Etc/UTC" | "Etc/UCT" | "UCT" | "Universal" | "Zulu" | "Etc/Universal" | "Etc/Zulu" => {
            Some(if long { "Coordinated Universal Time" } else { "UTC" }.to_string())
        }
        "America/New_York" | "US/Eastern" | "America/Detroit" | "America/Toronto" => {
            us("EST", "EDT", "Eastern Standard Time", "Eastern Daylight Time", -300)
        }
        "America/Chicago" | "US/Central" => us("CST", "CDT", "Central Standard Time", "Central Daylight Time", -360),
        "America/Denver" | "US/Mountain" => us("MST", "MDT", "Mountain Standard Time", "Mountain Daylight Time", -420),
        "America/Los_Angeles" | "US/Pacific" => {
            us("PST", "PDT", "Pacific Standard Time", "Pacific Daylight Time", -480)
        }
        "Asia/Kolkata" | "Asia/Calcutta" if long => Some("India Standard Time".to_string()),
        "Asia/Tokyo" if long => Some("Japan Standard Time".to_string()),
        "Europe/London" if long => {
            Some(if offset == 0 { "Greenwich Mean Time" } else { "British Summer Time" }.to_string())
        }
        _ => None,
    }
}

/// luxon `Duration.normalizeUnit`.
pub fn normalize_unit(unit: &str) -> Option<&'static str> {
    Some(match unit.to_lowercase().as_str() {
        "year" | "years" => "years",
        "quarter" | "quarters" => "quarters",
        "month" | "months" => "months",
        "week" | "weeks" => "weeks",
        "day" | "days" => "days",
        "hour" | "hours" => "hours",
        "minute" | "minutes" => "minutes",
        "second" | "seconds" => "seconds",
        "millisecond" | "milliseconds" => "milliseconds",
        _ => return None,
    })
}

// ---------------------------------------------------------------- Duration

const ORDERED_UNITS: [&str; 9] =
    ["years", "quarters", "months", "weeks", "days", "hours", "minutes", "seconds", "milliseconds"];

/// luxon's casual conversion matrix: `matrix(a, b)` is how many `b` make one `a`.
fn matrix(from: &str, to: &str) -> f64 {
    let ms = |u: &str| -> f64 {
        match u {
            "years" => 365.0 * 86_400_000.0,
            "quarters" => 91.0 * 86_400_000.0,
            "months" => 30.0 * 86_400_000.0,
            "weeks" => 7.0 * 86_400_000.0,
            "days" => 86_400_000.0,
            "hours" => 3_600_000.0,
            "minutes" => 60_000.0,
            "seconds" => 1000.0,
            _ => 1.0,
        }
    };
    match (from, to) {
        ("years", "quarters") => 4.0,
        ("years", "months") => 12.0,
        ("years", "weeks") => 52.0,
        ("quarters", "months") => 3.0,
        ("quarters", "weeks") => 13.0,
        ("months", "weeks") => 4.0,
        _ => ms(from) / ms(to),
    }
}

/// A luxon `Duration`: unit → value, keeping only the units that were set.
#[derive(Debug, Clone, PartialEq, Default)]
pub struct Duration {
    pub values: Map<f64>,
}

impl Duration {
    pub fn from_pairs(pairs: &[(&str, f64)]) -> Duration {
        let mut values = Map::new();
        for u in ORDERED_UNITS {
            if let Some((_, v)) = pairs.iter().find(|(k, _)| *k == u) {
                values.insert(u.to_string(), *v);
            }
        }
        Duration { values }
    }

    pub fn get(&self, unit: &str) -> f64 {
        self.values.get(unit).copied().unwrap_or(0.0)
    }

    pub fn negate(&self) -> Duration {
        Duration { values: self.values.iter().map(|(k, v)| (k.clone(), if *v == 0.0 { 0.0 } else { -v })).collect() }
    }

    /// luxon `as("milliseconds")`.
    pub fn as_milliseconds(&self) -> f64 {
        self.values.iter().map(|(k, v)| v * matrix(k, "milliseconds")).sum()
    }

    /// luxon `plus`: unit-wise sum over the units either side has.
    pub fn plus(&self, other: &Duration) -> Duration {
        let mut values = Map::new();
        for u in ORDERED_UNITS {
            if self.values.contains_key(u) || other.values.contains_key(u) {
                values.insert(u.to_string(), self.get(u) + other.get(u));
            }
        }
        Duration { values }
    }

    /// luxon `shiftTo(...units)`.
    pub fn shift_to(&self, units: &[&str]) -> Duration {
        if units.is_empty() {
            return self.clone();
        }
        let mut built: Map<f64> = Map::new();
        let mut accumulated: Map<f64> = Map::new();
        let mut last_unit = "";

        for k in ORDERED_UNITS {
            if units.contains(&k) {
                last_unit = k;
                let mut own = 0.0;
                for (ak, av) in accumulated.iter_mut() {
                    own += matrix(ak, k) * *av;
                    *av = 0.0;
                }
                if let Some(v) = self.values.get(k) {
                    own += v;
                }
                let i = own.trunc();
                built.insert(k.to_string(), i);
                accumulated.insert(k.to_string(), (own * 1000.0 - i * 1000.0) / 1000.0);
            } else if let Some(v) = self.values.get(k) {
                accumulated.insert(k.to_string(), *v);
            }
        }

        for (key, av) in &accumulated {
            if *av != 0.0 {
                let add = if key == last_unit { *av } else { av / matrix(last_unit, key) };
                *built.get_mut(last_unit).unwrap() += add;
            }
        }

        normalize_values(&mut built);
        Duration { values: built }
    }

    /// luxon `toObject()`.
    pub fn to_object(&self) -> Value {
        Value::Object(self.values.iter().map(|(k, v)| (k.clone(), number_value(*v))).collect())
    }
}

fn duration_to_millis(vals: &Map<f64>) -> f64 {
    vals.iter().map(|(k, v)| v * matrix(k, "milliseconds")).sum()
}

/// luxon `normalizeValues` (mutates).
fn normalize_values(vals: &mut Map<f64>) {
    let factor = if duration_to_millis(vals) < 0.0 { -1.0 } else { 1.0 };

    let mut previous: Option<&str> = None;
    for current in ORDERED_UNITS.iter().rev() {
        if !vals.contains_key(*current) {
            continue;
        }
        if let Some(prev) = previous {
            let previous_val = vals[prev] * factor;
            let conv = matrix(current, prev);
            let roll_up = (previous_val / conv).floor();
            *vals.get_mut(*current).unwrap() += roll_up * factor;
            *vals.get_mut(prev).unwrap() -= roll_up * conv * factor;
        }
        previous = Some(current);
    }

    let mut previous: Option<&str> = None;
    for current in ORDERED_UNITS {
        if !vals.contains_key(current) {
            continue;
        }
        if let Some(prev) = previous {
            // JS `%` keeps the dividend's sign, like Rust's.
            let fraction = vals[prev] % 1.0;
            *vals.get_mut(prev).unwrap() -= fraction;
            *vals.get_mut(current).unwrap() += fraction * matrix(prev, current);
        }
        previous = Some(current);
    }
}

/// luxon `impl/diff.js`.
fn diff_impl(earlier: &DateTime, later: &DateTime, units: &[&'static str]) -> Duration {
    let day_diff = |a: &DateTime, b: &DateTime| -> f64 {
        let start = |d: &DateTime| days_from_civil(d.c.year, d.c.month, d.c.day) * MS_PER_DAY;
        ((start(b) - start(a)) as f64 / MS_PER_DAY as f64).floor()
    };
    type Differ = fn(&DateTime, &DateTime, &dyn Fn(&DateTime, &DateTime) -> f64) -> f64;
    let differs: [(&str, Differ); 5] = [
        ("years", |a, b, _| (b.c.year - a.c.year) as f64),
        ("quarters", |a, b, _| (b.quarter() - a.quarter() + (b.c.year - a.c.year) * 4) as f64),
        ("months", |a, b, _| (b.c.month - a.c.month + (b.c.year - a.c.year) * 12) as f64),
        ("weeks", |a, b, dd| {
            let days = dd(a, b);
            (days - days % 7.0) / 7.0
        }),
        ("days", |a, b, dd| dd(a, b)),
    ];

    let mut results: Vec<(&'static str, f64)> = Vec::new();
    let mut cursor = earlier.clone();
    let mut high_water: Option<DateTime> = None;
    let mut lowest_order: Option<&'static str> = None;

    for (unit, differ) in differs {
        let Some(unit) = units.iter().copied().find(|u| *u == unit) else { continue };
        lowest_order = Some(unit);
        let value = differ(&cursor, later, &day_diff);
        results.push((unit, value));
        let mut hw = earlier.plus(&Duration::from_pairs(&results));
        if hw.ts > later.ts {
            results.last_mut().unwrap().1 -= 1.0;
            cursor = earlier.plus(&Duration::from_pairs(&results));
            if cursor.ts > later.ts {
                hw = cursor.clone();
                results.last_mut().unwrap().1 -= 1.0;
                cursor = earlier.plus(&Duration::from_pairs(&results));
            }
        } else {
            cursor = hw.clone();
        }
        high_water = Some(hw);
    }

    let remaining_millis = (later.ts - cursor.ts) as f64;
    let lower: Vec<&str> =
        units.iter().copied().filter(|u| ["hours", "minutes", "seconds", "milliseconds"].contains(u)).collect();

    if lower.is_empty() {
        if let Some(lowest) = lowest_order {
            let mut hw = high_water.clone().unwrap_or_else(|| cursor.clone());
            if hw.ts < later.ts {
                hw = cursor.plus(&Duration::from_pairs(&[(lowest, 1.0)]));
            }
            if hw.ts != cursor.ts {
                if let Some(r) = results.iter_mut().find(|(u, _)| *u == lowest) {
                    r.1 += remaining_millis / (hw.ts - cursor.ts) as f64;
                }
            }
        }
    }

    let duration = Duration::from_pairs(&results);
    if !lower.is_empty() {
        Duration::from_pairs(&[("milliseconds", remaining_millis)]).shift_to(&lower).plus(&duration)
    } else {
        duration
    }
}

// ---------------------------------------------------------------- ISO parsing

struct Parsed {
    fields: Fields,
    /// Offset in minutes when the text carried one.
    offset: Option<i32>,
}

fn digits(s: &str) -> bool {
    !s.is_empty() && s.bytes().all(|b| b.is_ascii_digit())
}

/// luxon `parseISODate`-style parsing of the common ISO 8601 shapes: calendar dates (extended
/// and basic), ordinal and week dates, optional time with fraction, `Z`/`±hh[:mm]` offsets.
fn parse_iso(text: &str) -> Option<Parsed> {
    let text = text.trim();
    // An IANA zone in brackets after the offset is accepted and ignored.
    let text = match text.find('[') {
        Some(i) if text.ends_with(']') => &text[..i],
        _ => text,
    };
    let (date_part, time_part) = match text.find('T') {
        Some(i) => (&text[..i], Some(&text[i + 1..])),
        None => (text, None),
    };

    let (year, month, day) = parse_iso_date(date_part)?;

    let mut fields = Fields { year, month, day, hour: 0, minute: 0, second: 0, millisecond: 0 };
    let mut offset = None;

    if let Some(t) = time_part {
        let (clock, off) = split_offset(t)?;
        offset = off;
        let (hms, frac) = match clock.find(['.', ',']) {
            Some(i) => (&clock[..i], Some(&clock[i + 1..])),
            None => (clock, None),
        };
        let parts: Vec<&str> = if hms.contains(':') {
            hms.split(':').collect()
        } else {
            (0..hms.len()).step_by(2).map(|i| &hms[i..(i + 2).min(hms.len())]).collect()
        };
        if parts.is_empty() || parts.len() > 3 || !parts.iter().all(|p| p.len() == 2 && digits(p)) {
            return None;
        }
        fields.hour = parts[0].parse().ok()?;
        fields.minute = parts.get(1).map_or(Some(0), |p| p.parse().ok())?;
        fields.second = parts.get(2).map_or(Some(0), |p| p.parse().ok())?;
        if let Some(f) = frac {
            if !digits(f) || parts.len() < 3 {
                return None;
            }
            fields.millisecond = (format!("0.{f}").parse::<f64>().ok()? * 1000.0).floor() as i64;
        }
    }

    // luxon validity: real calendar date, 24h clock.
    if !(1..=12).contains(&fields.month)
        || fields.day < 1
        || fields.day > days_in_month(fields.year, fields.month)
        || fields.hour > 23
        || fields.minute > 59
        || fields.second > 59
    {
        // luxon accepts 24:00 as the end of a day.
        if !(fields.hour == 24 && fields.minute == 0 && fields.second == 0 && fields.millisecond == 0) {
            return None;
        }
    }
    Some(Parsed { fields, offset })
}

fn parse_iso_date(s: &str) -> Option<(i64, i64, i64)> {
    // Extended or signed six-digit years.
    let (sign, body) = match s.as_bytes().first()? {
        b'+' => (1, &s[1..]),
        b'-' if s.len() > 7 && s.as_bytes()[7] == b'-' || s.len() == 7 => (-1, &s[1..]),
        _ => (1, s),
    };
    let parts: Vec<&str> = body.split('-').collect();
    let year_len = if sign == -1 || s.starts_with('+') { 6 } else { 4 };
    let year = |p: &str| -> Option<i64> {
        if p.len() == year_len && digits(p) {
            Some(sign * p.parse::<i64>().ok()?)
        } else {
            None
        }
    };
    match parts.as_slice() {
        [y] if y.len() == 8 && digits(y) => Some((y[..4].parse().ok()?, y[4..6].parse().ok()?, y[6..].parse().ok()?)),
        [y] => Some((year(y)?, 1, 1)),
        [y, w] if w.starts_with('W') => week_date(year(y)?, &w[1..], "1"),
        [y, w, d] if w.starts_with('W') => week_date(year(y)?, &w[1..], d),
        [y, o] if o.len() == 3 && digits(o) => {
            let y = year(y)?;
            let ordinal: i64 = o.parse().ok()?;
            let max = if is_leap_year(y) { 366 } else { 365 };
            if !(1..=max).contains(&ordinal) {
                return None;
            }
            let (yy, m, d) = civil_from_days(days_from_civil(y, 1, 1) + ordinal - 1);
            Some((yy, m, d))
        }
        [y, m] if m.len() == 2 && digits(m) => Some((year(y)?, m.parse().ok()?, 1)),
        [y, m, d] if m.len() == 2 && d.len() == 2 && digits(m) && digits(d) => {
            Some((year(y)?, m.parse().ok()?, d.parse().ok()?))
        }
        _ => None,
    }
}

fn week_date(year: i64, week: &str, day: &str) -> Option<(i64, i64, i64)> {
    if week.len() != 2 || !digits(week) || day.len() != 1 || !digits(day) {
        return None;
    }
    let (week, day): (i64, i64) = (week.parse().ok()?, day.parse().ok()?);
    if !(1..=53).contains(&week) || !(1..=7).contains(&day) {
        return None;
    }
    // Monday of ISO week 1 is the Monday on or before Jan 4.
    let jan4 = days_from_civil(year, 1, 4);
    let jan4_weekday = (jan4 + 3).rem_euclid(7) + 1;
    let days = jan4 - (jan4_weekday - 1) + (week - 1) * 7 + (day - 1);
    Some(civil_from_days(days))
}

/// Splits `10:20:30+05:30` into the clock and the offset (minutes).
fn split_offset(t: &str) -> Option<(&str, Option<i32>)> {
    if let Some(clock) = t.strip_suffix('Z').or_else(|| t.strip_suffix('z')) {
        return Some((clock, Some(0)));
    }
    match t.rfind(['+', '-']) {
        Some(i) => Some((&t[..i], Some(parse_offset(&t[i..])?))),
        None => Some((t, None)),
    }
}

// ---------------------------------------------------------------- locale data

#[derive(Clone, Copy, PartialEq)]
pub enum Lang {
    EnUs,
    EnGb,
    Fr,
    FrCa,
}

impl Lang {
    /// `system` and unknown locales are en-US (the kirun-js runtime default).
    pub fn of(locale: &str) -> Lang {
        let l = locale.to_lowercase().replace('_', "-");
        if l == "fr-ca" {
            Lang::FrCa
        } else if l.starts_with("fr") {
            Lang::Fr
        } else if l.starts_with("en-") && l != "en-us" {
            Lang::EnGb
        } else {
            Lang::EnUs
        }
    }

    fn is_fr(self) -> bool {
        matches!(self, Lang::Fr | Lang::FrCa)
    }
}

const EN_MONTHS: [&str; 12] = [
    "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
    "December",
];
const EN_MONTHS_SHORT: [&str; 12] = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
const FR_MONTHS: [&str; 12] = [
    "janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre",
    "décembre",
];
const FR_MONTHS_SHORT: [&str; 12] =
    ["janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc."];
const EN_WEEKDAYS: [&str; 7] = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
const EN_WEEKDAYS_SHORT: [&str; 7] = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const FR_WEEKDAYS: [&str; 7] = ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"];
const FR_WEEKDAYS_SHORT: [&str; 7] = ["lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim."];

pub fn month_name(lang: Lang, month: i64, long: bool) -> &'static str {
    let i = (month - 1).clamp(0, 11) as usize;
    match (lang.is_fr(), long) {
        (true, true) => FR_MONTHS[i],
        (true, false) => FR_MONTHS_SHORT[i],
        (false, true) => EN_MONTHS[i],
        (false, false) => EN_MONTHS_SHORT[i],
    }
}

pub fn weekday_name(lang: Lang, weekday: i64, long: bool) -> &'static str {
    let i = (weekday - 1).clamp(0, 6) as usize;
    match (lang.is_fr(), long) {
        (true, true) => FR_WEEKDAYS[i],
        (true, false) => FR_WEEKDAYS_SHORT[i],
        (false, true) => EN_WEEKDAYS[i],
        (false, false) => EN_WEEKDAYS_SHORT[i],
    }
}

/// `Intl.NumberFormat` grouping for integers (`1,234` / `1 234`).
fn group_number(n: f64, lang: Lang) -> String {
    let s = crate::util::primitive::js_number_to_string(n.abs());
    let (int, frac) = match s.split_once('.') {
        Some((i, f)) => (i.to_string(), Some(f.to_string())),
        None => (s, None),
    };
    let sep = if lang.is_fr() { "\u{202F}" } else { "," };
    let mut grouped = String::new();
    for (i, ch) in int.chars().enumerate() {
        if i > 0 && (int.len() - i) % 3 == 0 && int.len() > 3 {
            grouped.push_str(sep);
        }
        grouped.push(ch);
    }
    if let Some(f) = frac {
        grouped.push(if lang.is_fr() { ',' } else { '.' });
        grouped.push_str(&f);
    }
    grouped
}

// ---------------------------------------------------------------- toFormat

/// Splits a luxon format into (is_literal, text) tokens: runs of one letter, `'quoted'` text.
fn tokenize_format(fmt: &str) -> Vec<(bool, String)> {
    let chars: Vec<char> = fmt.chars().collect();
    let mut out: Vec<(bool, String)> = Vec::new();
    let mut i = 0;
    while i < chars.len() {
        let c = chars[i];
        if c == '\'' {
            let mut lit = String::new();
            i += 1;
            while i < chars.len() && chars[i] != '\'' {
                lit.push(chars[i]);
                i += 1;
            }
            i += 1;
            out.push((true, lit));
        } else if c.is_ascii_alphabetic() {
            let mut tok = String::new();
            while i < chars.len() && chars[i] == c {
                tok.push(c);
                i += 1;
            }
            out.push((false, tok));
        } else {
            match out.last_mut() {
                Some((true, lit)) => lit.push(c),
                _ => out.push((true, c.to_string())),
            }
            i += 1;
        }
    }
    out
}

fn localized_date(dt: &DateTime, lang: Lang, len: usize) -> String {
    let (y, m, d, wd) = (dt.c.year, dt.c.month, dt.c.day, dt.weekday());
    match (lang, len) {
        (Lang::EnUs, 1) => format!("{m}/{d}/{y}"),
        (Lang::EnUs, 2) => format!("{} {d}, {y}", month_name(lang, m, false)),
        (Lang::EnUs, 3) => format!("{} {d}, {y}", month_name(lang, m, true)),
        (Lang::EnUs, _) => format!("{}, {} {d}, {y}", weekday_name(lang, wd, true), month_name(lang, m, true)),
        (Lang::EnGb, 1) => format!("{d:02}/{m:02}/{y}"),
        (Lang::EnGb, 2) => format!("{d} {} {y}", month_name(lang, m, false)),
        (Lang::EnGb, 3) => format!("{d} {} {y}", month_name(lang, m, true)),
        (Lang::EnGb, _) => format!("{} {d} {} {y}", weekday_name(lang, wd, true), month_name(lang, m, true)),
        (Lang::FrCa, 1) => format!("{y}-{m:02}-{d:02}"),
        (_, 1) => format!("{d:02}/{m:02}/{y}"),
        (_, 2) => format!("{d} {} {y}", month_name(lang, m, false)),
        (_, 3) => format!("{d} {} {y}", month_name(lang, m, true)),
        (_, _) => format!("{} {d} {} {y}", weekday_name(lang, wd, true), month_name(lang, m, true)),
    }
}

fn localized_time(dt: &DateTime, lang: Lang, with_seconds: bool, h24: bool) -> String {
    let secs = if with_seconds { format!(":{:02}", dt.c.second) } else { String::new() };
    if h24 || lang.is_fr() || lang == Lang::EnGb {
        format!("{:02}:{:02}{secs}", dt.c.hour, dt.c.minute)
    } else {
        let h12 = if dt.c.hour % 12 == 0 { 12 } else { dt.c.hour % 12 };
        format!("{h12}:{:02}{secs} {}", dt.c.minute, if dt.c.hour < 12 { "AM" } else { "PM" })
    }
}

/// luxon `toFormat(fmt, { locale })`.
pub fn to_format(dt: &DateTime, fmt: &str, locale: &str) -> String {
    let lang = Lang::of(locale);
    let c = &dt.c;
    let h12 = if c.hour % 12 == 0 { 12 } else { c.hour % 12 };
    let mut out = String::new();
    for (literal, tok) in tokenize_format(fmt) {
        if literal {
            out.push_str(&tok);
            continue;
        }
        let s = match tok.as_str() {
            "S" => c.millisecond.to_string(),
            "SSS" => format!("{:03}", c.millisecond),
            "u" => format!("{:03}", c.millisecond),
            "uu" => format!("{:02}", c.millisecond / 10),
            "uuu" => (c.millisecond / 100).to_string(),
            "s" => c.second.to_string(),
            "ss" => format!("{:02}", c.second),
            "m" => c.minute.to_string(),
            "mm" => format!("{:02}", c.minute),
            "h" => h12.to_string(),
            "hh" => format!("{h12:02}"),
            "H" => c.hour.to_string(),
            "HH" => format!("{:02}", c.hour),
            "Z" => {
                if dt.zone.is_universal() && dt.o == 0 {
                    "Z".to_string()
                } else {
                    format_offset(dt.o, OffsetStyle::Narrow)
                }
            }
            "ZZ" => format_offset(dt.o, OffsetStyle::Short),
            "ZZZ" => format_offset(dt.o, OffsetStyle::Techie),
            "ZZZZ" => dt.offset_name_short(),
            "ZZZZZ" => dt.offset_name_long(),
            "z" => dt.zone.name(),
            "a" => if c.hour < 12 { "AM" } else { "PM" }.to_string(),
            "d" => c.day.to_string(),
            "dd" => format!("{:02}", c.day),
            "c" | "E" => dt.weekday().to_string(),
            "ccc" | "EEE" => weekday_name(lang, dt.weekday(), false).to_string(),
            "cccc" | "EEEE" => weekday_name(lang, dt.weekday(), true).to_string(),
            "ccccc" | "EEEEE" => weekday_name(lang, dt.weekday(), true).chars().next().unwrap().to_uppercase().to_string(),
            "L" | "M" => c.month.to_string(),
            "LL" | "MM" => format!("{:02}", c.month),
            "LLL" | "MMM" => month_name(lang, c.month, false).to_string(),
            "LLLL" | "MMMM" => month_name(lang, c.month, true).to_string(),
            "LLLLL" | "MMMMM" => month_name(lang, c.month, true).chars().next().unwrap().to_uppercase().to_string(),
            "y" => c.year.to_string(),
            "yy" => format!("{:02}", c.year.rem_euclid(100)),
            "yyyy" => format!("{:04}", c.year),
            "yyyyyy" => format!("{:06}", c.year),
            "G" => if c.year > 0 { "AD" } else { "BC" }.to_string(),
            "GG" => if c.year > 0 { "Anno Domini" } else { "Before Christ" }.to_string(),
            "GGGGG" => if c.year > 0 { "A" } else { "B" }.to_string(),
            "kk" => format!("{:02}", dt.iso_week().0.rem_euclid(100)),
            "kkkk" => format!("{:04}", dt.iso_week().0),
            "W" => dt.iso_week().1.to_string(),
            "WW" => format!("{:02}", dt.iso_week().1),
            "o" => dt.ordinal().to_string(),
            "ooo" => format!("{:03}", dt.ordinal()),
            "q" => dt.quarter().to_string(),
            "qq" => format!("{:02}", dt.quarter()),
            "X" => (dt.ts.div_euclid(1000)).to_string(),
            "x" => dt.ts.to_string(),
            "D" => localized_date(dt, lang, 1),
            "DD" => localized_date(dt, lang, 2),
            "DDD" => localized_date(dt, lang, 3),
            "DDDD" => localized_date(dt, lang, 4),
            "t" => localized_time(dt, lang, false, false),
            "tt" => localized_time(dt, lang, true, false),
            "T" => localized_time(dt, lang, false, true),
            "TT" => localized_time(dt, lang, true, true),
            "f" => format!("{}, {}", localized_date(dt, lang, 1), localized_time(dt, lang, false, false)),
            "ff" => format!("{}, {}", localized_date(dt, lang, 2), localized_time(dt, lang, false, false)),
            "fff" => format!("{} at {}", localized_date(dt, lang, 3), localized_time(dt, lang, false, false)),
            "F" => format!("{}, {}", localized_date(dt, lang, 1), localized_time(dt, lang, true, false)),
            "FF" => format!("{}, {}", localized_date(dt, lang, 2), localized_time(dt, lang, true, false)),
            // Unknown tokens print as-is, like luxon.
            other => other.to_string(),
        };
        out.push_str(&s);
    }
    out
}

// ---------------------------------------------------------------- fromFormat

/// luxon `DateTime.fromFormat(text, fmt)` in the default zone. `None` when it does not match
/// or the result is not a valid date.
pub fn from_format(text: &str, fmt: &str) -> Option<DateTime> {
    let chars: Vec<char> = text.chars().collect();
    let mut pos = 0usize;
    let mut year: Option<i64> = None;
    let mut month: Option<i64> = None;
    let mut day: Option<i64> = None;
    let mut hour: Option<i64> = None;
    let mut minute: Option<i64> = None;
    let mut second: Option<i64> = None;
    let mut millisecond: Option<i64> = None;
    let mut meridiem: Option<bool> = None;
    let mut offset: Option<i32> = None;

    let take_digits = |pos: &mut usize, min: usize, max: usize| -> Option<i64> {
        let start = *pos;
        while *pos < chars.len() && *pos - start < max && chars[*pos].is_ascii_digit() {
            *pos += 1;
        }
        if *pos - start < min {
            return None;
        }
        chars[start..*pos].iter().collect::<String>().parse().ok()
    };
    let take_name = |pos: &mut usize, names: &[&str]| -> Option<i64> {
        let rest: String = chars[*pos..].iter().collect::<String>().to_lowercase();
        let (i, n) = names
            .iter()
            .enumerate()
            .filter(|(_, n)| rest.starts_with(&n.to_lowercase()))
            .max_by_key(|(_, n)| n.len())?;
        *pos += n.chars().count();
        Some(i as i64 + 1)
    };

    for (literal, tok) in tokenize_format(fmt) {
        if literal {
            for lc in tok.chars() {
                if chars.get(pos) != Some(&lc) {
                    return None;
                }
                pos += 1;
            }
            continue;
        }
        match tok.as_str() {
            "yyyy" => year = Some(take_digits(&mut pos, 4, 4)?),
            "y" => year = Some(take_digits(&mut pos, 1, 6)?),
            "yy" => {
                let y = take_digits(&mut pos, 2, 2)?;
                year = Some(if y > 60 { 1900 + y } else { 2000 + y });
            }
            "yyyyyy" => year = Some(take_digits(&mut pos, 6, 6)?),
            "M" | "L" => month = Some(take_digits(&mut pos, 1, 2)?),
            "MM" | "LL" => month = Some(take_digits(&mut pos, 2, 2)?),
            "MMM" | "LLL" => month = Some(take_name(&mut pos, &EN_MONTHS_SHORT)?),
            "MMMM" | "LLLL" => month = Some(take_name(&mut pos, &EN_MONTHS)?),
            "d" => day = Some(take_digits(&mut pos, 1, 2)?),
            "dd" => day = Some(take_digits(&mut pos, 2, 2)?),
            "H" | "h" => hour = Some(take_digits(&mut pos, 1, 2)?),
            "HH" | "hh" => hour = Some(take_digits(&mut pos, 2, 2)?),
            "m" => minute = Some(take_digits(&mut pos, 1, 2)?),
            "mm" => minute = Some(take_digits(&mut pos, 2, 2)?),
            "s" => second = Some(take_digits(&mut pos, 1, 2)?),
            "ss" => second = Some(take_digits(&mut pos, 2, 2)?),
            "S" => millisecond = Some(take_digits(&mut pos, 1, 3)?),
            "SSS" => millisecond = Some(take_digits(&mut pos, 3, 3)?),
            "a" => {
                let rest: String = chars[pos..].iter().take(2).collect::<String>().to_uppercase();
                meridiem = match rest.as_str() {
                    "AM" => Some(false),
                    "PM" => Some(true),
                    _ => return None,
                };
                pos += 2;
            }
            "EEE" | "ccc" => {
                take_name(&mut pos, &EN_WEEKDAYS_SHORT)?;
            }
            "EEEE" | "cccc" => {
                take_name(&mut pos, &EN_WEEKDAYS)?;
            }
            "Z" | "ZZ" | "ZZZ" => {
                let rest: String = chars[pos..].iter().collect();
                if rest.starts_with('Z') {
                    offset = Some(0);
                    pos += 1;
                } else {
                    let len = rest
                        .char_indices()
                        .skip(1)
                        .find(|(_, ch)| !(ch.is_ascii_digit() || *ch == ':'))
                        .map_or(rest.len(), |(i, _)| i);
                    offset = Some(parse_offset(&rest[..len])?);
                    pos += rest[..len].chars().count();
                }
            }
            _ => return None,
        }
    }
    if pos != chars.len() {
        return None;
    }

    if let (Some(h), Some(pm)) = (hour, meridiem) {
        hour = Some(if pm && h != 12 { h + 12 } else if !pm && h == 12 { 0 } else { h });
    }

    // luxon fromObject: units above the first given one default to now, below it to minimums.
    let zone = match offset {
        Some(o) => Zone::Fixed(o),
        None => Zone::default_zone(),
    };
    let now = DateTime::now(zone.clone()).c;
    let given = [year, month, day, hour, minute, second, millisecond];
    let now_vals = [now.year, now.month, now.day, now.hour, now.minute, now.second, now.millisecond];
    let defaults = [0, 1, 1, 0, 0, 0, 0];
    let first = given.iter().position(Option::is_some).unwrap_or(given.len());
    let v: Vec<i64> = (0..7).map(|i| given[i].unwrap_or(if i < first { now_vals[i] } else { defaults[i] })).collect();

    let fields = Fields { year: v[0], month: v[1], day: v[2], hour: v[3], minute: v[4], second: v[5], millisecond: v[6] };
    if !(1..=12).contains(&fields.month)
        || fields.day < 1
        || fields.day > days_in_month(fields.year, fields.month)
        || fields.hour > 23
        || fields.minute > 59
        || fields.second > 59
        || fields.millisecond > 999
    {
        return None;
    }
    let dt = DateTime::from_local_fields(&fields, zone);
    Some(if offset.is_some() { dt.set_zone(Zone::default_zone()) } else { dt })
}

// ---------------------------------------------------------------- toRelative

#[derive(Clone, Copy, PartialEq)]
pub enum RelStyle {
    Long,
    Short,
    Narrow,
}

impl RelStyle {
    pub fn of(s: &str) -> RelStyle {
        match s.to_lowercase().as_str() {
            "short" => RelStyle::Short,
            "narrow" => RelStyle::Narrow,
            _ => RelStyle::Long,
        }
    }
}

/// `Intl.RelativeTimeFormat(locale, { numeric: 'always', style }).format(count, unit)`.
fn format_relative(count: f64, unit: &str, style: RelStyle, lang: Lang) -> String {
    let past = count < 0.0 || (count == 0.0 && count.is_sign_negative());
    let n = count.abs();
    let num = group_number(n, lang);
    let singular = if lang.is_fr() { n < 2.0 } else { n == 1.0 };

    let word: String = if lang.is_fr() {
        let (one, many) = match (unit, style) {
            ("years", RelStyle::Long) => ("an", "ans"),
            ("years", _) => ("a", "a"),
            ("quarters", RelStyle::Long) => ("trimestre", "trimestres"),
            ("quarters", _) => ("trim.", "trim."),
            ("months", RelStyle::Long) => ("mois", "mois"),
            ("months", _) => ("m.", "m."),
            ("weeks", RelStyle::Long) => ("semaine", "semaines"),
            ("weeks", _) => ("sem.", "sem."),
            ("days", RelStyle::Long) => ("jour", "jours"),
            ("days", _) => ("j", "j"),
            ("hours", RelStyle::Long) => ("heure", "heures"),
            ("hours", _) => ("h", "h"),
            ("minutes", RelStyle::Long) => ("minute", "minutes"),
            ("minutes", _) => ("min", "min"),
            ("seconds", RelStyle::Long) => ("seconde", "secondes"),
            _ => ("s", "s"),
        };
        if singular { one } else { many }.to_string()
    } else {
        let (one, many) = match (unit, style) {
            ("years", RelStyle::Long) => ("year", "years"),
            ("years", RelStyle::Short) => ("yr.", "yr."),
            ("years", RelStyle::Narrow) => ("y", "y"),
            ("quarters", RelStyle::Long) => ("quarter", "quarters"),
            ("quarters", RelStyle::Short) => ("qtr.", "qtrs."),
            ("quarters", RelStyle::Narrow) => ("q", "q"),
            ("months", RelStyle::Long) => ("month", "months"),
            ("months", RelStyle::Short) => ("mo.", "mo."),
            ("months", RelStyle::Narrow) => ("mo", "mo"),
            ("weeks", RelStyle::Long) => ("week", "weeks"),
            ("weeks", RelStyle::Short) => ("wk.", "wk."),
            ("weeks", RelStyle::Narrow) => ("w", "w"),
            ("days", RelStyle::Narrow) => ("d", "d"),
            ("days", _) => ("day", "days"),
            ("hours", RelStyle::Long) => ("hour", "hours"),
            ("hours", RelStyle::Short) => ("hr.", "hr."),
            ("hours", RelStyle::Narrow) => ("h", "h"),
            ("minutes", RelStyle::Long) => ("minute", "minutes"),
            ("minutes", RelStyle::Short) => ("min.", "min."),
            ("minutes", RelStyle::Narrow) => ("m", "m"),
            ("seconds", RelStyle::Long) => ("second", "seconds"),
            ("seconds", RelStyle::Short) => ("sec.", "sec."),
            _ => ("s", "s"),
        };
        if singular { one } else { many }.to_string()
    };

    let joiner = if style == RelStyle::Narrow && !lang.is_fr() { "" } else { " " };
    let amount = format!("{num}{joiner}{word}");
    match (lang.is_fr(), past) {
        (true, true) => format!("il y a {amount}"),
        (true, false) => format!("dans {amount}"),
        (false, true) => format!("{amount} ago"),
        (false, false) => format!("in {amount}"),
    }
}

/// luxon `toRelative({ base, style, round, locale, unit: [..] })`: the first of `units` (luxon's
/// default list, years … seconds, when empty) whose |count| >= 1.
pub fn to_relative(given: &DateTime, base: &DateTime, units: &[&'static str], style: RelStyle, round: bool, locale: &str) -> String {
    let lang = Lang::of(locale);
    let format = |count: f64, unit: &str| {
        let digits = if round { 0 } else { 2 };
        let factor = 10f64.powi(digits);
        let c = (count * factor).trunc() / factor;
        format_relative(c, unit, style, lang)
    };
    let differ = |unit: &'static str| given.diff(base, &[unit]).get(unit);

    let default_units: [&'static str; 6] = ["years", "months", "days", "hours", "minutes", "seconds"];
    let list: &[&'static str] = if units.is_empty() { &default_units } else { units };
    for unit in list {
        let count = differ(unit);
        if count.abs() >= 1.0 {
            return format(count, unit);
        }
    }
    let last = list[list.len() - 1];
    format(if base.ts > given.ts { -0.0 } else { 0.0 }, last)
}

// ---------------------------------------------------------------- JS Date helpers

/// JS `new Date(ms).toISOString()`; errors (RangeError) outside ±8.64e15 ms.
pub fn js_date_to_iso_string(ms: i64) -> Option<String> {
    if ms.abs() > 8_640_000_000_000_000 {
        return None;
    }
    let c = ts_to_obj(ms, 0);
    let year = if (0..=9999).contains(&c.year) {
        format!("{:04}", c.year)
    } else if c.year < 0 {
        format!("-{:06}", -c.year)
    } else {
        format!("+{:06}", c.year)
    };
    Some(format!(
        "{year}-{:02}-{:02}T{:02}:{:02}:{:02}.{:03}Z",
        c.month, c.day, c.hour, c.minute, c.second, c.millisecond
    ))
}

/// IANA zone names (JS `Intl.supportedValuesOf('timeZone')` stand-in).
pub fn time_zone_names() -> Vec<String> {
    chrono_tz::TZ_VARIANTS.iter().map(|tz| tz.name().to_string()).collect()
}

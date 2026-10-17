//! The part of `java.time` that kirun-java's `System.Date` functions use, reproduced so that the
//! functions answer what kirun-java answers for stored server functions: `ZoneId.of`,
//! `ZonedDateTime` arithmetic (`plus`, `with`, `truncatedTo`, `withZoneSameInstant`), the
//! `DateUtil.getDateTime` reader (`ISO_DATE_READING_FORMATTER.parseBest`) and the
//! `DateUtil.ISO_DATE_TIME_FORMATTER` writer, with Java's error messages.
//!
//! The process's zone and locale follow the JVM's defaults: the zone from `TZ`, else
//! `/etc/timezone`, else the `/etc/localtime` link (else UTC); the locale from `KIRUN_LOCALE`,
//! else `LC_ALL` / `LC_TIME` / `LANG` (`C`, `POSIX` or nothing is `en_US`, as the JVM maps
//! them). Tests set either for their own thread.

use std::cell::RefCell;
use std::sync::OnceLock;

use chrono::{Datelike, Duration, LocalResult, NaiveDate, NaiveDateTime, NaiveTime, Offset, TimeZone, Timelike};
use chrono_tz::{OffsetComponents, Tz};

use crate::{KIRunError, Result};

/// A kirun-java failure: the Java exception's message, passed on unwrapped.
pub fn java_error(message: impl Into<String>) -> KIRunError {
    KIRunError::uncaught(message)
}

// ------------------------------------------------------------------------------------ zones

/// Zone rules of a `ZoneRegion`: tzdb, or a fixed offset (`UTC`, `GMT+05:00`).
#[derive(Clone, Debug, PartialEq)]
pub enum Rules {
    Tz(Tz),
    Fixed(i32),
}

/// `java.time.ZoneId`: a `ZoneOffset` or a `ZoneRegion`.
#[derive(Clone, Debug, PartialEq)]
pub enum ZoneId {
    Offset(i32),
    Region { id: String, rules: Rules },
}

/// tzdb names chrono-tz knows and the JVM does not.
const NOT_IN_JAVA: [&str; 6] = ["EST", "HST", "MST", "ROC", "GMT+0", "GMT-0"];

/// Java `ZoneOffset.buildId`: `Z`, `+HH:MM` or `+HH:MM:SS`.
pub fn offset_id(total: i32) -> String {
    if total == 0 {
        return "Z".into();
    }
    let a = total.unsigned_abs();
    let (h, m, s) = (a / 3600, (a / 60) % 60, a % 60);
    let sign = if total < 0 { '-' } else { '+' };
    if s != 0 {
        format!("{sign}{h:02}:{m:02}:{s:02}")
    } else {
        format!("{sign}{h:02}:{m:02}")
    }
}

/// Java `ZoneOffset.of(String)`: `+h`, `+hh`, `+hhmm`, `+hh:mm`, `+hhmmss`, `+hh:mm:ss`.
pub fn zone_offset_of(id: &str) -> Result<i32> {
    if id == "Z" {
        return Ok(0);
    }
    // Java widens `+h` to `+0h` first, and its messages show the widened ID.
    let chars: Vec<char> = id.chars().collect();
    let id: String = if chars.len() == 2 { format!("{}0{}", chars[0], chars[1]) } else { id.to_string() };
    let b: Vec<char> = id.chars().collect();
    let invalid = |why: &str| java_error(format!("Invalid ID for ZoneOffset, {why}: {id}"));
    let num = |pos: usize, colon: bool| -> Result<i32> {
        if colon && b.get(pos.wrapping_sub(1)) != Some(&':') {
            return Err(invalid("colon not found when expected"));
        }
        match (b.get(pos).and_then(|c| c.to_digit(10)), b.get(pos + 1).and_then(|c| c.to_digit(10))) {
            (Some(x), Some(y)) => Ok((x * 10 + y) as i32),
            _ => Err(invalid("non numeric characters found")),
        }
    };
    let (h, m, s) = match b.len() {
        3 => (num(1, false)?, 0, 0),
        5 => (num(1, false)?, num(3, false)?, 0),
        6 => (num(1, false)?, num(4, true)?, 0),
        7 => (num(1, false)?, num(3, false)?, num(5, false)?),
        9 => (num(1, false)?, num(4, true)?, num(7, true)?),
        _ => return Err(invalid("invalid format")),
    };
    match b[0] {
        '+' => offset_of_hms(h, m, s),
        '-' => offset_of_hms(-h, -m, -s),
        _ => Err(invalid("plus/minus not found when expected")),
    }
}

/// Java `ZoneOffset.ofHoursMinutesSeconds` with its range checks.
pub fn offset_of_hms(h: i32, m: i32, s: i32) -> Result<i32> {
    if !(-18..=18).contains(&h) {
        return Err(java_error(format!("Zone offset hours not in valid range: value {h} is not in the range -18 to 18")));
    }
    if !(-59..=59).contains(&m) {
        return Err(java_error(format!("Zone offset minutes not in valid range: value {m} is not in the range -59 to 59")));
    }
    if !(-59..=59).contains(&s) {
        return Err(java_error(format!("Zone offset seconds not in valid range: value {s} is not in the range -59 to 59")));
    }
    if h.abs() == 18 && (m | s) != 0 {
        return Err(java_error("Zone offset not in valid range: -18:00 to +18:00"));
    }
    Ok(h * 3600 + m * 60 + s)
}

/// Java `ZoneOffset.ofTotalSeconds` range check.
pub fn offset_of_total(total: i64) -> Result<i32> {
    if total.abs() > 18 * 3600 {
        return Err(java_error("Zone offset not in valid range: -18:00 to +18:00"));
    }
    Ok(total as i32)
}

impl ZoneId {
    pub fn utc() -> ZoneId {
        ZoneId::Offset(0)
    }

    /// Java `ZoneId.of(String)`.
    pub fn of(id: &str) -> Result<ZoneId> {
        if id.chars().count() <= 1 || id.starts_with('+') || id.starts_with('-') {
            return zone_offset_of(id).map(ZoneId::Offset);
        }
        if id.starts_with("UTC") || id.starts_with("GMT") {
            return Self::of_with_prefix(id, 3);
        }
        if id.starts_with("UT") {
            return Self::of_with_prefix(id, 2);
        }
        Self::region(id)
    }

    fn of_with_prefix(id: &str, prefix_len: usize) -> Result<ZoneId> {
        let prefix = &id[..prefix_len];
        if id.len() == prefix_len {
            return Ok(Self::prefixed(prefix, 0));
        }
        let next = id.as_bytes()[prefix_len];
        if next != b'+' && next != b'-' {
            return Self::region(id);
        }
        match zone_offset_of(&id[prefix_len..]) {
            Ok(o) => Ok(Self::prefixed(prefix, o)),
            Err(_) => Err(java_error(format!("Invalid ID for offset-based ZoneId: {id}"))),
        }
    }

    /// Java `ZoneId.ofOffset(prefix, offset)`.
    pub fn prefixed(prefix: &str, offset: i32) -> ZoneId {
        let id = if offset == 0 { prefix.to_string() } else { format!("{prefix}{}", offset_id(offset)) };
        ZoneId::Region { id, rules: Rules::Fixed(offset) }
    }

    /// Java `ZoneRegion.ofId(id, true)`.
    pub fn region(id: &str) -> Result<ZoneId> {
        let mut chars = id.chars();
        let ok = chars.next().is_some_and(|c| c.is_ascii_alphabetic())
            && id.len() >= 2
            && chars.all(|c| c.is_ascii_alphanumeric() || "~/._+-".contains(c));
        if !ok {
            return Err(java_error(format!("Invalid ID for region-based ZoneId, invalid format: {id}")));
        }
        match java_tz(id) {
            Some(tz) => Ok(ZoneId::Region { id: id.to_string(), rules: Rules::Tz(tz) }),
            None => Err(java_error(format!("Unknown time-zone ID: {id}"))),
        }
    }

    /// Java `ZoneId.getId()`.
    pub fn id(&self) -> String {
        match self {
            ZoneId::Offset(o) => offset_id(*o),
            ZoneId::Region { id, .. } => id.clone(),
        }
    }

    /// The offset in force at an instant.
    pub fn offset_at(&self, epoch_second: i64) -> i32 {
        match self {
            ZoneId::Offset(o) | ZoneId::Region { rules: Rules::Fixed(o), .. } => *o,
            ZoneId::Region { rules: Rules::Tz(tz), .. } => match chrono::DateTime::from_timestamp(epoch_second, 0) {
                Some(utc) => tz.offset_from_utc_datetime(&utc.naive_utc()).fix().local_minus_utc(),
                None => 0,
            },
        }
    }

    /// The standard (non-daylight) offset at an instant.
    pub fn standard_offset_at(&self, epoch_second: i64) -> i32 {
        match self {
            ZoneId::Region { rules: Rules::Tz(tz), .. } => match chrono::DateTime::from_timestamp(epoch_second, 0) {
                Some(utc) => tz.offset_from_utc_datetime(&utc.naive_utc()).base_utc_offset().num_seconds() as i32,
                None => 0,
            },
            _ => self.offset_at(epoch_second),
        }
    }

    pub fn tz(&self) -> Option<Tz> {
        match self {
            ZoneId::Region { rules: Rules::Tz(tz), .. } => Some(*tz),
            _ => None,
        }
    }
}

/// A tzdb zone the JVM also has (case-sensitive, as Java's lookup).
pub fn java_tz(id: &str) -> Option<Tz> {
    if NOT_IN_JAVA.contains(&id) {
        return None;
    }
    id.parse::<Tz>().ok().filter(|tz| tz.name() == id)
}

/// Every tzdb ID the JVM has (for the zone parser's longest match).
pub fn java_zone_ids() -> &'static [&'static str] {
    static IDS: OnceLock<Vec<&'static str>> = OnceLock::new();
    IDS.get_or_init(|| {
        chrono_tz::TZ_VARIANTS.iter().map(|tz| tz.name()).filter(|n| !NOT_IN_JAVA.contains(n)).collect()
    })
}

thread_local! {
    static ZONE_OVERRIDE: RefCell<Option<ZoneId>> = const { RefCell::new(None) };
    static LOCALE_OVERRIDE: RefCell<Option<String>> = const { RefCell::new(None) };
}

/// Java `ZoneId.systemDefault()`.
pub fn system_default_zone() -> ZoneId {
    if let Some(z) = ZONE_OVERRIDE.with(|z| z.borrow().clone()) {
        return z;
    }
    static ZONE: OnceLock<ZoneId> = OnceLock::new();
    ZONE.get_or_init(machine_zone).clone()
}

/// The JVM's default zone on this machine: `TZ`, `/etc/timezone`, the `/etc/localtime` link.
fn machine_zone() -> ZoneId {
    let candidates = [
        std::env::var("TZ").ok().map(|t| t.trim_start_matches(':').to_string()),
        std::fs::read_to_string("/etc/timezone").ok().map(|s| s.lines().next().unwrap_or("").trim().to_string()),
        std::fs::read_link("/etc/localtime")
            .ok()
            .and_then(|p| p.to_str().and_then(|s| s.split("zoneinfo/").nth(1).map(str::to_string))),
    ];
    for c in candidates.into_iter().flatten() {
        if c.is_empty() {
            continue;
        }
        if let Ok(z) = ZoneId::of(&c) {
            return z;
        }
    }
    ZoneId::prefixed("UTC", 0)
}

/// Sets (or with `None`, clears) the default zone for the current thread.
pub fn set_thread_zone(zone: Option<ZoneId>) {
    ZONE_OVERRIDE.with(|z| *z.borrow_mut() = zone);
}

/// Java `Locale.getDefault(Locale.Category.FORMAT)`, as `language_COUNTRY`.
pub fn default_locale() -> String {
    if let Some(l) = LOCALE_OVERRIDE.with(|l| l.borrow().clone()) {
        return l;
    }
    static LOCALE: OnceLock<String> = OnceLock::new();
    LOCALE.get_or_init(machine_locale).clone()
}

fn machine_locale() -> String {
    if let Some(l) = std::env::var("KIRUN_LOCALE").ok().filter(|l| !l.trim().is_empty()) {
        return normalize_locale(&l);
    }
    for var in ["LC_ALL", "LC_TIME", "LANG"] {
        if let Some(v) = std::env::var(var).ok().filter(|v| !v.is_empty()) {
            return normalize_locale(&v);
        }
    }
    "en_US".into()
}

/// `en-IN`, `en_IN.UTF-8` → `en_IN`; `C` / `POSIX` → `en_US` (the JVM's mapping).
pub fn normalize_locale(tag: &str) -> String {
    let base = tag.split(['.', '@']).next().unwrap_or("").replace('-', "_");
    if base.is_empty() || base == "C" || base == "POSIX" {
        return "en_US".into();
    }
    base
}

/// Sets (or with `None`, clears) the default locale for the current thread.
pub fn set_thread_locale(locale: Option<&str>) {
    LOCALE_OVERRIDE.with(|l| *l.borrow_mut() = locale.map(normalize_locale));
}

// ------------------------------------------------------------------------ ZonedDateTime

/// `java.time.ZonedDateTime`: a local date-time, the offset in force and the zone.
#[derive(Clone, Debug)]
pub struct ZonedDateTime {
    pub local: NaiveDateTime,
    pub offset: i32,
    pub zone: ZoneId,
}

/// Java `ChronoUnit`s the functions use.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Unit {
    Millis,
    Seconds,
    Minutes,
    Hours,
    Days,
    Weeks,
    Months,
    Years,
}

impl Unit {
    /// Java `ChronoUnit.valueOf(name)`.
    pub fn chrono_value_of(name: &str) -> Result<Unit> {
        Ok(match name {
            "MILLIS" => Unit::Millis,
            "SECONDS" => Unit::Seconds,
            "MINUTES" => Unit::Minutes,
            "HOURS" => Unit::Hours,
            "DAYS" => Unit::Days,
            "WEEKS" => Unit::Weeks,
            "MONTHS" => Unit::Months,
            "YEARS" => Unit::Years,
            _ => return Err(java_error(format!("No enum constant java.time.temporal.ChronoUnit.{name}"))),
        })
    }

    pub fn is_date_based(self) -> bool {
        matches!(self, Unit::Days | Unit::Weeks | Unit::Months | Unit::Years)
    }

    /// `ChronoUnit.getDuration()` in seconds (estimated for the date units, as Java's).
    pub fn duration_seconds(self) -> i64 {
        match self {
            Unit::Millis => 0,
            Unit::Seconds => 1,
            Unit::Minutes => 60,
            Unit::Hours => 3600,
            Unit::Days => 86_400,
            Unit::Weeks => 7 * 86_400,
            Unit::Months => 31_556_952 / 12,
            Unit::Years => 31_556_952,
        }
    }

    /// `ChronoUnit.getDuration().toMillis()`.
    pub fn duration_millis(self) -> i64 {
        match self {
            Unit::Millis => 1,
            u => u.duration_seconds() * 1000,
        }
    }

    /// `ChronoUnit.toString()`: `Millis`, `Seconds`, ...
    pub fn name(self) -> &'static str {
        match self {
            Unit::Millis => "Millis",
            Unit::Seconds => "Seconds",
            Unit::Minutes => "Minutes",
            Unit::Hours => "Hours",
            Unit::Days => "Days",
            Unit::Weeks => "Weeks",
            Unit::Months => "Months",
            Unit::Years => "Years",
        }
    }
}

/// The `ChronoField`s `ZonedDateTime.with(field, value)` is called with.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum SetField {
    DayOfMonth,
    MonthOfYear,
    Year,
    HourOfDay,
    MinuteOfHour,
    SecondOfMinute,
    MilliOfSecond,
}

/// Java's `Invalid value for <Field> (valid values <range>): <value>`.
pub fn invalid_value(field: &str, range: &str, value: i64) -> KIRunError {
    java_error(format!("Invalid value for {field} (valid values {range}): {value}"))
}

const MONTH_NAMES: [&str; 12] = [
    "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER",
];

/// Java `LocalDate.of(y, m, d)`, with its message for a day the month does not have.
pub fn local_date_of(y: i32, m: u32, d: u32) -> Result<NaiveDate> {
    NaiveDate::from_ymd_opt(y, m, d).ok_or_else(|| {
        if m == 2 && d == 29 {
            java_error(format!("Invalid date 'February 29' as '{y}' is not a leap year"))
        } else {
            java_error(format!("Invalid date '{} {d}'", MONTH_NAMES[(m as usize).clamp(1, 12) - 1]))
        }
    })
}

pub fn is_leap(y: i32) -> bool {
    (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
}

pub fn month_length(y: i32, m: u32) -> u32 {
    match m {
        2 => {
            if is_leap(y) {
                29
            } else {
                28
            }
        }
        4 | 6 | 9 | 11 => 30,
        _ => 31,
    }
}

/// Java `LocalDate.resolvePreviousValid`: the day clamped to the month's length.
fn previous_valid(y: i32, m: u32, d: u32) -> NaiveDate {
    NaiveDate::from_ymd_opt(y, m, d.min(month_length(y, m))).unwrap_or(NaiveDate::MIN)
}

impl ZonedDateTime {
    /// Java `ZonedDateTime.ofLocal(local, zone, preferredOffset)`: a gap moves the time
    /// forward by the gap's length, an overlap keeps the preferred offset when it is one of the
    /// two, else the earlier.
    pub fn of_local(local: NaiveDateTime, zone: ZoneId, preferred: Option<i32>) -> ZonedDateTime {
        let tz = match &zone {
            ZoneId::Offset(o) | ZoneId::Region { rules: Rules::Fixed(o), .. } => {
                return ZonedDateTime { local, offset: *o, zone: zone.clone() }
            }
            ZoneId::Region { rules: Rules::Tz(tz), .. } => *tz,
        };
        let secs = |o: chrono_tz::TzOffset| o.fix().local_minus_utc();
        match tz.offset_from_local_datetime(&local) {
            LocalResult::Single(o) => ZonedDateTime { local, offset: secs(o), zone },
            LocalResult::Ambiguous(a, b) => {
                let (a, b) = (secs(a), secs(b));
                let offset = match preferred {
                    Some(p) if p == a || p == b => p,
                    _ => a,
                };
                ZonedDateTime { local, offset, zone }
            }
            LocalResult::None => {
                let before = secs(tz.offset_from_utc_datetime(&(local - Duration::hours(24))));
                let after = secs(tz.offset_from_utc_datetime(&(local - Duration::seconds(before as i64))));
                let local = local + Duration::seconds((after - before) as i64);
                ZonedDateTime { local, offset: after, zone }
            }
        }
    }

    /// Java `ZonedDateTime.ofInstant`.
    pub fn of_instant(epoch_second: i64, nano: u32, zone: ZoneId) -> Result<ZonedDateTime> {
        let offset = zone.offset_at(epoch_second);
        let local = chrono::DateTime::from_timestamp(epoch_second + offset as i64, nano)
            .map(|d| d.naive_utc())
            .ok_or_else(|| java_error("Instant exceeds minimum or maximum instant"))?;
        Ok(ZonedDateTime { local, offset, zone })
    }

    /// Java `ZonedDateTime.now()`.
    pub fn now() -> ZonedDateTime {
        let now = chrono::Utc::now();
        let nano = now.timestamp_subsec_nanos() / 1000 * 1000;
        ZonedDateTime::of_instant(now.timestamp(), nano, system_default_zone())
            .unwrap_or_else(|_| ZonedDateTime { local: now.naive_utc(), offset: 0, zone: ZoneId::utc() })
    }

    pub fn epoch_second(&self) -> i64 {
        self.local.and_utc().timestamp() - self.offset as i64
    }

    pub fn nano(&self) -> u32 {
        self.local.nanosecond() % 1_000_000_000
    }

    /// Java `toInstant().toEpochMilli()`.
    pub fn to_epoch_milli(&self) -> i64 {
        self.epoch_second() * 1000 + (self.nano() / 1_000_000) as i64
    }

    fn instant(&self) -> (i64, u32) {
        (self.epoch_second(), self.nano())
    }

    pub fn is_before(&self, other: &ZonedDateTime) -> bool {
        self.instant() < other.instant()
    }

    pub fn is_after(&self, other: &ZonedDateTime) -> bool {
        self.instant() > other.instant()
    }

    /// Java `isEqual`: the same instant.
    pub fn is_equal(&self, other: &ZonedDateTime) -> bool {
        self.instant() == other.instant()
    }

    /// Java `equals`: the same local date-time, offset and zone.
    pub fn java_equals(&self, other: &ZonedDateTime) -> bool {
        self.local == other.local && self.offset == other.offset && self.zone == other.zone
    }

    /// Java `compareTo`: instant, then local date-time, then zone ID.
    pub fn java_compare(&self, other: &ZonedDateTime) -> std::cmp::Ordering {
        self.instant()
            .cmp(&other.instant())
            .then_with(|| self.local.cmp(&other.local))
            .then_with(|| self.zone.id().cmp(&other.zone.id()))
    }

    fn resolve_local(&self, local: NaiveDateTime) -> ZonedDateTime {
        ZonedDateTime::of_local(local, self.zone.clone(), Some(self.offset))
    }

    fn resolve_instant(&self, local: NaiveDateTime) -> Result<ZonedDateTime> {
        let epoch = local.and_utc().timestamp() - self.offset as i64;
        ZonedDateTime::of_instant(epoch, local.nanosecond() % 1_000_000_000, self.zone.clone())
    }

    /// Java `plus(amount, unit)`: date units on the local date-time, time units on the instant.
    pub fn plus(&self, amount: i64, unit: Unit) -> Result<ZonedDateTime> {
        let overflow = || java_error("long overflow");
        let local = match unit {
            Unit::Years => {
                let y = (self.local.year() as i64).checked_add(amount).ok_or_else(overflow)?;
                let y = i32::try_from(y).map_err(|_| invalid_value("Year", "-999999999 - 999999999", y))?;
                previous_valid(y, self.local.month(), self.local.day()).and_time(self.local.time())
            }
            Unit::Months => {
                let total = self.local.year() as i64 * 12 + (self.local.month() as i64 - 1) + amount;
                let y = i32::try_from(total.div_euclid(12)).map_err(|_| overflow())?;
                let m = total.rem_euclid(12) as u32 + 1;
                previous_valid(y, m, self.local.day()).and_time(self.local.time())
            }
            Unit::Weeks => self.local + Duration::days(amount.checked_mul(7).ok_or_else(overflow)?),
            Unit::Days => self.local + Duration::days(amount),
            Unit::Hours => self.local + Duration::hours(amount),
            Unit::Minutes => self.local + Duration::minutes(amount),
            Unit::Seconds => self.local + Duration::seconds(amount),
            Unit::Millis => self.local + Duration::milliseconds(amount),
        };
        if unit.is_date_based() {
            Ok(self.resolve_local(local))
        } else {
            self.resolve_instant(local)
        }
    }

    /// Java `with(ChronoField, value)`.
    pub fn with(&self, field: SetField, value: i64) -> Result<ZonedDateTime> {
        let l = self.local;
        let check = |name: &str, range: &str, lo: i64, hi: i64| {
            if value < lo || value > hi {
                Err(invalid_value(name, range, value))
            } else {
                Ok(value)
            }
        };
        let local = match field {
            SetField::DayOfMonth => {
                let d = check("DayOfMonth", "1 - 28/31", 1, 31)? as u32;
                local_date_of(l.year(), l.month(), d)?.and_time(l.time())
            }
            SetField::MonthOfYear => {
                let m = check("MonthOfYear", "1 - 12", 1, 12)? as u32;
                previous_valid(l.year(), m, l.day()).and_time(l.time())
            }
            SetField::Year => {
                let y = check("Year", "-999999999 - 999999999", -999_999_999, 999_999_999)? as i32;
                previous_valid(y, l.month(), l.day()).and_time(l.time())
            }
            SetField::HourOfDay => {
                let h = check("HourOfDay", "0 - 23", 0, 23)? as u32;
                l.with_hour(h).unwrap_or(l)
            }
            SetField::MinuteOfHour => {
                let m = check("MinuteOfHour", "0 - 59", 0, 59)? as u32;
                l.with_minute(m).unwrap_or(l)
            }
            SetField::SecondOfMinute => {
                let s = check("SecondOfMinute", "0 - 59", 0, 59)? as u32;
                l.with_second(s).unwrap_or(l)
            }
            SetField::MilliOfSecond => {
                let ms = check("MilliOfSecond", "0 - 999", 0, 999)? as u32;
                l.with_nanosecond(ms * 1_000_000).unwrap_or(l)
            }
        };
        Ok(self.resolve_local(local))
    }

    /// Java `with(LocalDate adjuster)`, keeping the time.
    pub fn with_date(&self, date: NaiveDate) -> ZonedDateTime {
        self.resolve_local(date.and_time(self.local.time()))
    }

    /// Java `truncatedTo(unit)` for the time units and `DAYS`.
    pub fn truncated_to(&self, unit: Unit) -> ZonedDateTime {
        let t = self.local.time();
        let time = match unit {
            Unit::Days => NaiveTime::MIN,
            Unit::Hours => NaiveTime::from_hms_opt(t.hour(), 0, 0).unwrap_or(t),
            Unit::Minutes => NaiveTime::from_hms_opt(t.hour(), t.minute(), 0).unwrap_or(t),
            Unit::Seconds => NaiveTime::from_hms_opt(t.hour(), t.minute(), t.second()).unwrap_or(t),
            Unit::Millis => t.with_nanosecond(t.nanosecond() / 1_000_000 * 1_000_000).unwrap_or(t),
            _ => t,
        };
        self.resolve_local(self.local.date().and_time(time))
    }

    /// Java `withZoneSameInstant`.
    pub fn with_zone_same_instant(&self, zone: ZoneId) -> Result<ZonedDateTime> {
        ZonedDateTime::of_instant(self.epoch_second(), self.nano(), zone)
    }

    /// Java `getZone().getRules().isDaylightSavings(toInstant())`.
    pub fn is_daylight_savings(&self) -> bool {
        let e = self.epoch_second();
        self.zone.standard_offset_at(e) != self.zone.offset_at(e)
    }

    /// kirun-java `DateUtil.ISO_DATE_TIME_FORMATTER`: `yyyy-MM-ddTHH:mm:ss.SSS` and the offset ID.
    pub fn format_iso(&self) -> String {
        let l = self.local;
        format!(
            "{}-{:02}-{:02}T{:02}:{:02}:{:02}.{:03}{}",
            format_year(l.year()),
            l.month(),
            l.day(),
            l.hour(),
            l.minute(),
            l.second(),
            self.nano() / 1_000_000,
            offset_id(self.offset)
        )
    }
}

/// `appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)`.
pub fn format_year(y: i32) -> String {
    if y > 9999 {
        format!("+{y}")
    } else if y < 0 {
        format!("-{:04}", -(y as i64))
    } else {
        format!("{y:04}")
    }
}

// ------------------------------------------------------------------------- the ISO reader

/// Java `DateTimeParseException` messages (the text cut at 64 characters).
pub fn parse_error_at(text: &str, index: usize) -> KIRunError {
    java_error(format!("Text '{}' could not be parsed at index {index}", abbreviate(text)))
}

pub fn unparsed_error(text: &str, index: usize) -> KIRunError {
    java_error(format!("Text '{}' could not be parsed, unparsed text found at index {index}", abbreviate(text)))
}

pub fn resolve_error(text: &str, cause: &KIRunError) -> KIRunError {
    java_error(format!("Text '{}' could not be parsed: {cause}", abbreviate(text)))
}

fn abbreviate(text: &str) -> String {
    if text.chars().count() > 64 {
        format!("{}...", text.chars().take(64).collect::<String>())
    } else {
        text.to_string()
    }
}

struct IsoParts {
    year: i64,
    month: i64,
    day: i64,
    time: Option<(i64, i64, i64, i64)>,
    offset: Option<i32>,
    zone: Option<ZoneId>,
}

/// kirun-java `DateUtil.getDateTime`: `ISO_DATE_READING_FORMATTER.parseBest(ZonedDateTime,
/// LocalDateTime, LocalDate)`, a local value placed in the system zone. Its parse errors are
/// `DateTimeParseException`s, which the method's `catch (IllegalArgumentException)` lets through.
pub fn get_date_time(text: &str) -> Result<ZonedDateTime> {
    let b = text.as_bytes();
    let mut pos = 0;
    let parts = parse_iso(text, b, &mut pos)?;
    if pos < b.len() {
        return Err(unparsed_error(text, pos));
    }
    resolve_iso(text, parts)
}

fn digits_at(b: &[u8], pos: usize, n: usize) -> Option<i64> {
    if pos + n > b.len() {
        return None;
    }
    let mut v = 0i64;
    for &c in &b[pos..pos + n] {
        if !c.is_ascii_digit() {
            return None;
        }
        v = v * 10 + (c - b'0') as i64;
    }
    Some(v)
}

fn parse_iso(text: &str, b: &[u8], pos: &mut usize) -> Result<IsoParts> {
    // ISO_LOCAL_DATE: year (4 digits, or a sign and up to 10), '-', MM, '-', dd.
    let start = *pos;
    let (negative, positive) = match b.first() {
        Some(b'-') => (true, false),
        Some(b'+') => (false, true),
        _ => (false, false),
    };
    let mut p = start + usize::from(negative || positive);
    let mut n = 0;
    while p + n < b.len() && n < 10 && b[p + n].is_ascii_digit() {
        n += 1;
    }
    if n < 4 || (!negative && !positive && n > 4) || (positive && n <= 4) {
        return Err(parse_error_at(text, start));
    }
    let mut year = digits_at(b, p, n).unwrap_or(0);
    if negative {
        if year == 0 {
            return Err(parse_error_at(text, start));
        }
        year = -year;
    }
    p += n;
    let lit = |p: usize, c: u8| b.get(p) == Some(&c);
    if !lit(p, b'-') {
        return Err(parse_error_at(text, p));
    }
    let month = digits_at(b, p + 1, 2).ok_or_else(|| parse_error_at(text, p + 1))?;
    p += 3;
    if !lit(p, b'-') {
        return Err(parse_error_at(text, p));
    }
    let day = digits_at(b, p + 1, 2).ok_or_else(|| parse_error_at(text, p + 1))?;
    p += 3;
    let mut parts = IsoParts { year, month, day, time: None, offset: None, zone: None };
    *pos = p;

    // Optional: 'T' ISO_LOCAL_TIME [offset ['[' zone ']']].
    if !(lit(p, b'T') || lit(p, b't')) {
        return Ok(parts);
    }
    let Some((time, after_time)) = parse_local_time(b, p + 1) else { return Ok(parts) };
    parts.time = Some(time);
    *pos = after_time;
    let p = after_time;

    let Some((offset, after_offset)) = parse_offset_id(b, p) else { return Ok(parts) };
    parts.offset = Some(offset);
    *pos = after_offset;
    let p = after_offset;

    if lit(p, b'[') {
        if let Some((zone, end)) = parse_zone_id(b, p + 1) {
            if lit(end, b']') {
                parts.zone = Some(zone);
                *pos = end + 1;
            }
        }
    }
    Ok(parts)
}

/// `ISO_LOCAL_TIME`: HH ':' mm [':' ss ['.' 0-9 digits]].
fn parse_local_time(b: &[u8], p: usize) -> Option<((i64, i64, i64, i64), usize)> {
    let h = digits_at(b, p, 2)?;
    if b.get(p + 2) != Some(&b':') {
        return None;
    }
    let m = digits_at(b, p + 3, 2)?;
    let mut p = p + 5;
    let (mut s, mut nano) = (0, 0);
    if b.get(p) == Some(&b':') {
        if let Some(sv) = digits_at(b, p + 1, 2) {
            s = sv;
            p += 3;
            if b.get(p) == Some(&b'.') {
                p += 1;
                let mut n = 0;
                while n < 9 && p + n < b.len() && b[p + n].is_ascii_digit() {
                    n += 1;
                }
                let digits = digits_at(b, p, n).unwrap_or(0);
                nano = digits * 10i64.pow(9 - n as u32);
                p += n;
            }
        }
    }
    Some(((h, m, s, nano), p))
}

/// `appendOffsetId()` (`+HH:MM:ss`, `Z`), case-insensitive: `Z`, or a sign, HH ':' MM [':' ss].
fn parse_offset_id(b: &[u8], p: usize) -> Option<(i32, usize)> {
    match b.get(p) {
        Some(b'Z') | Some(b'z') => Some((0, p + 1)),
        Some(&c) if c == b'+' || c == b'-' => {
            let h = digits_at(b, p + 1, 2)?;
            if b.get(p + 3) != Some(&b':') {
                return None;
            }
            let m = digits_at(b, p + 4, 2)?;
            let mut end = p + 6;
            let mut s = 0;
            if b.get(end) == Some(&b':') {
                if let Some(sv) = digits_at(b, end + 1, 2) {
                    s = sv;
                    end += 3;
                }
            }
            if h > 23 || m > 59 || s > 59 {
                return None;
            }
            let total = (h * 3600 + m * 60 + s) as i32;
            Some((if c == b'-' { -total } else { total }, end))
        }
        _ => None,
    }
}

/// Java's `ZoneIdPrinterParser` (strict): an offset, a `UTC`/`UT`/`GMT` prefixed ID, or the
/// longest tzdb ID at the position.
pub fn parse_zone_id(b: &[u8], p: usize) -> Option<(ZoneId, usize)> {
    let rest = std::str::from_utf8(&b[p.min(b.len())..]).ok()?;
    if rest.is_empty() {
        return None;
    }
    if rest.starts_with('+') || rest.starts_with('-') {
        let (o, end) = parse_offset_id(b, p)?;
        return Some((ZoneId::Offset(o), end));
    }
    for prefix in ["UTC", "GMT", "UT"] {
        if rest.starts_with(prefix) && !(prefix == "UT" && rest.starts_with("UTC")) {
            if prefix == "GMT" && rest[3..].starts_with('0') {
                return ZoneId::region("GMT0").ok().map(|z| (z, p + 4));
            }
            let after = p + prefix.len();
            return match parse_offset_id(b, after).filter(|_| matches!(b.get(after), Some(b'+') | Some(b'-'))) {
                Some((o, end)) => Some((ZoneId::prefixed(prefix, o), end)),
                None => Some((ZoneId::prefixed(prefix, 0), after)),
            };
        }
    }
    let best = java_zone_ids().iter().filter(|id| rest.starts_with(**id)).max_by_key(|id| id.len());
    match best {
        Some(id) => Some((ZoneId::region(id).ok()?, p + id.len())),
        None if rest.starts_with('Z') => Some((ZoneId::Offset(0), p + 1)),
        None => None,
    }
}

fn resolve_iso(text: &str, parts: IsoParts) -> Result<ZonedDateTime> {
    let fail = |e: KIRunError| resolve_error(text, &e);
    let date = resolve_ymd(parts.year, parts.month, parts.day).map_err(fail)?;
    let Some((h, m, s, nano)) = parts.time else {
        return Ok(start_of_day(date, system_default_zone()));
    };
    let (time, extra_day) = resolve_time(h, m, s, nano).map_err(fail)?;
    let local = date.and_time(time) + Duration::days(extra_day);
    let Some(offset) = parts.offset else {
        return Ok(ZonedDateTime::of_local(local, system_default_zone(), None));
    };
    let offset = offset_of_total(offset as i64).map_err(fail)?;
    let epoch = local.and_utc().timestamp() - offset as i64;
    match parts.zone {
        Some(zone) => ZonedDateTime::of_instant(epoch, nano as u32, zone),
        None => Ok(ZonedDateTime { local, offset, zone: ZoneId::Offset(offset) }),
    }
}

/// Java `LocalDate.atStartOfDay(zone)`: midnight, or the first valid time after a gap.
pub fn start_of_day(date: NaiveDate, zone: ZoneId) -> ZonedDateTime {
    ZonedDateTime::of_local(date.and_time(NaiveTime::MIN), zone, None)
}

/// `IsoChronology.resolveYMD` in SMART mode: the day clamped to the month's last.
pub fn resolve_ymd(y: i64, m: i64, d: i64) -> Result<NaiveDate> {
    if !(-999_999_999..=999_999_999).contains(&y) {
        return Err(invalid_value("Year", "-999999999 - 999999999", y));
    }
    if !(1..=12).contains(&m) {
        return Err(invalid_value("MonthOfYear", "1 - 12", m));
    }
    if !(1..=31).contains(&d) {
        return Err(invalid_value("DayOfMonth", "1 - 28/31", d));
    }
    let (y, m) = (y as i32, m as u32);
    NaiveDate::from_ymd_opt(y, m, (d as u32).min(month_length(y, m)))
        .ok_or_else(|| java_error(format!("Invalid value for Year (valid values -999999999 - 999999999): {y}")))
}

/// `Parsed.resolveTime` in SMART mode: `24:00:00` is the next midnight.
pub fn resolve_time(h: i64, m: i64, s: i64, nano: i64) -> Result<(NaiveTime, i64)> {
    if !(0..=59).contains(&m) {
        return Err(invalid_value("MinuteOfHour", "0 - 59", m));
    }
    if !(0..=999_999_999).contains(&nano) {
        return Err(invalid_value("NanoOfSecond", "0 - 999999999", nano));
    }
    if h == 24 && m == 0 && s == 0 && nano == 0 {
        return Ok((NaiveTime::MIN, 1));
    }
    if !(0..=23).contains(&h) {
        return Err(invalid_value("HourOfDay", "0 - 23", h));
    }
    if !(0..=59).contains(&s) {
        return Err(invalid_value("SecondOfMinute", "0 - 59", s));
    }
    Ok((NaiveTime::from_hms_nano_opt(h as u32, m as u32, s as u32, nano as u32).unwrap_or(NaiveTime::MIN), 0))
}

#[cfg(test)]
mod tests {
    use super::*;

    fn iso(s: &str) -> String {
        match get_date_time(s) {
            Ok(z) => z.format_iso(),
            Err(e) => format!("ERR {e}"),
        }
    }

    #[test]
    fn reads_and_writes_like_java() {
        set_thread_zone(Some(ZoneId::of("Asia/Kolkata").unwrap()));
        assert_eq!(iso("2024-01-15"), "2024-01-15T00:00:00.000+05:30");
        assert_eq!(iso("2024-01-15t10:20:30z"), "2024-01-15T10:20:30.000Z");
        assert_eq!(iso("2024-01-15T10:20:30Z[Asia/Kolkata]"), "2024-01-15T15:50:30.000+05:30");
        assert_eq!(iso("2024-02-30"), "2024-02-29T00:00:00.000+05:30");
        assert_eq!(iso("2024-01-15T24:00"), "2024-01-16T00:00:00.000+05:30");
        assert_eq!(iso("+12024-01-15"), "+12024-01-15T00:00:00.000+05:30");
        assert_eq!(iso("-2024-01-15"), "-2024-01-15T00:00:00.000+05:53:28");
        assert_eq!(iso("2024-01-15T10:20:30+05"), "ERR Text '2024-01-15T10:20:30+05' could not be parsed, unparsed text found at index 19");
        assert_eq!(iso("2024-13-01"), "ERR Text '2024-13-01' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13");
        assert_eq!(iso("12024-01-15"), "ERR Text '12024-01-15' could not be parsed at index 0");
        assert_eq!(iso("2024-01-15T10:20:30+19:00"), "ERR Text '2024-01-15T10:20:30+19:00' could not be parsed: Zone offset not in valid range: -18:00 to +18:00");
        set_thread_zone(None);
    }

    #[test]
    fn zone_ids_like_java() {
        let id = |s: &str| ZoneId::of(s).map(|z| z.id()).unwrap_or_else(|e| format!("ERR {e}"));
        assert_eq!(id("+5"), "+05:00");
        assert_eq!(id("GMT+5"), "GMT+05:00");
        assert_eq!(id("UTC"), "UTC");
        assert_eq!(id("local"), "ERR Unknown time-zone ID: local");
        assert_eq!(id("EST"), "ERR Unknown time-zone ID: EST");
        assert_eq!(id(""), "ERR Invalid ID for ZoneOffset, invalid format: ");
        assert_eq!(id("+5:3"), "ERR Invalid ID for ZoneOffset, invalid format: +5:3");
        assert_eq!(id("+19:00"), "ERR Zone offset hours not in valid range: value 19 is not in the range -18 to 18");
    }
}

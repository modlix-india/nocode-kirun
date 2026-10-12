//! Port of kirun-js `function/system/date/common.ts`, plus the Rust stand-in for luxon's
//! process-wide `Settings.defaultZone`.
//!
//! The default zone is **thread-local** so that tests running in parallel (each
//! `#[tokio::test]` uses a current-thread runtime on its own thread) can each pick a zone,
//! the way every jest file sets `Settings.defaultZone` in its own module registry.
//! Accepted values follow luxon: an IANA name (`Asia/Kolkata`), `UTC`, or `UTC+05:30`.

use crate::function::system::date::luxon::{DateTime, Zone};
use crate::{KIRunError, Result};

/// JS `getDateTime(isoTimestamp)`: `DateTime.fromISO(iso, { setZone: true })`, error
/// `Invalid ISO timestamp` when invalid.
pub fn get_date_time(iso_timestamp: &str) -> Result<DateTime> {
    DateTime::from_iso(iso_timestamp, true).ok_or_else(|| KIRunError::runtime("Invalid ISO timestamp"))
}

/// luxon `Settings.defaultZone = zone` for the current thread. An unknown zone resets to the
/// system zone (luxon would make every DateTime invalid instead).
pub fn set_default_zone(zone: &str) {
    Zone::set_default_zone(Zone::parse(zone));
}

/// luxon `Settings.defaultZone.name` for the current thread.
pub fn get_default_zone() -> String {
    Zone::default_zone().name()
}

//! Thread-local defaults for the date functions' tests: the zone (`TimeZone.setDefault` in
//! kirun-java's tests) and the locale. Production uses the machine's, as the JVM does (see
//! `java_time`).

use crate::function::system::date::java_time::{set_thread_locale, set_thread_zone, system_default_zone, ZoneId};
pub use crate::function::system::date::java_time::get_date_time;

/// Sets the default zone for the current thread (a Java zone ID: `Asia/Kolkata`, `UTC`,
/// `+05:30`). An ID Java refuses resets to the machine's zone.
pub fn set_default_zone(zone: &str) {
    set_thread_zone(ZoneId::of(zone).ok());
}

/// The default zone's ID for the current thread.
pub fn get_default_zone() -> String {
    system_default_zone().id()
}

/// Sets (or with `None`, clears) the default locale for the current thread (`en_US`, `en-IN`).
pub fn set_default_locale(locale: Option<&str>) {
    set_thread_locale(locale);
}

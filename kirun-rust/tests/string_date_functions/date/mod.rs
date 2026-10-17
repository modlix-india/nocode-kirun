//! Ports of kirun-js `__tests__/engine/function/system/date/*`.
//!
//! Time zones: jest has no `TZ` setting (package.json `jest` block sets none), so JS results
//! depend on luxon's `Settings.defaultZone`, which most JS files set at the top. Each Rust
//! test calls `set_default_zone(..)` (thread-local stand-in for `Settings.defaultZone`; see
//! `kirun::function::system::date::common`) with the same value as its JS file. Files whose JS
//! sets no zone but whose expectations need one assumed the developer's process zone was
//! `Asia/Kolkata` (IST, +05:30); those tests set it explicitly and say so.

mod add_subtract_time_test;
mod date_function_repository_test;
mod difference_test;
mod epoch_to_timestamp_test;
mod from_date_string_test;
mod from_now_test;
mod get_current_time_stamp_test;
mod get_names_test;
mod is_between_test;
mod is_valid_iso_date_test;
mod java_parity_test;
mod last_first_of_test;
mod set_time_zone_test;
mod start_end_of_test;
mod time_as_test;
mod timestamp_to_epoch_test;
mod to_date_string_test;

use kirun::Value;

pub use kirun::function::system::date::common::set_default_zone;

/// JS `expect(actual).toMatchObject(expected)` for a flat object: every key of `expected`
/// must be present in `actual` with an equal value; extra keys in `actual` are allowed.
pub fn assert_match_object(actual: &Value, expected: &Value) {
    let a = actual.as_object().unwrap_or_else(|| panic!("not an object: {actual}"));
    for (k, v) in expected.as_object().expect("expected must be an object") {
        let got = a.get(k).unwrap_or_else(|| panic!("missing key {k:?} in {actual}"));
        assert!(crate::common::json_eq(got, v), "key {k:?}: actual {got}, expected {v} (in {actual})");
    }
}

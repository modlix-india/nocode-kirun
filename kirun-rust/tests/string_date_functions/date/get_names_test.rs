//! Port of kirun-js `__tests__/engine/function/system/date/GetNamesTest.ts`.

use kirun::function::system::date::get_names::GetNames;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn should_return_the_names_of_the_units() {
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "TIMEZONES" });

    let result = first_value(&get_names, fep, "names").await.unwrap();

    // JS: `toEqual(Intl.supportedValuesOf('timeZone'))`, i.e. whatever list the JS runtime's ICU
    // ships (418 entries on Node at the time of porting, ICU-canonical names such as
    // `Asia/Calcutta`, no `UTC`). That list cannot be reproduced exactly from Rust, so the intent
    // is asserted: a non-empty array of distinct IANA zone names that chrono-tz understands,
    // containing well-known zones.
    let names = result.as_array().expect("names is an array");
    assert!(!names.is_empty());
    let mut seen = std::collections::HashSet::new();
    for n in names {
        let s = n.as_str().expect("every name is a string");
        assert!(s.parse::<chrono_tz::Tz>().is_ok(), "{s} is not an IANA zone");
        assert!(seen.insert(s.to_string()), "duplicate zone {s}");
    }
    for z in ["Africa/Abidjan", "America/New_York", "Europe/London", "Asia/Tokyo"] {
        assert!(seen.contains(z), "missing {z}");
    }
}

#[tokio::test]
async fn should_return_the_names_of_the_months() {
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "MONTHS" });

    let result = first_value(&get_names, fep, "names").await.unwrap();

    assert_json_eq!(
        result,
        json!([
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        ])
    );
}

#[tokio::test]
async fn should_return_the_names_of_the_weekdays() {
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "WEEKDAYS" });

    let result = first_value(&get_names, fep, "names").await.unwrap();

    assert_json_eq!(
        result,
        json!(["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
    );
}

#[tokio::test]
async fn should_return_the_names_of_months_in_french() {
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "MONTHS", "locale" => "fr" });

    let result = first_value(&get_names, fep, "names").await.unwrap();

    assert_json_eq!(
        result,
        json!([
            "janvier",
            "février",
            "mars",
            "avril",
            "mai",
            "juin",
            "juillet",
            "août",
            "septembre",
            "octobre",
            "novembre",
            "décembre"
        ])
    );
}

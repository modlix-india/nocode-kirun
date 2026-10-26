//! Port of kirun-js `__tests__/engine/function/system/date/GetNamesTest.ts`.

use kirun::function::system::date::get_names::GetNames;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn should_return_the_names_of_the_units() {
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "TIMEZONES" });

    let result = first_value(&get_names, fep, "names").await.unwrap();

    // JS: `toEqual(Intl.supportedValuesOf('timeZone'))`. kirun-java lists every zone's display
    // name (in `Locale.forLanguageTag("system")`, in its `HashSet`'s order), which kirun-rust
    // cannot reproduce exactly: it lists a display name for every tzdb zone the JVM knows, by ID.
    let names = result.as_array().expect("names is an array");
    assert!(names.len() > 500);
    let seen: std::collections::HashSet<String> =
        names.iter().map(|n| n.as_str().expect("every name is a string").to_string()).collect();
    for z in ["Coordinated Universal Time", "Eastern Time", "London Time", "Japan Time", "Kolkata Time"] {
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
    // kirun-java ignores `locale` for months (it formats in the default locale); kirun-js gave
    // janvier ... décembre.
    let get_names = GetNames::new();
    let fep = fep().set_arguments(args! { "unit" => "MONTHS", "locale" => "fr" });

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

//! `System.Date` against kirun-java: `java_date_cases.jsonl` holds 944 calls and what kirun-java
//! 4.9.2 (the Java core's version) answered, measured with the JVM in UTC and `en_US` (the zone
//! and locale of the Linux images, which set neither). Each line is the function, its arguments,
//! and either the events kirun-java raised (`ok`) or its error message (`err`). Calls whose answer
//! depends on today (GetCurrentTimestamp, FromDateString formats without a full date, FromNow
//! without a base) and GetNames TIMEZONES (kirun-java lists them in its HashSet's order) are not
//! in it.
//!
//! Numbers compare exactly, a whole number and a double apart (kirun-java writes `-1.0`). In
//! error messages the `[Expected: ...]` type list is left out: kirun-java prints it in a hash
//! order that changes from one JVM run to the next.

use kirun::function::system::date::common::set_default_locale;
use kirun::function::system::date::date_function_repository::DateFunctionRepository;
use kirun::model::FunctionOutput;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

const CASES: &str = include_str!("java_date_cases.jsonl");

fn without_expected_types(message: &str) -> String {
    match (message.find(" [Expected: "), message.find("]: ")) {
        (Some(start), Some(end)) if end > start => format!("{}{}", &message[..start], &message[end + 1..]),
        _ => message.to_string(),
    }
}

async fn run(name: &str, args: &Value) -> Result<Value, String> {
    let fun = DateFunctionRepository::new().find(Namespaces::DATE, name).await.expect("a date function");
    let args: Map<Value> = args.as_object().unwrap().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
    let mut fep = fep_with_args(args);
    let mut out: FunctionOutput = fun.execute(&mut fep).await.map_err(|e| e.to_string())?;
    let mut events = Vec::new();
    while let Some(er) = out.next() {
        let result: serde_json::Map<String, Value> = er.get_result().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
        events.push(json!({ "name": er.get_name(), "result": result }));
    }
    Ok(Value::Array(events))
}

#[tokio::test]
async fn date_functions_answer_as_kirun_java() {
    set_default_zone("UTC");
    set_default_locale(Some("en_US"));
    let mut failures = Vec::new();
    let mut count = 0;
    for line in CASES.lines().filter(|l| !l.trim().is_empty()) {
        count += 1;
        let case: Value = serde_json::from_str(line).unwrap();
        let name = case["name"].as_str().unwrap();
        let got = run(name, &case["args"]).await;
        let ok = match (&got, case.get("ok"), case.get("err")) {
            (Ok(v), Some(expected), _) => v == expected,
            (Err(e), _, Some(Value::String(expected))) => without_expected_types(e) == without_expected_types(expected),
            _ => false,
        };
        if !ok {
            failures.push(format!("{name} {}\n    java {}\n    rust {got:?}", case["args"], case.get("ok").or(case.get("err")).unwrap()));
        }
    }
    set_default_locale(None);
    assert_eq!(count, 944);
    assert!(failures.is_empty(), "{} of {count} differ:\n{}", failures.len(), failures.join("\n"));
}

#[tokio::test]
async fn kirun_java_text_in_en_in() {
    // The local macOS JVM runs in en_IN: am/pm in lower case, September as Sept.
    set_default_zone("Asia/Kolkata");
    set_default_locale(Some("en_IN"));
    let args = json!({ "isoTimeStamp": "2024-09-05T14:07:09.045+05:30", "format": "dd MMM yyyy, hh:mm a" });
    let out = run("ToDateString", &args).await.unwrap();
    set_default_locale(None);
    assert_eq!(out, json!([{ "name": "output", "result": { "result": "05 Sept 2024, 02:07 pm" } }]));
}

#[tokio::test]
async fn weeks_start_on_the_locales_first_day() {
    // kirun-java `WeekFields.of(Locale.getDefault())`: Sunday in en_US and en_IN, Monday in en_GB.
    set_default_zone("UTC");
    let args = json!({ "isoTimeStamp": "2024-01-17T10:00:00Z", "unit": "WEEKS" });
    for (locale, start) in [("en_US", "2024-01-14T00:00:00.000Z"), ("en_IN", "2024-01-14T00:00:00.000Z"), ("en_GB", "2024-01-15T00:00:00.000Z")] {
        set_default_locale(Some(locale));
        let out = run("StartOf", &args).await.unwrap();
        assert_eq!(out[0]["result"]["isoTimeStamp"], json!(start), "{locale}");
    }
    set_default_locale(None);
}

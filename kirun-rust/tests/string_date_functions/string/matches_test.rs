//! Port of kirun-js `__tests__/engine/function/system/string/MatchesTest.ts`.
//! JS reuses one `fep` and calls `setArguments` again; Rust builds a fresh one per call
//! (same repositories, same arguments), which is behaviourally identical.

use kirun::function::system::string::matches::Matches;

use super::truthy;
use crate::common::*;

async fn run(regex: &str, string: &str) -> kirun::Value {
    let matches_f = Matches::new();
    let fep = fep().set_arguments(args! { "regex" => regex, "string" => string });
    first_value(&matches_f, fep, "result").await.unwrap()
}

#[tokio::test]
async fn simple_matches_test() {
    assert!(truthy(&run(r"(\d{2}).(\d{2}).(\d{4})", "10.12.1222").await));
    assert!(!truthy(&run(r"(\d{2}).(\d{2}).(\d{4})$", "10.12.1222 ").await));
    assert!(!truthy(&run(r"(\d{2}).(\d{2}).(\d{4})", "fdsgjhg10.12.122 2").await));
}

#[tokio::test]
async fn simple_name_test() {
    assert!(truthy(&run(r"(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)", "smith, Mr.John").await));
    assert!(truthy(
        &run(r"(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)", "How are you doing smith, Mr.John??").await
    ));
    assert!(!truthy(&run(r"$(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)$", "smith, Mr.Johnadf").await));
}

#[tokio::test]
async fn simple_time_test() {
    assert!(truthy(
        &run(r"^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$", "03:33:33").await
    ));
    assert!(!truthy(
        &run(
            r"([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$",
            "How are you d 03:33:33oing smith, Mr.John??"
        )
        .await
    ));
    assert!(truthy(
        &run(
            r"([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?",
            "surendhar12:12:12-02:54asd"
        )
        .await
    ));
    assert!(!truthy(
        &run(
            r"^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?",
            "surendhar12:12:12-02:54asd"
        )
        .await
    ));
}

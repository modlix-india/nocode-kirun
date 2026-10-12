//! Port of kirun-js `__tests__/engine/function/system/date/DateFunctionRepositoryTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`.

use kirun::function::system::date::date_function_repository::DateFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

async fn run(name: &str, args: Map<Value>) -> Value {
    set_default_zone("Asia/Kolkata");
    let fun = DateFunctionRepository::new().find(Namespaces::DATE, name).await.unwrap();
    first_value(fun.as_ref(), fep().set_arguments(args), "result").await.unwrap()
}

// describe('DateFunctionRepository')

#[tokio::test]
async fn should_return_date_by_using_get_date() {
    assert_json_eq!(run("GetDay", args! { "isoTimeStamp" => "2024-01-01" }).await, json!(1));
}

#[tokio::test]
async fn should_return_the_days_in_month_using_get_days_in_month() {
    assert_json_eq!(run("GetDaysInMonth", args! { "isoTimeStamp" => "2024-01-01" }).await, json!(31));
}

#[tokio::test]
async fn should_return_the_days_in_year_using_get_days_in_year() {
    assert_json_eq!(run("GetDaysInYear", args! { "isoTimeStamp" => "2024-01-01" }).await, json!(366));
}

#[tokio::test]
async fn should_return_the_day_of_week_using_get_day_of_week() {
    assert_json_eq!(run("GetDayOfWeek", args! { "isoTimeStamp" => "2024-01-01" }).await, json!(1));
}

#[tokio::test]
async fn should_return_the_days_in_a_non_leap_year_using_get_days_in_year() {
    assert_json_eq!(run("GetDaysInYear", args! { "isoTimeStamp" => "2023-01-01" }).await, json!(365));
}

#[tokio::test]
async fn should_return_the_date_with_the_day_set_using_set_day() {
    assert_json_eq!(
        run("SetDay", args! { "isoTimeStamp" => "2024-01-01", "number" => 2 }).await,
        json!("2024-01-02T00:00:00.000+05:30")
    );
}

// describe('DateFunctionRepository comparisons')

async fn compare(name: &str, t1: &str, t2: &str) -> Value {
    run(name, args! { "isoTimeStamp1" => t1, "isoTimeStamp2" => t2 }).await
}

#[tokio::test]
async fn is_same_is_true_for_identical_timestamps() {
    assert_json_eq!(
        compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T10:00:00.000Z").await,
        json!(true)
    );
}

#[tokio::test]
async fn is_same_is_true_for_the_same_instant_in_different_offsets() {
    assert_json_eq!(
        compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T15:30:00.000+05:30").await,
        json!(true)
    );
}

#[tokio::test]
async fn is_same_is_false_for_different_instants() {
    assert_json_eq!(
        compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T10:00:00.001Z").await,
        json!(false)
    );
    assert_json_eq!(compare("IsSame", "2024-01-01", "2024-01-02").await, json!(false));
}

#[tokio::test]
async fn is_before_is_after_is_same_or_before_is_same_or_after_agree_with_is_same() {
    let a = "2024-01-01T10:00:00.000Z";
    let same_as_a = "2024-01-01T15:30:00.000+05:30";
    let b = "2024-01-01T11:00:00.000Z";

    assert_json_eq!(compare("IsBefore", a, b).await, json!(true));
    assert_json_eq!(compare("IsBefore", a, same_as_a).await, json!(false));
    assert_json_eq!(compare("IsAfter", b, a).await, json!(true));
    assert_json_eq!(compare("IsAfter", a, same_as_a).await, json!(false));
    assert_json_eq!(compare("IsSameOrBefore", a, same_as_a).await, json!(true));
    assert_json_eq!(compare("IsSameOrBefore", b, a).await, json!(false));
    assert_json_eq!(compare("IsSameOrAfter", a, same_as_a).await, json!(true));
    assert_json_eq!(compare("IsSameOrAfter", a, b).await, json!(false));
}

//! Port of kirun-js `__tests__/engine/function/system/string/StringFunctionRepositoryTest.ts`.

use kirun::function::system::string::string_function_repository::StringFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::*;

async fn run(name: &str, args: Map<Value>) -> Value {
    let fun = StringFunctionRepository::new()
        .find(Namespaces::STRING, name)
        .await
        .expect("Function not available");
    first_value(fun.as_ref(), fep().set_arguments(args), "result").await.unwrap()
}

#[tokio::test]
async fn string_function_repository_trim() {
    assert_json_eq!(run("Trim", args! { "string" => " Kiran " }).await, json!("Kiran"));
}

#[tokio::test]
async fn string_function_repo_repeat() {
    assert_json_eq!(
        run("Repeat", args! { "string" => " surendhar ", "index" => 3 }).await,
        json!(" surendhar  surendhar  surendhar ")
    );
}

#[tokio::test]
async fn string_function_repo_lowercase() {
    assert_json_eq!(run("LowerCase", args! { "string" => " SURENDHAR " }).await, json!(" surendhar "));
}

#[tokio::test]
async fn string_function_repo_upper_case() {
    assert_json_eq!(run("UpperCase", args! { "string" => " surendhar " }).await, json!(" SURENDHAR "));
}

#[tokio::test]
async fn string_function_repo_blank1() {
    assert_json_eq!(run("IsBlank", args! { "string" => "" }).await, json!(true));
}

#[tokio::test]
async fn string_function_repo_blank2() {
    assert_json_eq!(run("IsBlank", args! { "string" => " this is a string" }).await, json!(false));
}

#[tokio::test]
async fn string_function_repo_empty1() {
    assert_json_eq!(run("IsEmpty", args! { "string" => "" }).await, json!(true));
}

#[tokio::test]
async fn string_function_repo_empty2() {
    assert_json_eq!(run("IsEmpty", args! { "string" => " " }).await, json!(false));
}

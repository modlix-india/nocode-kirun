//! Port of kirun-js `__tests__/engine/function/system/string/StringFunctionRepoTest4.ts`.

use kirun::function::system::string::string_function_repository::StringFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn string_function_repository_replace() {
    let fun = StringFunctionRepository::new()
        .find(Namespaces::STRING, "Replace")
        .await
        .expect("Function not available");

    let fep1 = fep().set_arguments(args! {
        "string" => "  new elemenet  ",
        "secondString" => " ",
        "thirdString" => "",
    });
    assert_json_eq!(first_value(fun.as_ref(), fep1, "result").await.unwrap(), json!("newelemenet"));

    let fep2 = fep().set_arguments(args! {
        "string" => "thereisnospace",
        "secondString" => "   ",
        "thirdString" => "  ",
    });
    assert_json_eq!(first_value(fun.as_ref(), fep2, "result").await.unwrap(), json!("thereisnospace"));
}

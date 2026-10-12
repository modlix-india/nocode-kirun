//! Port of kirun-js `__tests__/engine/function/system/string/StringFunctionRepoTest3.ts`.
//! Both JS tests are named `StringRepo3 - EqualsIgnoreCase`; the second gets a `_2` suffix.

use kirun::function::system::string::string_function_repository::StringFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use serde_json::json;

use super::truthy;
use crate::common::*;

async fn run(string: &str, search_string: &str) -> kirun::Value {
    let fun = StringFunctionRepository::new()
        .find(Namespaces::STRING, "EqualsIgnoreCase")
        .await
        .expect("Function not available");
    let fep = fep().set_arguments(args! { "string" => string, "searchString" => search_string });
    first_value(fun.as_ref(), fep, "result").await.unwrap()
}

#[tokio::test]
async fn string_repo3_equals_ignore_case() {
    assert!(truthy(&run(" THIS IS A NOcoDE plATFORM\t\t", " THIS IS A NOCODE PLATFORM\t\t").await));
    assert!(!truthy(
        &run("    20934 123 123 245-0 34\" 3434 \" 123", " w20934 123 123 245-0 34\" 3434 \" 123   ").await
    ));
}

#[tokio::test]
async fn string_repo3_equals_ignore_case_2() {
    assert_json_eq!(
        run("         no code  Kirun  PLATform   ", "         NO CODE  KIRUN  PLATFORM   ").await,
        json!(true)
    );
}

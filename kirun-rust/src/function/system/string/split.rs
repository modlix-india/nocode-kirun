//! Port of kirun-js `function/system/string/Split.ts`, splitting as kirun-java does.

use crate::function::system::string::abstract_string_function::{arg_str, output_event, signature, string_function};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::Parameter;
use crate::util::string::java_string;
use crate::{KIRunError, Map, Value};

string_function!(
    Split,
    signature(
        "Split",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("searchString", Schema::of_string("searchString")),
        ],
        output_event(Schema::of_array("result", vec![])),
    ),
    "result",
    |a: &Map<Value>| {
        Ok(Value::Array(
            // kirun-java calls `String.split`: a regular expression, trailing empty parts dropped.
            java_string::split(arg_str(a, "string"), arg_str(a, "searchString"))
                .map_err(KIRunError::runtime)?
                .into_iter()
                .map(Value::String)
                .collect(),
        ))
    }
);

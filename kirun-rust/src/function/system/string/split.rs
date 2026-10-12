//! Port of kirun-js `function/system/string/Split.ts`.

use crate::function::system::string::abstract_string_function::{arg_str, output_event, signature, string_function};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::Parameter;
#[allow(unused_imports)]
use crate::util::string::js_string;
#[allow(unused_imports)]
use crate::{Map, Value};

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
            js_string::split(arg_str(a, "string"), arg_str(a, "searchString")).into_iter().map(Value::String).collect(),
        ))
    }
);

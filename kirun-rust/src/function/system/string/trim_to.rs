//! Port of kirun-js `function/system/string/TrimTo.ts`.

use crate::function::system::string::abstract_string_function::{arg_num, arg_str, output_event, signature, string_function};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::Parameter;
#[allow(unused_imports)]
use crate::util::string::js_string;
#[allow(unused_imports)]
use crate::{Map, Value};

string_function!(
    TrimTo,
    signature(
        "TrimTo",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("length", Schema::of_integer("length")),
        ],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| Ok(Value::String(js_string::substring(arg_str(a, "string"), 0.0, arg_num(a, "length"))))
);

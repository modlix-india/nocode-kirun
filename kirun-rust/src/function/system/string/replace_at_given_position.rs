//! Port of kirun-js `function/system/string/ReplaceAtGivenPosition.ts`.

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
    ReplaceAtGivenPosition,
    signature(
        "ReplaceAtGivenPosition",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("startPosition", Schema::of_integer("startPosition")),
            Parameter::new("lengthPosition", Schema::of_integer("lengthPosition")),
            Parameter::new("replaceString", Schema::of_string("replaceString")),
        ],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let s = arg_str(a, "string");
        let (start, length) = (arg_num(a, "startPosition"), arg_num(a, "lengthPosition"));
        // JS compares the start with the length (not the string's length); kept for parity.
        if start < length {
            return Ok(Value::String(format!(
                "{}{}{}",
                js_string::substring(s, 0.0, start),
                arg_str(a, "replaceString"),
                js_string::substring(s, start + length, f64::INFINITY)
            )));
        }
        Ok(Value::String(s.to_string()))
    }
);

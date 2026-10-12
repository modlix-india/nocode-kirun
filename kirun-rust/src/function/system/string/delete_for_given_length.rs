//! Port of kirun-js `function/system/string/DeleteForGivenLength.ts`.

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
    DeleteForGivenLength,
    signature(
        "DeleteForGivenLength",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("startPosition", Schema::of_integer("startPosition")),
            Parameter::new("endPosition", Schema::of_integer("endPosition")),
        ],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let s = arg_str(a, "string");
        let (start, end) = (arg_num(a, "startPosition"), arg_num(a, "endPosition"));
        if end >= start {
            return Ok(Value::String(format!(
                "{}{}",
                js_string::substring(s, 0.0, start),
                js_string::substring(s, end, f64::INFINITY)
            )));
        }
        Ok(Value::String(s.to_string()))
    }
);

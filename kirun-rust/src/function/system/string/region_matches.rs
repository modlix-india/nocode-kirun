//! Port of kirun-js `function/system/string/RegionMatches.ts`.

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
    RegionMatches,
    signature(
        "RegionMatches",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("boolean", Schema::of_boolean("boolean")),
            Parameter::new("firstOffset", Schema::of_integer("firstOffset")),
            Parameter::new("otherString", Schema::of_string("otherString")),
            Parameter::new("secondOffset", Schema::of_integer("secondOffset")),
            Parameter::new("length", Schema::of_integer("length")),
        ],
        output_event(Schema::of_boolean("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let input = arg_str(a, "string");
        let other = arg_str(a, "otherString");
        let ignore_case = a.get("boolean").and_then(Value::as_bool).unwrap_or(false);
        let (toff, ooff, len) = (arg_num(a, "firstOffset"), arg_num(a, "secondOffset"), arg_num(a, "length"));

        if toff < 0.0
            || ooff < 0.0
            || toff + len > js_string::length(input) as f64
            || ooff + len > js_string::length(other) as f64
        {
            return Ok(Value::Bool(false));
        }
        let (s1, s2) = (js_string::substring(input, toff, toff + len), js_string::substring(other, ooff, ooff + len));
        Ok(Value::Bool(if ignore_case { s1.to_uppercase() == s2.to_uppercase() } else { s1 == s2 }))
    }
);

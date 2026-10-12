//! Port of kirun-js `function/system/string/InsertAtGivenPosition.ts`.

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
    InsertAtGivenPosition,
    signature(
        "InsertAtGivenPosition",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("position", Schema::of_integer("position")),
            Parameter::new("insertString", Schema::of_string("insertString")),
        ],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let s = arg_str(a, "string");
        let at = arg_num(a, "position");
        Ok(Value::String(format!(
            "{}{}{}",
            js_string::substring(s, 0.0, at),
            arg_str(a, "insertString"),
            js_string::substring(s, at, f64::INFINITY)
        )))
    }
);

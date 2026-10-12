//! Port of kirun-js `function/system/string/PrePad.ts`.

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
    PrePad,
    signature(
        "PrePad",
        vec![
            Parameter::new("string", Schema::of_string("string")),
            Parameter::new("prepadString", Schema::of_string("prepadString")),
            Parameter::new("length", Schema::of_integer("length")),
        ],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let input = arg_str(a, "string");
        let pad = arg_str(a, "prepadString");
        let length = arg_num(a, "length");
        let pad_len = js_string::length(pad) as f64;

        let mut out = String::new();
        // JS loops forever on an empty pad string; Rust stops instead.
        if pad_len > 0.0 {
            let mut padded = pad_len;
            while padded <= length {
                out.push_str(pad);
                padded += pad_len;
            }
        }
        let out_len = js_string::length(&out) as f64;
        if out_len < length {
            out.push_str(&js_string::substring(pad, 0.0, length - out_len));
        }
        out.push_str(input);
        Ok(Value::String(out))
    }
);

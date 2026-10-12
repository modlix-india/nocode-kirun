//! Port of kirun-js `function/system/string/Reverse.ts`.

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
    Reverse,
    signature(
        "Reverse",
        vec![Parameter::new("value", Schema::of_string("value"))],
        output_event(Schema::of_string("value")),
    ),
    "value",
    |a: &Map<Value>| Ok(Value::String(js_string::reverse(arg_str(a, "value"))))
);

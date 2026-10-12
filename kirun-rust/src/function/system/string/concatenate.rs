//! Port of kirun-js `function/system/string/Concatenate.ts`.

use crate::function::system::string::abstract_string_function::{output_event, signature, string_function};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::Parameter;
#[allow(unused_imports)]
use crate::util::string::js_string;
#[allow(unused_imports)]
use crate::{Map, Value};

string_function!(
    /// Joins its variable-argument strings.
    Concatenate,
    signature(
        "Concatenate",
        vec![Parameter::new("value", Schema::of_string("value")).set_variable_argument(true)],
        output_event(Schema::of_string("value")),
    ),
    "value",
    |a: &Map<Value>| {
        let parts = a.get("value").and_then(Value::as_array).cloned().unwrap_or_default();
        Ok(Value::String(parts.iter().map(crate::util::primitive::js_to_string).collect()))
    }
);

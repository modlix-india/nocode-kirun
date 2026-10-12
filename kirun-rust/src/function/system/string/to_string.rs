//! Port of kirun-js `function/system/string/ToString.ts`.

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
    /// Objects and arrays become 2-space-indented JSON; anything else is `'' + input`.
    ToString,
    signature(
        "ToString",
        vec![Parameter::new("anytype", Schema::of_any("anytype"))],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let input = a.get("anytype").cloned().unwrap_or(Value::Null);
        Ok(Value::String(match &input {
            Value::Object(_) | Value::Array(_) => serde_json::to_string_pretty(&input).unwrap_or_default(),
            // JS typeof null is 'object', and JSON.stringify(null) is 'null'.
            Value::Null => "null".to_string(),
            other => crate::util::primitive::js_to_string(other),
        }))
    }
);

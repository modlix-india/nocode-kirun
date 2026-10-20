//! Port of kirun-js `function/system/string/ToString.ts`, answering as kirun-java's `ToString`:
//! Gson with pretty printing, so a string comes back quoted (`DESC` gives `"DESC"`) and every
//! value prints as [`gson::to_json_pretty`] describes. kirun-js gives `'' + input` instead.

use crate::function::system::string::abstract_string_function::{output_event, signature, string_function};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::Parameter;
#[allow(unused_imports)]
use crate::util::string::js_string;
use crate::util::json::gson;
#[allow(unused_imports)]
use crate::{Map, Value};

string_function!(
    /// kirun-java: `new GsonBuilder().setPrettyPrinting().create().toJson(input)`.
    ToString,
    signature(
        "ToString",
        vec![Parameter::new("anytype", Schema::of_any("anytype"))],
        output_event(Schema::of_string("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let input = a.get("anytype").unwrap_or(&Value::Null);
        Ok(Value::String(gson::to_json_pretty(input)))
    }
);

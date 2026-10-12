//! Port of kirun-js `function/system/string/Matches.ts`.

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
    /// JS `!!string.match(regex)?.length`.
    Matches,
    signature(
        "Matches",
        vec![
            Parameter::new("regex", Schema::of_string("regex")),
            Parameter::new("string", Schema::of_string("string")),
        ],
        output_event(Schema::of_boolean("result")),
    ),
    "result",
    |a: &Map<Value>| {
        let re = crate::function::system::string::abstract_string_function::js_regex(arg_str(a, "regex"))?;
        Ok(Value::Bool(re.is_match(arg_str(a, "string")).unwrap_or(false)))
    }
);

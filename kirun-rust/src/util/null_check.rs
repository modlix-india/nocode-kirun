//! Port of kirun-js `util/NullCheck.ts`. `undefined` and `null` are both `Value::Null`.

use crate::Value;

pub fn is_null_value(v: &Value) -> bool {
    v.is_null()
}

pub fn is_null_opt(v: Option<&Value>) -> bool {
    v.map_or(true, Value::is_null)
}

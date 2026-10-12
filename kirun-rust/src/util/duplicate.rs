//! Port of kirun-js `util/duplicate.ts` (deep copy; `serde_json::Value` clones deeply).

use crate::Value;

pub fn duplicate(obj: &Value) -> Value {
    obj.clone()
}

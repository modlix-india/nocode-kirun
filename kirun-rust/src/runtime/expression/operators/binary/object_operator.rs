//! Port of kirun-js `runtime/expression/operators/binary/ObjectOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::util::string::string_formatter::StringFormatter;
use crate::KIRunError;

#[derive(Debug, Default, Clone, Copy)]
pub struct ObjectOperator;

impl ObjectOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ObjectOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                if !js::truthy(t) {
            return Err(KIRunError::execution("Cannot apply array operator on a null value"));
        }
        if !js::truthy(u) {
            return Err(KIRunError::execution("Cannot retrive null property value"));
        }
        match t {
            Value::Object(o) => Ok(o.get(&js::to_js_string(u)).cloned().unwrap_or(Value::Null)),
            Value::Array(a) => {
                let i = js::to_number(u);
                Ok(if i >= 0.0 && i.fract() == 0.0 { a.get(i as usize).cloned().unwrap_or(Value::Null) } else { Value::Null })
            }
            Value::String(s) => {
                let i = js::to_number(u);
                Ok(if i >= 0.0 && i.fract() == 0.0 {
                    js::char_at(s, i as usize).map(Value::String).unwrap_or(Value::Null)
                } else {
                    Value::Null
                })
            }
            _ => Err(KIRunError::execution(StringFormatter::format(
                "Cannot retrieve value from a primitive value $",
                &[&js::to_js_string(t)],
            ))),
        }
    }
}

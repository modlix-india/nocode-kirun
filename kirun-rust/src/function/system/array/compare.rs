//! Port of kirun-js `function/system/array/Compare.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::{js_to_string, number_value};
use crate::{KIRunError, Result, Value};

pub struct Compare {
    signature: FunctionSignature,
}

impl Compare {
    pub fn new() -> Self {
        Compare {
            signature: AbstractArrayFunction::signature(
                "Compare",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_array_find(), AbstractArrayFunction::parameter_int_find_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }

    /// JS `Compare.compare(source, srcfrom, srcto, find, findfrom, findto)`: 0 when equal, else the
    /// first non-zero element comparison. Throws on uneven ranges.
    pub fn compare(
        &self,
        source: &[Value],
        mut srcfrom: i64,
        mut srcto: i64,
        find: &[Value],
        mut findfrom: i64,
        mut findto: i64,
    ) -> Result<f64> {
        if srcto < srcfrom {
            std::mem::swap(&mut srcfrom, &mut srcto);
        }

        if findto < findfrom {
            std::mem::swap(&mut findfrom, &mut findto);
        }

        if srcto - srcfrom != findto - findfrom {
            return Err(KIRunError::runtime(format!(
                "Cannot compare uneven arrays from {} to {} in source array with {} to {} in find array",
                srcto, srcfrom, findto, findfrom
            )));
        }

        let (mut i, mut j) = (srcfrom, findfrom);
        while i < srcto {
            let s = at(source, i);
            let f = at(find, j);
            let mut x: f64 = 1.0;

            if s.is_null() || f.is_null() {
                let (sn, fnull) = (s.is_null(), f.is_null());
                if sn == fnull {
                    x = 0.0;
                } else if sn {
                    x = -1.0;
                }
            } else if s.is_object() || s.is_array() || f.is_object() || f.is_array() {
                x = 1.0;
            } else if s.is_string() || f.is_string() {
                let (ss, fs) = (js_to_string(&s), js_to_string(&f));
                if ss == fs {
                    x = 0.0;
                } else if ss.encode_utf16().lt(fs.encode_utf16()) {
                    x = -1.0;
                }
            } else if s.is_boolean() || f.is_boolean() {
                x = if s.is_boolean() == f.is_boolean() { 0.0 } else { 1.0 };
            } else if let (Some(a), Some(b)) = (s.as_f64(), f.as_f64()) {
                x = a - b;
            }

            if x != 0.0 {
                return Ok(x);
            }
            i += 1;
            j += 1;
        }

        Ok(0.0)
    }
}

impl Default for Compare {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Compare {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let srcfrom = arg_i64(context, "srcFrom");
        let find = arg_array(context, "find")?;
        let findfrom = arg_i64(context, "findFrom");
        let mut length = arg_i64(context, "length");

        if source.is_empty() {
            return Err(KIRunError::runtime("Compare source array cannot be empty"));
        }

        if find.is_empty() {
            return Err(KIRunError::runtime("Compare find array cannot be empty"));
        }

        if length == -1 {
            length = source.len() as i64 - srcfrom;
        }

        if srcfrom + length > source.len() as i64 {
            return Err(KIRunError::runtime(format!(
                "Source array size {} is less than comparing size {}",
                source.len(),
                srcfrom + length
            )));
        }

        if findfrom + length > find.len() as i64 {
            return Err(KIRunError::runtime(format!(
                "Find array size {} is less than comparing size {}",
                find.len(),
                findfrom + length
            )));
        }

        let v = self.compare(&source, srcfrom, srcfrom + length, &find, findfrom, findfrom + length)?;
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, number_value(v)))
    }
}

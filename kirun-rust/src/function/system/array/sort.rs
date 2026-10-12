//! Port of kirun-js `function/system/array/Sort.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use std::cmp::Ordering;

use crate::util::primitive::{js_to_string, js_truthy};
use crate::{KIRunError, Result, Value};

/// JS `compareFunction` in Sort.ts.
fn compare_function(a: &Value, b: &Value, ascending: bool) -> Ordering {
    if strict_equal(a, b) {
        return Ordering::Equal;
    }
    if a.is_null() {
        return Ordering::Greater;
    }
    if b.is_null() {
        return Ordering::Less;
    }
    let lt = js_less_than(a, b);
    if !ascending {
        return if lt { Ordering::Greater } else { Ordering::Less };
    }
    if lt {
        Ordering::Less
    } else {
        Ordering::Greater
    }
}

/// JS `Array.prototype.sort` as V8 implements it (TimSort). The comparator here need not be
/// a total order (mixed types), so the exact algorithm matters for the result: runs are found
/// and extended with binary insertion sort exactly as V8 does (which decides every array
/// shorter than 64 elements); longer runs are merged with a plain stable merge (V8 gallops,
/// which only differs for inconsistent comparators). `undefined` is `null` here and is passed
/// to the comparator rather than moved to the end.
fn js_sort(a: &mut Vec<Value>, cmp: &dyn Fn(&Value, &Value) -> Ordering) {
    let n = a.len();
    if n < 2 {
        return;
    }

    fn min_run_length(mut n: usize) -> usize {
        let mut low = 0;
        while n >= 64 {
            low |= n & 1;
            n >>= 1;
        }
        n + low
    }

    fn count_and_make_run(a: &mut [Value], low_arg: usize, high: usize, cmp: &dyn Fn(&Value, &Value) -> Ordering) -> usize {
        let low = low_arg + 1;
        if low == high {
            return 1;
        }
        let mut run_length = 2;
        let is_descending = cmp(&a[low], &a[low - 1]) == Ordering::Less;
        let mut idx = low + 1;
        while idx < high {
            let order = cmp(&a[idx], &a[idx - 1]);
            if is_descending {
                if order != Ordering::Less {
                    break;
                }
            } else if order == Ordering::Less {
                break;
            }
            run_length += 1;
            idx += 1;
        }
        if is_descending {
            a[low_arg..low_arg + run_length].reverse();
        }
        run_length
    }

    fn binary_insertion_sort(
        a: &mut [Value],
        low: usize,
        start_arg: usize,
        high: usize,
        cmp: &dyn Fn(&Value, &Value) -> Ordering,
    ) {
        let mut start = if low == start_arg { start_arg + 1 } else { start_arg };
        while start < high {
            let pivot = a[start].clone();
            let (mut left, mut right) = (low, start);
            while left < right {
                let mid = left + ((right - left) >> 1);
                if cmp(&pivot, &a[mid]) == Ordering::Less {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            a[left..=start].rotate_right(1);
            start += 1;
        }
    }

    fn merge(a: &mut Vec<Value>, lo: usize, mid: usize, hi: usize, cmp: &dyn Fn(&Value, &Value) -> Ordering) {
        let left: Vec<Value> = a[lo..mid].to_vec();
        let right: Vec<Value> = a[mid..hi].to_vec();
        let (mut i, mut j, mut k) = (0, 0, lo);
        while i < left.len() && j < right.len() {
            if cmp(&right[j], &left[i]) == Ordering::Less {
                a[k] = right[j].clone();
                j += 1;
            } else {
                a[k] = left[i].clone();
                i += 1;
            }
            k += 1;
        }
        while i < left.len() {
            a[k] = left[i].clone();
            i += 1;
            k += 1;
        }
        while j < right.len() {
            a[k] = right[j].clone();
            j += 1;
            k += 1;
        }
    }

    let min_run = min_run_length(n);
    let mut runs: Vec<(usize, usize)> = Vec::new();
    let (mut low, mut remaining) = (0, n);
    while remaining != 0 {
        let mut current = count_and_make_run(a, low, low + remaining, cmp);
        if current < min_run {
            let forced = min_run.min(remaining);
            binary_insertion_sort(a, low, low + current, low + forced, cmp);
            current = forced;
        }
        runs.push((low, low + current));
        low += current;
        remaining -= current;
    }

    while runs.len() > 1 {
        let mut next = Vec::with_capacity(runs.len() / 2 + 1);
        for pair in runs.chunks(2) {
            if let [(lo, mid), (_, hi)] = pair {
                merge(a, *lo, *mid, *hi, cmp);
                next.push((*lo, *hi));
            } else {
                next.push(pair[0]);
            }
        }
        runs = next;
    }
}

pub struct Sort {
    signature: FunctionSignature,
}

impl Sort {
    pub fn new() -> Self {
        Sort {
            signature: AbstractArrayFunction::signature(
                "Sort",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_find_from(), AbstractArrayFunction::parameter_int_length(), AbstractArrayFunction::parameter_boolean_ascending(), AbstractArrayFunction::parameter_key_path()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Sort {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Sort {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let start = arg_i64(context, "findFrom");
        let mut len = arg_i64(context, "length");
        let ascending = js_truthy(&arg(context, "ascending"));
        let key_path = match arg(context, "keyPath") {
            Value::Null => String::new(),
            v => js_to_string(&v),
        };

        if source.is_empty() {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)));
        }

        let size = source.len() as i64;
        if len == -1 {
            len = size - start;
        }

        if start < 0 || start >= size || start + len > size {
            return Err(KIRunError::runtime(
                "Given start point is more than the size of the array or not available at that point",
            ));
        }

        // JS slices one element past the range (`slice(start, start + len + 1)`) and splices the
        // sorted slice back over `len` elements; ported as is.
        let slice_end = (start + len + 1).min(size).max(start) as usize;
        let mut sliced: Vec<Value> = source[start as usize..slice_end].to_vec();

        // JS `typeof x === "object"` is true for objects, arrays and null.
        let is_js_object = |v: &Value| v.is_object() || v.is_array() || v.is_null();
        let cmp = |a: &Value, b: &Value| -> Ordering {
            if is_js_object(a) && is_js_object(b) && !key_path.is_empty() {
                let store = serde_json::json!({ "a": a, "b": b });
                let a_val = get_path_value(&store, &format!("a.{}", key_path));
                let b_val = get_path_value(&store, &format!("b.{}", key_path));
                return compare_function(&a_val, &b_val, ascending);
            }
            compare_function(a, b, ascending)
        };
        js_sort(&mut sliced, &cmp);

        let del_end = (start + len.max(0)) as usize;
        source.splice(start as usize..del_end, sliced);

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}

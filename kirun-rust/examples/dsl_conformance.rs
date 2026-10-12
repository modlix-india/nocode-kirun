//! DSL conformance against kirun-js.
//!
//! Input: a JSON-lines file where each line is `{ file, def, text, compiled }` (or `error`),
//! produced by running kirun-js `DSLCompiler.decompile(def)` and `DSLCompiler.compile(text)`
//! over stored function definitions. For each line this checks that kirun-rust
//!   1. decompiles `def` to the same text, and
//!   2. compiles kirun-js's `text` to the same JSON (parameter reference keys are random
//!      UUIDs, so refs are compared by position).
//!
//! Usage: cargo run --release --example dsl_conformance -- <file.jsonl> [max-reported]

use std::io::{BufRead, BufReader};

use kirun::dsl::DSLCompiler;
use serde_json::Value;

/// Replaces each parameterMap's `{ <uuid>: ref }` with the refs ordered by `order`, keyless.
fn normalize(def: &Value) -> Value {
    let mut def = def.clone();
    if let Some(steps) = def.get_mut("steps").and_then(Value::as_object_mut) {
        for step in steps.values_mut() {
            if let Some(pm) = step.get_mut("parameterMap").and_then(Value::as_object_mut) {
                for refs in pm.values_mut() {
                    let mut list: Vec<Value> = refs.as_object().map(|o| o.values().cloned().collect()).unwrap_or_default();
                    list.sort_by(|a, b| {
                        let o = |r: &Value| r["order"].as_f64().unwrap_or(0.0);
                        o(a).partial_cmp(&o(b)).unwrap()
                    });
                    for r in list.iter_mut() {
                        if let Some(o) = r.as_object_mut() {
                            o.remove("key");
                        }
                    }
                    *refs = Value::Array(list);
                }
            }
        }
    }
    def
}

/// JSON equality with numbers compared by value (`1 == 1.0`).
fn json_eq(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Number(x), Value::Number(y)) => x.as_f64() == y.as_f64(),
        (Value::Array(x), Value::Array(y)) => x.len() == y.len() && x.iter().zip(y).all(|(a, b)| json_eq(a, b)),
        (Value::Object(x), Value::Object(y)) => {
            x.len() == y.len() && x.iter().all(|(k, v)| y.get(k).is_some_and(|w| json_eq(v, w)))
        }
        _ => a == b,
    }
}

fn first_text_diff(a: &str, b: &str) -> String {
    for (i, (x, y)) in a.lines().zip(b.lines()).enumerate() {
        if x != y {
            return format!("line {}:\n    js:   {x}\n    rust: {y}", i + 1);
        }
    }
    format!("line counts differ: js {} / rust {}", a.lines().count(), b.lines().count())
}

fn main() {
    let mut args = std::env::args().skip(1);
    let path = args.next().expect("usage: dsl_conformance <file.jsonl> [max-reported]");
    let max_reported: usize = args.next().and_then(|s| s.parse().ok()).unwrap_or(10);

    let (mut total, mut js_errors, mut text_ok, mut compile_ok) = (0, 0, 0, 0);
    let (mut text_diffs, mut compile_diffs, mut rust_errors) = (Vec::new(), Vec::new(), Vec::new());

    for line in BufReader::new(std::fs::File::open(&path).expect("open input")).lines() {
        let rec: Value = serde_json::from_str(&line.expect("read line")).expect("json line");
        total += 1;
        let file = rec["file"].as_str().unwrap_or("?").to_string();
        if rec.get("error").is_some() {
            js_errors += 1;
            continue;
        }
        let js_text = rec["text"].as_str().unwrap_or("");

        match DSLCompiler::decompile(&rec["def"]) {
            Ok(t) if t == js_text => text_ok += 1,
            Ok(t) => text_diffs.push(format!("{file}: {}", first_text_diff(js_text, &t))),
            Err(e) => rust_errors.push(format!("{file}: decompile: {e}")),
        }

        match DSLCompiler::compile(js_text) {
            Ok(c) if json_eq(&normalize(&c), &normalize(&rec["compiled"])) => compile_ok += 1,
            Ok(_) => compile_diffs.push(file.clone()),
            Err(e) => rust_errors.push(format!("{file}: compile: {e}")),
        }
    }

    let checked = total - js_errors;
    println!("functions: {total} (kirun-js failed on {js_errors}, skipped)");
    println!("decompile text identical: {text_ok}/{checked}");
    println!("compile JSON identical:   {compile_ok}/{checked}");
    println!("rust errors: {}", rust_errors.len());
    for (title, list) in [("decompile diffs", &text_diffs), ("compile diffs", &compile_diffs), ("rust errors", &rust_errors)] {
        if !list.is_empty() {
            println!("\n{title} ({}):", list.len());
            for d in list.iter().take(max_reported) {
                println!("  {d}");
            }
        }
    }
}

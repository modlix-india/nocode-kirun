//! Gson's `toJson` on a `JsonElement`, the way kirun-java prints values in `ToString`
//! (`new GsonBuilder().setPrettyPrinting().create()`) and `JSONStringify` (`new Gson()`).
//! Stored server functions were written against these strings (regexes that expect a string
//! quoted, `5.0` for a double), so kirun-rust prints them the same way. Measured on kirun-java
//! 4.9.2 with Gson 2.10.1:
//!
//! - a string is written quoted and escaped (`DESC` prints as `"DESC"`);
//! - escaping is Gson's HTML-safe set: `"` and `\`, the short forms `\t \b \n \r \f`, other
//!   control characters and U+2028 / U+2029 as `\uXXXX` (lower-case hex), and `< > & = '` as
//!   `\u003c \u003e \u0026 \u003d \u0027`; everything else (DEL, non-ASCII) is written as is;
//! - numbers print as Java's `Number.toString()`: whole numbers as they are, a double as
//!   `Double.toString` (`5.0`, `1.0E7`, `1.0E-4`, `-0.0`);
//! - an object leaves out members whose value is null (`serializeNulls` is off), at any depth;
//!   an array keeps its nulls; a null at the top prints as `null`;
//! - pretty printing indents by two spaces, writes `"key": value`, and prints an empty (or
//!   all-null) object as `{}` and an empty array as `[]`.

use std::fmt::Write as _;

use crate::runtime::expression::operators::java_semantics::java_double_string;
use crate::Value;

/// `new Gson().toJson(element)`.
pub fn to_json(v: &Value) -> String {
    let mut out = String::new();
    write_value(v, None, 0, &mut out);
    out
}

/// `new GsonBuilder().setPrettyPrinting().create().toJson(element)`.
pub fn to_json_pretty(v: &Value) -> String {
    let mut out = String::new();
    write_value(v, Some("  "), 0, &mut out);
    out
}

/// Java `Number.toString()` for the number Gson holds.
pub fn number_string(n: &serde_json::Number) -> String {
    if let Some(i) = n.as_i64() {
        return i.to_string();
    }
    if let Some(u) = n.as_u64() {
        return u.to_string();
    }
    java_double_string(n.as_f64().unwrap_or(f64::NAN))
}

/// Gson `JsonWriter.string`, HTML-safe.
pub fn quote(s: &str, out: &mut String) {
    out.push('"');
    for c in s.chars() {
        match c {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\t' => out.push_str("\\t"),
            '\u{8}' => out.push_str("\\b"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\u{c}' => out.push_str("\\f"),
            '<' | '>' | '&' | '=' | '\'' | '\u{2028}' | '\u{2029}' => {
                let _ = write!(out, "\\u{:04x}", c as u32);
            }
            c if (c as u32) < 0x20 => {
                let _ = write!(out, "\\u{:04x}", c as u32);
            }
            c => out.push(c),
        }
    }
    out.push('"');
}

fn newline(indent: Option<&str>, depth: usize, out: &mut String) {
    if let Some(unit) = indent {
        out.push('\n');
        for _ in 0..depth {
            out.push_str(unit);
        }
    }
}

fn write_value(v: &Value, indent: Option<&str>, depth: usize, out: &mut String) {
    match v {
        Value::Null => out.push_str("null"),
        Value::Bool(b) => out.push_str(if *b { "true" } else { "false" }),
        Value::Number(n) => out.push_str(&number_string(n)),
        Value::String(s) => quote(s, out),
        Value::Array(a) => {
            out.push('[');
            if a.is_empty() {
                out.push(']');
                return;
            }
            for (i, e) in a.iter().enumerate() {
                if i > 0 {
                    out.push(',');
                }
                newline(indent, depth + 1, out);
                write_value(e, indent, depth + 1, out);
            }
            newline(indent, depth, out);
            out.push(']');
        }
        Value::Object(m) => {
            out.push('{');
            let mut written = false;
            for (k, e) in m.iter().filter(|(_, e)| !e.is_null()) {
                if written {
                    out.push(',');
                }
                written = true;
                newline(indent, depth + 1, out);
                quote(k, out);
                out.push(':');
                if indent.is_some() {
                    out.push(' ');
                }
                write_value(e, indent, depth + 1, out);
            }
            if written {
                newline(indent, depth, out);
            }
            out.push('}');
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    #[test]
    fn strings_are_quoted_and_html_escaped() {
        assert_eq!(to_json_pretty(&json!("DESC")), "\"DESC\"");
        assert_eq!(to_json(&json!("")), "\"\"");
        assert_eq!(to_json(&json!("a<b>&='c'")), r#""a\u003cb\u003e\u0026\u003d\u0027c\u0027""#);
        assert_eq!(to_json(&json!("line\nbreak\ttab\u{1}\u{7f}")), "\"line\\nbreak\\ttab\\u0001\u{7f}\"");
        assert_eq!(to_json(&json!("q\"uote\\back/slash")), r#""q\"uote\\back/slash""#);
        assert_eq!(to_json(&json!("\u{2028}x\u{2029}")), r#""\u2028x\u2029""#);
        assert_eq!(to_json(&json!("é ✓ 😀")), "\"é ✓ 😀\"");
        assert_eq!(to_json(&json!("\u{8}\u{c}\r\u{1f}")), r#""\b\f\r\u001f""#);
    }

    #[test]
    fn numbers_print_as_java_numbers() {
        assert_eq!(to_json(&json!(7)), "7");
        assert_eq!(to_json(&json!(-2147483648i64)), "-2147483648");
        assert_eq!(to_json(&json!(18000000000i64)), "18000000000");
        assert_eq!(to_json(&json!(u64::MAX)), "18446744073709551615");
        assert_eq!(to_json(&json!(5.0)), "5.0");
        assert_eq!(to_json(&json!(2.5)), "2.5");
        assert_eq!(to_json(&json!(1e7)), "1.0E7");
        assert_eq!(to_json(&json!(0.0001)), "1.0E-4");
        assert_eq!(to_json(&json!(-0.0)), "-0.0");
    }

    #[test]
    fn null_members_are_left_out_and_array_nulls_kept() {
        let v = json!({ "a": 1, "b": null, "c": "x", "d": { "e": null } });
        assert_eq!(to_json(&v), r#"{"a":1,"c":"x","d":{}}"#);
        assert_eq!(to_json_pretty(&v), "{\n  \"a\": 1,\n  \"c\": \"x\",\n  \"d\": {}\n}");
        assert_eq!(to_json(&json!({ "b": null, "a": 1 })), r#"{"a":1}"#);
        let a = json!([1, null, "x", [], {}]);
        assert_eq!(to_json(&a), r#"[1,null,"x",[],{}]"#);
        assert_eq!(to_json_pretty(&a), "[\n  1,\n  null,\n  \"x\",\n  [],\n  {}\n]");
        assert_eq!(to_json(&Value::Null), "null");
    }

    #[test]
    fn pretty_printing_nests_by_two_spaces_in_key_order() {
        let v = json!({ "s": "<&>", "n": { "m": [1, { "k": "v" }] } });
        assert_eq!(
            to_json_pretty(&v),
            "{\n  \"s\": \"\\u003c\\u0026\\u003e\",\n  \"n\": {\n    \"m\": [\n      1,\n      {\n        \"k\": \"v\"\n      }\n    ]\n  }\n}"
        );
        assert_eq!(to_json(&v), r#"{"s":"\u003c\u0026\u003e","n":{"m":[1,{"k":"v"}]}}"#);
        assert_eq!(to_json_pretty(&json!({})), "{}");
        assert_eq!(to_json_pretty(&json!([])), "[]");
        assert_eq!(to_json(&json!({ "k=": true })), r#"{"k\u003d":true}"#);
    }
}

//! Port of kirun-js `runtime/expression/tokenextractor/LiteralTokenValueExtractor.ts`, reading
//! numbers and bare words as kirun-java's `LiteralTokenValueExtractor` does: a token that is
//! not a keyword, a quoted string or a Java number literal is that text (`paymentDetails`,
//! `1e5`, `0x10`, `Infinity`, `2.val`), where kirun-js reads JS number syntax and fails on
//! anything else ("Unable to parse the literal or expression").

use super::token_value_extractor::{TokenValueExtractor, ValuesMap};
use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::util::string::string_formatter::StringFormatter;
use crate::util::string::string_util::StringUtil;
use crate::{Result, Value};

/// kirun-java `KEYWORDS`: `true`, `false` and `null`. Inside an expression `undefined` is just
/// text (kirun-js reads it as `undefined`); a whole expression that is `undefined` is null, which
/// the evaluator's literal check answers before this.
fn keyword(token: &str) -> Option<Value> {
    match token {
        "true" => Some(Value::Bool(true)),
        "false" => Some(Value::Bool(false)),
        "null" => Some(Value::Null),
        _ => None,
    }
}

/// JS `LiteralTokenValueExtractor.INSTANCE` is `LiteralTokenValueExtractor::INSTANCE`.
#[derive(Debug, Default, Clone, Copy)]
pub struct LiteralTokenValueExtractor;

impl LiteralTokenValueExtractor {
    pub const INSTANCE: LiteralTokenValueExtractor = LiteralTokenValueExtractor;

    /// JS `tryGetValue`: `None` stands for the JS `FAILED` symbol, which kirun-java's extractor
    /// never gives: only an unclosed string literal is not a value.
    pub fn try_get_value(&self, token: &str) -> Option<Value> {
        let prefix = self.get_prefix();
        if !prefix.is_empty() && !token.starts_with(prefix) {
            return None;
        }
        if StringUtil::is_null_or_blank(Some(token)) {
            return Some(Value::Null);
        }
        let token = token.trim();
        if let Some(v) = keyword(token) {
            return Some(v);
        }
        if token.starts_with('"') {
            if !token.ends_with('"') || token.len() < 2 {
                return None;
            }
            return Some(Value::String(token[1..token.len() - 1].to_string()));
        }
        Some(java_number_or_text(token))
    }

    /// JS `getValueFromExtractors(token, maps)`.
    pub fn get_value_from_extractors(&self, token: &str, maps: &ValuesMap) -> Result<Value> {
                if let Some(x) = maps.get(&format!("{token}.")) {
            return Ok(x.get_store());
        }
        self.get_value_with(token, Some(maps))
    }
}

impl TokenValueExtractor for LiteralTokenValueExtractor {
    fn get_prefix(&self) -> &str {
        ""
    }

    fn get_store(&self) -> Value {
        Value::Null
    }

    fn get_value_internal(&self, token: &str, _values_map: Option<&ValuesMap>) -> Result<Value> {
                if StringUtil::is_null_or_blank(Some(token)) {
            return Ok(Value::Null);
        }
        let token = token.trim();
        if let Some(v) = keyword(token) {
            return Ok(v);
        }
        if token.starts_with('"') {
            if !token.ends_with('"') || token.len() < 2 {
                return Err(expression_evaluation_exception(
                    token,
                    &StringFormatter::format("String literal $ is not closed properly", &[token]),
                ));
            }
            return Ok(Value::String(token[1..token.len() - 1].to_string()));
        }
        Ok(java_number_or_text(token))
    }
}

/// kirun-java `LiteralTokenValueExtractor.processNumbers`: a whole number (`Long.parseLong`:
/// an optional sign and digits, within a `long`), or a decimal whose digits after the first
/// dot are all digits (`Double.parseDouble`); anything else is the token's text.
///
/// kirun-java hands decimals back as `float` (`2.5` → `2.5f`) unless that is zero or infinite;
/// kirun-rust keeps the `f64` (see the crate's number notes: the result is the double). A decimal
/// stays a decimal even with no fraction (`1000000000.0`, `5.0`): Java's operators then work in
/// floating point, where an integer would wrap at 32 bits (`3 * 1000000000.0` is `3.0E9`).
pub fn java_number_or_text(token: &str) -> Value {
    let text = || Value::String(token.to_string());
    match token.find('.') {
        None => {
            let digits = token.strip_prefix(['-', '+']).unwrap_or(token);
            if digits.is_empty() || !digits.bytes().all(|b| b.is_ascii_digit()) {
                return text();
            }
            token.parse::<i64>().map(Value::from).unwrap_or_else(|_| text())
        }
        Some(dot) => {
            let (before, after) = (&token[..dot], &token[dot + 1..]);
            if !after.chars().all(|c| c.is_ascii_digit()) {
                return text();
            }
            let whole = before.strip_prefix(['-', '+']).unwrap_or(before);
            if !whole.bytes().all(|b| b.is_ascii_digit()) || (whole.is_empty() && after.is_empty()) {
                return text();
            }
            // Rust's parser wants digits on both sides (`5.`, `.5` are Java decimals).
            let normalized = format!("{}0{whole}.{after}0", &before[..before.len() - whole.len()]);
            match normalized.parse::<f64>() {
                Ok(d) => serde_json::Number::from_f64(d).map(Value::Number).unwrap_or(Value::Null),
                Err(_) => text(),
            }
        }
    }
}

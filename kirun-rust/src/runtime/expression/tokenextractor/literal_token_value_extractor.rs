//! Port of kirun-js `runtime/expression/tokenextractor/LiteralTokenValueExtractor.ts`.

use super::token_value_extractor::{TokenValueExtractor, ValuesMap};
use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::runtime::expression::js;
use crate::util::string::string_formatter::StringFormatter;
use crate::util::string::string_util::StringUtil;
use crate::{Result, Value};

/// JS `KEYWORDS` (`null` reads as `undefined`).
fn keyword(token: &str) -> Option<Value> {
    match token {
        "true" => Some(Value::Bool(true)),
        "false" => Some(Value::Bool(false)),
        "null" | "undefined" => Some(Value::Null),
        _ => None,
    }
}

/// JS `LiteralTokenValueExtractor.INSTANCE` is `LiteralTokenValueExtractor::INSTANCE`.
#[derive(Debug, Default, Clone, Copy)]
pub struct LiteralTokenValueExtractor;

impl LiteralTokenValueExtractor {
    pub const INSTANCE: LiteralTokenValueExtractor = LiteralTokenValueExtractor;

    /// JS `tryGetValue`: `None` stands for the JS `FAILED` symbol.
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
        js::parse_number(token).map(js::num)
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
        js::parse_number(token).map(js::num).ok_or_else(|| {
            expression_evaluation_exception(token, &format!("Unable to parse the literal or expression {token}"))
        })
    }
}
